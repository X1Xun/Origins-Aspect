package com.iafenvoy.origins.data.power.builtin.regular;

import com.google.common.collect.ImmutableSet;
import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.KeySettings;
import com.iafenvoy.origins.data.badge.Badge;
import com.iafenvoy.origins.data.badge.PresetBadges;
import com.iafenvoy.origins.data.power.HasCooldownPower;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.Toggleable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HolyBoltPower extends HasCooldownPower implements Toggleable {

    public static final MapCodec<HolyBoltPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            Codec.DOUBLE.fieldOf("max_distance").orElse(24.0D).forGetter(HolyBoltPower::getMaxDistance),
            Codec.DOUBLE.fieldOf("damage").orElse(7.0D).forGetter(HolyBoltPower::getDamage),
            KeySettings.CODEC.forGetter(HolyBoltPower::getKey)
    ).apply(i, HolyBoltPower::new));

    private final double maxDistance;
    private final double damage;
    private final KeySettings key;

    public HolyBoltPower(BaseSettings settings, CooldownSettings cooldown, double maxDistance, double damage, KeySettings key) {
        super(settings, cooldown);
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.key = key;
    }

    public double getMaxDistance() { return this.maxDistance; }
    public double getDamage() { return this.damage; }

    @Override public KeySettings getKey() { return this.key; }
    @Override public @NotNull MapCodec<? extends Power> codec() { return CODEC; }

    @Override
    public void collectBadges(ImmutableSet.Builder<Badge> builder) {
        super.collectBadges(builder);
        builder.add(PresetBadges.ACTIVE);
    }

    @Override
    public boolean isActive(OriginDataHolder holder) {
        return this.getCooldownComponent(holder).canUse();
    }

    @Override
    public void toggle(@NotNull OriginDataHolder holder, String key) {
        if (!this.key.match(key) || !this.isActive(holder)) return;

        this.getCooldownComponent(holder).useIfReady(() -> {
            Entity paladin = holder.getEntity();
            if (paladin == null || !(paladin.level() instanceof ServerLevel serverLevel)) return;
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(paladin,
                    entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity, this.maxDistance);
            if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity target && target.isAlive()) {

                    // Списываем кулдаун
                    this.getCooldownComponent(holder).startCooldown();
                    this.getCooldownComponent(holder).setValue(this.getCooldown().cooldown());

                    Vec3 targetPos = target.position();
                    LightningBolt lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(serverLevel);
                    if (lightning != null) {
                        lightning.moveTo(targetPos.x, targetPos.y, targetPos.z);
                        lightning.setVisualOnly(true);
                        serverLevel.addFreshEntity(lightning);
                    }
                    boolean isUndead = target.isInvertedHealAndHarm();

                    if (isUndead) {
                        target.hurt(paladin.damageSources().magic(), (float) (this.damage * 3.0D));
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 5));
                    } else {
                        target.hurt(paladin.damageSources().indirectMagic(lightning, paladin), (float) this.damage);
                        target.igniteForTicks(80);
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
                    }
                    serverLevel.sendParticles(ParticleTypes.SOUL, targetPos.x, targetPos.y + 1, targetPos.z, 15, 0.3, 0.5, 0.3, 0.05);
                    serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.1F);
                }
            }
        });
    }
}

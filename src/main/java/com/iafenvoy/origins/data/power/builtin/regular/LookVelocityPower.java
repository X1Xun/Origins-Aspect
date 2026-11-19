package com.iafenvoy.origins.data.power.builtin.regular;

import com.google.common.collect.ImmutableSet;
import com.iafenvoy.origins.ModMethods;
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
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class LookVelocityPower extends HasCooldownPower implements Toggleable {
    public static final MapCodec<LookVelocityPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            Codec.DOUBLE.fieldOf("strength").forGetter(LookVelocityPower::getStrength),
            KeySettings.CODEC.forGetter(LookVelocityPower::getKey)
    ).apply(i, LookVelocityPower::new));

    private final double strength;
    private final KeySettings key;

    public LookVelocityPower(BaseSettings settings, CooldownSettings cooldown, double strength, KeySettings key) {
        super(settings, cooldown);
        this.strength = strength;
        this.key = key;
    }

    public double getStrength() {
        return this.strength;
    }

    @Override
    public KeySettings getKey() {
        return this.key;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

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
        var player = holder.getEntity();
        if (player.level().isRainingAt(player.blockPosition())) return;

        this.getCooldownComponent(holder).useIfReady(() -> {
            Entity entity = holder.getEntity();

            if (entity != null && entity.level() instanceof ServerLevel serverLevel) {

                Vec3 lookAngle = entity.getLookAngle();
                boolean hasGrume = ModMethods.isGrumeInHotbar((Player) player);
                if (hasGrume){
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            lookAngle.x * this.strength * 2,
                            lookAngle.y * this.strength,
                            lookAngle.z * this.strength * 2
                    ));
                    Vector3f fromColor = new Vector3f(1.0F, 0.0F, 0.0F);
                    Vector3f toColor = new Vector3f(1.0F, 0.0F, 0.0F);
                    DustColorTransitionOptions redTransition = new DustColorTransitionOptions(fromColor, toColor, 1.0F);
                    serverLevel.sendParticles(redTransition, player.getX(), player.getY(), player.getZ(), 10, 1, 1, 1, 0.1);
                }
                else {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            lookAngle.x * this.strength,
                            lookAngle.y * this.strength * 0.5D,
                            lookAngle.z * this.strength
                    ));
                    this.getCooldownComponent(holder).startCooldown();
                    int cooldownTicks = this.getCooldown().cooldown();
                    this.getCooldownComponent(holder).setValue(cooldownTicks);
                }
                entity.hurtMarked = true;
            }
        });
    }

}

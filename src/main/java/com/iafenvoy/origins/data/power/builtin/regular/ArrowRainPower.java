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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ArrowRainPower extends HasCooldownPower implements Toggleable {

    public static final MapCodec<ArrowRainPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            Codec.INT.fieldOf("arrow_count").orElse(15).forGetter(ArrowRainPower::getArrowCount),
            Codec.DOUBLE.fieldOf("damage").orElse(4.0D).forGetter(ArrowRainPower::getDamage),
            KeySettings.CODEC.forGetter(ArrowRainPower::getKey)
    ).apply(i, ArrowRainPower::new));

    private final int arrowCount;
    private final double damage;
    private final KeySettings key;

    public ArrowRainPower(BaseSettings settings, CooldownSettings cooldown, int arrowCount, double damage, KeySettings key) {
        super(settings, cooldown);
        this.arrowCount = arrowCount;
        this.damage = damage;
        this.key = key;
    }

    public int getArrowCount() { return this.arrowCount; }
    public double getDamage() { return this.damage; }

    @Override
    public KeySettings getKey() { return this.key; }

    @Override
    public @NotNull MapCodec<? extends Power> codec() { return CODEC; }

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
            Entity player = holder.getEntity();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) return;
            this.getCooldownComponent(holder).startCooldown();
            this.getCooldownComponent(holder).setValue(this.getCooldown().cooldown());
            double raycastDistance = 32.0D;
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookAngle = player.getLookAngle();
            Vec3 targetVector = eyePosition.add(lookAngle.scale(raycastDistance));

            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, entity -> !entity.isSpectator() && entity.isPickable(), raycastDistance);

            Vec3 strikePoint;
            if (hitResult.getType() != HitResult.Type.MISS) {
                strikePoint = hitResult.getLocation();
            } else {
                strikePoint = eyePosition.add(lookAngle.scale(12.0D));
            }

            RandomSource random = serverLevel.getRandom();

            for (int i = 0; i < this.arrowCount; i++) {
                double offsetX = (random.nextDouble() - 0.5D) * 6.0D;
                double offsetZ = (random.nextDouble() - 0.5D) * 6.0D;
                double offsetY = 14.0D + random.nextDouble() * 5.0D;

                Vec3 spawnPos = new Vec3(strikePoint.x + offsetX, strikePoint.y + offsetY, strikePoint.z + offsetZ);

                Arrow arrow = new Arrow(net.minecraft.world.entity.EntityType.ARROW, serverLevel);
                arrow.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                arrow.setOwner(player);
                arrow.setBaseDamage(this.damage);
                arrow.pickup = Arrow.Pickup.DISALLOWED;

                double motionX = (random.nextDouble() - 0.5D) * 0.2D;
                double motionY = -1.0D - random.nextDouble() * 0.5D;
                double motionZ = (random.nextDouble() - 0.5D) * 0.2D;
                arrow.setDeltaMovement(motionX, motionY, motionZ);
                serverLevel.addFreshEntity(arrow);
            }
        });
    }
}

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GrapplingHookPower extends HasCooldownPower implements Toggleable {

    public static final MapCodec<GrapplingHookPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            Codec.DOUBLE.fieldOf("max_distance").orElse(24.0D).forGetter(GrapplingHookPower::getMaxDistance),
            Codec.DOUBLE.fieldOf("pull_speed").orElse(1.3D).forGetter(GrapplingHookPower::getPullSpeed),
            KeySettings.CODEC.forGetter(GrapplingHookPower::getKey)
    ).apply(i, GrapplingHookPower::new));

    private final double maxDistance;
    private final double pullSpeed;
    private final KeySettings key;

    public GrapplingHookPower(BaseSettings settings, CooldownSettings cooldown, double maxDistance, double pullSpeed, KeySettings key) {
        super(settings, cooldown);
        this.maxDistance = maxDistance;
        this.pullSpeed = pullSpeed;
        this.key = key;
    }

    public double getMaxDistance() { return this.maxDistance; }
    public double getPullSpeed() { return this.pullSpeed; }

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

            Vec3 eyePosition = player.getEyePosition();
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player,
                    entity -> !entity.isSpectator() && entity.isPickable(), this.maxDistance);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.getCooldownComponent(holder).startCooldown();
                this.getCooldownComponent(holder).setValue(this.getCooldown().cooldown());

                Vec3 strikePoint = hitResult.getLocation();
                Vec3 playerPos = player.position();
                Vec3 pullVector = strikePoint.subtract(playerPos).normalize();

                double motionX = pullVector.x * this.pullSpeed;
                double motionY = (pullVector.y * this.pullSpeed) + 0.25D;
                double motionZ = pullVector.z * this.pullSpeed;

                player.setDeltaMovement(motionX, motionY, motionZ);
                player.hurtMarked = true;
                serverLevel.sendParticles(ParticleTypes.CRIT, strikePoint.x, strikePoint.y, strikePoint.z, 12, 0.1D, 0.1D, 0.1D, 0.1D);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.4F);
            }
        });
    }
}

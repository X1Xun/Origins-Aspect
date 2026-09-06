package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.MiscUtil;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record FireProjectileAction(EntityType<?> entityType, ResourceReference divergence, ResourceReference speed,
                                   ResourceReference count,
                                   CompoundTag tag, EntityAction projectileAction) implements EntityAction {
    public static final MapCodec<FireProjectileAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(FireProjectileAction::entityType),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("divergence", ResourceReference.number(1)).forGetter(FireProjectileAction::divergence),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("speed", ResourceReference.number(1)).forGetter(FireProjectileAction::speed),
            ResourceReference.INT_CODEC.optionalFieldOf("count", ResourceReference.number(1)).forGetter(FireProjectileAction::count),
            CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(FireProjectileAction::tag),
            EntityAction.optionalCodec("projectile_action").forGetter(FireProjectileAction::projectileAction)
    ).apply(i, FireProjectileAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (!(source.level() instanceof ServerLevel serverWorld)) return;

        RandomSource random = serverWorld.getRandom();

        Vec3 velocity = source.getDeltaMovement();
        Vec3 verticalOffset = source.position().add(0, source.getEyeHeight(source.getPose()), 0);

        float pitch = source.getXRot();
        float yaw = source.getYRot();

        float divergence = this.divergence.resolveFloat(source);
        float speed = this.speed.resolveFloat(source);
        int count = Math.max(0, this.count.resolveInt(source));
        for (int i = 0; i < count; i++) {
            Entity entityToSpawn = MiscUtil
                    .getEntityWithPassengers(serverWorld, this.entityType, this.tag, verticalOffset, yaw, pitch)
                    .orElse(null);
            if (entityToSpawn == null) return;

            if (entityToSpawn instanceof Projectile projectileToSpawn) {
                if (projectileToSpawn instanceof AbstractHurtingProjectile explosiveProjectileToSpawn)
                    explosiveProjectileToSpawn.accelerationPower = speed;
                projectileToSpawn.setOwner(source);
                projectileToSpawn.shootFromRotation(source, pitch, yaw, 0F, speed, divergence);
            } else {
                float j = 0.017453292F;
                double k = 0.0075;

                float l = -Mth.sin(yaw * j) * Mth.cos(pitch * j);
                float m = -Mth.sin(pitch * j);
                float n = Mth.cos(yaw * j) * Mth.cos(pitch * j);

                Vec3 velocityToApply = new Vec3(l, m, n)
                        .normalize()
                        .add(random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence)
                        .scale(speed);
                entityToSpawn.setDeltaMovement(velocityToApply);
                entityToSpawn.push(velocity.x, source.onGround() ? 0.0D : velocity.y, velocity.z);
            }

            if (!this.tag.isEmpty()) {
                CompoundTag mergedNbt = entityToSpawn.saveWithoutId(new CompoundTag());
                mergedNbt.merge(this.tag);
                entityToSpawn.load(mergedNbt);
            }

            serverWorld.tryAddFreshEntityWithPassengers(entityToSpawn);
            this.projectileAction.execute(entityToSpawn);
        }
    }
}

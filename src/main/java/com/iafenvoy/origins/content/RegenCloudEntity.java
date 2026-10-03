package com.iafenvoy.origins.content;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class RegenCloudEntity extends AreaEffectCloud {
    public RegenCloudEntity(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
    }
    public RegenCloudEntity(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sLevel) {
            if (this.tickCount % 20 == 0) {
                LivingEntity owner = this.getOwner();
                double currentRadius = this.getRadius();
                if (owner != null && owner.isAlive() && owner.distanceToSqr(this) <= (currentRadius * currentRadius)) {
                    owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, false));
                }
                for (int i = 0; i < 360; i += 20) {
                    double angle = Math.toRadians(i);
                    double pX = this.getX() + Math.cos(angle) * currentRadius;
                    double pZ = this.getZ() + Math.sin(angle) * currentRadius;
                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            pX, this.getY() + 0.1D, pZ,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}

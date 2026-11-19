package com.iafenvoy.origins.content;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
public class BloodMistEffect extends MobEffect {

    public BloodMistEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            entity.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                    entity.getRandomX(0.5), entity.getRandomY(), entity.getRandomZ(0.5), 0, 0.1, 0);
        }
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.setSwimming(false);
            player.stopFallFlying();
        }
    }
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().value() instanceof BloodMistEffect) {
            if (event.getEntity() instanceof Player player) {
                handleBloodMistEnd(player);
            }
        }
    }
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() != null && event.getEffect().value() instanceof BloodMistEffect) {
            if (event.getEntity() instanceof Player player) {
                handleBloodMistEnd(player);
            }
        }
    }
    private static void handleBloodMistEnd(Player player) {
        if (!player.level().isClientSide) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            player.setHealth(1.0F);
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
        }
    }
}

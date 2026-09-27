package com.iafenvoy.origins.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import static com.iafenvoy.origins.content.BloodEffect.BLOOD;

public class ExhaustionEffect extends MobEffect {
    public static final ResourceKey<DamageType> EXHAUSTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("origins", "hurt_by_exhaustion")
    );
    public ExhaustionEffect() {
        super(MobEffectCategory.HARMFUL, 0xb5b5b5);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        DamageSource customSource = entity.level().damageSources().source(EXHAUSTION);


        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}

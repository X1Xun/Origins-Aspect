package com.iafenvoy.origins.content;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class LowGravityEffect extends MobEffect {
    public LowGravityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7AC1EB); // Полезный, голубой цвет частиц
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}

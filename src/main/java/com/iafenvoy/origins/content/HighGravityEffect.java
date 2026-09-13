package com.iafenvoy.origins.content;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class HighGravityEffect extends MobEffect {
    public HighGravityEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A3B5C); // Вредный, темно-фиолетовый цвет частиц
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}

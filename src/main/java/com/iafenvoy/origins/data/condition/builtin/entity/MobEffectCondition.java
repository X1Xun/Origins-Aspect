package com.iafenvoy.origins.data.condition.builtin.entity;

import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public record MobEffectCondition(Holder<MobEffect> effect, ResourceReference minAmplifier,
                                 ResourceReference maxAmplifier, ResourceReference minDuration,
                                 ResourceReference maxDuration, boolean inverted) implements EntityCondition {
    public static final MapCodec<MobEffectCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(MobEffectCondition::effect),
            ResourceReference.INT_CODEC.optionalFieldOf("min_amplifier", ResourceReference.number(0)).forGetter(MobEffectCondition::minAmplifier),
            ResourceReference.INT_CODEC.optionalFieldOf("max_amplifier", ResourceReference.number(Integer.MAX_VALUE)).forGetter(MobEffectCondition::maxAmplifier),
            ResourceReference.INT_CODEC.optionalFieldOf("min_duration", ResourceReference.number(-1)).forGetter(MobEffectCondition::minDuration),
            ResourceReference.INT_CODEC.optionalFieldOf("max_duration", ResourceReference.number(Integer.MAX_VALUE)).forGetter(MobEffectCondition::maxDuration),
            Codec.BOOL.optionalFieldOf("inverted", false).forGetter(MobEffectCondition::inverted)
    ).apply(i, MobEffectCondition::new));

    @Override
    public @NotNull MapCodec<? extends EntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        MobEffectInstance instance = living.getEffect(this.effect);
        int minAmplifier = this.minAmplifier.resolveInt(entity);
        int maxAmplifier = this.maxAmplifier.resolveInt(entity);
        int minDuration = this.minDuration.resolveInt(entity);
        int maxDuration = this.maxDuration.resolveInt(entity);
        return this.inverted ^ (instance != null
                && minAmplifier <= instance.getAmplifier() && instance.getAmplifier() <= maxAmplifier
                && minDuration <= instance.getDuration() && instance.getDuration() <= maxDuration);
    }
}

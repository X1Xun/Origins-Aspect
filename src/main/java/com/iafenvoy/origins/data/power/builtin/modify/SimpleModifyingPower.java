package com.iafenvoy.origins.data.power.builtin.modify;

import com.iafenvoy.origins.data._common.helper.ModifierPowerHelper;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.iafenvoy.origins.util.math.Modifier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic modifier holder for powers that consume modifiers through integrations.
 */
public final class SimpleModifyingPower extends Power implements ModifierPowerHelper {
    public static final MapCodec<SimpleModifyingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CombinedCodecs.MODIFIER.optionalFieldOf("modifier", List.of()).forGetter(SimpleModifyingPower::getModifier),
            CombinedCodecs.MODIFIER.optionalFieldOf("modifiers", List.of()).forGetter(SimpleModifyingPower::getModifiers)
    ).apply(instance, (settings, modifier, modifiers) -> new SimpleModifyingPower(settings, mergeModifiers(modifier, modifiers))));

    private final List<Modifier> modifier;

    public SimpleModifyingPower(BaseSettings settings, List<Modifier> modifier) {
        super(settings);
        this.modifier = modifier;
    }

    @Override
    public List<Modifier> getModifier() {
        return this.modifier;
    }

    public List<Modifier> getModifiers() {
        return this.modifier;
    }

    private static List<Modifier> mergeModifiers(List<Modifier> modifier, List<Modifier> modifiers) {
        List<Modifier> result = new ArrayList<>(modifier);
        result.addAll(modifiers);
        return List.copyOf(result);
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }
}

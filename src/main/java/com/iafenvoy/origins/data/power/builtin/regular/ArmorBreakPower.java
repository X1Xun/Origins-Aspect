package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class ArmorBreakPower extends Power {
    public static final MapCodec<ArmorBreakPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Power.BaseSettings.CODEC.forGetter(Power::getSettings)
    ).apply(i, ArmorBreakPower::new));


    public ArmorBreakPower(Power.BaseSettings settings) {
        super(settings);
    }

    @Override
    public boolean isActive(OriginDataHolder holder) {
        return true;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }
}

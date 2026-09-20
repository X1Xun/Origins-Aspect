package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class MeatBuffPower extends Power {
    public static final MapCodec<MeatBuffPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings)
    ).apply(i, MeatBuffPower::new));


    public MeatBuffPower(BaseSettings settings) {
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

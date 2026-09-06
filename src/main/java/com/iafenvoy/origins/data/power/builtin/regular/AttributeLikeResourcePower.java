package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.component.ComponentCollector;
import com.iafenvoy.origins.data.power.component.builtin.DoubleResourceComponent;
import com.iafenvoy.origins.util.codec.MiscCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;

public class AttributeLikeResourcePower extends Power implements ResourceValueHelper.ResourceValue {
    public static final MapCodec<AttributeLikeResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings), Codec.DOUBLE.fieldOf("min").forGetter(x -> x.min), Codec.DOUBLE.fieldOf("max").forGetter(x -> x.max), MiscCodecs.doubleValue("start_value").forGetter(x -> OptionalDouble.of(x.start))
    ).apply(i, (settings, min, max, start) -> new AttributeLikeResourcePower(settings, min, max, start.orElse(min))));
    private final double min, max, start;

    public AttributeLikeResourcePower(BaseSettings settings, double min, double max, double start) {
        super(settings);
        this.min = min;
        this.max = max;
        this.start = start;
    }

    @Override
    public void createComponents(ComponentCollector collector) {
        super.createComponents(collector);
        collector.add(new DoubleResourceComponent(this.start, this.min, this.max));
    }

    @Override
    public double getDoubleValue(OriginDataHolder h) {
        return h.getComponentFor(this, DoubleResourceComponent.class).map(DoubleResourceComponent::getValue).orElse(this.start);
    }

    @Override
    public double getDoubleMin(OriginDataHolder h) {
        return this.min;
    }

    @Override
    public double getDoubleMax(OriginDataHolder h) {
        return this.max;
    }

    @Override
    public void setDoubleValue(OriginDataHolder h, double value) {
        h.getComponentFor(this, DoubleResourceComponent.class).ifPresent(c -> c.setValue(value));
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }
}

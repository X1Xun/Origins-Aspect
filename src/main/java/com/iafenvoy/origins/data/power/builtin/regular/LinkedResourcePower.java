package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.component.ComponentCollector;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Base for non-persistent resources whose value is supplied by the game state.
 */
public abstract class LinkedResourcePower extends Power implements ResourceValueHelper.ResourceValue {
    protected LinkedResourcePower(BaseSettings settings) {
        super(settings);
    }

    protected abstract double supply(OriginDataHolder holder);

    protected double supplyMin(OriginDataHolder holder) {
        return Integer.MIN_VALUE;
    }

    protected double supplyMax(OriginDataHolder holder) {
        return Integer.MAX_VALUE;
    }

    @Override
    public final double getDoubleValue(OriginDataHolder holder) {
        return this.supply(holder);
    }

    @Override
    public double getDoubleMin(OriginDataHolder holder) {
        return this.supplyMin(holder);
    }

    @Override
    public double getDoubleMax(OriginDataHolder holder) {
        return this.supplyMax(holder);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void setDoubleValue(OriginDataHolder holder, double value) {
    }

    @Override
    public final @NotNull MapCodec<? extends Power> codec() {
        return this.codecImpl();
    }

    protected abstract MapCodec<? extends LinkedResourcePower> codecImpl();

    @Override
    public void createComponents(ComponentCollector collector) {
        super.createComponents(collector);
    }
}

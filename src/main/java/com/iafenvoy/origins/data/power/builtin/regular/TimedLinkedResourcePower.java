package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.component.ComponentCollector;
import com.iafenvoy.origins.data.power.component.builtin.TimedResourceComponent;

abstract class TimedLinkedResourcePower extends LinkedResourcePower {
    private final int duration;

    protected TimedLinkedResourcePower(BaseSettings settings, int duration) {
        super(settings);
        this.duration = duration;
    }

    public int duration() {
        return this.duration;
    }

    @Override
    public void createComponents(ComponentCollector collector) {
        super.createComponents(collector);
        collector.add(new TimedResourceComponent());
    }

    @Override
    protected double supply(OriginDataHolder holder) {
        return holder.getComponentFor(this, TimedResourceComponent.class).map(TimedResourceComponent::value).orElse(0D);
    }

    protected void setTemporaryValue(OriginDataHolder holder, double value) {
        holder.getComponentFor(this, TimedResourceComponent.class).ifPresent(component -> component.set(value, this.duration));
    }
}

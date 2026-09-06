package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.HudRender;
import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.power.HudRenderable;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.component.ComponentCollector;
import com.iafenvoy.origins.data.power.component.builtin.ResourceComponent;
import com.iafenvoy.origins.data.power.builtin.modify.ModifyResourceMaximumPower;
import com.iafenvoy.origins.data.power.builtin.modify.ModifyResourceMinimumPower;
import com.iafenvoy.origins.util.codec.MiscCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * A resource whose bounds are modified by active resource modifier powers.
 */
public final class ModifiableResourcePower extends Power implements HudRenderable, ResourceValueHelper.ResourceValue {
    public static final MapCodec<ModifiableResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.INT.fieldOf("min").forGetter(ModifiableResourcePower::getMinValue),
            Codec.INT.fieldOf("max").forGetter(ModifiableResourcePower::getMaxValue),
            MiscCodecs.integer("start_value").forGetter(ModifiableResourcePower::getStartValue),
            Codec.BOOL.optionalFieldOf("enforce_limits", true).forGetter(ModifiableResourcePower::enforceLimits),
            Codec.BOOL.optionalFieldOf("retain_value", false).forGetter(ModifiableResourcePower::retainValue),
            HudRender.CODEC.optionalFieldOf("hud_render").forGetter(ModifiableResourcePower::hudRender),
            HudRender.CODEC.optionalFieldOf("origins-math:hud_render").forGetter(power -> Optional.empty()),
            EntityAction.optionalCodec("min_action").forGetter(ModifiableResourcePower::minAction),
            EntityAction.optionalCodec("max_action").forGetter(ModifiableResourcePower::maxAction)
    ).apply(instance, (settings, min, max, startValue, enforceLimits, retainValue, hudRender, originsMathHudRender, minAction, maxAction) ->
            new ModifiableResourcePower(settings, min, max, startValue, enforceLimits, retainValue,
                    originsMathHudRender.or(() -> hudRender), minAction, maxAction)));

    private final int min;
    private final int max;
    private final OptionalInt startValue;
    private final boolean enforceLimits;
    private final boolean retainValue;
    private final Optional<HudRender> hudRender;
    private final EntityAction minAction;
    private final EntityAction maxAction;

    public ModifiableResourcePower(BaseSettings settings, int min, int max, OptionalInt startValue, boolean enforceLimits, boolean retainValue,
                                   Optional<HudRender> hudRender, EntityAction minAction, EntityAction maxAction) {
        super(settings);
        this.min = min;
        this.max = max;
        this.startValue = startValue;
        this.enforceLimits = enforceLimits;
        this.retainValue = retainValue;
        this.hudRender = hudRender;
        this.minAction = minAction;
        this.maxAction = maxAction;
    }

    @Override
    public int getMinValue() {
        return this.min;
    }

    @Override
    public int getMaxValue() {
        return this.max;
    }

    public OptionalInt getStartValue() {
        return this.startValue;
    }

    public boolean enforceLimits() {
        return this.enforceLimits;
    }

    public boolean retainValue() {
        return this.retainValue;
    }

    public Optional<HudRender> hudRender() {
        return this.hudRender;
    }

    public EntityAction minAction() {
        return this.minAction;
    }

    public EntityAction maxAction() {
        return this.maxAction;
    }

    @Override
    public void createComponents(ComponentCollector collector) {
        super.createComponents(collector);
        collector.add(new ResourceComponent(this.startValue.orElse(this.min), Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Override
    public double getDoubleMin(OriginDataHolder holder) {
        return ResourceValueHelper.applyModifiers(holder, ModifyResourceMinimumPower.class,
                power -> power.appliesTo(this.getId(holder.getAccess())), this.min);
    }

    @Override
    public double getDoubleMax(OriginDataHolder holder) {
        return ResourceValueHelper.applyModifiers(holder, ModifyResourceMaximumPower.class,
                power -> power.appliesTo(this.getId(holder.getAccess())), this.max);
    }

    @Override
    public double getDoubleValue(OriginDataHolder holder) {
        ResourceComponent component = holder.getComponentFor(this, ResourceComponent.class).orElse(null);
        if (component == null) return this.startValue.orElse(this.min);
        int value = component.getValue();
        if (!this.enforceLimits) return value;
        int clamped = (int) Math.clamp(value, this.getDoubleMin(holder), this.getDoubleMax(holder));
        if (!this.retainValue && value != clamped) component.setValue(clamped);
        return clamped;
    }

    @Override
    public void setDoubleValue(OriginDataHolder holder, double value) {
        ResourceComponent component = holder.getComponentFor(this, ResourceComponent.class).orElse(null);
        if (component == null) return;
        int previous = component.getValue();
        int requested = (int) value;
        int minimum = (int) this.getDoubleMin(holder);
        int maximum = (int) this.getDoubleMax(holder);
        if (this.retainValue && (requested < minimum || requested > maximum) && (previous < minimum || previous > maximum))
            return;
        int actual = Math.clamp(requested, minimum, maximum);
        if (previous == actual) return;
        component.setValue(actual);
        if (actual == minimum && this.minAction != null) this.minAction.execute(holder.getEntity());
        if (actual == maximum && this.maxAction != null) this.maxAction.execute(holder.getEntity());
    }

    @Override
    public int getValue(OriginDataHolder holder) {
        return (int) this.getDoubleValue(holder);
    }

    @Override
    public void setValue(OriginDataHolder holder, int value) {
        this.setDoubleValue(holder, value);
    }

    @Override
    public Power getPowerForHudRender() {
        return this;
    }

    @Override
    public Optional<HudRender> getHudRenderData() {
        return this.hudRender;
    }

    @Override
    public float getRenderPercentage(OriginDataHolder holder) {
        return HudRenderable.clampProgress((float) this.getDoubleValue(holder), (float) this.getDoubleMin(holder), (float) this.getDoubleMax(holder));
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }
}

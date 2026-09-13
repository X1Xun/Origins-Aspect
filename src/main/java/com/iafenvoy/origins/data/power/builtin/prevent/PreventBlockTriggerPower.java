package com.iafenvoy.origins.data.power.builtin.prevent;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class PreventBlockTriggerPower extends Power {
    public static final MapCodec<PreventBlockTriggerPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            EntityAction.optionalCodec("entity_action").forGetter(PreventBlockTriggerPower::getEntityAction)
    ).apply(i, PreventBlockTriggerPower::new));

    private final EntityAction entityAction;

    public PreventBlockTriggerPower(BaseSettings settings, EntityAction entityAction) {
        super(settings);
        this.entityAction = entityAction;
    }

    public EntityAction getEntityAction() {
        return this.entityAction;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }
}

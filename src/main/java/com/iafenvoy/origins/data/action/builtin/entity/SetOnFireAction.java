package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record SetOnFireAction(ResourceReference tick) implements EntityAction {
    public static final MapCodec<SetOnFireAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.INT_CODEC.fieldOf("tick").forGetter(SetOnFireAction::tick)
    ).apply(i, SetOnFireAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        source.setRemainingFireTicks(this.tick.resolveInt(source));
    }
}

package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record SetFallDistanceAction(ResourceReference fallDistance) implements EntityAction {
    public static final MapCodec<SetFallDistanceAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.FLOAT_CODEC.fieldOf("fall_distance").forGetter(SetFallDistanceAction::fallDistance)
    ).apply(i, SetFallDistanceAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        source.fallDistance = this.fallDistance.resolveFloat(source);
    }
}

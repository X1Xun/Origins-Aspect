package com.iafenvoy.origins.data.condition.builtin.entity;

import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.condition.EntityCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record EmptyResourceCondition(ResourceLocation resource) implements EntityCondition {
    public static final MapCodec<EmptyResourceCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("resource").forGetter(EmptyResourceCondition::resource)
    ).apply(i, EmptyResourceCondition::new));

    @Override
    public @NotNull MapCodec<? extends EntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity entity) {
        try {
            return ResourceValueHelper.valueOrThrow(entity, this.resource) <= ResourceValueHelper.minOrThrow(entity, this.resource);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}

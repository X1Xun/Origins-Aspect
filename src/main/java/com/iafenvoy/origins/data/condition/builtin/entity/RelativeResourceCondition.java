package com.iafenvoy.origins.data.condition.builtin.entity;

import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.Comparison;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record RelativeResourceCondition(ResourceLocation resource, Comparison comparison,
                                        ResourceReference relativity) implements EntityCondition {
    public static final MapCodec<RelativeResourceCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("resource").forGetter(RelativeResourceCondition::resource),
            Comparison.CODEC.forGetter(RelativeResourceCondition::comparison),
            ResourceReference.CODEC.fieldOf("relativity").forGetter(RelativeResourceCondition::relativity)
    ).apply(i, RelativeResourceCondition::new));

    @Override
    public @NotNull MapCodec<? extends EntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity entity) {
        try {
            return this.comparison.compare(ResourceValueHelper.valueOrThrow(entity, this.resource) / ResourceValueHelper.maxOrThrow(entity, this.resource), this.relativity.resolve(entity));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}

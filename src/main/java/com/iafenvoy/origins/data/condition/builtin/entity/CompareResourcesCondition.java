package com.iafenvoy.origins.data.condition.builtin.entity;

import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.Comparison;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record CompareResourcesCondition(ResourceLocation leftResource, Comparison comparison,
                                        ResourceLocation rightResource) implements EntityCondition {
    public static final MapCodec<CompareResourcesCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("left_resource").forGetter(CompareResourcesCondition::leftResource),
            Comparison.CODEC.forGetter(CompareResourcesCondition::comparison),
            ResourceLocation.CODEC.fieldOf("right_resource").forGetter(CompareResourcesCondition::rightResource)
    ).apply(i, CompareResourcesCondition::new));

    @Override
    public @NotNull MapCodec<? extends EntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity entity) {
        try {
            return this.comparison.compare(ResourceValueHelper.valueOrThrow(entity, this.leftResource), ResourceValueHelper.valueOrThrow(entity, this.rightResource));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}

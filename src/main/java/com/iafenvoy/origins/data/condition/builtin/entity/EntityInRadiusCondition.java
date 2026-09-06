package com.iafenvoy.origins.data.condition.builtin.entity;

import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.Comparison;
import com.iafenvoy.origins.util.math.Shape;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record EntityInRadiusCondition(EntityCondition entityCondition, ResourceReference radius, Shape shape,
                                      Comparison comparison) implements EntityCondition {
    public static final MapCodec<EntityInRadiusCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            EntityCondition.optionalCodec("entity_condition").forGetter(EntityInRadiusCondition::entityCondition),
            ResourceReference.INT_CODEC.fieldOf("radius").forGetter(EntityInRadiusCondition::radius),
            Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(EntityInRadiusCondition::shape),
            Comparison.optionalCodec(Comparison.CompareOperation.GREATER_THAN_OR_EQUAL, 1).forGetter(EntityInRadiusCondition::comparison)
    ).apply(i, EntityInRadiusCondition::new));

    @Override
    public @NotNull MapCodec<? extends EntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity entity) {
        int matches = 0;
        for (Entity e : this.shape.getEntities(entity.level(), entity.position(), Math.max(0, this.radius.resolveInt(entity))))
            if (this.entityCondition.test(e))
                ++matches;
        return this.comparison.compare(matches);
    }
}

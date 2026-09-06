package com.iafenvoy.origins.data.condition.builtin.bientity;

import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data.condition.BiEntityCondition;
import com.iafenvoy.origins.util.math.Comparison;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Compares a resource on the actor entity with a resource on the target entity.
 */
public record CompareResourcesCondition(ResourceLocation actorResource, Comparison comparison,
                                        ResourceLocation targetResource) implements BiEntityCondition {
    public static final MapCodec<CompareResourcesCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            // actor_resource is the bi-entity terminology used by Origins. Keep
            // source_resource as a read-only compatibility alias for early
            // NeoForge builds of this condition.
            ResourceLocation.CODEC.optionalFieldOf("actor_resource").forGetter(x -> Optional.ofNullable(x.actorResource)),
            ResourceLocation.CODEC.optionalFieldOf("source_resource").forGetter(x -> Optional.empty()),
            Comparison.CODEC.forGetter(CompareResourcesCondition::comparison),
            ResourceLocation.CODEC.fieldOf("target_resource").forGetter(CompareResourcesCondition::targetResource)
    ).apply(i, (actor, source, comparison, target) -> new CompareResourcesCondition(actor.or(() -> source).orElseThrow(() -> new IllegalArgumentException("Missing actor_resource")), comparison, target)));

    @Override
    public @NotNull MapCodec<? extends BiEntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity source, @NotNull Entity target) {
        if (!ResourceValueHelper.hasResource(source, this.actorResource) || !ResourceValueHelper.hasResource(target, this.targetResource))
            return false;
        return this.comparison.compare(ResourceValueHelper.value(source, this.actorResource), ResourceValueHelper.value(target, this.targetResource));
    }
}

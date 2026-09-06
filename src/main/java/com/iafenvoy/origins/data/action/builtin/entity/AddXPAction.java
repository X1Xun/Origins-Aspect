package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record AddXPAction(Optional<ResourceReference> points,
                          Optional<ResourceReference> levels) implements EntityAction {
    public static final MapCodec<AddXPAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.INT_CODEC.optionalFieldOf("points").forGetter(AddXPAction::points),
            ResourceReference.INT_CODEC.optionalFieldOf("levels").forGetter(AddXPAction::levels)
    ).apply(i, AddXPAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (source instanceof Player player) {
            this.points.ifPresent(value -> player.giveExperiencePoints(value.resolveInt(source)));
            this.levels.ifPresent(value -> player.giveExperienceLevels(value.resolveInt(source)));
        }
    }
}

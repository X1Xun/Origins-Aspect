package com.iafenvoy.origins.data.action.builtin.block.meta;

import com.iafenvoy.origins.data.action.BlockAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RelativeOffsetAction(BlockAction action, ResourceReference distance) implements BlockAction {
    public static final MapCodec<RelativeOffsetAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockAction.CODEC.fieldOf("action").forGetter(RelativeOffsetAction::action),
            ResourceReference.INT_CODEC.fieldOf("distance").forGetter(RelativeOffsetAction::distance)
    ).apply(i, RelativeOffsetAction::new));

    @Override
    public @NotNull MapCodec<? extends BlockAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        int distance = this.distance.resolveInt(BlockAction.executionEntity());
        direction.ifPresent(d -> this.action.execute(level, pos.relative(d, distance), direction));
    }
}

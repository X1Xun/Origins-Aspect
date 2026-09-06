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

public record ForRangeAction(ResourceReference range, BlockAction action) implements BlockAction {
    public static final MapCodec<ForRangeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceReference.CODEC.fieldOf("range").forGetter(ForRangeAction::range),
            BlockAction.CODEC.fieldOf("action").forGetter(ForRangeAction::action)
    ).apply(instance, ForRangeAction::new));

    @Override
    public @NotNull MapCodec<? extends BlockAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        // Block actions are invoked from an entity action/power in the common case.
        // Preserve that originating entity so resource-backed ranges can resolve
        // against the same holder instead of silently resolving to zero.
        int count = this.range.resolveIterations(BlockAction.executionEntity());
        for (int i = 0; i < count; i++) this.action.execute(level, pos, direction);
    }
}

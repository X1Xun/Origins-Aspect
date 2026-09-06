package com.iafenvoy.origins.data.action.builtin.block.meta;

import com.iafenvoy.origins.data.action.BlockAction;
import com.iafenvoy.origins.util.Timeout;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record DelayAction(BlockAction action, ResourceReference ticks) implements BlockAction {
    public static final MapCodec<DelayAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockAction.CODEC.fieldOf("action").forGetter(DelayAction::action),
            ResourceReference.INT_CODEC.fieldOf("ticks").forGetter(DelayAction::ticks)
    ).apply(i, DelayAction::new));

    @Override
    public @NotNull MapCodec<? extends BlockAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        double resolved = this.ticks.resolve(BlockAction.executionEntity());
        int ticks = resolved <= 0 ? 0 : resolved >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) resolved;
        Entity entity = BlockAction.executionEntity();
        Timeout.create(ticks, () -> {
            if (entity == null) this.action.execute(level, pos, direction);
            else BlockAction.executeWithContext(this.action, entity, level, pos, direction);
        });
    }
}

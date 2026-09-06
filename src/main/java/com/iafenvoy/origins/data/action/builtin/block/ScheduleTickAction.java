package com.iafenvoy.origins.data.action.builtin.block;

import com.iafenvoy.origins.data.action.BlockAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ScheduleTickAction(ResourceReference delay) implements BlockAction {
    public static final MapCodec<ScheduleTickAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceReference.INT_CODEC.fieldOf("delay").forGetter(ScheduleTickAction::delay)
    ).apply(instance, ScheduleTickAction::new));

    @Override
    public @NotNull MapCodec<? extends BlockAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        double resolved = this.delay.resolve(BlockAction.executionEntity());
        int delay = resolved <= 0 ? 0 : resolved >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) resolved;
        level.scheduleTick(pos, level.getBlockState(pos).getBlock(), delay);
    }
}

package com.iafenvoy.origins.data.action.builtin.block.meta;

import com.iafenvoy.origins.data.action.BlockAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record AbsoluteOffsetAction(BlockAction action, ResourceReference x, ResourceReference y,
                                   ResourceReference z) implements BlockAction {
    public static final MapCodec<AbsoluteOffsetAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockAction.CODEC.fieldOf("action").forGetter(AbsoluteOffsetAction::action),
            ResourceReference.INT_CODEC.optionalFieldOf("x", ResourceReference.number(0)).forGetter(AbsoluteOffsetAction::x),
            ResourceReference.INT_CODEC.optionalFieldOf("y", ResourceReference.number(0)).forGetter(AbsoluteOffsetAction::y),
            ResourceReference.INT_CODEC.optionalFieldOf("z", ResourceReference.number(0)).forGetter(AbsoluteOffsetAction::z)
    ).apply(i, AbsoluteOffsetAction::new));

    @Override
    public @NotNull MapCodec<? extends BlockAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        Entity entity = BlockAction.executionEntity();
        this.action.execute(level, pos.offset(this.x.resolveInt(entity), this.y.resolveInt(entity), this.z.resolveInt(entity)), direction);
    }
}

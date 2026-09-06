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

public record ChanceAction(BlockAction action, ResourceReference chance,
                           BlockAction failAction) implements BlockAction {
    public static final MapCodec<ChanceAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockAction.CODEC.fieldOf("action").forGetter(ChanceAction::action),
            ResourceReference.FLOAT_CODEC.fieldOf("chance").forGetter(ChanceAction::chance),
            BlockAction.optionalCodec("fail_action").forGetter(ChanceAction::failAction)
    ).apply(i, ChanceAction::new));

    @Override
    public @NotNull MapCodec<? extends BlockAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        if (Math.random() < this.chance.resolveFloat(BlockAction.executionEntity()))
            this.action.execute(level, pos, direction);
        else this.failAction.execute(level, pos, direction);
    }
}

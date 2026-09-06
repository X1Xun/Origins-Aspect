package com.iafenvoy.origins.data.action;

import com.iafenvoy.origins.data.action.builtin.block.meta.AndAction;
import com.iafenvoy.origins.util.codec.DefaultedCodec;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public interface BlockAction {
    ThreadLocal<Entity> EXECUTION_ENTITY = new ThreadLocal<>();
    Codec<BlockAction> SINGLE_CODEC = DefaultedCodec.registryDispatch(ActionRegistries.BLOCK_ACTION, BlockAction::codec, Function.identity(), () -> NoOpAction.INSTANCE);
    Codec<BlockAction> CODEC = Codec.either(SINGLE_CODEC.listOf(), SINGLE_CODEC).xmap(e -> e.map(AndAction::new, Function.identity()), Either::right);

    static MapCodec<BlockAction> optionalCodec(String name) {
        return CODEC.optionalFieldOf(name, NoOpAction.INSTANCE);
    }

    @NotNull
    MapCodec<? extends BlockAction> codec();

    void execute(@NotNull Level level, @NotNull BlockPos pos, @NotNull Optional<Direction> direction);

    /**
     * Executes a block action while exposing the originating entity to dynamic values.
     */
    static void executeWithContext(@NotNull BlockAction action, @NotNull Entity entity, @NotNull Level level,
                                   @NotNull BlockPos pos, @NotNull Optional<Direction> direction) {
        Entity previous = EXECUTION_ENTITY.get();
        EXECUTION_ENTITY.set(entity);
        try {
            action.execute(level, pos, direction);
        } finally {
            if (previous == null) EXECUTION_ENTITY.remove();
            else EXECUTION_ENTITY.set(previous);
        }
    }

    static Entity executionEntity() {
        return EXECUTION_ENTITY.get();
    }
}

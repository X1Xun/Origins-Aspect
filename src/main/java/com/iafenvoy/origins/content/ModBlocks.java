package com.iafenvoy.origins.content;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    // 1. Создаем регистратор для блоков под твой MODID
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks("origins");

    // 2. Регистрируем сам блок мела
    // Мы берем свойства (Properties) от ванильного редстоуна, чтобы мел ломался мгновенно, не имел коллизии и т.д.
    public static final Supplier<Block> CHALK_SIGN = BLOCKS.register("chalk_sign",
            () -> new ChalkSignBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            ));
    // Меняем WitchSaltWireBlock на WitchSaltBlock
    public static final Supplier<Block> WITCH_SALT_WIRE = BLOCKS.register("witch_salt_wire",
            () -> new WitchSaltBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            ));
}

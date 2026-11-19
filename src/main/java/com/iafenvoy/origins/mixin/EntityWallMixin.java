package com.iafenvoy.origins.mixin;

import com.iafenvoy.origins.PhaseCheck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BlockBehaviour.class)
public class EntityWallMixin {

    // Вместо объектов Blocks используем текстовые ID (они безопасны при старте)
    private static final Set<ResourceLocation> ORIGINS$BANNED_BLOCKS = Set.of(
            ResourceLocation.withDefaultNamespace("bedrock"),
            ResourceLocation.withDefaultNamespace("barrier"),
            ResourceLocation.withDefaultNamespace("obsidian"),
            ResourceLocation.withDefaultNamespace("diamond_block"),
            ResourceLocation.withDefaultNamespace("waxed_cut_copper"),
            ResourceLocation.withDefaultNamespace("waxed_copper_block")
    );

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void origins$cancelWallCollisions(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (context instanceof EntityCollisionContext entityContext) {
            if (entityContext.getEntity() != null && entityContext.getEntity() instanceof Player player) {

                // ПРОВЕРКА УСЛОВИЯ: Если игроку нельзя фазировать — коллизия работает как обычно
                if (!PhaseCheck.canPhase(player)) return;

                Block currentBlock = state.getBlock();
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(currentBlock);

                if (ORIGINS$BANNED_BLOCKS.contains(blockId)) return;

                if (player.isShiftKeyDown()) {
                    cir.setReturnValue(net.minecraft.world.phys.shapes.Shapes.empty());
                    return;
                }

                double playerFeetY = player.getY();
                double blockTopY = pos.getY() + 1.0;

                if (blockTopY <= playerFeetY + 0.1) return;

                cir.setReturnValue(net.minecraft.world.phys.shapes.Shapes.empty());
            }
        }
    }
}

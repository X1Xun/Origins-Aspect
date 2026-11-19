package com.iafenvoy.origins.content;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.iafenvoy.origins.content.ModBlocks.CHALK_SIGN;

public class ChalkItem extends Item {
    public ChalkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos().above();
        if (level.getBlockState(targetPos).isAir()) {
            if (!level.isClientSide) {
                int randomRune = level.random.nextInt(5);
                BlockState chalkState = CHALK_SIGN.get().defaultBlockState()
                        .setValue(ChalkSignBlock.RUNE_TYPE, randomRune);
                level.setBlock(targetPos, chalkState, 3);
                InteractionHand hand = context.getHand();
                EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                Player player = context.getPlayer();
                assert player != null;
                context.getItemInHand().hurtAndBreak(1, player, slot);
                level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}


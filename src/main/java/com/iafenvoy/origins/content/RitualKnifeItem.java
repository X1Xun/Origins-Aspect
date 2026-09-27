package com.iafenvoy.origins.content;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
public class RitualKnifeItem extends Item {

    public RitualKnifeItem(Tier tier, Properties properties) {
        super(properties.attributes(
                SwordItem.createAttributes(tier, 3, -1.8F)
        ));
    }
    @Override
    public boolean canAttackBlock(BlockState state, Level level, net.minecraft.core.BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.COBWEB)) {
            return 15.0F;
        }
        return super.getDestroySpeed(stack, state);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 20);
        player.swing(hand);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);

            ItemStack rewardStack = new ItemStack(ModItems.GRUME.get());
            boolean added = player.getInventory().add(rewardStack);
            if (!added) {
                player.drop(rewardStack, false);
            }
            player.addEffect(new MobEffectInstance(ModEffects.BLOOD, 120));
            Vector3f fromColor = new Vector3f(1.0F, 0.0F, 0.0F);
            Vector3f toColor = new Vector3f(1.0F, 0.0F, 0.0F);
            DustColorTransitionOptions redTransition = new DustColorTransitionOptions(fromColor, toColor, 0.2F);
            serverLevel.sendParticles(redTransition,
                    player.getX(), player.getY() + 0.5D, player.getZ(),
                    40, 0.5D, 0.5D, 0.5D, 0.1D);
            int currentBloodLvl = 0;

            try {
                if (ModAttachments.BLOODY != null && ModAttachments.BLOODY.get() != null && player.hasData(ModAttachments.BLOODY.get())) {
                    currentBloodLvl = player.getData(ModAttachments.BLOODY.get());
                }
                int nextBloodLvl = currentBloodLvl + 1;
                if (ModAttachments.BLOODY != null && ModAttachments.BLOODY.get() != null) {
                    player.setData(ModAttachments.BLOODY.get(), nextBloodLvl);
                    if (player.getData(ModAttachments.BLOODY.get())==59) {
                        player.sendSystemMessage(Component.literal("Внимание! Следующее использование кинжала приведёт к мгновенной смерти!"));
                    }
                }
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Уровень крови: " + nextBloodLvl));

            } catch (Exception e) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Ошибка мода]: Проблема с Attachment!"));
                e.printStackTrace();
            }
            EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            itemStack.hurtAndBreak(1, player, slot);
            player.containerMenu.broadcastChanges();

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.consume(itemStack);
    }
}

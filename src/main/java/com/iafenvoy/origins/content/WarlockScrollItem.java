package com.iafenvoy.origins.content;

import com.iafenvoy.origins.data.power.builtin.regular.WarlockMainPower.warlock_powers;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class WarlockScrollItem extends Item {
    private final warlock_powers targetPower;
    private final String tooltipTranslationKey;

    public WarlockScrollItem(Properties properties, warlock_powers targetPower, String tooltipTranslationKey) {
        super(properties);
        this.targetPower = targetPower;
        this.tooltipTranslationKey = tooltipTranslationKey;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(
            net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {

        net.minecraft.world.item.ItemStack itemStack = player.getItemInHand(hand);

        int secondsLeft = com.iafenvoy.origins.data.power.builtin.regular.WarlockMainPower.getScrollCooldownRemaining(player);
        if (secondsLeft > 0) {
            if (!level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§cСвитки перезаряжаются! Подождите еще §e" + secondsLeft + " §cсек."), true);
            }
            return net.minecraft.world.InteractionResultHolder.fail(itemStack);
        }

        com.iafenvoy.origins.data.power.builtin.regular.WarlockMainPower.changePowerForPlayer(player, targetPower);

        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.8F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);

            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§6Стихия успешно изменена на: " + targetPower.name()), true);
        }

        return net.minecraft.world.InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
    private String getPowerChatColor() {
        return switch (this.targetPower) {
            case EARTH -> "§a";
            case UNDEAD -> "§b";
            case INFERNAL -> "§c";
            case DRAGON -> "§d";
            case ANCIENT -> "§3";
            case PILLAGER -> "§7";
            case WIND -> "&b";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Используйте, чтобы открыть магию " + getPowerChatColor() + tooltipTranslationKey));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}

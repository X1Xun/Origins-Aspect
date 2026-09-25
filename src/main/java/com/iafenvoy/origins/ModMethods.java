package com.iafenvoy.origins;

import com.iafenvoy.origins.content.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModMethods {
    public static boolean isGrumeInHotbar(Player player) {
        Item targetItem = ModItems.GRUME.get();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(targetItem)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}

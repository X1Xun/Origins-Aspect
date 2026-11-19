package com.iafenvoy.origins;

import net.minecraft.world.entity.player.Player;

public class PhaseCheck {

    /**
     * ГЛАВНОЕ УСЛОВИЕ: Имеет ли право этот конкретный игрок проходить сквозь стены?
     */
    public static boolean canPhase(Player player) {
        if (player == null || player.isSpectator() || player.isCreative()) return false;

        // Тут будет твое условие (например, выбор расы, предмет, переменная)
        return false;
    }
}

package com.iafenvoy.origins.rituals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.List;

public record RitualContext(
        Level level,
        BlockPos pos,
        Player player,
        List<ItemEntity> itemsOnGround
) {}

package com.iafenvoy.origins; // Замени на свой пакет

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;

public class PhaseModeHandler {

    private static final Set<Block> BANNED_BLOCKS = Set.of(
            Blocks.BEDROCK,
            Blocks.BARRIER,
            Blocks.OBSIDIAN
    );

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
//        Player player = event.getEntity();
//        if (!PhaseCheck.canPhase(player)) return;
//
//        if (player.isSpectator() || player.isCreative()) return;
//
//        // Базовый вектор движения (куда игрок хочет идти)
//        Vec3 lookVec = player.getLookAngle();
//        // Получаем скорость нажатия клавиш WASD
//        double speed = 0.2;
//
//        // Проверяем блок строго под игроком и перед ним
//        BlockPos playerPos = player.blockPosition();
//        BlockPos blockBelow = playerPos.below();
//        Block blockUnderFeet = player.level().getBlockState(blockBelow).getBlock();
//
//        // Если под ногами Бедрок — аварийно выходим, возвращая стандартную физику
//        if (BANNED_BLOCKS.contains(blockUnderFeet)) {
//            player.noPhysics = false;
//            return;
//        }
//
//        // ПРОВЕРКА НА НАЖАТИЕ SHIFT (Сквозь пол)
//        if (player.isShiftKeyDown()) {
//            // Принудительно ставим флаг отсутствия физики
//            player.noPhysics = true;
//
//            // Двигаем игрока строго вниз сквозь пол
//            player.setDeltaMovement(player.getDeltaMovement().x, -0.25, player.getDeltaMovement().z);
//            return;
//        }
//
//        // ПРОВЕРКА НА ХОЖДЕНИЕ СКВОЗЬ СТЕНЫ
//        // Если игрок вплотную подошел к стене (или уже находится внутри блока)
//        boolean isInsideWall = player.level().getBlockState(playerPos).getBlock() != Blocks.AIR;
//
//        if (player.horizontalCollision || isInsideWall) {
//
//            // Проверяем, не пытается ли игрок пройти сквозь запрещенный блок перед собой
//            BlockPos frontPos = BlockPos.containing(player.getX() + lookVec.x, player.getY(), player.getZ() + lookVec.z);
//            if (BANNED_BLOCKS.contains(player.level().getBlockState(frontPos).getBlock())) {
//                player.noPhysics = false;
//                return; // Упрется в Бедрок
//            }
//
//            // Включаем noPhysics, чтобы отключить алгоритм отталкивания стенами
//            player.noPhysics = true;
//
//            // Так как в режиме noPhysics ванильные WASD кнопки перестают двигать игрока вперед по земле,
//            // мы рассчитываем вектор движения вперед вручную на основе поворота его головы
//            if (player.zza > 0) { // Игрок жмет W
//                player.setDeltaMovement(lookVec.x * speed, player.getDeltaMovement().y, lookVec.z * speed);
//            }
//        } else {
//            // Если стен вокруг нет — возвращаем обычную физику, чтобы игрок нормально ходил по земле
//            player.noPhysics = false;
//        }
    }
}

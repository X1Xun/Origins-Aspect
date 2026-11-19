package com.iafenvoy.origins.rituals;

import com.iafenvoy.origins.content.ChalkSignBlock;
import com.iafenvoy.origins.content.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public record FlexibleRitual(
        BlockPos[] circleOffsets, // Уникальная форма круга для ЭТОГО ритуала
        BiPredicate<RitualContext, Integer> condition, // Условие (Контекст + Сумма рун)
        BiConsumer<RitualContext, Integer> action      // Действие (Контекст + Сумма рун)
) {
    public boolean tryExecute(RitualContext context) {
        int totalRuneValue = 0;

        // 1. Проверяем, выложена ли именно НАША форма круга из мела в мире
        for (BlockPos offset : circleOffsets) {
            // Считаем мировые координаты блока с учетом позиции свечи
            BlockPos checkPos = context.pos().offset(offset.getX(), offset.getY(), offset.getZ());
            BlockState blockState = context.level().getBlockState(checkPos);

            // Если в нужной точке нет мела — рецепт сразу не подходит
            if (!blockState.is(ModBlocks.CHALK_SIGN.get())) {
                return false;
            }

            // Суммируем силу рун
            totalRuneValue += blockState.getValue(ChalkSignBlock.RUNE_TYPE);
        }
        if (condition.test(context, totalRuneValue)) {
            action.accept(context, totalRuneValue);
            return true;
        }

        return false;
    }
}

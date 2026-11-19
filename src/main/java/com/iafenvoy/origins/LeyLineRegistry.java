package com.iafenvoy.origins; // Замени на свой пакет

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class LeyLineRegistry {

    // Шаг сетки: 10 бит = 1024 блока. Это сделает сектора огромными
    private static final int SECTOR_BITS = 10;
    private static final int SECTOR_SIZE = 1 << SECTOR_BITS;

    public static boolean isNode(Level level, BlockPos pos) {
        // Тестовый сид для клиента, для сервера берем честный getSeed()
        long seed = level.isClientSide() ? 123456789L : ((ServerLevel) level).getSeed();

        // Делим координаты на гигантские сектора 1024х1024
        int sectorX = pos.getX() >> SECTOR_BITS;
        int sectorZ = pos.getZ() >> SECTOR_BITS;

        // Генерируем уникальный рандом для этого сектора
        long sectorSeed = (long) sectorX * 341873128712L + (long) sectorZ * 132897987541L + seed;
        java.util.Random random = new java.util.Random(sectorSeed);

        // ВАРИАЦИЯ РЕДКОСТИ: Шанс того, что в этом огромном секторе ВООБЩЕ есть жила — всего 25%
        if (random.nextFloat() > 0.25F) {
            return false;
        }

        // Если сектор удачный, вычисляем точные координаты центра узла внутри него (от 0 до 1023)
        int nodeOffsetX = random.nextInt(SECTOR_SIZE);
        int nodeOffsetZ = random.nextInt(SECTOR_SIZE);

        int targetX = (sectorX << SECTOR_BITS) + nodeOffsetX;
        int targetZ = (sectorZ << SECTOR_BITS) + nodeOffsetZ;

        // Игрок находится в радиусе 24 блоков от эпицентра астрального канала?
        // Увеличим радиус детекции до 24 блоков, чтобы редкую точку было проще поймать
        double distanceSq = pos.distToLowCornerSqr(targetX, pos.getY(), targetZ);
        return distanceSq <= 576.0D; // 24^2 = 576
    }
}

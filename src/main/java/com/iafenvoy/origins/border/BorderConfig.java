package com.iafenvoy.origins.border;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BorderConfig {
    // Этот билдер помогает построить структуру файла .toml
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Объявляем финальную спецификацию, которую передадим в NeoForge
    public static final ModConfigSpec SPEC;

    // Переменные, из которых мы будем читать координаты барьера в игре
    private static final ModConfigSpec.IntValue MIN_X;
    private static final ModConfigSpec.IntValue MIN_Z;
    private static final ModConfigSpec.IntValue MAX_X;
    private static final ModConfigSpec.IntValue MAX_Z;

    static {
        BUILDER.comment("Настройки кастомного барьера мира").push("custom_border");

        // Задаем значения по умолчанию (-1000 и 1000) и их допустимый диапазон
        MIN_X = BUILDER.comment("Минимальная граница барьера по X").defineInRange("minX", -1000, -30000000, 30000000);
        MIN_Z = BUILDER.comment("Минимальная граница барьера по Z").defineInRange("minZ", -1000, -30000000, 30000000);
        MAX_X = BUILDER.comment("Максимальная граница барьера по X").defineInRange("maxX", 1000, -30000000, 30000000);
        MAX_Z = BUILDER.comment("Максимальная граница барьера по Z").defineInRange("maxZ", 1000, -30000000, 30000000);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    // Удобные методы-геттеры, чтобы быстро получать чистые int значения в коде мода
    public static int getMinX() { return MIN_X.get(); }
    public static int getMinZ() { return MIN_Z.get(); }
    public static int getMaxX() { return MAX_X.get(); }
    public static int getMaxZ() { return MAX_Z.get(); }

    // Метод, который мы вызовем из твоей команды, чтобы перезаписать файл на диске
    public static void saveBounds(int x1, int z1, int x2, int z2) {
        MIN_X.set(Math.min(x1, x2));
        MIN_Z.set(Math.min(z1, z2));
        MAX_X.set(Math.max(x1, x2));
        MAX_Z.set(Math.max(z1, z2));

        // Принудительно сохраняем изменения в файл на жестком диске
        SPEC.save();
    }
}

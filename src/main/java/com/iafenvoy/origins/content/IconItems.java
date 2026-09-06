package com.iafenvoy.origins.content;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.iafenvoy.origins.Origins.MOD_ID;

public class IconItems {

    // Создаем регистратор для предметов в стиле NeoForge
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    // Регистрируем пустые предметы для ваших иконок
    public static final DeferredItem<Item> ALCEMIST =
            ITEMS.registerSimpleItem("alchemist_icon", new Item.Properties());

    public static final DeferredItem<Item> ARCHER =
            ITEMS.registerSimpleItem("archer_icon", new Item.Properties());

    public static final DeferredItem<Item> BARBARIAN =
            ITEMS.registerSimpleItem("barbarian_icon", new Item.Properties());

    public static final DeferredItem<Item> CLERIC =
            ITEMS.registerSimpleItem("cleric_icon", new Item.Properties());

    public static final DeferredItem<Item> MONK =
            ITEMS.registerSimpleItem("monk_icon", new Item.Properties());

    public static final DeferredItem<Item> PALADIN =
            ITEMS.registerSimpleItem("paladin_icon", new Item.Properties());

    public static final DeferredItem<Item> RANGER =
            ITEMS.registerSimpleItem("ranger_icon", new Item.Properties());

    public static final DeferredItem<Item> ROGUE =
            ITEMS.registerSimpleItem("rogue_icon", new Item.Properties());

    public static final DeferredItem<Item> WARLOCK =
            ITEMS.registerSimpleItem("warlock_icon", new Item.Properties());
    // Метод для подключения регистратора к шине событий мода
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}




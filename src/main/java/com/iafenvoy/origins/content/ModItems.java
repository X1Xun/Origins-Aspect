package com.iafenvoy.origins.content;

import com.iafenvoy.origins.Origins; // Убедитесь, что здесь правильный импорт класса вашего мода
import com.iafenvoy.origins.data.power.builtin.regular.WarlockMainPower.warlock_powers;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Origins.MOD_ID);

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_EARTH = ITEMS.register("warlock_scroll_earth",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.EARTH, "Земли"));

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_FROST = ITEMS.register("warlock_scroll_frost",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.FROST, "Вечного Мороза"));

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_INFERNAL = ITEMS.register("warlock_scroll_infernal",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.INFERNAL, "Ада"));

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_DRAGON = ITEMS.register("warlock_scroll_dragon",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.DRAGON, "Дракона"));

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_ANCIENT = ITEMS.register("warlock_scroll_ancient",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.ANCIENT, "Древних"));

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_PILLAGER = ITEMS.register("warlock_scroll_pillager",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.PILLAGER, "Разбойников"));

    public static final DeferredHolder<Item, WarlockScrollItem> SCROLL_WIND = ITEMS.register("warlock_scroll_wind",
            () -> new WarlockScrollItem(new Item.Properties().stacksTo(1), warlock_powers.WIND, "Ветра"));

    public static final java.util.function.Supplier<Item> GRAPPLING_HOOK = ITEMS.registerItem(
            "grappling_hook",
            GrapplingHook::new,
            new Item.Properties().stacksTo(1)
    );
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

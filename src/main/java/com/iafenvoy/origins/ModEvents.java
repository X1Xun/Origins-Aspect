package com.iafenvoy.origins; // ЗАМЕНИТЕ на ваш реальный пакет (например, origins.aspect.event)

 // ЗАМЕНИТЕ на правильный путь к вашему BlueprintItem
import com.iafenvoy.origins.content.BlueprintItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;

public class ModEvents {
    @net.neoforged.bus.api.SubscribeEvent
    public static void onAdditionalModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
        // Принудительно говорим игре: "Загрузи эту модель в память при старте, она нам понадобится!"
        event.register(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath("origins", "item/blueprint_base")));
    }

    public static void onModelBakingCompleted(ModelEvent.BakingCompleted event) {
        ResourceLocation baseModelPath = ResourceLocation.fromNamespaceAndPath("origins", "item/blueprint_base");
        BakedModel baseBlueprintModel = event.getModels().get(baseModelPath);

        if (baseBlueprintModel != null) {
            BuiltInRegistries.ITEM.forEach(item -> {
                if (item instanceof BlueprintItem) {
                    ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
                    ModelResourceLocation inventoryModelLoc = new ModelResourceLocation(itemKey, "inventory");

                    event.getModels().put(inventoryModelLoc, baseBlueprintModel);
                }
            });
        }
    }
}

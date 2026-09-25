package com.example.mymod.datagen;

import com.iafenvoy.origins.content.ModItems; // Путь к вашему классу предметов
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ResourceLocation baseTexture = ResourceLocation.fromNamespaceAndPath("origins", "item/blueprint_base");
        DeferredHolder<Item, ?>[] blueprints = new DeferredHolder[]{
                ModItems.GRAPPLING_HOOK_BLUEPRINT,
                ModItems.EXPLOSIVE_ARROW_BLUEPRINT
        };

        for (DeferredHolder<Item, ?> blueprint : blueprints) {
            withExistingParent(blueprint.getId().getPath(), "item/generated")
                    .texture("layer0", baseTexture);
        }
    }
}

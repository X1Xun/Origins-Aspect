package com.iafenvoy.origins.data.power.builtin.prevent;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber
public class PreventBrewingInsertPower extends Power {

    public static final MapCodec<PreventBrewingInsertPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            BuiltInRegistries.ITEM.holderByNameCodec().listOf().fieldOf("ingredients").forGetter(PreventBrewingInsertPower::getIngredients),
            EntityAction.optionalCodec("entity_action").forGetter(PreventBrewingInsertPower::getEntityAction)
    ).apply(i, PreventBrewingInsertPower::new));

    private final List<Holder<Item>> ingredients;
    private final EntityAction entityAction;

    public PreventBrewingInsertPower(BaseSettings settings, List<Holder<Item>> ingredients, EntityAction entityAction) {
        super(settings);
        this.ingredients = ingredients;
        this.entityAction = entityAction;
    }

    public List<Holder<Item>> getIngredients() {
        return this.ingredients;
    }

    public EntityAction getEntityAction() {
        return this.entityAction;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemStacked(ItemStackedOnOtherEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;
        if (player.containerMenu instanceof BrewingStandMenu && event.getSlot().index == 3) {
            ItemStack carried = event.getCarriedItem();

            if (!carried.isEmpty()) {
                List<PreventBrewingInsertPower> activePowers = PowerHelper.get(player).listActive(PreventBrewingInsertPower.class);
                if (activePowers.isEmpty()) return;
                boolean isForbidden = activePowers.stream().anyMatch(p ->
                        p.ingredients.stream().anyMatch(forbidden -> forbidden.value() == carried.getItem())
                );

                if (isForbidden) {
                    event.setCanceled(true);

                    activePowers.forEach(x -> {
                        if (x.entityAction != null) {
                            x.entityAction.execute(player);
                        }
                    });
                }
            }
        }
    }
}

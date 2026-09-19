package com.iafenvoy.origins.content;

import com.iafenvoy.origins.content.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;

@EventBusSubscriber(modid = "origins")
public class ExplosiveArrowHandler {
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            if (arrow.getTags().contains("origins_explosive")) {
                Level level = arrow.level();
                if (!level.isClientSide) {
                    level.explode(arrow, arrow.getX(), arrow.getY(), arrow.getZ(), 3.0F, Level.ExplosionInteraction.TNT);
                    arrow.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        if (event.getProjectileWeaponItemStack().is(Items.BOW) && event.getProjectileItemStack().isEmpty()) {
            if (event.getEntity() instanceof Player player) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(ModItems.EXPLOSIVE_ARROW_ITEM.get())) {
                        event.setProjectileItemStack(stack);
                        return;
                    }
                }
            }
        }
    }
}

package com.iafenvoy.origins;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.config.OriginsConfig;
import com.iafenvoy.origins.content.ExplosiveArrowHandler;
import com.iafenvoy.origins.content.IconItems;
import com.iafenvoy.origins.content.ModEffects;
import com.iafenvoy.origins.content.ModItems;
import com.iafenvoy.origins.data.action.builtin.BiEntityActions;
import com.iafenvoy.origins.data.action.builtin.BlockActions;
import com.iafenvoy.origins.data.action.builtin.EntityActions;
import com.iafenvoy.origins.data.action.builtin.ItemActions;
import com.iafenvoy.origins.data.badge.BuiltinBadges;
import com.iafenvoy.origins.data.condition.builtin.*;
import com.iafenvoy.origins.data.power.builtin.ActionPowers;
import com.iafenvoy.origins.data.power.builtin.ModifyPowers;
import com.iafenvoy.origins.data.power.builtin.PreventPowers;
import com.iafenvoy.origins.data.power.builtin.RegularPowers;
import com.iafenvoy.origins.data.power.builtin.modify.ModifyPotionDurationPower;
import com.iafenvoy.origins.data.power.builtin.regular.HolyBoltPower;
import com.iafenvoy.origins.data.power.builtin.regular.WarlockMainPower;
import com.iafenvoy.origins.data.power.component.BuiltinComponents;
import com.iafenvoy.origins.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

@Mod(Origins.MOD_ID)
public final class Origins {
    public static final String MOD_ID = "origins";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().hasEffect(ModEffects.LOW_GRAVITY)) {
            event.setDistance(0.0F);
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Villager villager) {
            Player player = event.getEntity();
            var list = PowerHelper.get(player).listActive(WarlockMainPower.class);
            if (!list.isEmpty()) {
                if (!player.level().isClientSide()) {
                    MerchantOffers offers = villager.getOffers();
                    for (MerchantOffer offer : offers) {
                        int baseCost = offer.getBaseCostA().getCount();
                        int priceIncrease = (int) (baseCost);
                        offer.setSpecialPriceDiff(priceIncrease);
                    }
                }
            } else {

            }
        }
    }

    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        try {
            Player player = (Player) event.getEntity().getLastAttacker();
            var list = PowerHelper.get(player).listActive(HolyBoltPower.class);
            if (!list.isEmpty()) {
                if (event.getEntity() instanceof Villager villager) {
                    if (event.getSource().getEntity() instanceof Player player1) {
                        player.hurt(player.damageSources().playerAttack(player1), 3.0F);
                    }
                }
            } else {

            }
        }
        catch (Exception ex){

        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        var list = PowerHelper.get(player).listActive(HolyBoltPower.class);
        if (!player.level().isClientSide() && player.isAlive() && !list.isEmpty()) {
            if (player.level().isRainingAt(player.blockPosition())) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        200,
                        0,
                        false,
                        true
                ));
            }
        }
    }

    public Origins(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(Origins::onLivingFall);
        NeoForge.EVENT_BUS.addListener(ExplosiveArrowHandler::onGetProjectile);
        NeoForge.EVENT_BUS.addListener(ExplosiveArrowHandler::onProjectileImpact);
        NeoForge.EVENT_BUS.addListener(Origins::onVillagerInteract);
        NeoForge.EVENT_BUS.addListener(ModifyPotionDurationPower::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(Origins::onVillagerDeath);
        NeoForge.EVENT_BUS.addListener(Origins::onPlayerTick);
        ModEffects.register(bus);
        ConfigManager.getInstance().registerServerConfigHandler(OriginsConfig.INSTANCE, ServerConfigManager.PermissionChecker.IS_OPERATOR);
        ModItems.register(bus);
        OriginsAttachments.REGISTRY.register(bus);
        OriginsBlocks.REGISTRY.register(bus);
        OriginsCriterionTriggers.REGISTRY.register(bus);
        OriginsDataComponents.REGISTRY.register(bus);
        OriginsEntities.REGISTRY.register(bus);
        OriginsItems.REGISTRY.register(bus);
        OriginsLootItemConditions.REGISTRY.register(bus);
        OriginsLootItemFunctions.REGISTRY.register(bus);
        OriginsRecipeSerializers.REGISTRY.register(bus);
        IconItems.register(bus);
        //Action
        BiEntityActions.REGISTRY.register(bus);
        BlockActions.REGISTRY.register(bus);
        EntityActions.REGISTRY.register(bus);
        ItemActions.REGISTRY.register(bus);
        //Badge
        BuiltinBadges.REGISTRY.register(bus);
        //Condition
        BiEntityConditions.REGISTRY.register(bus);
        BiomeConditions.REGISTRY.register(bus);
        BlockConditions.REGISTRY.register(bus);
        DamageConditions.REGISTRY.register(bus);
        EntityConditions.REGISTRY.register(bus);
        FluidConditions.REGISTRY.register(bus);
        ItemConditions.REGISTRY.register(bus);
        //Powers
        ActionPowers.REGISTRY.register(bus);
        ModifyPowers.REGISTRY.register(bus);
        PreventPowers.REGISTRY.register(bus);
        RegularPowers.REGISTRY.register(bus);
        //Power Components
        BuiltinComponents.REGISTRY.register(bus);


    }
}

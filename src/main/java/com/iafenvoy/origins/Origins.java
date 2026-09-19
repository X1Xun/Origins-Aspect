package com.iafenvoy.origins;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.config.OriginsConfig;
import com.iafenvoy.origins.content.*;
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
import com.iafenvoy.origins.data.power.builtin.prevent.PreventBrewingInsertPower;
import com.iafenvoy.origins.data.power.builtin.regular.*;
import com.iafenvoy.origins.data.power.component.BuiltinComponents;
import com.iafenvoy.origins.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

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
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof AbstractIllager) {
            var list = PowerHelper.get(event.getSource().getEntity()).listActive(ModifyPotionDurationPower.class);
            if (event.getSource().getEntity() instanceof Player && !list.isEmpty()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player) {
            var list = PowerHelper.get(player).listActive(WarlockMainPower.class);
            if (!list.isEmpty()) {
                boolean isIllager = event.getEntity() instanceof AbstractIllager;
                boolean isUndead = event.getEntity().getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD);
                if (isIllager || isUndead) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(MobEffects.INVISIBILITY)) {
            var list = PowerHelper.get(player).listActive(ArrowRainPower.class);
            if (!list.isEmpty()) {
                event.setCanceled(true);
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
    public static void onFoodEat(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity instanceof Player player)) return;
        if (PowerHelper.get(player).listActive(AppleUpgradePower.class).isEmpty()) return;

        ItemStack eatenItem = event.getItem();
        if (eatenItem.is(Items.APPLE)) {
            applyFoodEffectsAndStats(player, Items.GOLDEN_APPLE.getDefaultInstance());
        }
        else if (eatenItem.is(Items.GOLDEN_APPLE)) {
            applyFoodEffectsAndStats(player, Items.ENCHANTED_GOLDEN_APPLE.getDefaultInstance());
        }
    }
    private static void applyFoodEffectsAndStats(Player player, ItemStack targetApple) {
        FoodProperties food = targetApple.get(net.minecraft.core.component.DataComponents.FOOD);

        if (food != null) {
            player.getFoodData().eat(food.nutrition(), food.saturation());
            for (FoodProperties.PossibleEffect possibleEffect : food.effects()) {
                if (player.getRandom().nextFloat() < possibleEffect.probability()) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(possibleEffect.effect()));
                }
            }
        }
    }

    private static boolean processingSoulBind = false;

    @SubscribeEvent
    public static void onJrecTakeDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || !(victim instanceof Player player) || processingSoulBind) return;
        UUID boundTargetUUID = SoulBindPower.ACTIVE_BINDS.get(player.getUUID());
        if (boundTargetUUID == null) return;
        float incomingDamage = event.getAmount();
        if (incomingDamage <= 0.0F) return;

        float sharedDamage = incomingDamage * 0.30F;
        ServerLevel serverLevel = (ServerLevel) player.level();
        Entity boundEntity = serverLevel.getEntity(boundTargetUUID);
        if (boundEntity instanceof LivingEntity livingBound && livingBound.isAlive()) {
            processingSoulBind = true;
            try {
                livingBound.hurt(serverLevel.damageSources().indirectMagic(player, player), sharedDamage);
            } finally {
                processingSoulBind = false;
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || event.getLevel().getGameTime() % 20 != 0) return;

        var level = event.getLevel();
        for (Player player : level.players()) {
            if (!PowerHelper.get(player).listActive(PreventBrewingInsertPower.class).isEmpty()) {
                BlockPos playerPos = player.blockPosition();
                int radius = 6;

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos checkPos = playerPos.offset(x, y, z);
                            if (level.getBlockEntity(checkPos) instanceof BrewingStandBlockEntity brewer) {
                                ItemStack ingredient = brewer.getItem(3); // Достаем верхний слот

                                if (ingredient.is(Items.DIAMOND)) {

                                    AABB standArea = new AABB(checkPos).inflate(4.0);
                                    List<Player> helpers = level.getEntitiesOfClass(Player.class, standArea);

                                    boolean realAlchemistPresent = helpers.stream().anyMatch(p ->
                                            PowerHelper.get(p).listActive(PreventBrewingInsertPower.class).isEmpty()
                                    );
                                    if (!realAlchemistPresent) {
                                        ItemStack diamondCopy = ingredient.copy();
                                        brewer.setItem(3, ItemStack.EMPTY);
                                        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                                                level,
                                                checkPos.getX() + 0.5,
                                                checkPos.getY() + 1.2,
                                                checkPos.getZ() + 0.5,
                                                diamondCopy
                                        );
                                        level.addFreshEntity(itemEntity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        Ingredient diamond = Ingredient.of(Items.DIAMOND);
        registerStrictSet(builder, Potions.STRONG_STRENGTH, ModPotions.STRENGTH_3.getDelegate(), diamond);
        registerStrictSet(builder, Potions.STRONG_REGENERATION, ModPotions.REGENERATION_3.getDelegate(), diamond);
        registerStrictSet(builder, Potions.STRONG_LEAPING, ModPotions.LEAPING_3.getDelegate(), diamond);
        registerStrictSet(builder, Potions.STRONG_SWIFTNESS, ModPotions.SWIFTNESS_3.getDelegate(), diamond);
    }

    private static void registerStrictSet(PotionBrewing.Builder builder, Holder<Potion> inputType, Holder<Potion> outputType, Ingredient ingredient) {
        addStrictRecipe(builder, Items.POTION, inputType, Items.POTION, outputType, ingredient);
        addStrictRecipe(builder, Items.SPLASH_POTION, inputType, Items.SPLASH_POTION, outputType, ingredient);
        addStrictRecipe(builder, Items.LINGERING_POTION, inputType, Items.LINGERING_POTION, outputType, ingredient);
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity brewer) {
            for (int i = 0; i < 3; i++) {
                ItemStack potionStack = brewer.getItem(i);
                var contents = potionStack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                if (contents != null && contents.potion().isPresent()) {
                    net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion = contents.potion().get();
                    if (potion.is(ModPotions.STRENGTH_3.getKey()) || potion.is(ModPotions.REGENERATION_3.getKey()) || potion.is(ModPotions.LEAPING_3.getKey()) || potion.is(ModPotions.SWIFTNESS_3.getKey())) {
                        brewer.clearContent();
                        event.getPlayer().level().explode(null, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 1.0f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                        return;
                    }
                }
            }
        }
    }


    private static void addStrictRecipe(PotionBrewing.Builder builder, Item inputItem, Holder<Potion> inputType, Item outputItem, Holder<Potion> outputType, Ingredient ingredient) {
        ItemStack inputStack = PotionContents.createItemStack(inputItem, inputType);
        ItemStack outputStack = PotionContents.createItemStack(outputItem, outputType);
        Ingredient strictInput = DataComponentIngredient.of(true, inputStack);

        builder.addRecipe(new BrewingRecipe(strictInput, ingredient, outputStack));
    }
    public Origins(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(Origins::onLivingFall);
        NeoForge.EVENT_BUS.addListener(Origins::onFoodEat);
        NeoForge.EVENT_BUS.addListener(Origins::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ExplosiveArrowHandler::onGetProjectile);
        NeoForge.EVENT_BUS.addListener(ExplosiveArrowHandler::onProjectileImpact);
        NeoForge.EVENT_BUS.addListener(Origins::onVillagerInteract);
        NeoForge.EVENT_BUS.addListener(ModifyPotionDurationPower::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(Origins::onVillagerDeath);
        NeoForge.EVENT_BUS.addListener(Origins::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(Origins::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(Origins::onChangeTarget);
        NeoForge.EVENT_BUS.addListener(Origins::onRenderLiving);
        NeoForge.EVENT_BUS.addListener(Origins::registerBrewingRecipes);
        NeoForge.EVENT_BUS.addListener(Origins::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(Origins::onJrecTakeDamage);
        ModPotions.register(bus);
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

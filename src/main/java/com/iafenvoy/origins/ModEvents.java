package com.iafenvoy.origins;
import com.iafenvoy.origins.content.BlueprintItem;
import com.iafenvoy.origins.content.ExhaustionEffect;
import com.iafenvoy.origins.content.ModAttachments;
import com.iafenvoy.origins.content.ModEffects;
import com.iafenvoy.origins.data.power.builtin.regular.HolyHorseSpawnPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;

import static com.iafenvoy.origins.content.BloodEffect.BLOOD;
import static com.iafenvoy.origins.data.power.builtin.regular.HolyHorseSpawnPower.timer;

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
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Horse horse && !horse.level().isClientSide() && horse.getTags().contains("paladin_steed")) {
            net.minecraft.world.level.Level level = horse.level();
            if (horse.getPersistentData().contains("despawn_timer")) {
                if (timer <= 0) {
                    horse.ejectPassengers();
                    horse.discard();
                    return;
                } else {
                    timer = timer - 1;
                }
            }
            horse.fallDistance = 0.0F;
            net.minecraft.core.BlockPos horsePos = horse.blockPosition();
            net.minecraft.core.BlockPos underPos = horsePos.below();
            boolean isWaterUnder = level.getFluidState(underPos).is(net.minecraft.tags.FluidTags.WATER);
            boolean isWaterInside = level.getFluidState(horsePos).is(net.minecraft.tags.FluidTags.WATER);
            if (isWaterUnder && level.getBlockState(underPos).is(net.minecraft.world.level.block.Blocks.WATER)) {
                level.setBlockAndUpdate(underPos, net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState());
            }
            if (isWaterInside && level.getBlockState(horsePos).is(net.minecraft.world.level.block.Blocks.WATER)) {
                level.setBlockAndUpdate(horsePos, net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState());
                horse.absMoveTo(horse.getX(), horse.getY() + 0.1D, horse.getZ(), horse.getYRot(), horse.getXRot());
            }
            if (level instanceof ServerLevel serverLevel) {
                boolean isMoving = false;
                if (horse.getControllingPassenger() instanceof LivingEntity driver) {
                    isMoving = Math.abs(driver.xxa) > 0.01F || Math.abs(driver.zza) > 0.01F;
                } else {
                    isMoving = horse.getDeltaMovement().horizontalDistanceSqr() > 0.001D;
                }
                if (isMoving) {
                    double offsetX = (horse.getRandom().nextDouble() - 0.5D) * 0.5D;
                    double offsetZ = (horse.getRandom().nextDouble() - 0.5D) * 0.5D;
                    net.minecraft.world.level.block.state.BlockState currentUnderState = level.getBlockState(underPos);
                    if (currentUnderState.is(net.minecraft.world.level.block.Blocks.FROSTED_ICE)) {
                        serverLevel.sendParticles(
                                ParticleTypes.WAX_ON,
                                horse.getX() + offsetX,
                                horse.getY() + 0.1D,
                                horse.getZ() + offsetZ,
                                2, 0.05D, 0.0D, 0.05D, 0.01D
                        );
                    }
                }
            }
        }
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

            int currentBloodLvl = player.getData(ModAttachments.BLOODY.get());
            if (currentBloodLvl > 0) {
                int currentTimer = player.getData(ModAttachments.BLOOD_TIMER.get());
                currentTimer++;
                if (currentTimer >= 10000) {
                    int newBloodLvl = currentBloodLvl - 1;
                    player.setData(ModAttachments.BLOODY.get(), newBloodLvl);
                    player.setData(ModAttachments.BLOOD_TIMER.get(), 0);
                } else {
                    player.setData(ModAttachments.BLOOD_TIMER.get(), currentTimer);
                }
            } else {
                if (player.getData(ModAttachments.BLOOD_TIMER.get()) > 0) {
                    player.setData(ModAttachments.BLOOD_TIMER.get(), 0);
                }
            }
            AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (player.getData(ModAttachments.BLOODY.get()) == 0) {
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(20);
                }
            }
            if (player.getData(ModAttachments.BLOODY.get()) >= 5) {
                if (Math.random()>=0.9999) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0, false, false));
                }
            }
            if (player.getData(ModAttachments.BLOODY.get()) >= 10 && player.getData(ModAttachments.BLOODY.get()) <20) {
                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 40, 0, false, false));
            }
            else if (player.getData(ModAttachments.BLOODY.get()) >= 20 && player.getData(ModAttachments.BLOODY.get()) <30) {
                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 40, 1, false, false));
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(20);
                }
            }
            else if (player.getData(ModAttachments.BLOODY.get()) >= 30 && player.getData(ModAttachments.BLOODY.get()) <40) {
                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 40, 2, false, false));
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(16);
                }
            }
            else if (player.getData(ModAttachments.BLOODY.get()) >= 40 && player.getData(ModAttachments.BLOODY.get()) <50) {
                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 40, 4, false, false));
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(10);
                }
            }
            else if (player.getData(ModAttachments.BLOODY.get()) >= 50 && player.getData(ModAttachments.BLOODY.get()) <60) {
                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 40, 5, false, false));
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(4);
                }
            }
            else if (player.getData(ModAttachments.BLOODY.get()) >= 60) {
                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 40, 2, false, false));
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(1);
                    DamageSource customSource = player.level().damageSources().source(ExhaustionEffect.EXHAUSTION);
                    player.setData(ModAttachments.BLOODY.get(), 0);
                    player.hurt(customSource, 20000);
                }
            }
        }
    }
}

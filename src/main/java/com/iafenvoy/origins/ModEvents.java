package com.iafenvoy.origins;
import com.iafenvoy.origins.content.*;
import com.iafenvoy.origins.data.power.builtin.regular.HolyHorseSpawnPower;
import com.iafenvoy.origins.rituals.FlexibleRitual;
import com.iafenvoy.origins.rituals.ModRituals;
import com.iafenvoy.origins.rituals.RitualContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Objects;
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

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Проверяем, будет ли этот урон фатальным (больше или равен текущему здоровью)
        if (event.getOriginalDamage() >= player.getHealth()) {

            // Ищем тотем в руках
            InteractionHand totemHand = null;
            if (player.getMainHandItem().is(ModItems.PHANTOM_BLOOD_TOTEM.get())) {
                totemHand = InteractionHand.MAIN_HAND;
            } else if (player.getOffhandItem().is(ModItems.PHANTOM_BLOOD_TOTEM.get())) {
                totemHand = InteractionHand.OFF_HAND;
            }

            // Если тотем нашли — спасаем игрока ДО того, как он технически умрет
            if (totemHand != null) {
                event.setNewDamage(0); // ОТМЕНЯЕМ сам входящий УРОН!

                ItemStack totemStack = player.getItemInHand(totemHand);

                // Ванильная анимация вспышки тотема на экране
                if (player.level().isClientSide) {
                    Minecraft.getInstance().gameRenderer.displayItemActivation(totemStack);
                }

                // Тратим тотем
                totemStack.shrink(1);

                // Очищаем все эффекты (отравления, иссушения и т.д.)
                player.removeAllEffects();

                // Выставляем 20 ХП (полное здоровье) на время полета, чтобы его не убило случайным тиком
                player.setHealth(20.0F);

                // Накладываем Кровавый туман на 5 секунд (100 тиков)
                player.addEffect(new MobEffectInstance(ModEffects.BLOOD_MIST, 100, 0, false, false));

                // Звуки
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BAT_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        var effectInstance = player.getEffect(ModEffects.LOW_GRAVITY);
        if (effectInstance == null) {
            return;
        }
        int amplifier = effectInstance.getAmplifier();

        // Проверяем наличие твоего эффекта (замени AntiGravityEffect.get() на свой регистратор)
        if (player.hasEffect(ModEffects.LOW_GRAVITY) || amplifier == 1) {
            Vec3 motion = player.getDeltaMovement();
            double ySpeed = 0;
            player.setPose(Pose.SWIMMING);
            Vec3 currentMotion = player.getDeltaMovement();

            if (Minecraft.getInstance().options.keyUp.isDown()) {
                Vec3 lookVec = player.getLookAngle();

                // 1. Уменьшаем ускорение, чтобы разгон был постепенным
                double acceleration = 0.04;
                Vec3 accelVec = lookVec.scale(acceleration);

                // 2. Считаем потенциально новую скорость
                double nextX = currentMotion.x * 0.85 + accelVec.x;
                double nextY = currentMotion.y * 0.85 + accelVec.y;
                double nextZ = currentMotion.z * 0.85 + accelVec.z;

                // 3. Ограничиваем максимальную скорость (например, не быстрее бега ~0.35)
                double maxSpeed = 0.35;
                Vec3 nextMotion = new Vec3(nextX, nextY, nextZ);
                if (nextMotion.length() > maxSpeed) {
                    nextMotion = nextMotion.normalize().scale(maxSpeed);
                }

                player.setDeltaMovement(nextMotion);
            } else {
                // Дрейф при отпущенных кнопках (0.8 - довольно быстрое торможение, можно 0.95 для скольжения)
                player.setDeltaMovement(currentMotion.x * 0.8, currentMotion.y * 0.8, currentMotion.z * 0.8);
            }
        }
    }

    @SubscribeEvent
    public static void onXpDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player != null) {
            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                int count = inv.findCurios(ModItems.CLOVER.get()).size();
                if (count > 0) {
                    // +50% опыта за каждый талисман
                    int bonusXp = (int) (event.getDroppedExperience() * (0.5f * count));
                    event.setDroppedExperience(event.getDroppedExperience() + bonusXp);
                }
            });
        }
    }


    @SubscribeEvent
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            var effectInstance = player.getEffect(ModEffects.LOW_GRAVITY);
            if (effectInstance == null) {
                return;
            }
            int amplifier = effectInstance.getAmplifier();

            if (player != null && player.hasEffect(ModEffects.LOW_GRAVITY) || amplifier == 1) {
                HitResult hit = mc.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    if (player.getEyePosition().distanceTo(blockHit.getLocation()) <= 3.0) {
                        Vec3 lookVec = player.getLookAngle();
                        Vec3 pushVec = new Vec3(-lookVec.x, -lookVec.y, -lookVec.z).normalize().scale(0.8);
                        player.setDeltaMovement(player.getDeltaMovement().add(pushVec));
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public static void onCandleInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        if (state.getBlock() instanceof CandleBlock && state.getValue(CandleBlock.LIT)) {

            if (!level.isClientSide) {
                AABB scanArea = new AABB(pos).inflate(5.0D, 2.0D, 5.0D);
                List<ItemEntity> itemsInCircle = level.getEntitiesOfClass(ItemEntity.class, scanArea);
                RitualContext context = new RitualContext(level, pos, player, itemsInCircle);
                for (FlexibleRitual ritual : ModRituals.REGISTRY) {
                    boolean geometryMatch = true;
                    int totalRuneValue = 0;
                    for (BlockPos offset : ritual.circleOffsets()) {
                        BlockPos checkPos = pos.offset(offset.getX(), offset.getY(), offset.getZ());
                        BlockState blockState = level.getBlockState(checkPos);

                        if (!blockState.is(ModBlocks.CHALK_SIGN.get())) {
                            geometryMatch = false;
                            break;
                        } else {
                            totalRuneValue += blockState.getValue(ChalkSignBlock.RUNE_TYPE);
                        }
                    }
                    if (geometryMatch && ritual.condition().test(context, totalRuneValue)) {
                        ritual.action().accept(context, totalRuneValue);
                        level.setBlock(pos, state.setValue(CandleBlock.LIT, false), 3);
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 45, 0.2, 0.2, 0.2, 0.05);
                        }
                        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide) {
            if (entity instanceof Witch || entity instanceof Enemy) {
                BlockPos entityPos = entity.blockPosition();
                if (entity.level().getBlockState(entityPos).is(ModBlocks.WITCH_SALT_WIRE.get())) {
                    Vec3 lookVec = entity.getLookAngle();
                    Vec3 pushVec = new Vec3(-lookVec.x, 0.2D, -lookVec.z).normalize().scale(0.6D);

                    entity.setDeltaMovement(pushVec);
                    entity.hurtMarked = true;
                    if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.INSTANT_EFFECT,
                                entity.getX(), entity.getY() + 1.0D, entity.getZ(), 10, 0.2, 0.5, 0.2, 0.0D);
                    }
                }
            }
        }
    }
//    @SubscribeEvent
//    public static void onAnchorUse(PlayerInteractEvent.RightClickItem event) {
//        Level level = event.getLevel();
//        ItemStack stack = event.getItemStack();
//        Player player = event.getEntity();
//
//        // Проверяем, что в руке компас, он заряжен и игрок нажал Shift
//        if (stack.is(Items.COMPASS) && stack.has(DataComponents.LODESTONE_TRACKER) && player.isCrouching()) {
//
//            // Проигрываем анимацию руки на той стороне, где сработал вызов
//            if (level.isClientSide) {
//                player.swing(event.getHand(), true);
//            }
//
//            // Уведомляем пайплайн NeoForge, что взаимодействие успешно.
//            // НЕ вызываем event.setCanceled(true), чтобы пакет улетел на сервер!
//            event.setCancellationResult(InteractionResult.SUCCESS);
//
//            // СТРОГО СЕРВЕРНАЯ ЛОГИКА ТЕЛЕПОРТАЦИИ
//            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
//                LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
//
//                if (tracker != null && tracker.target().isPresent()) {
//                    GlobalPos globalPos = tracker.target().get();
//                    BlockPos targetPos = globalPos.pos();
//                    ResourceKey<Level> targetDim = globalPos.dimension();
//
//                    // Проверяем кастомный флаг CrossDim через CUSTOM_DATA (если круг стоял на магме)
//                    boolean canCrossDim = false;
//                    if (stack.has(DataComponents.CUSTOM_DATA)) {
//                        canCrossDim = stack.get(DataComponents.CUSTOM_DATA).getUnsafe().getBoolean("CrossDim");
//                    }
//
//                    // Если измерения не совпадают и межпространственный режим выключен — блокируем портал
//                    if (!level.dimension().equals(targetDim) && !canCrossDim) {
//                        serverPlayer.displayClientMessage(Component.literal("§cЯкорь находится в другом измерении! Резонанс утерян."), true);
//                        serverPlayer.playNotifySound(SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 0.5F);
//                        return;
//                    }
//
//                    // Получаем целевой мир на сервере
//                    ServerLevel targetLevel = serverPlayer.server.getLevel(targetDim);
//                    if (targetLevel != null) {
//                        // Эффекты ДО телепортации на старом месте
//                        ((ServerLevel) level).sendParticles(ParticleTypes.PORTAL, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(), 40, 0.2, 0.5, 0.2, 0.2);
//                        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
//
//                        // Безопасная телепортация игрока
//                        serverPlayer.teleportTo(
//                                targetLevel,
//                                targetPos.getX() + 0.5D,
//                                targetPos.getY() + 0.1D,
//                                targetPos.getZ() + 0.5D,
//                                serverPlayer.getYRot(),
//                                serverPlayer.getXRot()
//                        );
//
//                        // Эффекты ПОСЛЕ телепортации на новом месте
//                        targetLevel.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5, targetPos.getY() + 1.1, targetPos.getZ() + 0.5, 40, 0.2, 0.5, 0.2, 0.2);
//                        targetLevel.playSound(null, targetPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
//
//                        // Забираем компас (если игрок не в креативе)
//                        if (!serverPlayer.isCreative()) {
//                            stack.shrink(1);
//                        }
//
//                        serverPlayer.displayClientMessage(Component.literal("§dПространственный якорь перенёс вас!"), true);
//                    }
//                }
//            }
//        }
//    }
}

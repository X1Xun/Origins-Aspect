package com.iafenvoy.origins.rituals;

import com.iafenvoy.origins.TaskScheduler;
import com.iafenvoy.origins.content.ModBlocks;
import com.iafenvoy.origins.content.ModEffects;
import com.iafenvoy.origins.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.iafenvoy.origins.content.ExhaustionEffect.EXHAUSTION;

public class ModRituals {
    public static final List<FlexibleRitual> REGISTRY = new ArrayList<>();

    public static void register() {

        BlockPos[] mediumCircle = {
                new BlockPos(-1, 0, -2), new BlockPos(0, 0, -2), new BlockPos(1, 0, -2),
                new BlockPos(-1, 0, 2),  new BlockPos(0, 0, 2),  new BlockPos(1, 0, 2),
                new BlockPos(-2, 0, -1), new BlockPos(-2, 0, 0), new BlockPos(-2, 0, 1),
                new BlockPos(2, 0, -1),  new BlockPos(2, 0, 0),  new BlockPos(2, 0, 1)
        };
        BlockPos[] smallTriangle = {
                new BlockPos(0, 0, -1),
                new BlockPos(-1, 0, 1),
                new BlockPos(1, 0, 1)
        };

        BlockPos[] magicCross = {
                new BlockPos(0, 0, -1),
                new BlockPos(0, 0, 1),
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0)
        };
        BlockPos[] netherForm = {
                new BlockPos(0, 0, -1),
                new BlockPos(0, 0, 1),
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0),
                // круг
                new BlockPos(-1, 0, -2), new BlockPos(0, 0, -2), new BlockPos(1, 0, -2),
                new BlockPos(-1, 0, 2),  new BlockPos(0, 0, 2),  new BlockPos(1, 0, 2),
                new BlockPos(-2, 0, -1), new BlockPos(-2, 0, 0), new BlockPos(-2, 0, 1),
                new BlockPos(2, 0, -1),  new BlockPos(2, 0, 0),  new BlockPos(2, 0, 1),
                new BlockPos(2, 0, 2), new BlockPos(2, 0, -2), new BlockPos(-2, 0, 2), new BlockPos(-2, 0, -2),
                new BlockPos(3, 0, 0), new BlockPos(0, 0, 3), new BlockPos(-3, 0, 0), new BlockPos(0, 0, -3),
        };
//        REGISTRY.add(new FlexibleRitual(
//                mediumCircle,
//                (ctx, sum) -> ctx.player().hasEffect(MobEffects.WITHER), // Условие: например, если игрок иссушен/истощен
//                (ctx, sum) -> {
//                    ctx.player().addEffect(new MobEffectInstance(MobEffects.LEVITATION, 400, 0));
//                    ctx.player().displayClientMessage(Component.literal("§dДревний круг поглотил твое истощение!"), true);
//                }
//        ));


//        REGISTRY.add(new FlexibleRitual(
//                smallTriangle,
//                (ctx, sum) -> sum == 6 && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.IRON_INGOT)),
//                (ctx, sum) -> {
//                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.IRON_INGOT)).findFirst().ifPresent(i -> {
//                        i.getItem().shrink(1);
//                        if (i.getItem().isEmpty()) i.discard();
//                    });
//                    ctx.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
//                            ctx.level(), ctx.pos().getX() + 0.5, ctx.pos().getY() + 0.4, ctx.pos().getZ() + 0.5, new net.minecraft.world.item.ItemStack(Items.GOLD_INGOT)
//                    ));
//                }
//        ));

        //РИТУАЛ ТРАНСМУТАЦИИ: сахар в соль (питерскую)
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(net.minecraft.world.item.Items.SUGAR)),
                (ctx, sum) -> {
                    net.minecraft.world.level.Level level = ctx.level();
                    net.minecraft.core.BlockPos pos = ctx.pos();
                    net.minecraft.world.entity.player.Player player = ctx.player();
                    if (sum == 6) {
                        level.explode(
                                null,
                                null,
                                null,
                                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                3.0F,
                                false,
                                net.minecraft.world.level.Level.ExplosionInteraction.TNT
                        );
                        ctx.itemsOnGround().stream().filter(i -> i.getItem().is(net.minecraft.world.item.Items.SUGAR)).findFirst().ifPresent(i -> {
                            i.getItem().shrink(1);
                            if (i.getItem().isEmpty()) i.discard();
                        });
                        return;
                    }
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(net.minecraft.world.item.Items.SUGAR)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    int saltCount = 2;
                    net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.literal("§dМагия ритуала успешно сработала!");
                    int moonPhase = level.dimensionType().moonPhase(level.dayTime());

                    if (moonPhase == 0) {
                        saltCount = 3;
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.3, 1.0, 0.3, 0.02);
                        }
                    } else if (moonPhase == 4) {
                        saltCount = 1;
                    }
                    for (int i = 0; i < saltCount; i++) {
                        level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                                level,
                                pos.getX() + 0.5,
                                pos.getY() + 0.4,
                                pos.getZ() + 0.5,
                                new net.minecraft.world.item.ItemStack(ModItems.WITCH_SALT.get())
                        ));
                    }

                    if (player != null) {
                        player.displayClientMessage(message, true);
                    }
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(ModItems.WITCH_SALT.get())),
                (ctx, sum) -> {
                    net.minecraft.world.level.Level level = ctx.level();
                    net.minecraft.core.BlockPos pos = ctx.pos();
                    net.minecraft.world.entity.player.Player player = ctx.player();
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(ModItems.WITCH_SALT.get())).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });

                    for (net.minecraft.core.BlockPos offset : magicCross) {
                        net.minecraft.core.BlockPos chalkPos = pos.offset(offset);
                        if (level.getBlockState(chalkPos).is(ModBlocks.CHALK_SIGN.get())) {
                            level.destroyBlock(chalkPos, false);
                        }
                    }
                    if (sum == 6) {
                        level.explode(
                                null,
                                null,
                                null,
                                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                3.0F,
                                false,
                                net.minecraft.world.level.Level.ExplosionInteraction.TNT
                        );
                        return;
                    }
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.CLOUD,
                                pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                                30, 0.5, 0.1, 0.5, 0.05
                        );
                    }
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.REDSTONE)) && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.WHEAT_SEEDS)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();

                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.REDSTONE)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.WHEAT_SEEDS)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });

                    if (sum == 6) {
                        level.explode(
                                null,
                                null,
                                null,
                                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                3.0F,
                                false,
                                net.minecraft.world.level.Level.ExplosionInteraction.TNT
                        );
                        return;
                    }

                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack((ItemLike) ModItems.VERDANT)));

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.01);
                    }
                    level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.2F);
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                netherForm,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.OBSIDIAN)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.01);
                        Vec3 center = Vec3.atCenterOf(pos);
                        AABB area = new AABB(pos).inflate(40);
                        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, area);

                        for (Player player : players) {
                            if (player.distanceToSqr(center) <= 40.0 * 40.0) {
                                player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 100, 3, false, false));
                            }
                        }
                        TaskScheduler.schedule(60, () -> {
                            if (serverLevel.getServer().isRunning()) {
                                serverLevel.sendParticles(ParticleTypes.ASH,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        100, 3.0, 2.0, 3.0, 0.1
                                );
                                net.minecraft.resources.ResourceLocation backportSoundId =
                                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "entity.creaking.death");

                                net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> customSound =
                                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
                                                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(backportSoundId)
                                        );
                                serverLevel.playSound(
                                        null,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        customSound.value(),
                                        net.minecraft.sounds.SoundSource.BLOCKS,
                                        20.0F,
                                        0.7F
                                );
                            }
                        });
                        TaskScheduler.schedule(100, () -> {
                            if (serverLevel.getServer().isRunning()) {
                                serverLevel.sendParticles(ParticleTypes.ASH,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        100, 3.0, 2.0, 3.0, 0.1
                                );
                                net.minecraft.resources.ResourceLocation backportSoundId =
                                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "entity.creaking.death");

                                net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> customSound =
                                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
                                                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(backportSoundId)
                                        );
                                serverLevel.playSound(
                                        null,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        customSound.value(),
                                        net.minecraft.sounds.SoundSource.BLOCKS,
                                        20.0F,
                                        0.7F
                                );
                            }
                        });
                        TaskScheduler.schedule(120, () -> {
                            if (serverLevel.getServer().isRunning()) {
                                serverLevel.sendParticles(ParticleTypes.ASH,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        100, 3.0, 2.0, 3.0, 0.1
                                );
                                net.minecraft.resources.ResourceLocation backportSoundId =
                                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "entity.creaking.death");

                                net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> customSound =
                                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
                                                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(backportSoundId)
                                        );
                                serverLevel.playSound(
                                        null,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        customSound.value(),
                                        net.minecraft.sounds.SoundSource.BLOCKS,
                                        20.0F,
                                        0.7F
                                );
                            }
                        });
                    }

                }
        ));

        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(ModItems.WITCH_SALT.get())),
                (ctx, sum) -> {
                    net.minecraft.world.level.Level level = ctx.level();
                    net.minecraft.core.BlockPos pos = ctx.pos();
                    net.minecraft.world.entity.player.Player player = ctx.player();
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(ModItems.WITCH_SALT.get())).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    for (net.minecraft.core.BlockPos offset : mediumCircle) {
                        net.minecraft.core.BlockPos chalkPos = pos.offset(offset);
                        if (level.getBlockState(chalkPos).is(ModBlocks.CHALK_SIGN.get())) {
                            level.destroyBlock(chalkPos, false);
                        }
                    }
                    if (sum == 6) {
                        level.explode(
                                null,
                                null,
                                null,
                                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                3.0F,
                                false,
                                net.minecraft.world.level.Level.ExplosionInteraction.TNT
                        );
                        return;
                    }
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.CLOUD,
                                pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                                30, 0.5, 0.1, 0.5, 0.05
                        );
                    }
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.WET_SPONGE) || i.getItem().is(Items.POTION)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();
                    boolean isSponge = ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.WET_SPONGE));
                    Item targetItem = isSponge ? Items.WET_SPONGE : Items.POTION;
                    Item rewardItem = isSponge ? Items.SPONGE : Items.GLASS_BOTTLE;
                    BlockState floorState = level.getBlockState(pos.below());
                    int moonPhase = level.dimensionType().moonPhase(level.dayTime());
                    int processCount = 1;
                    if (floorState.is(Blocks.NETHERRACK)) {
                        processCount = 2;
                    }
                    boolean fails = (moonPhase == 0 && !floorState.is(Blocks.NETHERRACK));
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(targetItem)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });

                    if (fails) {
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(targetItem)));
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.1);
                        }
                        if (player != null) player.displayClientMessage(Component.literal("§cПолная луна не дает влаге испариться!"), true);
                    } else {
                        for (int j = 0; j < processCount; j++) {
                            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(rewardItem)));
                        }
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.2, 0.2, 0.2, 0.05);
                        }
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.2F);
                    }
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(net.minecraft.tags.ItemTags.LOGS)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();
                    ItemEntity logEntity = ctx.itemsOnGround().stream().filter(i -> i.getItem().is(net.minecraft.tags.ItemTags.LOGS)).findFirst().orElse(null);
                    if (logEntity == null) return;

                    ItemStack logStack = logEntity.getItem();
                    Item reward = Items.CHARCOAL;
                    if (logStack.is(Items.CRIMSON_STEM) || logStack.is(Items.WARPED_STEM)) {
                        reward = Items.BLAZE_POWDER;
                    }

                    logStack.shrink(1);
                    if (logStack.isEmpty()) logEntity.discard();
                    BlockState floor = level.getBlockState(pos.below());
                    if (floor.is(Blocks.SAND) && level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 15, 0.1, 0.1, 0.1, 0.02);
                    }
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(reward)));
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.8F, 0.8F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.CLAY_BALL)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();
                    ItemEntity clayEntity = ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.CLAY_BALL)).findFirst().orElse(null);
                    if (clayEntity == null) return;

                    ItemStack logStack = clayEntity.getItem();
                    Item reward = Items.BRICK;
                    logStack.shrink(1);
                    if (logStack.isEmpty()) clayEntity.discard();
                    BlockState floor = level.getBlockState(pos.below());
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 15, 0.1, 0.1, 0.1, 0.02);
                    }
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(reward)));
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.8F, 0.8F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.DIRT)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();
                    ItemEntity logEntity = ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.DIRT)).findFirst().orElse(null);
                    if (logEntity == null) return;

                    ItemStack logStack = logEntity.getItem();
                    Item reward = Items.MUD;

                    logStack.shrink(1);
                    if (logStack.isEmpty()) logEntity.discard();
                    BlockState floor = level.getBlockState(pos.below());
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.FALLING_WATER, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 400, 2, 1, 2, 0.01);
                    }
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(reward)));
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.8F, 0.8F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.GRAVEL)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();

                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.GRAVEL)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    BlockState floor = level.getBlockState(pos.below());
                    int count = (floor.is(Blocks.OBSIDIAN) || floor.is(Blocks.DEEPSLATE)) ? 2 : 1;

                    for (int j = 0; j < count; j++) {
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.FLINT)));
                    }

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.1);
                    }
                    level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5F, 1.8F);
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.COBWEB)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    int moonPhase = level.dimensionType().moonPhase(level.dayTime());

                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.COBWEB)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    int stringCount = (moonPhase == 4) ? 1 : 3;

                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.STRING, stringCount)));

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.01);
                    }
                    level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.2F);
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.WHEAT_SEEDS))
                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.BONE_MEAL)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.WHEAT_SEEDS)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.BONE_MEAL)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });

                    BlockState floor = level.getBlockState(pos.below());
                    if (floor.is(Blocks.GRASS_BLOCK)) {
                        for (BlockPos localOffset : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, -1, 2))) {
                            if (level.getBlockState(localOffset).is(Blocks.GRASS_BLOCK) && level.random.nextFloat() < 0.4F) {
                                BlockPos plantPos = localOffset.above();
                                if (level.isEmptyBlock(plantPos) && !plantPos.equals(pos)) {
                                    Block flower = level.random.nextBoolean() ? Blocks.DANDELION : Blocks.POPPY;
                                    level.setBlock(plantPos, flower.defaultBlockState(), 3);
                                }
                            }
                        }
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Blocks.MOSS_BLOCK)));
                    } else {
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Blocks.MOSS_BLOCK)));
                    }

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.4, 0.2, 0.4, 0.02);
                    }
                    level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.STICK))
                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.FLINT)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.STICK)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.FLINT)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    BlockState floor = level.getBlockState(pos.below());
                    int count = (floor.is(Blocks.BASALT) || floor.is(Blocks.DEEPSLATE)) ? 2 : 1;
                    for (int j = 0; j < count; j++) {
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.BLAZE_ROD)));
                    }

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.2, 0.2, 0.2, 0.05);
                    }
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.CHARCOAL))
                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.REDSTONE)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();

                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.CHARCOAL)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.REDSTONE)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    int moonPhase = level.getMoonPhase();
                    int count = (moonPhase == 0) ? 3 : 1;

                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.BLAZE_POWDER, count)));

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.02);
                    }
                    level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.6F, 1.4F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.TORCH))
                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.REDSTONE)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();

                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.TORCH)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.REDSTONE)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    int moonPhase = level.getMoonPhase();
                    int count = (moonPhase == 0) ? 3 : 1;

                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.GLOWSTONE_DUST, count)));

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.02);
                    }
                    level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.6F, 1.4F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.WHEAT_SEEDS))
                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.SPIDER_EYE)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();

                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.WHEAT_SEEDS)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.SPIDER_EYE)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });

                    BlockState floor = level.getBlockState(pos.below());
                    int moonPhase = level.getMoonPhase();
                    if (moonPhase == 4 && !floor.is(Blocks.CLAY)) {
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.ROTTEN_FLESH)));
                    } else {
                        int count = floor.is(Blocks.CLAY) ? 2 : 1;
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, new ItemStack(Items.NETHER_WART, count)));

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.2, 0.3, 0.03);
                        }
                        level.playSound(null, pos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 0.6F);
                    }
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                magicCross,
                (ctx, sum) -> ctx.itemsOnGround().isEmpty(),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();
                    if (player == null) return;
                    if (level.isRaining() || level.isThundering()) {
                        player.hurt(level.damageSources().magic(), 4.0F);

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.2, 0.2, 0.2, 0.01);
                        }
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.8F);
                        player.displayClientMessage(Component.literal("§cДождь подавил внутреннее пламя круга! Магия ударила по вам."), true);
                        return;
                    }
                    if (sum <= 4) {
                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 2));
                        player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTION, 1200, 1));
                        DamageSource customSource = player.level().damageSources().source(EXHAUSTION);
                        player.hurt(customSource,8);
                        player.igniteForSeconds(5);
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                        }
                        level.playSound(null, pos, SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 0.8F, 0.5F);
                        return;
                    }
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().eat(20, 5.0F);
                    if (sum >= 12) {
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.4, 0.4, 0.4, 0.02);
                        }
                    }
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 35, 0.5, 0.5, 0.5, 0.02);
                    }
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 0.8F);
                }
        ));


        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.FERMENTED_SPIDER_EYE))
                        && !ctx.level().getEntitiesOfClass(Zombie.class, new AABB(ctx.pos()).inflate(2.5D, 1.0D, 2.5D)).isEmpty(),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.FERMENTED_SPIDER_EYE)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, new AABB(pos).inflate(2.5D, 1.0D, 2.5D));
                    if (!zombies.isEmpty() && level instanceof ServerLevel serverLevel) {
                        Zombie zombie = zombies.get(0);
                        BlockPos spawnPos = zombie.blockPosition();


                        if (sum >= 30) {
                            WitherSkeleton skeleton = EntityType.WITHER_SKELETON.create(serverLevel);
                            if (skeleton != null) {
                                skeleton.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                                skeleton.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
                                serverLevel.addFreshEntity(skeleton);
                            }
                            serverLevel.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.05);
                        } else {
                            Husk husk = EntityType.HUSK.create(serverLevel);
                            if (husk != null) {
                                husk.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                                serverLevel.addFreshEntity(husk);
                            }
                            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.02);
                        }
                        zombie.discard();
                        level.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 1.0F, 0.8F);
                    }
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.WHEAT)).mapToInt(i -> i.getItem().getCount()).sum() >= 5
                        && !ctx.level().getEntitiesOfClass(LivingEntity.class, new AABB(ctx.pos()).inflate(2.5D, 1.0D, 2.5D),
                        e -> e.getType() == EntityType.COW || e.getType() == EntityType.SHEEP).isEmpty(),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    int toRemove = 5;
                    for (ItemEntity entity : ctx.itemsOnGround()) {
                        if (entity.getItem().is(Items.WHEAT)) {
                            int count = entity.getItem().getCount();
                            if (count >= toRemove) {
                                entity.getItem().shrink(toRemove);
                                if (entity.getItem().isEmpty()) entity.discard();
                                break;
                            } else {
                                toRemove -= count;
                                entity.discard();
                            }
                        }
                    }
                    List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(2.5D, 1.0D, 2.5D),
                            e -> e.getType() == EntityType.COW || e.getType() == EntityType.SHEEP);

                    if (!targets.isEmpty()) {
                        LivingEntity animal = targets.get(0);
                        animal.discard();
                        BlockState floor = level.getBlockState(pos.below());
                        int radius = floor.is(Blocks.PODZOL) ? 24 : 16;
                        for (BlockPos targetPos : BlockPos.betweenClosed(pos.offset(-radius, -2, -radius), pos.offset(radius, 2, radius))) {
                            BlockState cropState = level.getBlockState(targetPos);
                            if (cropState.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bonemealable) {
                                if (bonemealable.isValidBonemealTarget(level, targetPos, cropState) && level instanceof ServerLevel serverLevel) {
                                    bonemealable.performBonemeal(serverLevel, level.random, targetPos, cropState);
                                }
                            }
                        }

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 1.0, 0.2, 1.0, 0.05);
                        }
                        level.playSound(null, pos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
        ));
        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.PHANTOM_MEMBRANE))
                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.FEATHER)),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();

                    // Поглощение
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.PHANTOM_MEMBRANE)).findFirst().ifPresent(i -> { i.getItem().shrink(1); if (i.getItem().isEmpty()) i.discard(); });
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.FEATHER)).findFirst().ifPresent(i -> { i.getItem().shrink(1); if (i.getItem().isEmpty()) i.discard(); });

                    AABB effectZone = new AABB(pos).inflate(12.0D, 6.0D, 12.0D);
                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectZone);

                    if (sum == 0) {
                        for (LivingEntity entity : entities) {
                            if (entity instanceof Player || entity.getType().getCategory().isFriendly()) {
                                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 4));
                                entity.setDeltaMovement(new Vec3(0, -1.5D, 0));
                            }
                        }
                        level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 0.5F);
                    } else {
                        // Базовый эффект: подкидывает монстров вверх
                        for (LivingEntity entity : entities) {
                            if (!entity.getType().getCategory().isFriendly() && !(entity instanceof Player)) {
                                entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 300, 1));
                            }
                        }
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.02);
                        }
                        level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 2.0F);
                    }
                }
        ));

//        REGISTRY.add(new FlexibleRitual(
//                mediumCircle,
//                // Условие: на полу лежит Плачущий обсидиан и обычный Компас
//                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.CRYING_OBSIDIAN))
//                        && ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.COMPASS)),
//                (ctx, sum) -> {
//                    Level level = ctx.level();
//                    BlockPos pos = ctx.pos();
//
//                    // 1. Поглощаем плачущий обсидиан
//                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.CRYING_OBSIDIAN)).findFirst().ifPresent(i -> {
//                        i.getItem().shrink(1);
//                        if (i.getItem().isEmpty()) i.discard();
//                    });
//
//                    // 2. Находим компас и привязываем его к свече через LodestoneTracker
//                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.COMPASS)).findFirst().ifPresent(itemEntity -> {
//                        ItemStack compassStack = itemEntity.getItem();
//
//                        // Проверяем, стоит ли круг на магме для включения межпространственного режима
//                        boolean isMagma = level.getBlockState(pos.below()).is(Blocks.MAGMA_BLOCK);
//
//                        // Создаем глобальную позицию (Измерение + Координаты)
//                        GlobalPos targetPos = GlobalPos.of(level.dimension(), pos.above());
//
//                        // В 1.21.1 LodestoneTracker принимает Optional<GlobalPos> и boolean (tracked)
//                        LodestoneTracker tracker = new LodestoneTracker(Optional.of(targetPos), !isMagma);
//
//                        // Прошиваем компонент в ItemStack
//                        compassStack.set(DataComponents.LODESTONE_TRACKER, tracker);
//
//                        // Стилизуем предмет
//                        compassStack.set(DataComponents.CUSTOM_NAME, Component.literal("§dЯкорь Перемещения"));
//
//                        if (level instanceof ServerLevel serverLevel) {
//                            serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.2, 0.2, 0.2, 0.1);
//                        }
//                        level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.2F);
//                    });
//                }
//        ));
        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.LIGHTNING_ROD))
                        && ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.IRON_INGOT)).mapToInt(i -> i.getItem().getCount()).sum() >= 3,
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    ctx.itemsOnGround().stream().filter(i -> i.getItem().is(Items.LIGHTNING_ROD)).findFirst().ifPresent(i -> {
                        i.getItem().shrink(1);
                        if (i.getItem().isEmpty()) i.discard();
                    });
                    int toRemove = 3;
                    for (ItemEntity entity : ctx.itemsOnGround()) {
                        if (entity.getItem().is(Items.IRON_INGOT)) {
                            int count = entity.getItem().getCount();
                            if (count >= toRemove) {
                                entity.getItem().shrink(toRemove);
                                if (entity.getItem().isEmpty()) entity.discard();
                                break;
                            } else {
                                toRemove -= count;
                                entity.discard();
                            }
                        }
                    }
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.setWeatherParameters(0, 12000, true, true);
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                        if (bolt != null) {
                            bolt.moveTo(Vec3.atBottomCenterOf(pos));
                            serverLevel.addFreshEntity(bolt);
                        }
                        List<Creeper> creepers = serverLevel.getEntitiesOfClass(Creeper.class, new AABB(pos).inflate(2.5D, 1.0D, 2.5D));
                        for (Creeper creeper : creepers) {
                            net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
                            creeper.saveWithoutId(nbt);
                            nbt.putBoolean("powered", true);
                            creeper.load(nbt);
                            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, creeper.getX(), creeper.getY() + 1.0, creeper.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
                        }
                    }
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().stream().anyMatch(i -> i.getItem().is(Items.WIND_CHARGE)),
                (ctx, sum) -> {
                    net.minecraft.world.level.Level level = ctx.level();
                    net.minecraft.core.BlockPos pos = ctx.pos();
                    net.minecraft.world.entity.player.Player player = ctx.player();

                    if (level instanceof ServerLevel serverLevel) {
                        LaunchpadManager.activate(serverLevel, pos);
                        serverLevel.sendParticles(
                                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                                40, 1.0, 0.5, 1.0, 0.05
                        );
                        if (sum >= 30) {
                            serverLevel.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
                        }
                    }
                    level.playSound(null, pos, SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 1.5F, 1.5F);
                }
        ));

        REGISTRY.add(new FlexibleRitual(
                mediumCircle,
                (ctx, sum) -> ctx.itemsOnGround().isEmpty(),
                (ctx, sum) -> {
                    Level level = ctx.level();
                    BlockPos pos = ctx.pos();
                    Player player = ctx.player();

                    if (player == null) return;
                    if (level instanceof ServerLevel serverLevel) {
                        long currentTime = serverLevel.getDayTime() % 24000;
                        boolean isNight = currentTime >= 13000 && currentTime < 23000;
                        long targetTime;
                        String timeMessage;

                        if (isNight) {
                            targetTime = ((serverLevel.getDayTime() / 24000) * 24000) + 24000;
                        } else {
                            targetTime = ((serverLevel.getDayTime() / 24000) * 24000) + 13000;
                        }
                        serverLevel.setDayTime(targetTime);
                        if (sum >= 24) {
                            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0));
                            serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 35, 0.4, 0.4, 0.4, 0.05);
                            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.2, 0.5, 0.2, 0.02);

                            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.0F);
                        } else {
                            player.hurt(level.damageSources().magic(), 4.0F);
                            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.02);
                            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.5F, 0.5F);
                        }
                    }
                }
        ));



    }
}

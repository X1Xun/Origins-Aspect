package com.iafenvoy.origins.rituals; // Укажи свой пакет

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent; // Подключаем эвент падения

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LaunchpadManager {

    private static final String FALL_IMMUNITY_TAG = "OriginsLaunchpadImmunity";

    public static class ActivePad {
        private final ResourceKey<Level> dimension;
        private final BlockPos pos;
        private int remainingTicks;

        public ActivePad(ResourceKey<Level> dimension, BlockPos pos, int remainingTicks) {
            this.dimension = dimension;
            this.pos = pos;
            this.remainingTicks = remainingTicks;
        }

        public ResourceKey<Level> dimension() { return dimension; }
        public BlockPos pos() { return pos; }
        public int remainingTicks() { return remainingTicks; }
        public void decrement() { this.remainingTicks--; }
    }

    private static final Map<BlockPos, ActivePad> ACTIVE_PADS = new ConcurrentHashMap<>();

    private static final int[][] EDGE_OFFSETS = {
            {-1, -2}, {0, -2}, {1, -2},
            {-1, 2}, {0, 2}, {1, 2},
            {-2, -1}, {-2, 0}, {-2, 1},
            {2, -1}, {2, 0}, {2, 1}
    };

    public static void activate(Level level, BlockPos pos) {
        ACTIVE_PADS.put(pos, new ActivePad(level.dimension(), pos.immutable(), 600));
    }

    // 1. КОНТРОЛЬ ТАЙМЕРА, ВИЗУАЛА И ПОДБРОСА (Оптимальный тикер сервера)
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_PADS.isEmpty()) return;

        var server = event.getServer();
        Iterator<Map.Entry<BlockPos, ActivePad>> iterator = ACTIVE_PADS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, ActivePad> entry = iterator.next();
            ActivePad pad = entry.getValue();
            ServerLevel serverLevel = server.getLevel(pad.dimension());

            if (serverLevel == null) {
                iterator.remove();
                continue;
            }

            BlockPos center = pad.pos();

            // Визуал границ 5х5 (каждые 5 тиков)
            if (pad.remainingTicks() % 5 == 0) {
                for (int[] offset : EDGE_OFFSETS) {
                    double px = center.getX() + offset[0] + 0.5D;
                    double py = center.getY() + 0.2D;
                    double pz = center.getZ() + offset[1] + 0.5D;

                    serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.05, 0.1, 0.05, 0.01);
                }
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.getX() + 0.5, center.getY() + 0.3, center.getZ() + 0.5, 1, 0.1, 0.5, 0.1, 0.02);
            }

            // Физика катапультирования
            AABB zone = new AABB(center).inflate(2.5D, 0.0D, 2.5D).expandTowards(0, 5.0D, 0);
            java.util.List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, zone);

            for (LivingEntity entity : entities) {
                entity.setDeltaMovement(new Vec3(entity.getDeltaMovement().x, 2.3D, entity.getDeltaMovement().z));
                entity.hurtMarked = true;

                // КРИТИЧЕСКИЙ ФИКС: Вешаем персистентную метку защиты от падения на сущность
                entity.getPersistentData().putBoolean(FALL_IMMUNITY_TAG, true);

                serverLevel.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
                entity.fallDistance = 0.0F;
            }

            if (pad.remainingTicks() <= 1) {
                iterator.remove();
            } else {
                pad.decrement();
            }
        }
    }

    // 2. КРИТИЧЕСКИЙ ФИКС: Перехват и полное гашение урона от падения в любой точке мира
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();

        // Если у сущности в NBT-данных есть наш маркер
        if (entity.getPersistentData().contains(FALL_IMMUNITY_TAG)) {
            // Обнуляем дистанцию падения и повреждения в самом событии NeoForge
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
            event.setCanceled(true); // Отменяем эвент падения

            // Стираем маркер, чтобы иммунитет был строго одноразовым (до первого приземления)
            entity.getPersistentData().remove(FALL_IMMUNITY_TAG);
        }
    }
}

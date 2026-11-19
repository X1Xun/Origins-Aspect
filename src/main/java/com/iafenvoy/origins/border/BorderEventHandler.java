package com.iafenvoy.origins.border;

import com.iafenvoy.origins.TaskScheduler;
import com.iafenvoy.origins.content.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class BorderEventHandler {
    public static final ResourceKey<DamageType> FOG = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("origins", "hurt_by_fog")
    );

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();

        // Работаем только на сервере (чтобы накладывать эффекты и урон) и только для живых
        if (player.level().isClientSide() || !player.isAlive()) return;
        if (player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        if (player.isCreative() || player.isSpectator()) return;

        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
        BorderSaveData data = BorderSaveData.get(serverLevel);

        double pX = player.getX();
        double pZ = player.getZ();

        double distanceX = 0;
        double distanceZ = 0;

        // Вычисляем, насколько далеко игрок ушел по оси X
        if (pX > data.maxX) distanceX = pX - data.maxX;
        else if (pX < data.minX) distanceX = data.minX - pX;

        // Вычисляем, насколько далеко игрок ушел по оси Z
        if (pZ > data.maxZ) distanceZ = pZ - data.maxZ;
        else if (pZ < data.minZ) distanceZ = data.minZ - pZ;

        // Берем максимальное углубление среди осей
        double totalOutsideDistance = Math.max(distanceX, distanceZ);

        // Если игрок ЗА границей (totalOutsideDistance > 0)
        if (totalOutsideDistance > 0) {

            // --- ДИНАМИЧЕСКИЕ ДЕБАФФЫ НА ОСНОВЕ РАССТОЯНИЯ ---

            // Уровень 1: Ушел дальше 5 блоков — даем Замедление
            if (totalOutsideDistance >= 10.0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        ModEffects.EXHAUSTION, 40, 2, false, false
                ));
            }
            if (totalOutsideDistance >= 35.0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.DARKNESS, 40, 2, false, false
                ));
            }
            if (totalOutsideDistance >= 90.0) {

            }
            if (totalOutsideDistance >= 110.0) {
                DamageSource customSource = player.level().damageSources().source(FOG);
                TaskScheduler.schedule(100, () -> {
                    ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY(), player.getZ(), 50, 1, 1, 1, 0.1);
                });
                TaskScheduler.schedule(180, () -> {
                    player.hurt(customSource, 10000.0F);
                });

            }
        }
    }
}

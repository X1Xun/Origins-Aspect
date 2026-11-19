package com.iafenvoy.origins;

import com.iafenvoy.jupiter.render.screen.ConfigSelectScreen;
import com.iafenvoy.origins.border.BorderConfig;
import com.iafenvoy.origins.border.BorderSaveData;
import com.iafenvoy.origins.config.OriginsConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Origins.MOD_ID, dist = Dist.CLIENT)
public final class OriginsClient {
    private static net.minecraft.client.resources.sounds.SimpleSoundInstance currentMusicInstance = null;

    public static final long TARGET_NIGHT_TIME = 18000; // Целевое время (полночь)
    public static final double START_DISTANCE = 30.0;   // Дистанция начала смены времени
    public static final double END_DISTANCE = 60.0;// Дистанция полной темноты

    private static double clientSideSmoothTime = -1;
    public OriginsClient(ModContainer container) {
        Proxies.TICK_COUNT = () -> Minecraft.getInstance().clientTickCount;
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> ConfigSelectScreen.builder(Component.empty(), parent).common(OriginsConfig.INSTANCE).build());
        NeoForge.EVENT_BUS.addListener(OriginsClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(OriginsClient::onComputeCameraAngles);
        container.getEventBus().addListener(OriginsClient::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(LeyLineRenderer::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(OriginsClient::onComputeFogColor);
        NeoForge.EVENT_BUS.addListener(OriginsClient::onRenderFog);
        NeoForge.EVENT_BUS.addListener(OriginsClient::onClientTickPre);
    }

    private static float smoothAlpha = 0.0F;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        float targetAlpha = 0.0F;
        if (player.hasEffect(com.iafenvoy.origins.content.ModEffects.EXHAUSTION)) {
            net.minecraft.world.effect.MobEffectInstance effect = player.getEffect(com.iafenvoy.origins.content.ModEffects.EXHAUSTION);
            if (effect != null) {
                int amplifier = effect.getAmplifier();
                targetAlpha = Math.min(0.3F + (amplifier * 1.5F), 0.85F);
            }
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        float fadeSpeed = 0.05F * partialTick;

        if (smoothAlpha < targetAlpha) {
            smoothAlpha = Math.min(smoothAlpha + fadeSpeed, targetAlpha);
        } else if (smoothAlpha > targetAlpha) {
            smoothAlpha = Math.max(smoothAlpha - fadeSpeed, targetAlpha);
        }

        if (smoothAlpha <= 0.0F) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ZERO,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderColor(smoothAlpha, smoothAlpha, smoothAlpha, smoothAlpha);
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        net.minecraft.resources.ResourceLocation vignetteTex = net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
        event.getGuiGraphics().blit(vignetteTex, 0, 0, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }



    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.hasEffect(com.iafenvoy.origins.content.ModEffects.EXHAUSTION)) {
            net.minecraft.world.effect.MobEffectInstance effect = mc.player.getEffect(com.iafenvoy.origins.content.ModEffects.EXHAUSTION);
            if (effect == null) return;
            int amplifier = effect.getAmplifier();
            if (amplifier >= 1) {
                float ticks = mc.level != null ? (float)(mc.level.getGameTime() + event.getPartialTick()) : 0.0F;
                int levelStep = amplifier;
                float shakeIntensity = smoothAlpha * (levelStep * (levelStep * 1.0F));
                float deltaPitch = (float) Math.sin(ticks * 0.40F) * (float) Math.cos(ticks * 0.25F) * shakeIntensity;
                float deltaYaw = (float) Math.cos(ticks * -0.35F) * (float) Math.sin(ticks * 0.20F) * shakeIntensity;
                float deltaRoll = (float) Math.sin(ticks * 0.15F) * shakeIntensity * 0.8F;
                event.setPitch(event.getPitch() + deltaPitch);
                event.setYaw(event.getYaw() + deltaYaw);
                event.setRoll(event.getRoll() + deltaRoll);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.level().dimension() != Level.OVERWORLD) return;
        if (player.isCreative() || player.isSpectator()) return;

        double pX = player.getX();
        double pZ = player.getZ();
        double distanceX = 0, distanceZ = 0;

        if (pX > BorderSaveData.clientMaxX) distanceX = pX - BorderSaveData.clientMaxX;
        else if (pX < BorderSaveData.clientMinX) distanceX = BorderSaveData.clientMinX - pX;

        if (pZ > BorderSaveData.clientMaxZ) distanceZ = pZ - BorderSaveData.clientMaxZ;
        else if (pZ < BorderSaveData.clientMinZ) distanceZ = BorderSaveData.clientMinZ - pZ;

        double totalOutsideDistance = Math.max(distanceX, distanceZ);

        // Туман начинает сгущаться строго с 15 блоков
        if (totalOutsideDistance >= 15.0) {
            // Плавное сгущение в диапазоне от 15 до 60 блоков (длина пути 45 блоков)
            float lambda = (float) Math.min(1.0, (totalOutsideDistance - 15.0) / 70.0);

            // Финал на 60+ блоках: видимость падает до расстояния вытянутой руки (2 блока)
            float targetFogStart = 1.0F;  // Туман вплотную прижимается к глазам
            float targetFogEnd = 8.0F;    // Максимальная видимость — 2 метра

            // Рассчитываем текущие рамки тумана на основе плавности
            float currentFogStart = event.getNearPlaneDistance() + (targetFogStart - event.getNearPlaneDistance()) * lambda;
            float currentFogEnd = event.getFarPlaneDistance() + (targetFogEnd - event.getFarPlaneDistance()) * lambda;

            event.setNearPlaneDistance(currentFogStart);
            event.setFarPlaneDistance(currentFogEnd);

            // Сферический туман обволакивает игрока со всех сторон куполом
            event.setFogShape(FogShape.SPHERE);
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.level().dimension() != Level.OVERWORLD) return;

        double pX = player.getX();
        double pZ = player.getZ();
        double distanceX = 0, distanceZ = 0;

        if (pX > BorderSaveData.clientMaxX) distanceX = pX - BorderSaveData.clientMaxX;
        else if (pX < BorderSaveData.clientMinX) distanceX = BorderSaveData.clientMinX - pX;
        if (pZ > BorderSaveData.clientMaxZ) distanceZ = pZ - BorderSaveData.clientMaxZ;
        else if (pZ < BorderSaveData.clientMinZ) distanceZ = BorderSaveData.clientMinZ - pZ;

        double totalOutsideDistance = Math.max(distanceX, distanceZ);

        if (totalOutsideDistance >= 15.0) {
            float lambda = (float) Math.min(1.0, (totalOutsideDistance - 15.0) / 45.0);

            // Чистый, ослепительно белый хоррор-туман (RGB: 0.9F, 0.9F, 0.95F)
            // Такой цвет полностью перекроет небосвод и сокроет солнце
            float targetR = 0.0F;
            float targetG = 0.0F;
            float targetB = 0.1F;

            event.setRed(event.getRed() + (targetR - event.getRed()) * lambda);
            event.setGreen(event.getGreen() + (targetG - event.getGreen()) * lambda);
            event.setBlue(event.getBlue() + (targetB - event.getBlue()) * lambda);
        }
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        if (player == null || level == null) {
            clientSideSmoothTime = -1;
            return;
        }

        // Координаты барьера из BorderSaveData
        int minX = BorderSaveData.clientMinX;
        int minZ = BorderSaveData.clientMinZ;
        int maxX = BorderSaveData.clientMaxX;
        int maxZ = BorderSaveData.clientMaxZ;

        double px = player.getX();
        double pz = player.getZ();

        // Расчет дистанции выхода за барьер
        double distanceX = 0;
        if (px < minX) distanceX = minX - px;
        else if (px > maxX) distanceX = px - maxX;

        double distanceZ = 0;
        if (pz < minZ) distanceZ = minZ - pz;
        else if (pz > maxZ) distanceZ = pz - maxZ;

        double distanceOutside = Math.max(distanceX, distanceZ);

        if (distanceOutside <= START_DISTANCE) {
            // Игрок внутри — плавно возвращаем небо к серверному времени, если до этого были за барьером
            if (clientSideSmoothTime != -1) {
                long serverTime = level.getLevelData().getDayTime();

                // Плавный возврат к серверному времени (интерполяция шагом в 5 тиков)
                if (Math.abs(clientSideSmoothTime - serverTime) > 5) {
                    clientSideSmoothTime += (serverTime - clientSideSmoothTime) * 0.2;
                    if (level.getLevelData() instanceof ClientLevel.ClientLevelData clientData) {
                        clientData.setDayTime((long) clientSideSmoothTime);
                    }
                } else {
                    clientSideSmoothTime = -1; // Полностью синхронизировано с сервером
                }
            }
        } else {
            // Игрок за барьером
            long realServerTime = level.getLevelData().getDayTime();
            if (clientSideSmoothTime == -1) {
                clientSideSmoothTime = realServerTime;
            }

            // Считаем коэффициент перехода alpha
            double currentRange = distanceOutside - START_DISTANCE;
            double maxRange = END_DISTANCE - START_DISTANCE;
            double alpha = Math.min(1.0, Math.max(0.0, currentRange / maxRange));

            // Вычисляем целевое время суток текущего дня
            long currentDay = realServerTime / 24000;
            long targetAbsoluteTime = (currentDay * 24000) + TARGET_NIGHT_TIME;

            // Вместо резкой перезаписи делаем линейный сдвиг (LERP)
            double desiredTime = (1.0 - alpha) * realServerTime + alpha * targetAbsoluteTime;

            // Чтобы луна за барьером не застывала намертво, а продолжала едва заметно
            // и плавно двигаться вместе с ходом времени, добавляем небольшой инкремент:
            clientSideSmoothTime = desiredTime;

            // Записываем финальное отрендеренное время в клиентскую дату
            if (level.getLevelData() instanceof ClientLevel.ClientLevelData clientData) {
                clientData.setDayTime((long) clientSideSmoothTime);
            }
        }
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath("origins", "astral_vignette"),
                OriginsClient::render
        );
    }
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            ResourceLocation trueSightId = ResourceLocation.fromNamespaceAndPath("origins", "true_sight");

            BuiltInRegistries.MOB_EFFECT.getHolder(trueSightId).ifPresent(effectHolder -> {
                if (mc.player.hasEffect(effectHolder)) {
                    int width = guiGraphics.guiWidth();
                    int height = guiGraphics.guiHeight();

                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.enableBlend();

                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);

                    guiGraphics.fill(0, 0, width, height, 0x0D00FF88);

                    RenderSystem.disableBlend();
                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();
                }
            });
        }
    }
}

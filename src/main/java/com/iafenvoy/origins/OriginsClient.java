package com.iafenvoy.origins;

import com.iafenvoy.jupiter.render.screen.ConfigSelectScreen;
import com.iafenvoy.origins.config.OriginsConfig;
import com.iafenvoy.origins.content.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Origins.MOD_ID, dist = Dist.CLIENT)
public final class OriginsClient {
    public OriginsClient(ModContainer container) {
        Proxies.TICK_COUNT = () -> Minecraft.getInstance().clientTickCount;
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> ConfigSelectScreen.builder(Component.empty(), parent).common(OriginsConfig.INSTANCE).build());
        NeoForge.EVENT_BUS.addListener(OriginsClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(OriginsClient::onComputeCameraAngles);
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
}

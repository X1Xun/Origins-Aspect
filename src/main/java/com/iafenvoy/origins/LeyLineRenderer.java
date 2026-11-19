package com.iafenvoy.origins;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class LeyLineRenderer {

    private static final ResourceLocation TRUE_SIGHT_ID = ResourceLocation.fromNamespaceAndPath("origins", "true_sight");
    private static final int CYLINDER_SEGMENTS = 32;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        BuiltInRegistries.MOB_EFFECT.getHolder(TRUE_SIGHT_ID).ifPresent(effectHolder -> {
            if (!mc.player.hasEffect(effectHolder)) return;
            BlockPos pPos = mc.player.blockPosition();
            int playerSectorX = pPos.getX() >> 10;
            int playerSectorZ = pPos.getZ() >> 10;

            PoseStack poseStack = event.getPoseStack();
            Vec3 cameraPos = event.getCamera().getPosition();
            float ticks = mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaTicks();
            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    int currentSectorX = playerSectorX + sx;
                    int currentSectorZ = playerSectorZ + sz;
                    long sectorSeed = (long) currentSectorX * 341873128712L + (long) currentSectorZ * 132897987541L + 123456789L;
                    java.util.Random rand = new java.util.Random(sectorSeed);
                    if (rand.nextFloat() > 0.25F) continue;
                    double nodeX = (currentSectorX << 10) + rand.nextInt(1024) + 0.5D;
                    double nodeZ = (currentSectorZ << 10) + rand.nextInt(1024) + 0.5D;
                    double nodeY = mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int)nodeX, (int)nodeZ) - 5.0D;

                    BlockPos nodePos = new BlockPos((int)nodeX, (int)nodeY + 5, (int)nodeZ);
                    if (!LeyLineRegistry.isNode(mc.level, nodePos)) {
                        continue;
                    }
                    float renderX = (float) (nodeX - cameraPos.x);
                    float renderY = (float) (nodeY - cameraPos.y);
                    float renderZ = (float) (nodeZ - cameraPos.z);
                    if (renderX * renderX + renderZ * renderZ > 32400.0F) continue;
                    if (renderX * renderX + renderZ * renderZ > 65536.0F) continue;
                    poseStack.pushPose();
                    poseStack.translate(renderX, renderY, renderZ);
                    poseStack.mulPose(Axis.YP.rotationDegrees(ticks * 0.5F));
                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.disableCull();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                    float height = 140.0F;
                    float radius = 2.5F;
                    float alpha = 0.15F + (float) Math.sin(ticks * 0.08F) * 0.03F;

                    for (int i = 0; i < CYLINDER_SEGMENTS; i++) {
                        double angle = (i * 2.0 * Math.PI) / CYLINDER_SEGMENTS;
                        double nextAngle = ((i + 1) * 2.0 * Math.PI) / CYLINDER_SEGMENTS;

                        float x1 = (float) (Math.cos(angle) * radius);
                        float z1 = (float) (Math.sin(angle) * radius);
                        float x2 = (float) (Math.cos(nextAngle) * radius);
                        float z2 = (float) (Math.sin(nextAngle) * radius);

                        buffer.addVertex(poseStack.last().pose(), x1, 0, z1).setColor(0.0F, 0.9F, 0.4F, alpha);
                        buffer.addVertex(poseStack.last().pose(), x2, 0, z2).setColor(0.0F, 0.9F, 0.4F, alpha);
                        buffer.addVertex(poseStack.last().pose(), x2, height, z2).setColor(0.0F, 0.5F, 1.0F, 0.0F);
                        buffer.addVertex(poseStack.last().pose(), x1, height, z1).setColor(0.0F, 0.5F, 1.0F, 0.0F);
                    }

                    BufferUploader.drawWithShader(buffer.buildOrThrow());

                    RenderSystem.depthMask(true);
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                    poseStack.popPose();
                }
            }
        });
    }
}

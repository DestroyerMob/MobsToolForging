package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.world.CrucibleContents;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.joml.Vector3f;

public class FoundryForgeRenderer implements BlockEntityRenderer<FoundryForgeBlockEntity> {
    private static final ResourceLocation LAVA_TEXTURE = ResourceLocation.withDefaultNamespace("block/lava_still");
    private static final float CRUCIBLE_SCALE = 0.42F;
    private static final float CRUCIBLE_BASE_Y = 0.565F;
    private static final float LAVA_MIN_Y = 2.8F / 16.0F;
    private static final float LAVA_MAX_Y = 3.7F / 16.0F;
    private static final float LAVA_SURFACE_MIN = 3.0F / 16.0F;
    private static final float LAVA_SURFACE_MAX = 13.0F / 16.0F;
    private static final float GAUGE_MIN_Y = 4.25F / 16.0F;
    private static final float GAUGE_MAX_Y = 5.85F / 16.0F;
    private static final float GAUGE_INSET = 1.08F / 16.0F;
    private static final float[] GAUGE_SLOTS = {
            4.0F / 16.0F, 5.25F / 16.0F,
            6.5F / 16.0F, 7.75F / 16.0F,
            8.25F / 16.0F, 9.5F / 16.0F,
            10.75F / 16.0F, 12.0F / 16.0F
    };
    private static final float HALO_MIN = 4.25F / 16.0F;
    private static final float HALO_MAX = 11.75F / 16.0F;
    private static final float HALO_THICKNESS = 0.75F / 16.0F;
    private static final float HALO_Y = 15.9F / 16.0F;

    private final BlockRenderDispatcher blockRenderer;
    private final CrucibleContentsRenderer contentsRenderer = new CrucibleContentsRenderer();

    public FoundryForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderLavaSurface(forge, partialTick, poseStack, bufferSource, packedOverlay);
        renderLavaGauges(forge, partialTick, poseStack, bufferSource, packedOverlay);
        if (!forge.hasCrucible()) {
            return;
        }

        CrucibleContents contents = forge.crucibleContents();
        renderCrucible(poseStack, bufferSource, packedLight, packedOverlay);
        renderProcessHalo(forge, contents, partialTick, poseStack, bufferSource, packedOverlay);

        poseStack.pushPose();
        applyCrucibleTransform(poseStack);
        contentsRenderer.renderHeatGlow(contents, forge.isLit(), partialTick, poseStack, bufferSource, packedOverlay, forge.getLevel());
        contentsRenderer.renderContents(contents, forge.heatProgressFraction(), partialTick, poseStack, bufferSource, packedLight, packedOverlay, forge.getLevel());
        poseStack.popPose();
    }

    private void renderLavaSurface(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        if (!forge.isLit()) {
            return;
        }
        float fill = forge.lavaVisualFraction();
        float y = LAVA_MIN_Y + (LAVA_MAX_Y - LAVA_MIN_Y) * fill;
        float time = forge.getLevel() == null ? partialTick : forge.getLevel().getGameTime() + partialTick;
        float flicker = ((float) Math.sin(time * 0.27F) + 1.0F) * 0.5F;
        int color = withAlpha(0xFFFFB13B, 0.84F + flicker * 0.10F);
        renderHorizontalSurface(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)),
                blockSprite(LAVA_TEXTURE),
                color,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                LAVA_SURFACE_MIN,
                y,
                LAVA_SURFACE_MIN,
                LAVA_SURFACE_MAX,
                LAVA_SURFACE_MAX
        );
    }

    private void renderLavaGauges(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        if (!forge.isLit()) {
            return;
        }
        float fill = forge.lavaVisualFraction();
        if (fill <= 0.01F) {
            return;
        }
        float maxY = GAUGE_MIN_Y + (GAUGE_MAX_Y - GAUGE_MIN_Y) * fill;
        float time = forge.getLevel() == null ? partialTick : forge.getLevel().getGameTime() + partialTick;
        float flicker = ((float) Math.sin(time * 0.31F) + 1.0F) * 0.5F;
        int color = withAlpha(0xFFFF7A18, 0.62F + flicker * 0.14F);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        TextureAtlasSprite sprite = blockSprite(LAVA_TEXTURE);
        for (int slot = 0; slot < GAUGE_SLOTS.length; slot += 2) {
            float min = GAUGE_SLOTS[slot];
            float max = GAUGE_SLOTS[slot + 1];
            renderNorthPanel(poseStack, consumer, sprite, color, LightTexture.FULL_BRIGHT, packedOverlay, min, GAUGE_MIN_Y, GAUGE_INSET, max, maxY);
            renderSouthPanel(poseStack, consumer, sprite, color, LightTexture.FULL_BRIGHT, packedOverlay, min, GAUGE_MIN_Y, 1.0F - GAUGE_INSET, max, maxY);
            renderWestPanel(poseStack, consumer, sprite, color, LightTexture.FULL_BRIGHT, packedOverlay, GAUGE_INSET, GAUGE_MIN_Y, min, maxY, max);
            renderEastPanel(poseStack, consumer, sprite, color, LightTexture.FULL_BRIGHT, packedOverlay, 1.0F - GAUGE_INSET, GAUGE_MIN_Y, min, maxY, max);
        }
    }

    private void renderProcessHalo(FoundryForgeBlockEntity forge, CrucibleContents contents, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        float time = forge.getLevel() == null ? partialTick : forge.getLevel().getGameTime() + partialTick;
        float pulse = ((float) Math.sin(time * 0.18F) + 1.0F) * 0.5F;
        float progress = processProgress(forge, contents);
        int baseColor = withAlpha(statusColor(forge, contents), 0.16F);
        int fillColor = withAlpha(statusColor(forge, contents), contents.hasMoltenMaterial() ? 0.72F + pulse * 0.20F : 0.50F);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        TextureAtlasSprite sprite = blockSprite(LAVA_TEXTURE);
        renderRing(poseStack, consumer, sprite, baseColor, LightTexture.FULL_BRIGHT, packedOverlay, 1.0F);
        renderRing(poseStack, consumer, sprite, fillColor, LightTexture.FULL_BRIGHT, packedOverlay, progress);
    }

    private float processProgress(FoundryForgeBlockEntity forge, CrucibleContents contents) {
        if (contents.hasMoltenMaterial()) {
            return 1.0F;
        }
        if (contents.hasItem()) {
            return Math.max(contents.heat(), forge.heatProgressFraction());
        }
        return forge.isLit() ? 0.18F : 0.0F;
    }

    private int statusColor(FoundryForgeBlockEntity forge, CrucibleContents contents) {
        if (contents.hasMoltenMaterial()) {
            return 0xFFFFF1A8;
        }
        if (contents.hasItem()) {
            return forge.isLit() ? 0xFFFF8A24 : 0xFF8D98A6;
        }
        return forge.isLit() ? 0xFFFF5F18 : 0xFF5C6672;
    }

    private void renderCrucible(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyCrucibleTransform(poseStack);
        blockRenderer.renderSingleBlock(ModBlocks.CRUCIBLE.get().defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void applyCrucibleTransform(PoseStack poseStack) {
        poseStack.translate(0.5F, CRUCIBLE_BASE_Y, 0.5F);
        poseStack.scale(CRUCIBLE_SCALE, CRUCIBLE_SCALE, CRUCIBLE_SCALE);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
    }

    private TextureAtlasSprite blockSprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }

    private static int withAlpha(int color, float alpha) {
        return Math.round(clamp(alpha) * 255.0F) << 24 | color & 0x00FFFFFF;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static void renderHorizontalSurface(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float minX, float y, float minZ, float maxX, float maxZ) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.UP.step(), color, light, overlay, minX, y, minZ, u0, v1, minX, y, maxZ, u0, v0, maxX, y, maxZ, u1, v0, maxX, y, minZ, u1, v1);
    }

    private static void renderRing(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float progress) {
        float remaining = clamp(progress) * 4.0F;
        if (remaining <= 0.0F) {
            return;
        }
        float segment = Math.min(1.0F, remaining);
        renderHorizontalSurface(poseStack, consumer, sprite, color, light, overlay, HALO_MIN, HALO_Y, HALO_MIN, HALO_MIN + (HALO_MAX - HALO_MIN) * segment, HALO_MIN + HALO_THICKNESS);
        remaining -= segment;
        if (remaining <= 0.0F) {
            return;
        }
        segment = Math.min(1.0F, remaining);
        renderHorizontalSurface(poseStack, consumer, sprite, color, light, overlay, HALO_MAX - HALO_THICKNESS, HALO_Y, HALO_MIN, HALO_MAX, HALO_MIN + (HALO_MAX - HALO_MIN) * segment);
        remaining -= segment;
        if (remaining <= 0.0F) {
            return;
        }
        segment = Math.min(1.0F, remaining);
        renderHorizontalSurface(poseStack, consumer, sprite, color, light, overlay, HALO_MAX - (HALO_MAX - HALO_MIN) * segment, HALO_Y, HALO_MAX - HALO_THICKNESS, HALO_MAX, HALO_MAX);
        remaining -= segment;
        if (remaining <= 0.0F) {
            return;
        }
        segment = Math.min(1.0F, remaining);
        renderHorizontalSurface(poseStack, consumer, sprite, color, light, overlay, HALO_MIN, HALO_Y, HALO_MAX - (HALO_MAX - HALO_MIN) * segment, HALO_MIN + HALO_THICKNESS, HALO_MAX);
    }

    private static void renderNorthPanel(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float minX, float minY, float z, float maxX, float maxY) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.NORTH.step(), color, light, overlay, minX, minY, z, u0, v1, maxX, minY, z, u1, v1, maxX, maxY, z, u1, v0, minX, maxY, z, u0, v0);
    }

    private static void renderSouthPanel(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float minX, float minY, float z, float maxX, float maxY) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.SOUTH.step(), color, light, overlay, maxX, minY, z, u0, v1, minX, minY, z, u1, v1, minX, maxY, z, u1, v0, maxX, maxY, z, u0, v0);
    }

    private static void renderWestPanel(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float x, float minY, float minZ, float maxY, float maxZ) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.WEST.step(), color, light, overlay, x, minY, maxZ, u0, v1, x, minY, minZ, u1, v1, x, maxY, minZ, u1, v0, x, maxY, maxZ, u0, v0);
    }

    private static void renderEastPanel(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float x, float minY, float minZ, float maxY, float maxZ) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.EAST.step(), color, light, overlay, x, minY, minZ, u0, v1, x, minY, maxZ, u1, v1, x, maxY, maxZ, u1, v0, x, maxY, minZ, u0, v0);
    }

    private static void quad(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int color, int light, int overlay, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3, float v3) {
        vertex(poseStack, consumer, color, light, overlay, normal, x0, y0, z0, u0, v0);
        vertex(poseStack, consumer, color, light, overlay, normal, x1, y1, z1, u1, v1);
        vertex(poseStack, consumer, color, light, overlay, normal, x2, y2, z2, u2, v2);
        vertex(poseStack, consumer, color, light, overlay, normal, x3, y3, z3, u3, v3);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, int color, int light, int overlay, Vector3f normal, float x, float y, float z, float u, float v) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, color >>> 24 & 0xFF)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }
}

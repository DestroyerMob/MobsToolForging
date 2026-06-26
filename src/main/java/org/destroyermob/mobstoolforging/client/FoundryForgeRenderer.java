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
    private static final float LAVA_MIN_Y = 4.15F / 16.0F;
    private static final float LAVA_MAX_Y = 5.65F / 16.0F;

    private final BlockRenderDispatcher blockRenderer;
    private final CrucibleContentsRenderer contentsRenderer = new CrucibleContentsRenderer();

    public FoundryForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderLavaSurface(forge, partialTick, poseStack, bufferSource, packedOverlay);
        if (!forge.hasCrucible()) {
            return;
        }

        CrucibleContents contents = forge.crucibleContents();
        renderCrucible(poseStack, bufferSource, packedLight, packedOverlay);

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
                1.2F / 16.0F,
                y,
                1.2F / 16.0F,
                14.8F / 16.0F,
                14.8F / 16.0F
        );
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

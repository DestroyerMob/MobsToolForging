package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.joml.Vector3f;

public final class HeatingForgeVoxelRenderer {
    private static final Set<ResourceLocation> MISSING_TEXTURE_WARNINGS = new HashSet<>();

    private HeatingForgeVoxelRenderer() {
    }

    public static void render(
            HeatingForgeVoxelModel model,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            float heat
    ) {
        if (model.elements().isEmpty()) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        warnIfMissing(texture, sprite);
        boolean centerSample = texture.getPath().startsWith("item/");
        float clampedHeat = clamp(heat);
        int baseLight = clampedHeat > 0.04F ? LightTexture.FULL_BRIGHT : packedLight;
        renderModel(model, sprite, centerSample, poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), baseLight, packedOverlay, baseColor(clampedHeat));
        if (clampedHeat > 0.05F) {
            poseStack.pushPose();
            float edgeScale = 1.03F + clampedHeat * 0.035F;
            poseStack.scale(edgeScale, edgeScale, edgeScale);
            renderModel(model, sprite, centerSample, poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), LightTexture.FULL_BRIGHT, packedOverlay, edgeColor(clampedHeat));
            poseStack.popPose();
        }
    }

    private static void renderModel(HeatingForgeVoxelModel model, TextureAtlasSprite sprite, boolean centerSample, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        for (HeatingForgeVoxelModel.Element element : model.elements()) {
            float x1 = coordinate(element.from().x(), true);
            float y1 = element.from().y() / 16.0F;
            float z1 = coordinate(element.from().z(), true);
            float x2 = coordinate(element.to().x(), true);
            float y2 = element.to().y() / 16.0F;
            float z2 = coordinate(element.to().z(), true);
            for (var entry : element.faces().entrySet()) {
                renderFace(entry.getKey(), entry.getValue(), sprite, centerSample, poseStack, consumer, light, overlay, color, x1, y1, z1, x2, y2, z2);
            }
        }
    }

    private static void renderFace(
            Direction direction,
            HeatingForgeVoxelModel.Face face,
            TextureAtlasSprite sprite,
            boolean centerSample,
            PoseStack poseStack,
            VertexConsumer consumer,
            int light,
            int overlay,
            int color,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2
    ) {
        float[] uv = centerSample ? centerSampleUv() : face.uv();
        float u0 = sprite.getU(uv[0] / 16.0F);
        float v0 = sprite.getV(uv[1] / 16.0F);
        float u1 = sprite.getU(uv[2] / 16.0F);
        float v1 = sprite.getV(uv[3] / 16.0F);
        Vector3f normal = direction.step();
        switch (direction) {
            case NORTH -> quad(poseStack, consumer, color, light, overlay, normal, x1, y1, z1, u0, v1, x1, y2, z1, u0, v0, x2, y2, z1, u1, v0, x2, y1, z1, u1, v1);
            case SOUTH -> quad(poseStack, consumer, color, light, overlay, normal, x2, y1, z2, u0, v1, x2, y2, z2, u0, v0, x1, y2, z2, u1, v0, x1, y1, z2, u1, v1);
            case WEST -> quad(poseStack, consumer, color, light, overlay, normal, x1, y1, z2, u0, v1, x1, y2, z2, u0, v0, x1, y2, z1, u1, v0, x1, y1, z1, u1, v1);
            case EAST -> quad(poseStack, consumer, color, light, overlay, normal, x2, y1, z1, u0, v1, x2, y2, z1, u0, v0, x2, y2, z2, u1, v0, x2, y1, z2, u1, v1);
            case UP -> quad(poseStack, consumer, color, light, overlay, normal, x1, y2, z1, u0, v1, x1, y2, z2, u0, v0, x2, y2, z2, u1, v0, x2, y2, z1, u1, v1);
            case DOWN -> quad(poseStack, consumer, color, light, overlay, normal, x1, y1, z2, u0, v1, x1, y1, z1, u0, v0, x2, y1, z1, u1, v0, x2, y1, z2, u1, v1);
        }
    }

    private static void quad(
            PoseStack poseStack,
            VertexConsumer consumer,
            int color,
            int light,
            int overlay,
            Vector3f normal,
            float x0,
            float y0,
            float z0,
            float u0,
            float v0,
            float x1,
            float y1,
            float z1,
            float u1,
            float v1,
            float x2,
            float y2,
            float z2,
            float u2,
            float v2,
            float x3,
            float y3,
            float z3,
            float u3,
            float v3
    ) {
        vertex(poseStack, consumer, color, light, overlay, normal, x0, y0, z0, u0, v0);
        vertex(poseStack, consumer, color, light, overlay, normal, x1, y1, z1, u1, v1);
        vertex(poseStack, consumer, color, light, overlay, normal, x2, y2, z2, u2, v2);
        vertex(poseStack, consumer, color, light, overlay, normal, x3, y3, z3, u3, v3);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, int color, int light, int overlay, Vector3f normal, float x, float y, float z, float u, float v) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }

    private static float coordinate(float value, boolean centered) {
        return centered ? (value - 8.0F) / 16.0F : value / 16.0F;
    }

    private static float[] centerSampleUv() {
        return new float[] {5.0F, 5.0F, 11.0F, 11.0F};
    }

    private static void warnIfMissing(ResourceLocation requested, TextureAtlasSprite sprite) {
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) && MISSING_TEXTURE_WARNINGS.add(requested)) {
            MobsToolForging.LOGGER.warn("Missing heating forge insert texture sprite {}. Add it to assets/minecraft/atlases/blocks.json or use an already-atlased block texture.", requested);
        }
    }

    private static int baseColor(float heat) {
        if (heat <= 0.02F) {
            return 0xFFFFFFFF;
        }
        int red = 255;
        int green = Math.round(255.0F - (1.0F - heat) * 92.0F);
        int blue = Math.round(255.0F - (1.0F - heat * heat) * 204.0F);
        return 0xFF << 24 | red << 16 | green << 8 | blue;
    }

    private static int edgeColor(float heat) {
        int alpha = Math.round(72.0F + heat * 112.0F);
        int green = Math.round(70.0F + heat * 88.0F);
        int blue = Math.round(8.0F + heat * 12.0F);
        return alpha << 24 | 0xFF << 16 | green << 8 | blue;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

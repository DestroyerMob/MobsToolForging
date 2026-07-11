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
            HeatingForgeInsertVisual visual,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            float heat,
            HeatVisualProfile profile
    ) {
        if (model.elements().isEmpty()) {
            return;
        }
        ResourceLocation texture = visual.texture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        warnIfMissing(texture, sprite);
        float clampedHeat = HeatVisuals.clamp(heat);
        int heatedLight = HeatVisuals.heatedLight(packedLight, profile, clampedHeat);
        renderModel(model, sprite, centerSample(texture), poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), heatedLight, packedOverlay, HeatVisuals.surfaceTint(profile, clampedHeat));

        ResourceLocation hotTexture = visual.hotTexture();
        float hotBlend = hotTexture == null ? 0.0F : HeatVisuals.smoothstep(0.9F, 1.0F, clampedHeat) * 0.28F;
        if (hotTexture != null && hotBlend > 0.01F) {
            TextureAtlasSprite hotSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(hotTexture);
            warnIfMissing(hotTexture, hotSprite);
            renderModel(model, hotSprite, centerSample(hotTexture), poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), heatedLight, packedOverlay, HeatVisuals.withAlpha(0xFFFFFFFF, hotBlend));
        }

        float overlayAlpha = HeatVisuals.overlayAlpha(profile, clampedHeat);
        if (overlayAlpha > 0.01F) {
            ResourceLocation glowTexture = visual.glowTexture();
            ResourceLocation emissiveTexture = glowTexture == null ? texture : glowTexture;
            TextureAtlasSprite emissiveSprite = glowTexture == null
                    ? sprite
                    : Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(glowTexture);
            if (glowTexture != null) {
                warnIfMissing(glowTexture, emissiveSprite);
            }
            int maskColor = HeatVisuals.withAlpha(HeatVisuals.heatColor(profile, clampedHeat), overlayAlpha);
            renderModel(
                    model,
                    emissiveSprite,
                    centerSample(emissiveTexture),
                    poseStack,
                    bufferSource.getBuffer(HeatRenderTypes.heatMask()),
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    maskColor
            );

            renderThermalHalo(
                    model,
                    emissiveSprite,
                    centerSample(emissiveTexture),
                    poseStack,
                    bufferSource,
                    packedOverlay,
                    clampedHeat,
                    profile
            );
        }
    }

    private static void renderThermalHalo(
            HeatingForgeVoxelModel model,
            TextureAtlasSprite sprite,
            boolean centerSample,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedOverlay,
            float heat,
            HeatVisualProfile profile
    ) {
        float strength = HeatVisuals.smoothstep(Math.min(0.9F, profile.haloStart() + 0.22F), 1.0F, heat);
        if (strength <= 0.01F) {
            return;
        }

        float scale = profile.haloBaseScale() + 0.006F + strength * (profile.haloExtraScale() + 0.004F);
        float alpha = strength * Math.min(0.16F, profile.haloAlpha() * 1.55F);
        int haloColor = HeatVisuals.withAlpha(HeatVisuals.heatColor(profile, Math.max(0.62F, heat)), alpha);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -0.5F, 0.0F);
        renderModel(
                model,
                sprite,
                centerSample,
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS, false)),
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                haloColor
        );
        poseStack.popPose();
    }

    public static void renderShimmer(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float heat,
            HeatVisualProfile profile
    ) {
        float start = Math.min(0.86F, Math.max(0.66F, profile.emissionStart() + 0.48F));
        float strength = HeatVisuals.smoothstep(start, 1.0F, heat);
        if (strength <= 0.01F) {
            return;
        }

        int color = HeatVisuals.heatColor(profile, Math.max(0.72F, heat));
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        int alpha = Math.round(strength * 0.075F * 255.0F);
        VertexConsumer consumer = bufferSource.getBuffer(HeatRenderTypes.heatShimmer());
        shimmerQuad(poseStack, consumer, red, green, blue, alpha,
                -0.34F, 0.02F, 0.0F,
                0.34F, 0.02F, 0.0F,
                0.34F, 0.68F, 0.0F,
                -0.34F, 0.68F, 0.0F);
        shimmerQuad(poseStack, consumer, red, green, blue, alpha,
                0.0F, 0.02F, -0.34F,
                0.0F, 0.02F, 0.34F,
                0.0F, 0.68F, 0.34F,
                0.0F, 0.68F, -0.34F);
    }

    private static void shimmerQuad(
            PoseStack poseStack,
            VertexConsumer consumer,
            int red,
            int green,
            int blue,
            int alpha,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3
    ) {
        consumer.addVertex(poseStack.last().pose(), x0, y0, z0).setColor(red, green, blue, alpha);
        consumer.addVertex(poseStack.last().pose(), x1, y1, z1).setColor(red, green, blue, alpha);
        consumer.addVertex(poseStack.last().pose(), x2, y2, z2).setColor(red, green, blue, alpha);
        consumer.addVertex(poseStack.last().pose(), x3, y3, z3).setColor(red, green, blue, alpha);
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
        int alpha = color >>> 24 & 0xFF;
        int red = color >>> 16 & 0xFF;
        int green = color >>> 8 & 0xFF;
        int blue = color & 0xFF;
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(red, green, blue, alpha)
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

    private static boolean centerSample(ResourceLocation texture) {
        return texture.getPath().startsWith("item/");
    }

    private static void warnIfMissing(ResourceLocation requested, TextureAtlasSprite sprite) {
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) && MISSING_TEXTURE_WARNINGS.add(requested)) {
            MobsToolForging.LOGGER.warn("Missing heating forge insert texture sprite {}. Add it to assets/minecraft/atlases/blocks.json or use an already-atlased block texture.", requested);
        }
    }

}

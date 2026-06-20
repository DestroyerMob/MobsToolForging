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
    private static final float HEAT_GLOW_THRESHOLD = 0.05F;
    private static final float WHITE_HOT_THRESHOLD = 0.9F;
    private static final float WHITE_HOT_TEXTURE_START = 0.82F;
    private static final int[] HEAT_COLOR_STOPS = {
            0xFF3A0903,
            0xFF520D04,
            0xFF6D1205,
            0xFF861806,
            0xFFA11F07,
            0xFFB92908,
            0xFFD2350A,
            0xFFE4430E,
            0xFFF15316,
            0xFFFF6420,
            0xFFFF762B,
            0xFFFF8836,
            0xFFFF9A42,
            0xFFFFAD50,
            0xFFFFBE61,
            0xFFFFCE73,
            0xFFFFDC8A,
            0xFFFFE8A3,
            0xFFFFF1C0,
            0xFFFFF8DF,
            0xFFFFFFFF
    };
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
            float heat
    ) {
        if (model.elements().isEmpty()) {
            return;
        }
        ResourceLocation texture = visual.texture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        warnIfMissing(texture, sprite);
        float clampedHeat = clamp(heat);
        boolean whiteHot = clampedHeat >= WHITE_HOT_THRESHOLD;
        ResourceLocation hotTexture = visual.hotTexture();
        float hotBlend = hotTexture == null ? 0.0F : smoothstep(WHITE_HOT_TEXTURE_START, 1.0F, clampedHeat);
        float baseAlpha = 1.0F - hotBlend;
        int baseLight = whiteHot ? LightTexture.FULL_BRIGHT : packedLight;
        if (baseAlpha > 0.01F) {
            RenderType baseRenderType = baseAlpha >= 0.999F ? RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS) : RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
            int visibleBaseColor = baseAlpha >= 0.999F ? baseColor(clampedHeat) : withAlpha(baseColor(clampedHeat), baseAlpha);
            renderModel(model, sprite, centerSample(texture), poseStack, bufferSource.getBuffer(baseRenderType), baseLight, packedOverlay, visibleBaseColor);
        }
        if (clampedHeat >= HEAT_GLOW_THRESHOLD && baseAlpha > 0.01F) {
            poseStack.pushPose();
            float heatScale = 1.012F + clampedHeat * 0.024F;
            poseStack.scale(heatScale, heatScale, heatScale);
            renderModel(model, sprite, centerSample(texture), poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), LightTexture.FULL_BRIGHT, packedOverlay, multiplyAlpha(heatGlowColor(clampedHeat), baseAlpha));
            poseStack.popPose();
        }
        if (hotTexture != null && clampedHeat >= WHITE_HOT_TEXTURE_START) {
            TextureAtlasSprite hotSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(hotTexture);
            warnIfMissing(hotTexture, hotSprite);
            poseStack.pushPose();
            float hotScale = 1.002F + clampedHeat * 0.004F;
            poseStack.scale(hotScale, hotScale, hotScale);
            renderModel(model, hotSprite, centerSample(hotTexture), poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), LightTexture.FULL_BRIGHT, packedOverlay, withAlpha(0xFFFFFFFF, hotBlend));
            poseStack.popPose();
        }
        if (clampedHeat >= HEAT_GLOW_THRESHOLD) {
            ResourceLocation glowTexture = visual.glowTexture();
            ResourceLocation overlayTexture = glowTexture == null ? texture : glowTexture;
            TextureAtlasSprite overlaySprite = glowTexture == null ? sprite : Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(glowTexture);
            if (glowTexture != null) {
                warnIfMissing(glowTexture, overlaySprite);
            }
            poseStack.pushPose();
            float edgeScale = 1.035F + clampedHeat * 0.04F;
            poseStack.scale(edgeScale, edgeScale, edgeScale);
            int overlayColor = glowTexture == null ? heatGlowColor(clampedHeat) : whiteHotGlowColor(clampedHeat);
            renderModel(model, overlaySprite, centerSample(overlayTexture), poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), LightTexture.FULL_BRIGHT, packedOverlay, overlayColor);
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

    private static int baseColor(float heat) {
        if (heat <= 0.02F) {
            return 0xFFFFFFFF;
        }
        float amount = smoothstep(0.02F, 1.0F, heat);
        return lerpColor(0xFFFFFFFF, heatColor(heat), 0.30F + amount * 0.70F);
    }

    private static int heatGlowColor(float heat) {
        float amount = smoothstep(HEAT_GLOW_THRESHOLD, 1.0F, heat);
        int alpha = Math.round(28.0F + amount * 164.0F);
        return withAlpha(heatColor(heat), alpha / 255.0F);
    }

    private static int whiteHotGlowColor(float heat) {
        float amount = smoothstep(HEAT_GLOW_THRESHOLD, 1.0F, heat);
        int alpha = Math.round(amount * 210.0F);
        return withAlpha(heatColor(heat), alpha / 255.0F);
    }

    private static int heatColor(float heat) {
        float scaled = clamp(heat) * (HEAT_COLOR_STOPS.length - 1);
        int index = Math.min(HEAT_COLOR_STOPS.length - 2, (int) Math.floor(scaled));
        float amount = smoothstep(0.0F, 1.0F, scaled - index);
        return lerpColor(HEAT_COLOR_STOPS[index], HEAT_COLOR_STOPS[index + 1], amount);
    }

    private static int withAlpha(int color, float alpha) {
        return Math.round(clamp(alpha) * 255.0F) << 24 | color & 0x00FFFFFF;
    }

    private static int multiplyAlpha(int color, float multiplier) {
        float alpha = (color >>> 24 & 0xFF) / 255.0F;
        return withAlpha(color, alpha * multiplier);
    }

    private static float smoothstep(float from, float to, float value) {
        if (from >= to) {
            return clamp(value);
        }
        float t = clamp((value - from) / (to - from));
        return t * t * (3.0F - 2.0F * t);
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * clamp(amount);
    }

    private static int lerpColor(int from, int to, float amount) {
        float clamped = clamp(amount);
        int alpha = Math.round(lerp(from >>> 24 & 0xFF, to >>> 24 & 0xFF, clamped));
        int red = Math.round(lerp(from >>> 16 & 0xFF, to >>> 16 & 0xFF, clamped));
        int green = Math.round(lerp(from >>> 8 & 0xFF, to >>> 8 & 0xFF, clamped));
        int blue = Math.round(lerp(from & 0xFF, to & 0xFF, clamped));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

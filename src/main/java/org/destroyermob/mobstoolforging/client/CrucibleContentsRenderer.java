package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.client.model.ToolMaterialVisualManager;
import org.destroyermob.mobstoolforging.world.CrucibleContents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCategory;
import org.destroyermob.mobstoolforging.world.ToolMaterialDefinition;
import org.joml.Vector3f;

public final class CrucibleContentsRenderer {
    private static final ResourceLocation MOLTEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "block/molten_liquid");
    private static final float INNER_MIN = 4.7F / 16.0F;
    private static final float INNER_MAX = 11.3F / 16.0F;
    private static final float LIQUID_MIN_Y = 2.6F / 16.0F;
    private static final float LIQUID_MAX_Y = 12.1F / 16.0F;

    private final ItemRenderer itemRenderer;

    public CrucibleContentsRenderer() {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    public void renderContents(CrucibleContents contents, float meltProgress, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Level level) {
        renderMoltenMetal(contents, meltProgress, poseStack, bufferSource, packedOverlay);
        renderMeltingItem(contents, meltProgress, partialTick, poseStack, bufferSource, packedLight, packedOverlay, level);
    }

    public void renderHeatGlow(CrucibleContents contents, boolean activeHeat, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay, Level level) {
        float heat = contents.heat();
        float time = level == null ? partialTick : level.getGameTime() + partialTick;
        float flicker = ((float) Math.sin(time * 0.20F) + 1.0F) * 0.5F;
        float strength = clamp((activeHeat ? 0.14F : 0.0F) + heat * 0.58F + flicker * heat * 0.10F);
        if (strength <= 0.02F) {
            return;
        }

        TextureAtlasSprite sprite = blockSprite(MOLTEN_TEXTURE);
        int color = withAlpha(heatColor(heat), 0.08F + strength * 0.22F);
        renderBox(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)),
                sprite,
                color,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                0.18F,
                0.08F,
                0.18F,
                0.82F,
                0.92F,
                0.82F
        );
    }

    private void renderMoltenMetal(CrucibleContents contents, float meltProgress, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        Optional<ResourceLocation> material = moltenMaterial(contents);
        if (material.isEmpty()) {
            return;
        }

        float fill = contents.hasMoltenMaterial() ? 1.0F : meltProgress;
        if (fill <= 0.01F) {
            return;
        }
        float surfaceY = LIQUID_MIN_Y + (LIQUID_MAX_Y - LIQUID_MIN_Y) * clamp(fill);
        int color = moltenColor(material.get(), contents.heat());
        renderBox(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)),
                blockSprite(MOLTEN_TEXTURE),
                color,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                INNER_MIN,
                LIQUID_MIN_Y,
                INNER_MIN,
                INNER_MAX,
                surfaceY,
                INNER_MAX
        );
    }

    private void renderMeltingItem(CrucibleContents contents, float meltProgress, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Level level) {
        if (!contents.hasItem()) {
            return;
        }
        ItemStack stack = contents.item();
        float progress = clamp(meltProgress);
        float meltScale = 0.46F - progress * 0.20F;
        if (meltScale <= 0.05F) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.72F - progress * 0.12F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(itemRotation(level, partialTick)));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(meltScale, meltScale, meltScale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        if (contents.heat() > 0.02F) {
            HeatRenderUtil.renderHeatedItem(itemRenderer, stack, ItemDisplayContext.GROUND, packedOverlay, poseStack, bufferSource, level, contents.heat());
        }
        poseStack.popPose();
    }

    private Optional<ResourceLocation> moltenMaterial(CrucibleContents contents) {
        if (contents.hasMoltenMaterial()) {
            return contents.moltenMaterial();
        }
        if (!contents.hasItem()) {
            return Optional.empty();
        }
        ItemStack item = contents.item();
        if (item.is(Items.NETHERITE_SCRAP)) {
            return Optional.of(MaterialCatalog.NETHERITE);
        }
        return MaterialCatalog.resolve(item)
                .filter(definition -> definition.category() == MaterialCategory.METAL)
                .map(ToolMaterialDefinition::id);
    }

    private TextureAtlasSprite blockSprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }

    private static float itemRotation(Level level, float partialTick) {
        float time = level == null ? partialTick : level.getGameTime() + partialTick;
        return time * 1.8F;
    }

    private static int moltenColor(ResourceLocation material, float heat) {
        int materialColor = ToolMaterialVisualManager.INSTANCE.tintColor(material);
        int warmColor = lerpColor(materialColor, 0xFFFF651E, 0.72F);
        int hotColor = lerpColor(warmColor, 0xFFFFF1B8, smoothstep(0.68F, 1.0F, heat) * 0.55F);
        return withAlpha(hotColor, 0.94F);
    }

    private static int heatColor(float heat) {
        float amount = smoothstep(0.0F, 1.0F, heat);
        return lerpColor(0xFFFF5B18, 0xFFFFFFFF, amount * amount);
    }

    private static int withAlpha(int color, float alpha) {
        return Math.round(clamp(alpha) * 255.0F) << 24 | color & 0x00FFFFFF;
    }

    private static int lerpColor(int from, int to, float amount) {
        float clamped = clamp(amount);
        int alpha = Math.round(lerp(from >>> 24 & 0xFF, to >>> 24 & 0xFF, clamped));
        int red = Math.round(lerp(from >>> 16 & 0xFF, to >>> 16 & 0xFF, clamped));
        int green = Math.round(lerp(from >>> 8 & 0xFF, to >>> 8 & 0xFF, clamped));
        int blue = Math.round(lerp(from & 0xFF, to & 0xFF, clamped));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * clamp(amount);
    }

    private static float smoothstep(float from, float to, float value) {
        if (from >= to) {
            return clamp(value);
        }
        float t = clamp((value - from) / (to - from));
        return t * t * (3.0F - 2.0F * t);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (maxX <= minX || maxY <= minY || maxZ <= minZ) {
            return;
        }
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.UP.step(), color, light, overlay, minX, maxY, minZ, u0, v1, minX, maxY, maxZ, u0, v0, maxX, maxY, maxZ, u1, v0, maxX, maxY, minZ, u1, v1);
        quad(poseStack, consumer, Direction.DOWN.step(), color, light, overlay, minX, minY, maxZ, u0, v1, minX, minY, minZ, u0, v0, maxX, minY, minZ, u1, v0, maxX, minY, maxZ, u1, v1);
        quad(poseStack, consumer, Direction.NORTH.step(), color, light, overlay, minX, minY, minZ, u0, v1, minX, maxY, minZ, u0, v0, maxX, maxY, minZ, u1, v0, maxX, minY, minZ, u1, v1);
        quad(poseStack, consumer, Direction.SOUTH.step(), color, light, overlay, maxX, minY, maxZ, u0, v1, maxX, maxY, maxZ, u0, v0, minX, maxY, maxZ, u1, v0, minX, minY, maxZ, u1, v1);
        quad(poseStack, consumer, Direction.WEST.step(), color, light, overlay, minX, minY, maxZ, u0, v1, minX, maxY, maxZ, u0, v0, minX, maxY, minZ, u1, v0, minX, minY, minZ, u1, v1);
        quad(poseStack, consumer, Direction.EAST.step(), color, light, overlay, maxX, minY, minZ, u0, v1, maxX, maxY, minZ, u0, v0, maxX, maxY, maxZ, u1, v0, maxX, minY, maxZ, u1, v1);
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

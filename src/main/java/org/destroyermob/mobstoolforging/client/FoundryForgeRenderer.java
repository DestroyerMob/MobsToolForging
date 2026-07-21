package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.joml.Vector3f;

public class FoundryForgeRenderer implements BlockEntityRenderer<FoundryForgeBlockEntity> {
    private static final ResourceLocation MOLTEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "block/molten_still");
    private static final int MAX_RENDERED_INPUT_STACKS = 16;
    private static final float ITEM_SCALE = 0.32F;

    private final ItemRenderer itemRenderer;

    public FoundryForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public boolean shouldRenderOffScreen(FoundryForgeBlockEntity forge) {
        return false;
    }

    @Override
    public AABB getRenderBoundingBox(FoundryForgeBlockEntity forge) {
        return forge.renderBoundingBox();
    }

    @Override
    public void render(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!forge.isFormed()) {
            return;
        }
        float moltenY = renderMoltenContents(forge, partialTick, poseStack, bufferSource, packedOverlay);
        renderSolidInputs(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, moltenY);
    }

    private float renderMoltenContents(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        int capacity = forge.fluidCapacityMb();
        if (capacity <= 0 || forge.moltenLayers().isEmpty()) {
            return 0.06F;
        }
        float baseY = 0.04F;
        float usableHeight = Math.max(0.1F, forge.interiorRenderHeight() - 0.08F);
        float layerBottom = baseY;
        int accumulated = 0;
        int layerIndex = 0;
        float time = forge.getLevel() == null ? partialTick : forge.getLevel().getGameTime() + partialTick;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        TextureAtlasSprite sprite = blockSprite(MOLTEN_TEXTURE);
        for (FoundryForgeBlockEntity.MoltenLayer layer : forge.moltenLayers()) {
            accumulated += layer.amountMb();
            float layerTop = baseY + usableHeight * Math.min(1.0F, accumulated / (float) capacity);
            float drainPulse = layerIndex == 0 ? 0.10F + 0.10F * ((float) Math.sin(time * 0.22F) + 1.0F) : 0.0F;
            int color = withAlpha(moltenColor(layer.material()), 0.70F + drainPulse);
            renderTiledVerticalSides(
                    poseStack,
                    consumer,
                    sprite,
                    color,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    forge.interiorMinRenderX(),
                    layerBottom,
                    forge.interiorMinRenderZ(),
                    forge.interiorMaxRenderX(),
                    layerTop,
                    forge.interiorMaxRenderZ()
            );
            layerBottom = layerTop;
            layerIndex++;
        }
        renderLayerMarkers(forge, poseStack, bufferSource, packedOverlay, capacity, baseY, usableHeight);
        return renderMoltenSurface(forge, partialTick, poseStack, bufferSource, packedOverlay);
    }

    private float renderMoltenSurface(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        float fill = forge.moltenVisualFraction();
        if (fill <= 0.0F) {
            return 0.06F;
        }
        float y = 0.04F + Math.max(0.1F, forge.interiorRenderHeight() - 0.08F) * fill;
        float time = forge.getLevel() == null ? partialTick : forge.getLevel().getGameTime() + partialTick;
        float flicker = ((float) Math.sin(time * 0.21F) + 1.0F) * 0.5F;
        int base = forge.visibleMoltenMaterial().map(FoundryForgeRenderer::moltenColor).orElse(0xFFFF8A24);
        int color = withAlpha(base, 0.82F + flicker * 0.12F);
        renderTiledHorizontalSurface(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)),
                blockSprite(MOLTEN_TEXTURE),
                color,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                forge.interiorMinRenderX(),
                y,
                forge.interiorMinRenderZ(),
                forge.interiorMaxRenderX(),
                forge.interiorMaxRenderZ()
        );
        return y;
    }

    private void renderSolidInputs(FoundryForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float surfaceY) {
        List<ItemStack> stacks = forge.solidRenderStacks();
        int count = Math.min(MAX_RENDERED_INPUT_STACKS, stacks.size());
        if (count == 0) {
            return;
        }
        int columns = Math.max(1, (int) Math.ceil(Math.sqrt(count)));
        int rows = (int) Math.ceil(count / (float) columns);
        float minX = forge.interiorMinRenderX();
        float maxX = forge.interiorMaxRenderX();
        float minZ = forge.interiorMinRenderZ();
        float maxZ = forge.interiorMaxRenderZ();
        float progress = forge.meltProgressFraction();
        float time = forge.getLevel() == null ? partialTick : forge.getLevel().getGameTime() + partialTick;
        float y = Math.max(0.09F, surfaceY + 0.025F);
        for (int index = 0; index < count; index++) {
            int column = index % columns;
            int row = index / columns;
            float x = minX + (maxX - minX) * (column + 0.5F) / columns;
            float z = minZ + (maxZ - minZ) * (row + 0.5F) / rows;
            boolean melting = index == 0 && progress > 0.0F;
            float meltScale = melting ? ITEM_SCALE * (1.0F - progress * 0.58F) : ITEM_SCALE;
            float sink = melting ? progress * 0.085F : 0.0F;
            poseStack.pushPose();
            poseStack.translate(x, y + index * 0.001F - sink, z);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(index * 37.0F + (melting ? (float) Math.sin(time * 0.16F) * 8.0F : 0.0F)));
            poseStack.scale(meltScale, meltScale, meltScale);
            itemRenderer.renderStatic(stacks.get(index), ItemDisplayContext.FIXED, melting ? LightTexture.FULL_BRIGHT : packedLight,
                    packedOverlay, poseStack, bufferSource, forge.getLevel(), index);
            poseStack.popPose();
        }
    }

    private void renderLayerMarkers(FoundryForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay,
                                    int capacity, float baseY, float usableHeight) {
        int accumulated = 0;
        int index = 0;
        float minX = forge.interiorMinRenderX();
        float maxX = forge.interiorMaxRenderX();
        float minZ = forge.interiorMinRenderZ();
        float maxZ = forge.interiorMaxRenderZ();
        for (FoundryForgeBlockEntity.MoltenLayer layer : forge.moltenLayers()) {
            float bottom = baseY + usableHeight * Math.min(1.0F, accumulated / (float) capacity);
            accumulated += layer.amountMb();
            float top = baseY + usableHeight * Math.min(1.0F, accumulated / (float) capacity);
            if (top - bottom < 0.065F) {
                index++;
                continue;
            }
            ItemStack icon;
            try {
                icon = MaterialCatalog.displayStack(layer.material());
            } catch (RuntimeException ignored) {
                index++;
                continue;
            }
            if (icon.isEmpty()) {
                index++;
                continue;
            }
            float y = (bottom + top) * 0.5F;
            float scale = Math.min(0.26F, Math.max(0.14F, (top - bottom) * 0.52F));
            renderMarker(forge, icon, poseStack, bufferSource, packedOverlay, (minX + maxX) * 0.5F, y, minZ - 0.012F, 180.0F, scale, index * 4);
            renderMarker(forge, icon, poseStack, bufferSource, packedOverlay, (minX + maxX) * 0.5F, y, maxZ + 0.012F, 0.0F, scale, index * 4 + 1);
            renderMarker(forge, icon, poseStack, bufferSource, packedOverlay, minX - 0.012F, y, (minZ + maxZ) * 0.5F, -90.0F, scale, index * 4 + 2);
            renderMarker(forge, icon, poseStack, bufferSource, packedOverlay, maxX + 0.012F, y, (minZ + maxZ) * 0.5F, 90.0F, scale, index * 4 + 3);
            index++;
        }
    }

    private void renderMarker(FoundryForgeBlockEntity forge, ItemStack icon, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay,
                              float x, float y, float z, float rotation, float scale, int seed) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(icon, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay,
                poseStack, bufferSource, forge.getLevel(), 7100 + seed);
        poseStack.popPose();
    }

    private TextureAtlasSprite blockSprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }

    static int moltenColor(ResourceLocation material) {
        if (MaterialCatalog.IRON.equals(material)) {
            return 0xFFF0E8DD;
        }
        if (MaterialCatalog.GOLD.equals(material)) {
            return 0xFFFFD84A;
        }
        if (MaterialCatalog.COPPER.equals(material)) {
            return 0xFFE77C56;
        }
        if (MaterialCatalog.NETHERITE.equals(material)) {
            return 0xFF6C5B5E;
        }
        if (MaterialCatalog.NETHERITE_SCRAP.equals(material)) {
            return 0xFF4B3D40;
        }
        if (MaterialCatalog.STEEL.equals(material)) {
            return 0xFFAEB9C4;
        }
        if (MaterialCatalog.BRONZE.equals(material)) {
            return 0xFFCC7A32;
        }
        return 0xFFB8B8B8;
    }

    private static int withAlpha(int color, float alpha) {
        return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | color & 0x00FFFFFF;
    }

    private static void renderTiledHorizontalSurface(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float minX, float y, float minZ, float maxX, float maxZ) {
        int firstX = (int) Math.floor(minX);
        int lastX = (int) Math.ceil(maxX);
        int firstZ = (int) Math.floor(minZ);
        int lastZ = (int) Math.ceil(maxZ);
        for (int x = firstX; x < lastX; x++) {
            float tileMinX = Math.max(minX, x);
            float tileMaxX = Math.min(maxX, x + 1.0F);
            if (tileMaxX <= tileMinX) {
                continue;
            }
            for (int z = firstZ; z < lastZ; z++) {
                float tileMinZ = Math.max(minZ, z);
                float tileMaxZ = Math.min(maxZ, z + 1.0F);
                if (tileMaxZ > tileMinZ) {
                    renderHorizontalSurface(poseStack, consumer, sprite, color, light, overlay,
                            tileMinX, y, tileMinZ, tileMaxX, tileMaxZ,
                            tileMinX - x, tileMaxX - x, tileMinZ - z, tileMaxZ - z);
                }
            }
        }
    }

    private static void renderHorizontalSurface(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay,
                                                float minX, float y, float minZ, float maxX, float maxZ,
                                                float minU, float maxU, float minV, float maxV) {
        float u0 = sprite.getU(minU);
        float v0 = sprite.getV(minV);
        float u1 = sprite.getU(maxU);
        float v1 = sprite.getV(maxV);
        Vector3f normal = Direction.UP.step();
        vertex(poseStack, consumer, normal, color, light, overlay, minX, y, minZ, u0, v1);
        vertex(poseStack, consumer, normal, color, light, overlay, minX, y, maxZ, u0, v0);
        vertex(poseStack, consumer, normal, color, light, overlay, maxX, y, maxZ, u1, v0);
        vertex(poseStack, consumer, normal, color, light, overlay, maxX, y, minZ, u1, v1);
    }

    private static void renderTiledVerticalSides(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay,
                                                 float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (maxY <= minY) {
            return;
        }
        renderTiledVerticalX(poseStack, consumer, sprite, color, light, overlay, minX, minY, minZ, maxY, maxZ, Direction.WEST);
        renderTiledVerticalX(poseStack, consumer, sprite, color, light, overlay, maxX, minY, minZ, maxY, maxZ, Direction.EAST);
        renderTiledVerticalZ(poseStack, consumer, sprite, color, light, overlay, minZ, minX, minY, maxX, maxY, Direction.NORTH);
        renderTiledVerticalZ(poseStack, consumer, sprite, color, light, overlay, maxZ, minX, minY, maxX, maxY, Direction.SOUTH);
    }

    private static void renderTiledVerticalX(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay,
                                             float x, float minY, float minZ, float maxY, float maxZ, Direction direction) {
        int firstY = (int) Math.floor(minY);
        int lastY = (int) Math.ceil(maxY);
        int firstZ = (int) Math.floor(minZ);
        int lastZ = (int) Math.ceil(maxZ);
        for (int y = firstY; y < lastY; y++) {
            float tileMinY = Math.max(minY, y);
            float tileMaxY = Math.min(maxY, y + 1.0F);
            for (int z = firstZ; z < lastZ; z++) {
                float tileMinZ = Math.max(minZ, z);
                float tileMaxZ = Math.min(maxZ, z + 1.0F);
                if (tileMaxY > tileMinY && tileMaxZ > tileMinZ) {
                    renderVerticalX(poseStack, consumer, sprite, color, light, overlay, x, tileMinY, tileMinZ, tileMaxY, tileMaxZ,
                            tileMinZ - z, tileMaxZ - z, tileMinY - y, tileMaxY - y, direction);
                }
            }
        }
    }

    private static void renderTiledVerticalZ(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay,
                                             float z, float minX, float minY, float maxX, float maxY, Direction direction) {
        int firstX = (int) Math.floor(minX);
        int lastX = (int) Math.ceil(maxX);
        int firstY = (int) Math.floor(minY);
        int lastY = (int) Math.ceil(maxY);
        for (int x = firstX; x < lastX; x++) {
            float tileMinX = Math.max(minX, x);
            float tileMaxX = Math.min(maxX, x + 1.0F);
            for (int y = firstY; y < lastY; y++) {
                float tileMinY = Math.max(minY, y);
                float tileMaxY = Math.min(maxY, y + 1.0F);
                if (tileMaxX > tileMinX && tileMaxY > tileMinY) {
                    renderVerticalZ(poseStack, consumer, sprite, color, light, overlay, z, tileMinX, tileMinY, tileMaxX, tileMaxY,
                            tileMinX - x, tileMaxX - x, tileMinY - y, tileMaxY - y, direction);
                }
            }
        }
    }

    private static void renderVerticalX(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay,
                                        float x, float minY, float minZ, float maxY, float maxZ,
                                        float minU, float maxU, float minV, float maxV, Direction direction) {
        float u0 = sprite.getU(minU);
        float u1 = sprite.getU(maxU);
        float v0 = sprite.getV(minV);
        float v1 = sprite.getV(maxV);
        Vector3f normal = direction.step();
        if (direction == Direction.WEST) {
            vertex(poseStack, consumer, normal, color, light, overlay, x, minY, minZ, u0, v1);
            vertex(poseStack, consumer, normal, color, light, overlay, x, minY, maxZ, u1, v1);
            vertex(poseStack, consumer, normal, color, light, overlay, x, maxY, maxZ, u1, v0);
            vertex(poseStack, consumer, normal, color, light, overlay, x, maxY, minZ, u0, v0);
        } else {
            vertex(poseStack, consumer, normal, color, light, overlay, x, minY, minZ, u0, v1);
            vertex(poseStack, consumer, normal, color, light, overlay, x, maxY, minZ, u0, v0);
            vertex(poseStack, consumer, normal, color, light, overlay, x, maxY, maxZ, u1, v0);
            vertex(poseStack, consumer, normal, color, light, overlay, x, minY, maxZ, u1, v1);
        }
    }

    private static void renderVerticalZ(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay,
                                        float z, float minX, float minY, float maxX, float maxY,
                                        float minU, float maxU, float minV, float maxV, Direction direction) {
        float u0 = sprite.getU(minU);
        float u1 = sprite.getU(maxU);
        float v0 = sprite.getV(minV);
        float v1 = sprite.getV(maxV);
        Vector3f normal = direction.step();
        if (direction == Direction.NORTH) {
            vertex(poseStack, consumer, normal, color, light, overlay, minX, minY, z, u0, v1);
            vertex(poseStack, consumer, normal, color, light, overlay, minX, maxY, z, u0, v0);
            vertex(poseStack, consumer, normal, color, light, overlay, maxX, maxY, z, u1, v0);
            vertex(poseStack, consumer, normal, color, light, overlay, maxX, minY, z, u1, v1);
        } else {
            vertex(poseStack, consumer, normal, color, light, overlay, minX, minY, z, u0, v1);
            vertex(poseStack, consumer, normal, color, light, overlay, maxX, minY, z, u1, v1);
            vertex(poseStack, consumer, normal, color, light, overlay, maxX, maxY, z, u1, v0);
            vertex(poseStack, consumer, normal, color, light, overlay, minX, maxY, z, u0, v0);
        }
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int color, int light, int overlay, float x, float y, float z, float u, float v) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, color >>> 24 & 0xFF)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }
}

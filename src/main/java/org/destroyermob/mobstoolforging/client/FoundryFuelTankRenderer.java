package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.destroyermob.mobstoolforging.world.FoundryFuelTankBlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;

public class FoundryFuelTankRenderer implements BlockEntityRenderer<FoundryFuelTankBlockEntity> {
    private static final float MIN = 2.01F / 16.0F;
    private static final float MAX = 13.99F / 16.0F;

    public FoundryFuelTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FoundryFuelTankBlockEntity tank, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        FluidStack fluid = tank.fluidStack();
        float fill = tank.lavaVisualFraction();
        if (fill <= 0.0F || fluid.isEmpty()) {
            return;
        }
        float top = MIN + (MAX - MIN) * fill;
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        var texture = extensions.getStillTexture(fluid);
        if (texture == null) {
            return;
        }
        int tint = extensions.getTintColor(fluid);
        int alpha = tint >>> 24 & 0xFF;
        int color = (alpha == 0 ? 0xFF : alpha) << 24 | tint & 0x00FFFFFF;
        int fluidLight = fluid.getFluidType().getLightLevel(fluid);
        int light = LightTexture.pack(Math.max(LightTexture.block(packedLight), fluidLight), LightTexture.sky(packedLight));
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        renderHorizontal(poseStack, consumer, sprite, color, light, MIN, top, MIN, MAX, MAX, packedOverlay);
        renderVerticalZ(poseStack, consumer, sprite, color, light, MIN, MIN, top, MIN, MAX, Direction.NORTH, packedOverlay);
        renderVerticalZ(poseStack, consumer, sprite, color, light, MAX, MIN, top, MIN, MAX, Direction.SOUTH, packedOverlay);
        renderVerticalX(poseStack, consumer, sprite, color, light, MIN, MIN, top, MIN, MAX, Direction.WEST, packedOverlay);
        renderVerticalX(poseStack, consumer, sprite, color, light, MAX, MIN, top, MIN, MAX, Direction.EAST, packedOverlay);
    }

    private static void renderHorizontal(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, float minX, float y, float minZ, float maxX, float maxZ, int overlay) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(maxX - minX);
        float v1 = sprite.getV(maxZ - minZ);
        Vector3f normal = Direction.UP.step();
        vertex(poseStack, consumer, normal, color, light, minX, y, minZ, u0, v1, overlay);
        vertex(poseStack, consumer, normal, color, light, minX, y, maxZ, u0, v0, overlay);
        vertex(poseStack, consumer, normal, color, light, maxX, y, maxZ, u1, v0, overlay);
        vertex(poseStack, consumer, normal, color, light, maxX, y, minZ, u1, v1, overlay);
    }

    private static void renderVerticalZ(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, float z, float minY, float maxY, float minX, float maxX, Direction direction, int overlay) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(maxX - minX);
        float v1 = sprite.getV(maxY - minY);
        Vector3f normal = direction.step();
        if (direction == Direction.NORTH) {
            vertex(poseStack, consumer, normal, color, light, maxX, minY, z, u0, v1, overlay);
            vertex(poseStack, consumer, normal, color, light, maxX, maxY, z, u0, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, minX, maxY, z, u1, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, minX, minY, z, u1, v1, overlay);
        } else {
            vertex(poseStack, consumer, normal, color, light, minX, minY, z, u0, v1, overlay);
            vertex(poseStack, consumer, normal, color, light, minX, maxY, z, u0, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, maxX, maxY, z, u1, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, maxX, minY, z, u1, v1, overlay);
        }
    }

    private static void renderVerticalX(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, float x, float minY, float maxY, float minZ, float maxZ, Direction direction, int overlay) {
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(maxZ - minZ);
        float v1 = sprite.getV(maxY - minY);
        Vector3f normal = direction.step();
        if (direction == Direction.WEST) {
            vertex(poseStack, consumer, normal, color, light, x, minY, minZ, u0, v1, overlay);
            vertex(poseStack, consumer, normal, color, light, x, maxY, minZ, u0, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, x, maxY, maxZ, u1, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, x, minY, maxZ, u1, v1, overlay);
        } else {
            vertex(poseStack, consumer, normal, color, light, x, minY, maxZ, u0, v1, overlay);
            vertex(poseStack, consumer, normal, color, light, x, maxY, maxZ, u0, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, x, maxY, minZ, u1, v0, overlay);
            vertex(poseStack, consumer, normal, color, light, x, minY, minZ, u1, v1, overlay);
        }
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int color, int light, float x, float y, float z, float u, float v, int overlay) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, color >>> 24 & 0xFF)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }
}

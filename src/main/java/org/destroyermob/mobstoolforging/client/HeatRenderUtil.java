package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HeatRenderUtil {
    private HeatRenderUtil() {
    }

    public static void renderHeatedItem(ItemRenderer itemRenderer, ItemStack stack, ItemDisplayContext context, int packedOverlay, PoseStack poseStack, MultiBufferSource bufferSource, Level level, float heat) {
        if (heat <= 0.02F) {
            return;
        }
        itemRenderer.renderStatic(stack, context, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, new HeatTintingBufferSource(bufferSource, heat), level, 1);
    }

    public static int inventoryTintColor(float heat) {
        float clamped = clamp(heat);
        int green = Math.round(80.0F + clamped * 175.0F);
        int blue = Math.round(16.0F + clamped * clamped * 220.0F);
        int alpha = Math.round(80.0F + clamped * 120.0F);
        return alpha << 24 | 0xFF << 16 | green << 8 | blue;
    }

    private record HeatTintingBufferSource(MultiBufferSource delegate, float heat) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new HeatTintingVertexConsumer(delegate.getBuffer(renderType), heat);
        }
    }

    private static class HeatTintingVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float heat;

        private HeatTintingVertexConsumer(VertexConsumer delegate, float heat) {
            this.delegate = delegate;
            this.heat = clamp(heat);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            float strength = 0.22F + heat * 0.62F;
            int hotGreen = Math.round(80.0F + heat * 175.0F);
            int hotBlue = Math.round(16.0F + heat * heat * 220.0F);
            delegate.setColor(mix(red, 255, strength), mix(green, hotGreen, strength), mix(blue, hotBlue, strength), alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }

        private static int mix(int base, int target, float amount) {
            return Math.round(base + (target - base) * amount);
        }
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

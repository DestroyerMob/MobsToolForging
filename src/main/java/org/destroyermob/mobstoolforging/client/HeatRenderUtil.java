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

    public static void renderHeatedItem(ItemRenderer itemRenderer, ItemStack stack, ItemDisplayContext context, int packedLight, int packedOverlay, PoseStack poseStack, MultiBufferSource bufferSource, Level level, float heat) {
        float clamped = HeatVisuals.clamp(heat);
        itemRenderer.renderStatic(stack, context, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        if (clamped <= 0.02F) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -0.018F);
        itemRenderer.renderStatic(stack, context, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, new HeatTintingBufferSource(bufferSource, clamped, HeatLayer.EDGE), level, 1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -0.028F);
        itemRenderer.renderStatic(stack, context, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, new HeatTintingBufferSource(bufferSource, clamped, HeatLayer.CORE), level, 2);
        poseStack.popPose();
    }

    public static int inventoryTintColor(float heat) {
        float clamped = HeatVisuals.clamp(heat);
        int alpha = Math.round(80.0F + clamped * 120.0F);
        return HeatVisuals.withAlpha(HeatVisuals.heatColor(clamped), alpha / 255.0F);
    }

    private enum HeatLayer {
        EDGE,
        CORE
    }

    private record HeatTintingBufferSource(MultiBufferSource delegate, float heat, HeatLayer layer) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new HeatTintingVertexConsumer(delegate.getBuffer(renderType), heat, layer);
        }
    }

    private static class HeatTintingVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float heat;
        private final HeatLayer layer;

        private HeatTintingVertexConsumer(VertexConsumer delegate, float heat, HeatLayer layer) {
            this.delegate = delegate;
            this.heat = HeatVisuals.clamp(heat);
            this.layer = layer;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            int hotColor = HeatVisuals.heatColor(heat);
            float strength = layer == HeatLayer.CORE ? 0.18F + heat * 0.32F : 0.42F + heat * 0.46F;
            int hotRed = hotColor >>> 16 & 0xFF;
            int hotGreen = hotColor >>> 8 & 0xFF;
            int hotBlue = hotColor & 0xFF;
            delegate.setColor(mix(red, hotRed, strength), mix(green, hotGreen, strength), mix(blue, hotBlue, strength), alpha);
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

}

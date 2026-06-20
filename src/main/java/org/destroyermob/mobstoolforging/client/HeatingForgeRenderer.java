package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class HeatingForgeRenderer implements BlockEntityRenderer<HeatingForgeBlockEntity> {
    private final ItemRenderer itemRenderer;

    public HeatingForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack fuel = forge.fuelStack();
        if (!fuel.isEmpty()) {
            renderFlatItem(forge, fuel, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, -0.12F, 0.28F, 0.13F, 35.0F);
        }
        ItemStack workpiece = forge.workpieceStack();
        if (!workpiece.isEmpty()) {
            float heat = heatAmount(forge, workpiece);
            float heatScale = 0.36F + heat * 0.04F;
            renderFlatItem(forge, workpiece, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, -0.12F, heatScale, 0.60F, 0.0F, heat);
        }
    }

    private void renderFlatItem(HeatingForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation) {
        renderFlatItem(forge, stack, poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, surfaceY, localRotation, 0.0F);
    }

    private void renderFlatItem(HeatingForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation, float heat) {
        Level level = forge.getLevel();
        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation(forge.getBlockState().getValue(HeatingForgeBlock.FACING))));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        if (heat > 0.02F) {
            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, new HeatTintingBufferSource(bufferSource, heat), level, 1);
        }
        poseStack.popPose();
    }

    private static float facingRotation(Direction direction) {
        return switch (direction) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float heatAmount(HeatingForgeBlockEntity forge, ItemStack stack) {
        Level level = forge.getLevel();
        float stackTemperature = level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
        return Math.max(forge.heatProgressFraction(), stackTemperature);
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
            this.heat = Math.max(0.0F, Math.min(1.0F, heat));
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            float strength = 0.20F + heat * 0.55F;
            int hotGreen = 48 + Math.round(heat * 146.0F);
            delegate.setColor(mix(red, 255, strength), mix(green, hotGreen, strength), mix(blue, 16, strength), alpha);
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

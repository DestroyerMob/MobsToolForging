package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
        if (heat > 0.02F) {
            HeatRenderUtil.renderHeatedItem(itemRenderer, stack, ItemDisplayContext.GROUND, packedOverlay, poseStack, bufferSource, level, heat);
        } else {
            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        }
        poseStack.popPose();
    }

    private static float facingRotation(Direction direction) {
        // The authored forge model opens west before blockstate rotation.
        return switch (direction) {
            case EAST -> 180.0F;
            case SOUTH -> 270.0F;
            case WEST -> 0.0F;
            default -> 90.0F;
        };
    }

    private static float heatAmount(HeatingForgeBlockEntity forge, ItemStack stack) {
        Level level = forge.getLevel();
        float stackTemperature = level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
        return Math.max(forge.heatProgressFraction(), stackTemperature);
    }
}

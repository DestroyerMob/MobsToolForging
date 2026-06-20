package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class HeatingForgeRenderer implements BlockEntityRenderer<HeatingForgeBlockEntity> {
    private static final float[][] FUEL_OFFSETS = {
            {-0.12F, -0.18F, -12.0F},
            {0.12F, -0.18F, 18.0F},
            {-0.12F, 0.05F, 24.0F},
            {0.12F, 0.05F, -28.0F}
    };

    public HeatingForgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderFuel(forge, poseStack, bufferSource, packedLight, packedOverlay);
        ItemStack workpiece = forge.workpieceStack();
        if (!workpiece.isEmpty()) {
            float heat = heatAmount(forge, workpiece);
            renderInsert(forge, HeatingForgeInsertVisualManager.workpiece(workpiece), poseStack, bufferSource, packedLight, packedOverlay, 0.0F, -0.12F, 1.15F, 0.60F, 0.0F, heat);
        }
    }

    private void renderFuel(HeatingForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack fuel = forge.fuelStack();
        int visibleFuel = Math.min(FUEL_OFFSETS.length, fuel.getCount() + (forge.isLit() ? 1 : 0));
        if (visibleFuel <= 0) {
            return;
        }
        ItemStack visualFuel = fuel.isEmpty() ? new ItemStack(Items.COAL) : fuel;
        HeatingForgeInsertVisualManager.ResolvedInsert visual = HeatingForgeInsertVisualManager.fuel(visualFuel);
        for (int index = 0; index < visibleFuel; index++) {
            float[] offset = FUEL_OFFSETS[index];
            renderInsert(forge, visual, poseStack, bufferSource, packedLight, packedOverlay, offset[0], offset[1], 1.65F, 0.13F, offset[2], forge.isLit() ? 0.20F : 0.0F);
        }
    }

    private void renderInsert(HeatingForgeBlockEntity forge, HeatingForgeInsertVisualManager.ResolvedInsert visual, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation, float heat) {
        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation(forge.getBlockState().getValue(HeatingForgeBlock.FACING))));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation));
        poseStack.scale(scale, scale, scale);
        HeatingForgeVoxelRenderer.render(visual.model(), visual.visual().texture(), poseStack, bufferSource, packedLight, packedOverlay, heat);
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

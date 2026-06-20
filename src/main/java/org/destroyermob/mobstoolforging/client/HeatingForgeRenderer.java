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
    private static final float WORKPIECE_PAIR_SIDE_OFFSET = 0.18F;
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
        renderFuel(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        int workpieceCount = forge.workpieceCount();
        int renderedWorkpiece = 0;
        for (int slot = 0; slot < forge.workpieceSlots(); slot++) {
            ItemStack workpiece = forge.workpieceStack(slot);
            if (workpiece.isEmpty()) {
                continue;
            }
            float localZ = workpieceCount == 1 ? 0.0F : (renderedWorkpiece == 0 ? -WORKPIECE_PAIR_SIDE_OFFSET : WORKPIECE_PAIR_SIDE_OFFSET);
            float localRotation = workpieceCount == 1 ? 0.0F : (renderedWorkpiece == 0 ? -4.0F : 4.0F);
            float heat = heatAmount(forge, slot, workpiece);
            renderInsert(forge, HeatingForgeInsertVisualManager.workpiece(workpiece), poseStack, bufferSource, packedLight, packedOverlay, 0.0F, localZ, 1.15F, 0.60F, localRotation, heat);
            renderedWorkpiece++;
        }
    }

    private void renderFuel(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack fuel = forge.fuelStack();
        int visibleFuel = Math.min(FUEL_OFFSETS.length, fuel.getCount() + (forge.isLit() ? 1 : 0));
        if (visibleFuel <= 0) {
            return;
        }
        ItemStack visualFuel = fuel.isEmpty() ? new ItemStack(Items.COAL) : fuel;
        HeatingForgeInsertVisualManager.ResolvedInsert visual = HeatingForgeInsertVisualManager.fuel(visualFuel);
        for (int index = 0; index < visibleFuel; index++) {
            float[] offset = FUEL_OFFSETS[index];
            renderInsert(forge, visual, poseStack, bufferSource, packedLight, packedOverlay, offset[0], offset[1], 1.65F, 0.13F, offset[2], fuelHeat(forge, partialTick, index));
        }
    }

    private void renderInsert(HeatingForgeBlockEntity forge, HeatingForgeInsertVisualManager.ResolvedInsert visual, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation, float heat) {
        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation(forge.getBlockState().getValue(HeatingForgeBlock.FACING))));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation));
        poseStack.scale(scale, scale, scale);
        HeatingForgeVoxelRenderer.render(visual.model(), visual.visual(), poseStack, bufferSource, packedLight, packedOverlay, heat);
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

    private static float heatAmount(HeatingForgeBlockEntity forge, int slot, ItemStack stack) {
        Level level = forge.getLevel();
        float stackTemperature = level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
        return Math.max(forge.heatProgressFraction(slot), stackTemperature);
    }

    private static float fuelHeat(HeatingForgeBlockEntity forge, float partialTick, int index) {
        if (!forge.isLit()) {
            return 0.0F;
        }
        Level level = forge.getLevel();
        float time = level == null ? 0.0F : level.getGameTime() + partialTick;
        float pulse = ((float) Math.sin(time * 0.18F + index * 1.7F) + 1.0F) * 0.5F;
        float workpieceBonus = forge.hasWorkpiece() ? forge.heatProgressFraction() * 0.08F : 0.0F;
        return clamp(0.38F + pulse * 0.18F + workpieceBonus, 0.0F, 0.68F);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

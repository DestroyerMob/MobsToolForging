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
    private static final Placement[] FUEL_PLACEMENTS = {
            new Placement(-0.1875F, -0.1875F, 1.0F, 0.75F, 0.0F),
            new Placement(0.1875F, -0.1875F, 1.0F, 0.75F, 0.0F),
            new Placement(0.1875F, 0.1875F, 1.0F, 0.75F, 0.0F),
            new Placement(-0.1875F, 0.1875F, 1.0F, 0.75F, 0.0F)
    };
    private static final Placement[] WORKPIECE_PLACEMENTS = {
            new Placement(-0.21875F, -0.15625F, 1.0F, 0.875F, 90.0F),
            new Placement(-0.15625F, 0.21875F, 1.0F, 0.875F, 0.0F),
            new Placement(0.15625F, -0.21875F, 1.0F, 0.875F, 0.0F),
            new Placement(0.21875F, 0.15625F, 1.0F, 0.875F, 90.0F)
    };

    public HeatingForgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderAsh(forge, poseStack, bufferSource, packedLight, packedOverlay);
        renderFuel(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        for (int slot = 0; slot < forge.workpieceSlots(); slot++) {
            ItemStack workpiece = forge.workpieceStack(slot);
            if (workpiece.isEmpty()) {
                continue;
            }
            Placement placement = WORKPIECE_PLACEMENTS[Math.min(slot, WORKPIECE_PLACEMENTS.length - 1)];
            float heat = heatAmount(forge, slot, workpiece);
            renderInsert(forge, HeatingForgeInsertVisualManager.workpiece(workpiece), poseStack, bufferSource, packedLight, packedOverlay, placement, heat);
        }
    }

    private void renderAsh(HeatingForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        HeatingForgeInsertVisualManager.ResolvedInsert ash = HeatingForgeInsertVisualManager.ash();
        for (int layer = 0; layer < forge.ashLayers(); layer++) {
            renderInsert(forge, ash, poseStack, bufferSource, packedLight, packedOverlay, new Placement(0.0F, 0.0F, 1.0F, (2.0F + layer) / 16.0F, 0.0F), 0.0F);
        }
    }

    private void renderFuel(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack fuel = forge.fuelStack();
        int visibleFuel = Math.min(FUEL_PLACEMENTS.length, forge.fuelBedCount());
        if (visibleFuel <= 0) {
            return;
        }
        HeatingForgeInsertVisualManager.ResolvedInsert visual = forge.hasSpentFuelBed()
                ? HeatingForgeInsertVisualManager.spentFuel()
                : HeatingForgeInsertVisualManager.fuel(fuel.isEmpty() ? new ItemStack(Items.COAL) : fuel);
        for (int index = 0; index < visibleFuel; index++) {
            renderInsert(forge, visual, poseStack, bufferSource, packedLight, packedOverlay, FUEL_PLACEMENTS[index], forge.hasSpentFuelBed() ? 0.0F : fuelHeat(forge, partialTick, index));
        }
    }

    private void renderInsert(HeatingForgeBlockEntity forge, HeatingForgeInsertVisualManager.ResolvedInsert visual, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Placement placement, float heat) {
        poseStack.pushPose();
        poseStack.translate(0.5F, placement.surfaceY(), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation(forge.getBlockState().getValue(HeatingForgeBlock.FACING))));
        poseStack.translate(placement.localX(), 0.0F, placement.localZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(placement.rotation()));
        poseStack.scale(placement.scale(), placement.scale(), placement.scale());
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
        float fuelTemperature = forge.fuelTemperatureFraction();
        float workpieceBonus = forge.hasWorkpiece() ? forge.heatProgressFraction() * 0.08F : 0.0F;
        return clamp(0.24F + fuelTemperature * 0.42F + pulse * 0.12F + workpieceBonus, 0.0F, 0.84F);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Placement(float localX, float localZ, float scale, float surfaceY, float rotation) {
    }
}

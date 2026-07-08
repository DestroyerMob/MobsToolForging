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
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.world.DryingRackBlock;
import org.destroyermob.mobstoolforging.world.DryingRackBlockEntity;

public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    private static final float MODEL_UNITS = 16.0F;
    private static final float RACK_CENTER_X = 8.0F / MODEL_UNITS;
    private static final float RACK_LOWER_EDGE_Y = 15.0F / MODEL_UNITS;
    private static final float RACK_FRONT_Z = 15.0F / MODEL_UNITS;
    private static final float ITEM_PLANE_CLEARANCE = 0.5F / MODEL_UNITS;
    private static final float ITEM_DISPLAY_SIZE = 14.0F / MODEL_UNITS;
    private static final float ITEM_CENTER_Y = RACK_LOWER_EDGE_Y - ITEM_DISPLAY_SIZE * 0.5F;
    private static final float ITEM_SURFACE_Z = RACK_FRONT_Z - ITEM_PLANE_CLEARANCE;

    private final ItemRenderer itemRenderer;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(DryingRackBlockEntity rack, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = rack.displayStack();
        if (stack.isEmpty()) {
            return;
        }
        BlockState state = rack.getBlockState();
        if (!state.hasProperty(DryingRackBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(DryingRackBlock.FACING);
        Level level = rack.getLevel();
        float worldX = worldX(facing, RACK_CENTER_X, ITEM_SURFACE_Z);
        float worldZ = worldZ(facing, RACK_CENTER_X, ITEM_SURFACE_Z);

        poseStack.pushPose();
        poseStack.translate(worldX, ITEM_CENTER_Y, worldZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(modelRotation(facing)));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(ITEM_DISPLAY_SIZE, ITEM_DISPLAY_SIZE, ITEM_DISPLAY_SIZE);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }

    private static float worldX(Direction facing, float modelX, float modelZ) {
        return switch (facing) {
            case EAST -> 1.0F - modelZ;
            case SOUTH -> 1.0F - modelX;
            case WEST -> modelZ;
            case NORTH, UP, DOWN -> modelX;
        };
    }

    private static float worldZ(Direction facing, float modelX, float modelZ) {
        return switch (facing) {
            case EAST -> modelX;
            case SOUTH -> 1.0F - modelZ;
            case WEST -> 1.0F - modelX;
            case NORTH, UP, DOWN -> modelZ;
        };
    }

    private static float modelRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            case NORTH, UP, DOWN -> 0.0F;
        };
    }
}

package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;

public class ToolForgeRenderer implements BlockEntityRenderer<ToolForgeBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ToolForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack display = forge.displayMaterialStack();
        if (display.isEmpty()) {
            return;
        }
        if (forge.isComplete()) {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, 0.62F);
            return;
        }

        int count = forge.materialCount();
        float spread = 0.28F * (1.0F - forge.progress());
        if (count == 1) {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, 0.52F);
        } else if (count == 2) {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, -spread, 0.52F);
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, spread, 0.52F);
        } else {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, -spread, 0.52F);
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, 0.54F);
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, spread, 0.52F);
        }
    }

    private void renderItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale) {
        Level level = forge.getLevel();
        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.05F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingRotation));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }
}

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
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;

public class HeatingForgeRenderer implements BlockEntityRenderer<HeatingForgeBlockEntity> {
    private final ItemRenderer itemRenderer;

    public HeatingForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack fuel = forge.fuelStack();
        if (!fuel.isEmpty()) {
            renderFlatItem(forge, fuel, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, 0.30F, 0.255F, 35.0F);
        }
        ItemStack workpiece = forge.workpieceStack();
        if (!workpiece.isEmpty()) {
            float heatScale = 0.44F + forge.heatProgressFraction() * 0.05F;
            renderFlatItem(forge, workpiece, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, heatScale, 0.905F, 0.0F);
        }
    }

    private void renderFlatItem(HeatingForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation) {
        Level level = forge.getLevel();
        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }
}

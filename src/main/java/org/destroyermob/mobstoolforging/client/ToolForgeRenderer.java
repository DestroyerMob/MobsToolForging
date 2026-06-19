package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCategory;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;

public class ToolForgeRenderer implements BlockEntityRenderer<ToolForgeBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ToolForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (forge.template() != null && !forge.isComplete()) {
            renderTemplatePreview(forge, poseStack, bufferSource, packedLight, packedOverlay);
        }

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

    private void renderTemplatePreview(ToolForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ForgeTemplate template = forge.template();
        if (template == null) {
            return;
        }
        ItemStack preview = template.outputStack(previewMaterial(forge));
        if (!preview.isEmpty()) {
            renderFlatItem(forge, preview, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, 0.36F, 1.035F, 0.0F);
        }
    }

    private void renderItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale) {
        renderFlatItem(forge, stack, poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, 1.05F, forge.displayRotationDegrees());
    }

    private void renderFlatItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation) {
        Level level = forge.getLevel();
        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingRotation));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }

    private static ResourceLocation previewMaterial(ToolForgeBlockEntity forge) {
        BlockState state = forge.getBlockState();
        if (state.getBlock() instanceof ToolWorkstationBlock workstation && workstation.kind().materialCategory() == MaterialCategory.GEM) {
            return MaterialCatalog.DIAMOND;
        }
        return MaterialCatalog.IRON;
    }
}

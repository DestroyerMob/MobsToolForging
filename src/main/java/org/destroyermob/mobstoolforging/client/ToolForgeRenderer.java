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
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
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
        DisplayLayout layout = layout(forge);
        if (forge.template() != null && !forge.isComplete()) {
            renderTemplatePreview(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
        }

        ItemStack display = forge.displayMaterialStack();
        if (display.isEmpty()) {
            return;
        }
        if (forge.isComplete()) {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, layout.outputScale());
            return;
        }

        int count = forge.materialCount();
        float spread = layout.spread() * (1.0F - forge.progress());
        if (count == 1) {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, layout.materialScale());
        } else if (count == 2) {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, -spread, layout.materialScale());
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, spread, layout.materialScale());
        } else {
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, -spread, layout.materialScale());
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, layout.centerMaterialScale());
            renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, spread, layout.materialScale());
        }
    }

    private void renderTemplatePreview(ToolForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        ForgeTemplateDefinition template = forge.template();
        if (template == null) {
            return;
        }
        ItemStack preview = template.outputStack(previewMaterial(forge));
        if (!preview.isEmpty()) {
            renderFlatItem(forge, preview, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, layout.previewScale(), layout.previewSurfaceY(), 0.0F);
        }
    }

    private void renderItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout, float offset, float scale) {
        float localX = layout.spreadAxis() == SpreadAxis.X ? offset : 0.0F;
        float localZ = layout.spreadAxis() == SpreadAxis.Z ? offset : 0.0F;
        renderFlatItem(forge, stack, poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, layout.materialSurfaceY(), forge.displayRotationDegrees());
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

    private static DisplayLayout layout(ToolForgeBlockEntity forge) {
        BlockState state = forge.getBlockState();
        if (state.getBlock() instanceof ToolWorkstationBlock workstation && workstation.kind().materialCategory() == MaterialCategory.GEM) {
            return DisplayLayout.LAPIDARY;
        }
        return DisplayLayout.TOOL_FORGE;
    }

    private enum SpreadAxis {
        X,
        Z
    }

    private record DisplayLayout(
            float materialSurfaceY,
            float previewSurfaceY,
            float materialScale,
            float centerMaterialScale,
            float outputScale,
            float previewScale,
            float spread,
            SpreadAxis spreadAxis
    ) {
        private static final DisplayLayout TOOL_FORGE = new DisplayLayout(
                0.707F,
                0.697F,
                0.46F,
                0.48F,
                0.58F,
                0.32F,
                0.24F,
                SpreadAxis.X
        );
        private static final DisplayLayout LAPIDARY = new DisplayLayout(
                0.770F,
                0.758F,
                0.42F,
                0.44F,
                0.52F,
                0.30F,
                0.20F,
                SpreadAxis.X
        );
    }
}

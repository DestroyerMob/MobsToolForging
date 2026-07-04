package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
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
import org.destroyermob.mobstoolforging.world.ArmorForgeAttachment;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class ToolForgeRenderer implements BlockEntityRenderer<ToolForgeBlockEntity> {
    private static final float CAMPFIRE_ITEM_SCALE = 0.375F;

    private final ItemRenderer itemRenderer;

    public ToolForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        DisplayLayout layout = layout(forge);
        if (isToolmakersBench(forge) && !forge.isComplete()) {
            renderBenchStacks(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.hasRepairStacks() && !forge.isComplete()) {
            renderBenchStacks(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.hasArmorAttachmentTarget() && !forge.isComplete()) {
            renderArmorAttachmentWork(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.template() != null && !forge.isComplete()) {
            renderTemplatePreview(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
        }
        renderAbrasive(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);

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

    private void renderArmorAttachmentWork(ToolForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        ItemStack target = forge.armorAttachmentTarget();
        if (!target.isEmpty()) {
            renderFlatItem(forge, target, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, layout.outputScale(), layout.materialSurfaceY(), forge.displayRotationDegrees());
        }

        ItemStack material = forge.displayMaterialStack();
        if (material.isEmpty()) {
            return;
        }

        int count = forge.materialCount();
        float spread = layout.spread();
        if (count == 1) {
            renderItem(forge, material, poseStack, bufferSource, packedLight, packedOverlay, layout, spread, layout.materialScale());
        } else if (count == 2) {
            renderItem(forge, material, poseStack, bufferSource, packedLight, packedOverlay, layout, -spread, layout.materialScale());
            renderItem(forge, material, poseStack, bufferSource, packedLight, packedOverlay, layout, spread, layout.materialScale());
        } else {
            renderItem(forge, material, poseStack, bufferSource, packedLight, packedOverlay, layout, -spread, layout.materialScale());
            renderItem(forge, material, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, layout.centerMaterialScale());
            renderItem(forge, material, poseStack, bufferSource, packedLight, packedOverlay, layout, spread, layout.materialScale());
        }
    }

    private void renderAbrasive(ToolForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        ItemStack abrasive = forge.abrasiveStack();
        if (abrasive.isEmpty() || layout.abrasiveScale() <= 0.0F) {
            return;
        }
        renderFlatItem(forge, abrasive, poseStack, bufferSource, packedLight, packedOverlay, layout.abrasiveX(), layout.abrasiveZ(), layout.abrasiveScale(), layout.abrasiveSurfaceY(), 0.0F);
    }

    private void renderBenchStacks(ToolForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        List<ItemStack> stacks = forge.benchStacks();
        int count = stacks.size();
        if (count == 0) {
            return;
        }
        if (count == 1) {
            renderFlatItem(forge, stacks.get(0), poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, layout.materialScale(), layout.materialSurfaceY(), forge.displayRotationDegrees());
            return;
        }
        float radius = count <= 4 ? 0.18F : 0.25F;
        float scale = count <= 4 ? layout.materialScale() : layout.centerMaterialScale();
        for (int index = 0; index < count; index++) {
            float angle = (float) (Math.PI * 2.0D * index / count);
            float localX = (float) Math.cos(angle) * radius;
            float localZ = (float) Math.sin(angle) * radius;
            renderFlatItem(forge, stacks.get(index), poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, layout.materialSurfaceY(), forge.displayRotationDegrees() + index * 23.0F);
        }
    }

    private void renderTemplatePreview(ToolForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        ForgeTemplateDefinition template = forge.template();
        if (template == null) {
            return;
        }
        ResourceLocation material = previewMaterial(forge);
        ItemStack preview = ArmorForgeAttachment.isAttachmentTemplate(template)
                ? ArmorForgeAttachment.previewOutputStack(template.id(), material)
                : template.outputStack(material);
        if (!preview.isEmpty()) {
            renderFlatItem(forge, preview, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.0F, layout.previewScale(), layout.previewSurfaceY(), 0.0F);
        }
    }

    private void renderItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout, float offset, float scale) {
        float localX = layout.spreadAxis() == SpreadAxis.X ? offset : 0.0F;
        float localZ = layout.spreadAxis() == SpreadAxis.Z ? offset : 0.0F;
        boolean qualityWindow = forge.isTimingQualityWindow();
        float surfaceY = qualityWindow ? layout.materialSurfaceY() + 0.018F : layout.materialSurfaceY();
        renderFlatItem(forge, stack, poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, surfaceY, forge.displayRotationDegrees());
    }

    private void renderFlatItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation) {
        Level level = forge.getLevel();
        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingRotation));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        float heat = level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
        if (heat > 0.02F) {
            HeatRenderUtil.renderHeatedItem(itemRenderer, stack, ItemDisplayContext.FIXED, packedOverlay, poseStack, bufferSource, level, heat);
        } else {
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        }
        poseStack.popPose();
    }

    private static ResourceLocation previewMaterial(ToolForgeBlockEntity forge) {
        BlockState state = forge.getBlockState();
        if (state.getBlock() instanceof ToolWorkstationBlock workstation && workstation.kind() == WorkstationKind.LAPIDARY_TABLE) {
            return MaterialCatalog.DIAMOND;
        }
        return MaterialCatalog.IRON;
    }

    private static DisplayLayout layout(ToolForgeBlockEntity forge) {
        BlockState state = forge.getBlockState();
        if (state.getBlock() instanceof ToolWorkstationBlock workstation) {
            if (workstation.kind() == WorkstationKind.LAPIDARY_TABLE) {
                return DisplayLayout.LAPIDARY;
            }
            if (workstation.kind() == WorkstationKind.TOOLMAKERS_BENCH) {
                return DisplayLayout.TOOLMAKERS_BENCH;
            }
        }
        return DisplayLayout.TOOL_FORGE;
    }

    private static boolean isToolmakersBench(ToolForgeBlockEntity forge) {
        BlockState state = forge.getBlockState();
        return state.getBlock() instanceof ToolWorkstationBlock workstation && workstation.kind() == WorkstationKind.TOOLMAKERS_BENCH;
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
            SpreadAxis spreadAxis,
            float abrasiveSurfaceY,
            float abrasiveScale,
            float abrasiveX,
            float abrasiveZ
    ) {
        private static final DisplayLayout TOOL_FORGE = new DisplayLayout(
                0.707F,
                0.697F,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                0.24F,
                SpreadAxis.X,
                0.0F,
                0.0F,
                0.0F,
                0.0F
        );
        private static final DisplayLayout LAPIDARY = new DisplayLayout(
                0.770F,
                0.758F,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                0.20F,
                SpreadAxis.X,
                0.775F,
                CAMPFIRE_ITEM_SCALE,
                0.0F,
                -0.28F
        );
        private static final DisplayLayout TOOLMAKERS_BENCH = new DisplayLayout(
                0.585F,
                0.575F,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                0.20F,
                SpreadAxis.X,
                0.0F,
                0.0F,
                0.0F,
                0.0F
        );
    }
}

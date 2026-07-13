package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ArmorForgeAttachment;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ForgeTemplatePreview;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ToolmakerBenchAssembly;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class ToolForgeRenderer implements BlockEntityRenderer<ToolForgeBlockEntity> {
    private static final float CAMPFIRE_ITEM_SCALE = 0.375F;
    private static final float ARMOR_FLAT_ITEM_SCALE = 14.0F / 16.0F;
    private static final float ARMOR_PART_DISPLAY_SCALE = 0.80F;
    private static final float TOOL_PART_DISPLAY_SCALE = 0.88F;
    private static final float CROSSBOW_PART_DISPLAY_SCALE = 0.80F;
    private static final float GHOST_MATERIAL_ALPHA = 0.45F;
    private static final int MATERIAL_PREVIEW_TICKS = 40;

    private final ItemRenderer itemRenderer;

    public ToolForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        DisplayLayout layout = layout(forge);
        if (isToolmakersBench(forge) && !forge.isComplete()) {
            renderBenchStacks(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderToolmakerResultPreview(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.hasRepairStacks() && !forge.isComplete()) {
            renderBenchStacks(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.hasArmorAttachmentTarget() && !forge.isComplete()) {
            renderArmorAttachmentWork(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderTemplateMaterialRequirements(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderAnvilReadyPartPreview(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.hasLapidaryCoatingBase() && !forge.isComplete()) {
            renderLapidaryCoatingWork(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        StationWorkRecipe stationWorkRecipe = stationWorkRecipe(forge);
        if (stationWorkRecipe != null && !forge.isComplete()) {
            renderStationWorkDisplay(forge, stationWorkRecipe, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            return;
        }
        if (forge.template() != null && !forge.isComplete()) {
            renderTemplateMaterialRequirements(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderAnvilReadyPartPreview(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
        }
        renderAbrasive(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);

        ItemStack display = forge.displayMaterialStack();
        if (display.isEmpty()) {
            return;
        }
        if (forge.isComplete()) {
            if (forge.hasDirectOutput()) {
                renderCompletedOutput(forge, display, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
            } else {
                renderItem(forge, display, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, layout.outputScale());
            }
            return;
        }

        int count = forge.materialCount();
        renderMaterialCopies(forge, display, count, layout.spread(), 0.0F, poseStack, bufferSource, packedLight, packedOverlay, layout);
    }

    private void renderStationWorkDisplay(ToolForgeBlockEntity forge, StationWorkRecipe recipe, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        StationWorkPositions positions = StationWorkPositions.forRecipe(recipe);
        boolean active = forge.looseWorkRecipe() != null;
        int placedPrimary = active ? Math.min(forge.materialCount(), recipe.input().count()) : 0;
        if (placedPrimary > 0) {
            ItemStack placed = forge.displayMaterialStack();
            if (!placed.isEmpty()) {
                renderMaterialCopiesAt(forge, placed, placedPrimary, layout.spread(), positions.primaryX(), positions.primaryZ(), poseStack, bufferSource, packedLight, packedOverlay, layout);
            }
        }
        int remainingPrimary = Math.max(0, recipe.input().count() - placedPrimary);
        ItemStack primaryPreview = stationInputPreview(forge, recipe.input(), partialTick);
        if (!primaryPreview.isEmpty() && remainingPrimary > 0) {
            renderGhostMaterials(forge, primaryPreview, remainingPrimary, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout, positions.primaryX(), positions.primaryZ());
        }

        recipe.secondaryInput().ifPresent(input -> {
            ItemStack placed = active ? forge.abrasiveStack() : ItemStack.EMPTY;
            int placedCount = placed.isEmpty() ? 0 : Math.min(placed.getCount(), input.count());
            if (placedCount > 0) {
                renderMaterialCopiesAt(forge, placed, placedCount, layout.spread(), positions.secondaryX(), positions.secondaryZ(), poseStack, bufferSource, packedLight, packedOverlay, layout);
            }
            int remaining = Math.max(0, input.count() - placedCount);
            ItemStack preview = stationInputPreview(forge, input, partialTick);
            if (!preview.isEmpty() && remaining > 0) {
                renderGhostMaterials(forge, preview, remaining, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout, positions.secondaryX(), positions.secondaryZ());
            }
        });

        renderStationReadyPartPreview(forge, recipe, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
    }

    private void renderStationReadyPartPreview(ToolForgeBlockEntity forge, StationWorkRecipe recipe, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        Level level = forge.getLevel();
        if (level == null
                || forge.looseWorkRecipe() == null
                || forge.needsLooseWorkInput()
                || forge.needsLooseWorkSecondary()
                || forge.hitCount() > 0) {
            return;
        }
        ItemStack preview = recipe.outputCopy();
        if (preview.isEmpty()) {
            return;
        }
        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;
        float time = level.getGameTime() + partialTick;
        renderFloatingItem(level, preview, time, facingRotation, poseStack, bufferSource, packedLight, packedOverlay, layout, 43);
    }

    private void renderCompletedOutput(ToolForgeBlockEntity forge, ItemStack output, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        Level level = forge.getLevel();
        if (level == null) {
            return;
        }
        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;
        float time = level.getGameTime() + partialTick;
        renderFloatingItem(level, output, time, facingRotation, poseStack, bufferSource, packedLight, packedOverlay, layout, 59);
    }

    private void renderLapidaryCoatingWork(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        renderAbrasive(forge, poseStack, bufferSource, packedLight, packedOverlay, layout);

        ItemStack base = forge.lapidaryCoatingBaseStack();
        if (!base.isEmpty()) {
            renderFlatItem(forge, base, poseStack, bufferSource, packedLight, packedOverlay, 0.0F, 0.13F, layout.outputScale(), layout.materialSurfaceY(), forge.displayRotationDegrees());
        }

        ItemStack preview = forge.lapidaryCoatingMaterialPreviewStack();
        int remaining = forge.remainingMaterials();
        if (!preview.isEmpty() && remaining > 0) {
            renderGhostMaterials(forge, preview, remaining, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, -0.17F);
        }

        if (forge.materialCount() > 0) {
            ItemStack placed = forge.displayMaterialStack();
            if (!placed.isEmpty()) {
                float spread = layout.spread() * (1.0F - forge.progress());
                renderMaterialCopies(forge, placed, forge.materialCount(), spread, -0.17F, poseStack, bufferSource, packedLight, packedOverlay, layout);
            }
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
            renderFlatItem(forge, stacks.get(0), poseStack, bufferSource, packedLight, packedOverlay, layout.originX(), layout.originZ(), layout.materialScale(), layout.materialSurfaceY(), forge.displayRotationDegrees());
            return;
        }
        float radius = count <= 4 ? 0.18F : 0.25F;
        float scale = count <= 4 ? layout.materialScale() : layout.centerMaterialScale();
        for (int index = 0; index < count; index++) {
            float angle = (float) (Math.PI * 2.0D * index / count);
            float localX = layout.originX() + (float) Math.cos(angle) * radius;
            float localZ = layout.originZ() + (float) Math.sin(angle) * radius;
            renderFlatItem(forge, stacks.get(index), poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, layout.materialSurfaceY(), forge.displayRotationDegrees() + index * 23.0F);
        }
    }

    private void renderToolmakerResultPreview(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        Level level = forge.getLevel();
        if (level == null) {
            return;
        }

        List<ItemStack> stacks = forge.benchStacks();
        if (stacks.size() <= 1 || stacks.stream().anyMatch(ToolmakerBenchAssembly::isFinishedTool)) {
            return;
        }

        ItemStack preview = ToolmakerBenchAssembly.assemble(stacks, level.registryAccess());
        if (preview.isEmpty()) {
            return;
        }

        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;
        float time = level.getGameTime() + partialTick;

        renderFloatingItem(level, preview, time, facingRotation, poseStack, bufferSource, packedLight, packedOverlay, layout, 13);
    }

    private void renderAnvilReadyPartPreview(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        Level level = forge.getLevel();
        ForgeTemplateDefinition template = forge.template();
        ResourceLocation material = forge.materialId();
        if (level == null
                || template == null
                || material == null
                || !forge.workstationKind().isSmithingAnvilLike()
                || forge.hitCount() > 0
                || !forge.canHammer()) {
            return;
        }

        ItemStack preview = ArmorForgeAttachment.isAttachmentTemplate(template) && forge.hasArmorAttachmentTarget()
                ? ArmorForgeAttachment.apply(forge.armorAttachmentTarget(), template.id(), material, forge.completedQualityScore())
                : template.outputStack(material, forge.completedQualityScore());
        if (preview.isEmpty()) {
            return;
        }

        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;
        float time = level.getGameTime() + partialTick;
        renderFloatingItem(level, preview, time, facingRotation, poseStack, bufferSource, packedLight, packedOverlay, layout, 29);
    }

    private void renderFloatingItem(Level level, ItemStack stack, float time, float facingRotation, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout, int seed) {
        float bob = (float) Math.sin(time * 0.16F) * 0.035F;
        float scale = fittedDisplayScale(stack, layout.resultScale());

        poseStack.pushPose();
        poseStack.translate(0.5F, layout.resultFloatY() + bob, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.25F - facingRotation));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, seed);
        poseStack.popPose();
    }

    private void renderTemplateMaterialRequirements(ToolForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        ForgeTemplateDefinition template = forge.template();
        if (template == null) {
            return;
        }
        int remaining = Math.max(0, template.requiredMaterials() - forge.materialCount());
        if (remaining <= 0) {
            return;
        }

        ResourceLocation material = forge.materialId() == null ? cycledPreviewMaterial(forge, template) : forge.materialId();
        if (material == null) {
            return;
        }
        ItemStack preview = MaterialCatalog.displayStack(material);
        if (preview.isEmpty()) {
            return;
        }

        renderGhostMaterials(forge, preview, remaining, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout);
    }

    private void renderGhostMaterials(ToolForgeBlockEntity forge, ItemStack stack, int count, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        renderGhostMaterials(forge, stack, count, partialTick, poseStack, bufferSource, packedLight, packedOverlay, layout, 0.0F, 0.0F);
    }

    private void renderGhostMaterials(ToolForgeBlockEntity forge, ItemStack stack, int count, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout, float originX, float originZ) {
        Level level = forge.getLevel();
        float time = level == null ? partialTick : level.getGameTime() + partialTick;
        int columns = count <= 3 ? count : Math.min(4, (int) Math.ceil(Math.sqrt(count)));
        int rows = (int) Math.ceil(count / (float) columns);
        float step = count <= 3 ? 0.18F : 0.15F;
        float startX = -(columns - 1) * step * 0.5F;
        float startZ = -(rows - 1) * step * 0.5F;
        float scale = count <= 3 ? layout.materialScale() * 0.74F : layout.centerMaterialScale() * 0.58F;

        for (int index = 0; index < count; index++) {
            int column = index % columns;
            int row = index / columns;
            float shimmer = (float) Math.sin(time * 0.18F + index * 0.9F) * 0.012F;
            float localX = layout.originX() + originX + startX + column * step;
            float localZ = layout.originZ() + originZ + startZ + row * step;
            float rotation = forge.displayRotationDegrees() + time * 0.45F + index * 29.0F;
            renderGhostFlatItem(forge, stack, poseStack, bufferSource, packedLight, packedOverlay, localX, localZ, scale, layout.previewSurfaceY() + 0.018F + shimmer, rotation, index);
        }
    }

    private void renderGhostFlatItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float localX, float localZ, float scale, float surfaceY, float localRotation, int seed) {
        Level level = forge.getLevel();
        BlockState state = forge.getBlockState();
        float facingRotation = state.hasProperty(ToolWorkstationBlock.FACING) ? state.getValue(ToolWorkstationBlock.FACING).toYRot() : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, surfaceY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingRotation));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(localRotation + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        scale = fittedDisplayScale(stack, scale);
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, new GhostBufferSource(bufferSource, GHOST_MATERIAL_ALPHA), level, 100 + seed);
        poseStack.popPose();
    }

    private void renderMaterialCopies(ToolForgeBlockEntity forge, ItemStack display, int count, float spread, float localZ, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        renderMaterialCopiesAt(forge, display, count, spread, 0.0F, localZ, poseStack, bufferSource, packedLight, packedOverlay, layout);
    }

    private void renderMaterialCopiesAt(ToolForgeBlockEntity forge, ItemStack display, int count, float spread, float originX, float originZ, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        if (count <= 0) {
            return;
        }
        if (count == 1) {
            renderMaterialCopyAt(forge, display, 0.0F, originX, originZ, layout.materialScale(), poseStack, bufferSource, packedLight, packedOverlay, layout);
        } else if (count == 2) {
            renderMaterialCopyAt(forge, display, -spread, originX, originZ, layout.materialScale(), poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderMaterialCopyAt(forge, display, spread, originX, originZ, layout.materialScale(), poseStack, bufferSource, packedLight, packedOverlay, layout);
        } else {
            renderMaterialCopyAt(forge, display, -spread, originX, originZ, layout.materialScale(), poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderMaterialCopyAt(forge, display, 0.0F, originX, originZ, layout.centerMaterialScale(), poseStack, bufferSource, packedLight, packedOverlay, layout);
            renderMaterialCopyAt(forge, display, spread, originX, originZ, layout.materialScale(), poseStack, bufferSource, packedLight, packedOverlay, layout);
        }
    }

    private void renderMaterialCopyAt(ToolForgeBlockEntity forge, ItemStack stack, float offset, float originX, float originZ, float scale, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout) {
        float localX = layout.originX() + originX + (layout.spreadAxis() == SpreadAxis.X ? offset : 0.0F);
        float z = layout.originZ() + originZ + (layout.spreadAxis() == SpreadAxis.Z ? offset : 0.0F);
        boolean qualityWindow = forge.isTimingQualityWindow();
        float surfaceY = qualityWindow ? layout.materialSurfaceY() + 0.018F : layout.materialSurfaceY();
        renderFlatItem(forge, stack, poseStack, bufferSource, packedLight, packedOverlay, localX, z, scale, surfaceY, forge.displayRotationDegrees());
    }

    private void renderItem(ToolForgeBlockEntity forge, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, DisplayLayout layout, float offset, float scale) {
        float localX = layout.originX() + (layout.spreadAxis() == SpreadAxis.X ? offset : 0.0F);
        float localZ = layout.originZ() + (layout.spreadAxis() == SpreadAxis.Z ? offset : 0.0F);
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
        scale = fittedDisplayScale(stack, scale);
        poseStack.scale(scale, scale, scale);
        float heat = level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
        if (heat > 0.02F) {
            HeatRenderUtil.renderHeatedItem(itemRenderer, stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, heat);
        } else {
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, 0);
        }
        poseStack.popPose();
    }

    private static ResourceLocation cycledPreviewMaterial(ToolForgeBlockEntity forge, ForgeTemplateDefinition template) {
        WorkstationKind kind = forge.workstationKind();
        List<ResourceLocation> materials = MaterialCatalog.starterMaterialIds().stream()
                .filter(template::allowsMaterial)
                .filter(material -> MaterialCatalog.definition(material)
                        .filter(definition -> definition.category() == kind.materialCategory())
                        .isPresent())
                .filter(material -> !ForgeTemplatePreview.stack(template, material).isEmpty())
                .toList();
        if (materials.isEmpty()) {
            return null;
        }

        Level level = forge.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        int index = (int) (gameTime / MATERIAL_PREVIEW_TICKS % materials.size());
        return materials.get(index);
    }

    private static StationWorkRecipe stationWorkRecipe(ToolForgeBlockEntity forge) {
        StationWorkRecipe active = forge.looseWorkRecipe();
        return active != null
                ? active
                : StationWorkRecipeRegistry.findPatternRecipe(forge.workstationKind(), forge.templateId()).orElse(null);
    }

    private static ItemStack stationInputPreview(ToolForgeBlockEntity forge, StationWorkRecipe.Input input, float partialTick) {
        List<ItemStack> stacks = stationInputStacks(input);
        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Level level = forge.getLevel();
        long previewTick = level == null ? (long) partialTick : level.getGameTime();
        int index = (int) (previewTick / MATERIAL_PREVIEW_TICKS % stacks.size());
        return stacks.get(index).copyWithCount(1);
    }

    private static List<ItemStack> stationInputStacks(StationWorkRecipe.Input input) {
        if (input.itemId().isPresent()) {
            Item item = BuiltInRegistries.ITEM.get(input.itemId().get());
            return item == Items.AIR ? List.of() : List.of(new ItemStack(item));
        }
        if (input.tag().isEmpty()) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(input.tag().get()).forEach(holder -> {
            if (holder.value() != Items.AIR) {
                stacks.add(new ItemStack(holder.value()));
            }
        });
        return stacks;
    }

    private static DisplayLayout layout(ToolForgeBlockEntity forge) {
        BlockState state = forge.getBlockState();
        if (state.getBlock() instanceof ToolWorkstationBlock workstation) {
            if (workstation.kind() == WorkstationKind.LAPIDARY_TABLE) {
                return DisplayLayout.LAPIDARY;
            }
            if (workstation.kind() == WorkstationKind.SAWMILL) {
                return DisplayLayout.SAWMILL;
            }
            if (workstation.kind() == WorkstationKind.LEATHER_STATION) {
                return DisplayLayout.LEATHER_STATION;
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

    private static float fittedDisplayScale(ItemStack stack, float scale) {
        if (ArmorForgeAttachment.isArmorStack(stack)) {
            return scale * ARMOR_FLAT_ITEM_SCALE;
        }
        ArmorPartData armorPart = stack.get(ModDataComponents.ARMOR_PART.get());
        if (armorPart != null) {
            return scale * ARMOR_PART_DISPLAY_SCALE;
        }
        ToolPartData toolPart = stack.get(ModDataComponents.TOOL_PART.get());
        if (toolPart == null) {
            return scale;
        }
        if (ToolPartData.CROSSBOW_BODY.equals(toolPart.partType()) || ToolPartData.CROSSBOW_LIMBS.equals(toolPart.partType())) {
            return scale * CROSSBOW_PART_DISPLAY_SCALE;
        }
        return scale * TOOL_PART_DISPLAY_SCALE;
    }

    private enum SpreadAxis {
        X,
        Z
    }

    private record StationWorkPositions(
            float primaryX,
            float primaryZ,
            float secondaryX,
            float secondaryZ
    ) {
        private static StationWorkPositions forRecipe(StationWorkRecipe recipe) {
            return recipe.requiresSecondaryInput()
                    ? new StationWorkPositions(-0.21F, -0.14F, 0.21F, -0.14F)
                    : new StationWorkPositions(0.0F, -0.13F, 0.0F, -0.13F);
        }
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
            float originX,
            float originZ,
            float abrasiveSurfaceY,
            float abrasiveScale,
            float abrasiveX,
            float abrasiveZ,
            float resultFloatY,
            float resultScale
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
                0.0F,
                0.0F,
                0.0F,
                1.08F,
                0.50F
        );
        private static final DisplayLayout LAPIDARY = new DisplayLayout(
                0.828F,
                0.820F,
                CAMPFIRE_ITEM_SCALE * 0.92F,
                CAMPFIRE_ITEM_SCALE * 0.88F,
                CAMPFIRE_ITEM_SCALE * 0.92F,
                CAMPFIRE_ITEM_SCALE * 0.92F,
                0.18F,
                SpreadAxis.X,
                0.0F,
                0.0F,
                0.830F,
                CAMPFIRE_ITEM_SCALE * 0.82F,
                0.0F,
                -0.31F,
                1.17F,
                0.52F
        );
        private static final DisplayLayout SAWMILL = new DisplayLayout(
                0.828F,
                0.820F,
                CAMPFIRE_ITEM_SCALE * 0.92F,
                CAMPFIRE_ITEM_SCALE * 0.86F,
                CAMPFIRE_ITEM_SCALE * 0.92F,
                CAMPFIRE_ITEM_SCALE * 0.92F,
                0.18F,
                SpreadAxis.X,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                1.15F,
                0.50F
        );
        private static final DisplayLayout LEATHER_STATION = new DisplayLayout(
                0.822F,
                0.814F,
                CAMPFIRE_ITEM_SCALE * 0.86F,
                CAMPFIRE_ITEM_SCALE * 0.74F,
                CAMPFIRE_ITEM_SCALE * 0.86F,
                CAMPFIRE_ITEM_SCALE * 0.86F,
                0.24F,
                SpreadAxis.X,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                1.16F,
                0.50F
        );
        private static final DisplayLayout TOOLMAKERS_BENCH = new DisplayLayout(
                0.828F,
                0.820F,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                CAMPFIRE_ITEM_SCALE,
                0.20F,
                SpreadAxis.X,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                1.18F,
                0.52F
        );
    }

    private record GhostBufferSource(MultiBufferSource delegate, float alpha) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new GhostVertexConsumer(delegate.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), alpha);
        }
    }

    private static class GhostVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;

        private GhostVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            int ghostAlpha = Math.min(alpha, Math.round(alpha * this.alpha));
            delegate.setColor(mix(red, 185, 0.18F), mix(green, 225, 0.18F), mix(blue, 255, 0.18F), ghostAlpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }

        private static int mix(int base, int target, float amount) {
            return Math.round(base + (target - base) * amount);
        }
    }
}

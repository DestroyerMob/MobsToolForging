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
import org.destroyermob.mobstoolforging.world.ForgeTemplatePreview;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class PatternRackRenderer implements BlockEntityRenderer<PatternRackBlockEntity> {
    private static final float ITEM_SCALE = 0.275F;
    private static final float ITEM_SURFACE_Z = 0.328F;
    private static final float[] SLOT_X = {-0.3125F, 0.0F, 0.3125F};
    private static final float[] SLOT_Y = {0.8125F, 0.5F, 0.1875F};

    private final ItemRenderer itemRenderer;

    public PatternRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PatternRackBlockEntity rack, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = rack.getBlockState();
        if (!state.hasProperty(PatternRackBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(PatternRackBlock.FACING);
        Level level = rack.getLevel();
        for (int slot = 0; slot < PatternRackBlockEntity.SLOT_COUNT; slot++) {
            ItemStack pattern = rack.patternStack(slot);
            if (pattern.isEmpty()) {
                continue;
            }
            int row = slot / 3;
            int column = slot % 3;
            RackPreview preview = previewStack(pattern);
            renderPattern(preview.stack(), poseStack, bufferSource, packedLight, packedOverlay, level, facing, SLOT_X[column] + preview.offsetX(), SLOT_Y[row] + preview.offsetY(), preview.scale());
        }
    }

    private static RackPreview previewStack(ItemStack pattern) {
        ForgeTemplateDefinition template = PatternRackBlockEntity.template(pattern).orElse(null);
        if (template == null) {
            return new RackPreview(pattern, 0.0F, 0.0F, ITEM_SCALE);
        }
        ItemStack preview = ForgeTemplatePreview.stack(template);
        RackAlignment alignment = alignment(template.partType());
        return new RackPreview(preview.isEmpty() ? pattern : preview, alignment.offsetX(), alignment.offsetY(), ITEM_SCALE * alignment.scale());
    }

    private void renderPattern(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Level level, Direction facing, float x, float y, float scale) {
        float modelX = 0.5F + x;
        float modelZ = 0.5F + ITEM_SURFACE_Z;
        float worldX = worldX(facing, modelX, modelZ);
        float worldZ = worldZ(facing, modelX, modelZ);

        poseStack.pushPose();
        poseStack.translate(worldX, y, worldZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(modelRotation(facing)));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
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

    private static RackAlignment alignment(String partType) {
        return switch (partType) {
            case ToolPartData.PICKAXE_HEAD -> new RackAlignment(-0.024F, -0.028F, 1.0F);
            case ToolPartData.AXE_HEAD -> new RackAlignment(-0.018F, -0.032F, 1.0F);
            case ToolPartData.SHOVEL_HEAD -> new RackAlignment(-0.020F, -0.036F, 1.0F);
            case ToolPartData.HOE_HEAD -> new RackAlignment(-0.018F, -0.040F, 1.0F);
            case ToolPartData.SWORD_BLADE -> new RackAlignment(-0.018F, -0.030F, 1.0F);
            case ToolPartData.SWORD_GUARD -> new RackAlignment(0.018F, 0.020F, 1.0F);
            case ToolPartData.SMITHING_HAMMER_HEAD -> new RackAlignment(-0.018F, -0.028F, 1.0F);
            case ToolPartData.SCREWDRIVER_HEAD, ToolPartData.GEM_CUTTERS_BLADE -> new RackAlignment(-0.014F, -0.022F, 1.0F);
            default -> new RackAlignment(0.0F, 0.0F, 1.0F);
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

    private record RackPreview(ItemStack stack, float offsetX, float offsetY, float scale) {
    }

    private record RackAlignment(float offsetX, float offsetY, float scale) {
    }
}

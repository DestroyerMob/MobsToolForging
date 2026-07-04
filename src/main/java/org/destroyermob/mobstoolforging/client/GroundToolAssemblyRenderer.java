package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.world.GroundToolAssemblyBlock;
import org.destroyermob.mobstoolforging.world.GroundToolAssemblyBlockEntity;

public class GroundToolAssemblyRenderer implements BlockEntityRenderer<GroundToolAssemblyBlockEntity> {
    private static final float SURFACE_Y = 0.08F;
    private static final float CAMPFIRE_ITEM_SCALE = 0.375F;

    private final ItemRenderer itemRenderer;

    public GroundToolAssemblyRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(GroundToolAssemblyBlockEntity assembly, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        List<ItemStack> stacks = assembly.stacks();
        int count = stacks.size();
        if (count == 0) {
            return;
        }
        float facingRotation = assembly.getBlockState().getValue(GroundToolAssemblyBlock.FACING).toYRot();
        if (count == 1) {
            renderFlatItem(assembly.getLevel(), stacks.getFirst(), poseStack, bufferSource, packedLight, packedOverlay, facingRotation, 0.0F, 0.0F, CAMPFIRE_ITEM_SCALE, 0);
            return;
        }
        float radius = count == 2 ? 0.23F : 0.27F;
        for (int index = 0; index < count; index++) {
            float angle = (float) (Math.PI * 2.0D * index / count);
            float localX = (float) Math.cos(angle) * radius;
            float localZ = (float) Math.sin(angle) * radius;
            renderFlatItem(assembly.getLevel(), stacks.get(index), poseStack, bufferSource, packedLight, packedOverlay, facingRotation, localX, localZ, CAMPFIRE_ITEM_SCALE, index);
        }
    }

    private void renderFlatItem(Level level, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float facingRotation, float localX, float localZ, float scale, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, SURFACE_Y, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingRotation));
        poseStack.translate(localX, 0.0F, localZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, seed);
        poseStack.popPose();
    }
}

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
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlock;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlockEntity;

public class KnappingFlintRenderer implements BlockEntityRenderer<KnappingFlintBlockEntity> {
    private final ItemRenderer itemRenderer;

    public KnappingFlintRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(KnappingFlintBlockEntity knapping, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float facingRotation = knapping.getBlockState().getValue(KnappingFlintBlock.FACING).toYRot();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.11F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.96F, 0.96F, 0.96F);
        itemRenderer.renderStatic(new ItemStack(Items.FLINT), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, bufferSource, knapping.getLevel(), 0);
        poseStack.popPose();
    }
}

package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.destroyermob.mobstoolforging.world.CrucibleBlockEntity;
import org.destroyermob.mobstoolforging.world.CrucibleContents;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    private final CrucibleContentsRenderer contentsRenderer = new CrucibleContentsRenderer();

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrucibleBlockEntity crucible, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        CrucibleContents contents = crucible.contents();
        if (contents.isEmpty()) {
            return;
        }
        float meltProgress = contents.hasMoltenMaterial() ? 1.0F : contents.heat();
        contentsRenderer.renderHeatGlow(contents, false, partialTick, poseStack, bufferSource, packedOverlay, crucible.getLevel());
        contentsRenderer.renderContents(contents, meltProgress, partialTick, poseStack, bufferSource, packedLight, packedOverlay, crucible.getLevel());
    }
}

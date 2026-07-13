package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ModularHelmetModel;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorPartData;

public final class ModularHelmetLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ModularHelmetModel helmetModel;

    public ModularHelmetLayer(RenderLayerParent<T, M> renderer, ModularHelmetModel helmetModel) {
        super(renderer);
        this.helmetModel = helmetModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (livingEntity.isInvisible() || !(getParentModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.HELMET_TYPE.equals(construction.armorType())) {
            return;
        }

        poseStack.pushPose();
        humanoidModel.head.translateAndRotate(poseStack);
        renderParts(poseStack, bufferSource, packedLight, construction);
        renderTrim(poseStack, bufferSource, packedLight, stack, construction);
        if (stack.hasFoil()) {
            renderGlint(poseStack, bufferSource, packedLight, construction);
        }
        poseStack.popPose();
    }

    private void renderParts(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction) {
        ArmorMaterialTextureManager.WornArmorTexture chainmail = ArmorMaterialTextureManager.INSTANCE.wornChainmailTexture(construction.helmetChainmailMaterial(), ArmorPartData.HELMET_CHAINMAIL);
        renderChainmail(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(chainmail.texture())), chainmail.color(), packedLight);
        construction.helmetPlateMaterial().ifPresent(material -> {
            ArmorMaterialTextureManager.WornArmorTexture texture = ArmorMaterialTextureManager.INSTANCE.wornMaterialTexture(material, ArmorPartData.HELMET_PLATE);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture.texture()));
            helmetModel.renderMaterial(poseStack, consumer, texture.color(), packedLight, OverlayTexture.NO_OVERLAY);
        });
    }

    private void renderTrim(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack, ArmorConstructionData construction) {
        VertexConsumer trim = ModularArmorTrimRenderer.consumer(stack, bufferSource, false);
        if (trim == null) {
            return;
        }
        helmetModel.renderTrim(
                poseStack,
                trim,
                construction.helmetPlateMaterial().isPresent(),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction) {
        VertexConsumer glint = bufferSource.getBuffer(RenderType.armorEntityGlint());
        renderChainmail(poseStack, glint, 0xFFFFFFFF, packedLight);
        construction.helmetPlateMaterial().ifPresent(material -> {
            helmetModel.renderMaterial(poseStack, glint, 0xFFFFFFFF, packedLight, OverlayTexture.NO_OVERLAY);
        });
    }

    private void renderChainmail(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight) {
        helmetModel.renderChainmail(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
    }
}

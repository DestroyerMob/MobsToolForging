package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ModularBodyArmourModel;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorPartData;

public final class ModularBodyArmourLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ModularBodyArmourModel bodyArmourModel;

    public ModularBodyArmourLayer(RenderLayerParent<T, M> renderer, ModularBodyArmourModel bodyArmourModel) {
        super(renderer);
        this.bodyArmourModel = bodyArmourModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (livingEntity.isInvisible() || !(getParentModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.CHESTPLATE_TYPE.equals(construction.armorType())) {
            return;
        }

        renderChainmailLayer(poseStack, bufferSource, packedLight, construction, humanoidModel);
        renderLayer(poseStack, bufferSource, packedLight, humanoidModel, construction);
        if (stack.hasFoil()) {
            VertexConsumer glint = bufferSource.getBuffer(RenderType.armorEntityGlint());
            renderChainmailLayer(poseStack, glint, 0xFFFFFFFF, packedLight, humanoidModel);
            renderLayer(poseStack, glint, 0xFFFFFFFF, packedLight, humanoidModel, construction);
        }
    }

    private void renderChainmailLayer(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction, HumanoidModel<?> humanoidModel) {
        ArmorMaterialTextureManager.WornArmorTexture chainmail = ArmorMaterialTextureManager.INSTANCE.wornChainmailTexture(construction.chestplateChainmailMaterial(), ArmorPartData.CHESTPLATE_CHAINMAIL);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(chainmail.texture()));
        renderChainmailLayer(poseStack, consumer, chainmail.color(), packedLight, humanoidModel);
    }

    private void renderChainmailLayer(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, HumanoidModel<?> humanoidModel) {
        renderChainmailBodyPart(poseStack, humanoidModel.body, consumer, color, packedLight);
        renderChainmailRightArmPart(poseStack, humanoidModel.rightArm, consumer, color, packedLight);
        renderChainmailLeftArmPart(poseStack, humanoidModel.leftArm, consumer, color, packedLight);
    }

    private void renderLayer(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HumanoidModel<?> humanoidModel, ArmorConstructionData construction) {
        construction.chestplatePlateMaterial().ifPresent(material -> {
            ArmorMaterialTextureManager.WornArmorTexture texture = ArmorMaterialTextureManager.INSTANCE.wornMaterialTexture(material, ArmorPartData.CHESTPLATE_BODY);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture.texture()));
            renderLayer(poseStack, consumer, texture.color(), packedLight, humanoidModel, construction);
        });
    }

    private void renderLayer(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, HumanoidModel<?> humanoidModel, ArmorConstructionData construction) {
        if (construction.chestplatePlateMaterial().isEmpty()) {
            return;
        }
        renderBodyPart(poseStack, humanoidModel.body, consumer, color, packedLight);
        renderRightArmPart(poseStack, humanoidModel.rightArm, consumer, color, packedLight);
        renderLeftArmPart(poseStack, humanoidModel.leftArm, consumer, color, packedLight);
    }

    private void renderChainmailBodyPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderChainmailBody(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailRightArmPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderChainmailRightArm(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailLeftArmPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderChainmailLeftArm(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderBodyPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderBody(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderRightArmPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderRightArm(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderLeftArmPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderLeftArm(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

}

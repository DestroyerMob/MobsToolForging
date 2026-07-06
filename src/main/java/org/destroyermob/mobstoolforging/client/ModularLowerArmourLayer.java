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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ModularLowerArmourModel;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorPartData;

public final class ModularLowerArmourLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ModularLowerArmourModel lowerArmourModel;

    public ModularLowerArmourLayer(RenderLayerParent<T, M> renderer, ModularLowerArmourModel lowerArmourModel) {
        super(renderer);
        this.lowerArmourModel = lowerArmourModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (livingEntity.isInvisible() || !(getParentModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }
        renderLeggingsSlot(poseStack, bufferSource, packedLight, humanoidModel, livingEntity);
        renderBootsSlot(poseStack, bufferSource, packedLight, humanoidModel, livingEntity);
    }

    private void renderLeggingsSlot(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HumanoidModel<?> humanoidModel, T livingEntity) {
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.LEGGINGS_TYPE.equals(construction.armorType())) {
            return;
        }
        renderLeggingsParts(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), packedLight, construction);
        if (stack.hasFoil()) {
            renderLeggingsParts(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, construction);
        }
    }

    private void renderBootsSlot(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HumanoidModel<?> humanoidModel, T livingEntity) {
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.BOOTS_TYPE.equals(construction.armorType())) {
            return;
        }
        renderBootsParts(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), packedLight, construction);
        if (stack.hasFoil()) {
            renderBootsParts(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, construction);
        }
    }

    private void renderLeggingsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, VertexConsumer consumer, int packedLight, ArmorConstructionData construction) {
        TextureAtlasSprite chainmailSprite = ArmorMaterialTextureManager.INSTANCE.wornChainmailSprite(ArmorPartData.LEGGINGS_CHAINMAIL);
        renderChainmailRightLeggings(poseStack, humanoidModel.rightLeg, consumer, chainmailSprite, packedLight);
        renderChainmailLeftLeggings(poseStack, humanoidModel.leftLeg, consumer, chainmailSprite, packedLight);
        construction.leggingsPlateMaterial().ifPresent(material -> {
            ArmorMaterialTextureManager.ResolvedArmorTexture texture = ArmorMaterialTextureManager.INSTANCE.wornMaterialTexture(material, ArmorPartData.LEGGINGS_PLATE);
            renderRightLeggings(poseStack, humanoidModel.rightLeg, consumer, texture, packedLight);
            renderLeftLeggings(poseStack, humanoidModel.leftLeg, consumer, texture, packedLight);
        });
    }

    private void renderBootsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, VertexConsumer consumer, int packedLight, ArmorConstructionData construction) {
        TextureAtlasSprite chainmailSprite = ArmorMaterialTextureManager.INSTANCE.wornChainmailSprite(ArmorPartData.BOOTS_CHAINMAIL);
        renderChainmailRightBoot(poseStack, humanoidModel.rightLeg, consumer, chainmailSprite, packedLight);
        renderChainmailLeftBoot(poseStack, humanoidModel.leftLeg, consumer, chainmailSprite, packedLight);
        construction.bootsPlateMaterial().ifPresent(material -> {
            ArmorMaterialTextureManager.ResolvedArmorTexture texture = ArmorMaterialTextureManager.INSTANCE.wornMaterialTexture(material, ArmorPartData.BOOTS_PLATE);
            renderRightBoot(poseStack, humanoidModel.rightLeg, consumer, texture, packedLight);
            renderLeftBoot(poseStack, humanoidModel.leftLeg, consumer, texture, packedLight);
        });
    }

    private void renderChainmailRightLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailRightLeggings(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailLeftLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailLeftLeggings(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderRightLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, ArmorMaterialTextureManager.ResolvedArmorTexture texture, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderRightLeggings(poseStack, consumer, texture.sprite(), texture.color(), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderLeftLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, ArmorMaterialTextureManager.ResolvedArmorTexture texture, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderLeftLeggings(poseStack, consumer, texture.sprite(), texture.color(), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailRightBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailRightBoot(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailLeftBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailLeftBoot(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderRightBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, ArmorMaterialTextureManager.ResolvedArmorTexture texture, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderRightBoot(poseStack, consumer, texture.sprite(), texture.color(), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderLeftBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, ArmorMaterialTextureManager.ResolvedArmorTexture texture, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderLeftBoot(poseStack, consumer, texture.sprite(), texture.color(), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

}

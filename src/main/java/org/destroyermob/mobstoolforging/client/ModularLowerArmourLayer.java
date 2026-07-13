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
        renderLeggingsParts(poseStack, humanoidModel, bufferSource, packedLight, construction);
        renderLeggingsTrim(poseStack, humanoidModel, bufferSource, packedLight, stack, construction);
        if (stack.hasFoil()) {
            renderLeggingsParts(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.armorEntityGlint()), 0xFFFFFFFF, packedLight, construction);
        }
    }

    private void renderBootsSlot(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HumanoidModel<?> humanoidModel, T livingEntity) {
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.BOOTS_TYPE.equals(construction.armorType())) {
            return;
        }
        renderBootsParts(poseStack, humanoidModel, bufferSource, packedLight, construction);
        renderBootsTrim(poseStack, humanoidModel, bufferSource, packedLight, stack, construction);
        if (stack.hasFoil()) {
            renderBootsParts(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.armorEntityGlint()), 0xFFFFFFFF, packedLight, construction);
        }
    }

    private void renderLeggingsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction) {
        ArmorMaterialTextureManager.WornArmorTexture chainmail = ArmorMaterialTextureManager.INSTANCE.wornChainmailTexture(construction.leggingsChainmailMaterial(), ArmorPartData.LEGGINGS_CHAINMAIL);
        VertexConsumer chainmailConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(chainmail.texture()));
        renderChainmailRightLeggings(poseStack, humanoidModel.rightLeg, chainmailConsumer, chainmail.color(), packedLight);
        renderChainmailLeftLeggings(poseStack, humanoidModel.leftLeg, chainmailConsumer, chainmail.color(), packedLight);
        construction.leggingsPlateMaterial().ifPresent(material -> {
            ArmorMaterialTextureManager.WornArmorTexture texture = ArmorMaterialTextureManager.INSTANCE.wornMaterialTexture(material, ArmorPartData.LEGGINGS_PLATE);
            VertexConsumer materialConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture.texture()));
            renderRightLeggings(poseStack, humanoidModel.rightLeg, materialConsumer, texture.color(), packedLight);
            renderLeftLeggings(poseStack, humanoidModel.leftLeg, materialConsumer, texture.color(), packedLight);
        });
    }

    private void renderLeggingsTrim(PoseStack poseStack, HumanoidModel<?> humanoidModel, MultiBufferSource bufferSource, int packedLight, ItemStack stack, ArmorConstructionData construction) {
        VertexConsumer trim = ModularArmorTrimRenderer.consumer(stack, bufferSource, true);
        if (trim == null) {
            return;
        }
        boolean plated = construction.leggingsPlateMaterial().isPresent();
        renderTrimRightLeggings(poseStack, humanoidModel.rightLeg, trim, plated, packedLight);
        renderTrimLeftLeggings(poseStack, humanoidModel.leftLeg, trim, plated, packedLight);
    }

    private void renderLeggingsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, VertexConsumer consumer, int color, int packedLight, ArmorConstructionData construction) {
        renderChainmailRightLeggings(poseStack, humanoidModel.rightLeg, consumer, color, packedLight);
        renderChainmailLeftLeggings(poseStack, humanoidModel.leftLeg, consumer, color, packedLight);
        if (construction.leggingsPlateMaterial().isPresent()) {
            renderRightLeggings(poseStack, humanoidModel.rightLeg, consumer, color, packedLight);
            renderLeftLeggings(poseStack, humanoidModel.leftLeg, consumer, color, packedLight);
        }
    }

    private void renderBootsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction) {
        ArmorMaterialTextureManager.WornArmorTexture chainmail = ArmorMaterialTextureManager.INSTANCE.wornChainmailTexture(construction.bootsChainmailMaterial(), ArmorPartData.BOOTS_CHAINMAIL);
        VertexConsumer chainmailConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(chainmail.texture()));
        renderChainmailRightBoot(poseStack, humanoidModel.rightLeg, chainmailConsumer, chainmail.color(), packedLight);
        renderChainmailLeftBoot(poseStack, humanoidModel.leftLeg, chainmailConsumer, chainmail.color(), packedLight);
        construction.bootsPlateMaterial().ifPresent(material -> {
            ArmorMaterialTextureManager.WornArmorTexture texture = ArmorMaterialTextureManager.INSTANCE.wornMaterialTexture(material, ArmorPartData.BOOTS_PLATE);
            VertexConsumer materialConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture.texture()));
            renderRightBoot(poseStack, humanoidModel.rightLeg, materialConsumer, texture.color(), packedLight);
            renderLeftBoot(poseStack, humanoidModel.leftLeg, materialConsumer, texture.color(), packedLight);
        });
    }

    private void renderBootsTrim(PoseStack poseStack, HumanoidModel<?> humanoidModel, MultiBufferSource bufferSource, int packedLight, ItemStack stack, ArmorConstructionData construction) {
        VertexConsumer trim = ModularArmorTrimRenderer.consumer(stack, bufferSource, false);
        if (trim == null) {
            return;
        }
        boolean plated = construction.bootsPlateMaterial().isPresent();
        renderTrimRightBoot(poseStack, humanoidModel.rightLeg, trim, plated, packedLight);
        renderTrimLeftBoot(poseStack, humanoidModel.leftLeg, trim, plated, packedLight);
    }

    private void renderBootsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, VertexConsumer consumer, int color, int packedLight, ArmorConstructionData construction) {
        renderChainmailRightBoot(poseStack, humanoidModel.rightLeg, consumer, color, packedLight);
        renderChainmailLeftBoot(poseStack, humanoidModel.leftLeg, consumer, color, packedLight);
        if (construction.bootsPlateMaterial().isPresent()) {
            renderRightBoot(poseStack, humanoidModel.rightLeg, consumer, color, packedLight);
            renderLeftBoot(poseStack, humanoidModel.leftLeg, consumer, color, packedLight);
        }
    }

    private void renderChainmailRightLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailRightLeggings(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailLeftLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailLeftLeggings(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderRightLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderRightLeggings(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderLeftLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderLeftLeggings(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailRightBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailRightBoot(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderChainmailLeftBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderChainmailLeftBoot(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderRightBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderRightBoot(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderLeftBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int color, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderLeftBoot(poseStack, consumer, color, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderTrimRightLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, boolean plated, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderTrimRightLeggings(poseStack, consumer, plated, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderTrimLeftLeggings(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, boolean plated, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderTrimLeftLeggings(poseStack, consumer, plated, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderTrimRightBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, boolean plated, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderTrimRightBoot(poseStack, consumer, plated, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderTrimLeftBoot(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, boolean plated, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        lowerArmourModel.renderTrimLeftBoot(poseStack, consumer, plated, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

}

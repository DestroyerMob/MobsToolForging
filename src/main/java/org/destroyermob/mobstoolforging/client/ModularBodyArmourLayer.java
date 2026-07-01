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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ModularBodyArmourModel;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;

public final class ModularBodyArmourLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation VANILLA_CHAINMAIL_ARMOR_TEXTURE = ResourceLocation.withDefaultNamespace("textures/models/armor/chainmail_layer_1.png");
    private final ModularBodyArmourModel bodyArmourModel;
    private final HumanoidModel<?> chainmailModel;

    public ModularBodyArmourLayer(RenderLayerParent<T, M> renderer, ModularBodyArmourModel bodyArmourModel, HumanoidModel<?> chainmailModel) {
        super(renderer);
        this.bodyArmourModel = bodyArmourModel;
        this.chainmailModel = chainmailModel;
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

        renderVanillaChainmail(poseStack, bufferSource.getBuffer(RenderType.armorCutoutNoCull(VANILLA_CHAINMAIL_ARMOR_TEXTURE)), packedLight, humanoidModel);
        renderLayer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), packedLight, humanoidModel, construction);
        if (stack.hasFoil()) {
            renderVanillaChainmail(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, humanoidModel);
            renderLayer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, humanoidModel, construction);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderVanillaChainmail(PoseStack poseStack, VertexConsumer consumer, int packedLight, HumanoidModel<?> humanoidModel) {
        ((HumanoidModel) humanoidModel).copyPropertiesTo((HumanoidModel) chainmailModel);
        chainmailModel.setAllVisible(false);
        chainmailModel.body.visible = true;
        chainmailModel.rightArm.visible = true;
        chainmailModel.leftArm.visible = true;
        chainmailModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderLayer(PoseStack poseStack, VertexConsumer consumer, int packedLight, HumanoidModel<?> humanoidModel, ArmorConstructionData construction) {
        construction.chestplatePlateMaterial().ifPresent(material -> {
            TextureAtlasSprite bodySprite = sprite(material);
            renderBodyPart(poseStack, humanoidModel.body, consumer, bodySprite, packedLight);
            renderRightArmPart(poseStack, humanoidModel.rightArm, consumer, bodySprite, packedLight);
            renderLeftArmPart(poseStack, humanoidModel.leftArm, consumer, bodySprite, packedLight);
        });
    }

    private void renderBodyPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderBody(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderRightArmPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderRightArm(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderLeftArmPart(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        bodyArmourModel.renderLeftArm(poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private TextureAtlasSprite sprite(ResourceLocation materialId) {
        return ArmorMaterialTextureManager.INSTANCE.sprite(materialId);
    }
}

package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ModularLowerArmourGeometry;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;

public final class ModularLowerArmourLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public ModularLowerArmourLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (livingEntity.isInvisible() || !(getParentModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }
        renderLeggingsSlot(poseStack, bufferSource, packedLight, humanoidModel, livingEntity);
        renderSlot(poseStack, bufferSource, packedLight, humanoidModel, livingEntity, EquipmentSlot.FEET, ArmorConstructionData.BOOTS_TYPE, ModularLowerArmourGeometry.BOOTS);
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

    private void renderSlot(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HumanoidModel<?> humanoidModel, T livingEntity, EquipmentSlot slot, ResourceLocation armorType, List<ModularLowerArmourGeometry.Cuboid> cuboids) {
        ItemStack stack = livingEntity.getItemBySlot(slot);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !armorType.equals(construction.armorType())) {
            return;
        }
        TextureAtlasSprite sprite = sprite(construction.skullMaterial());
        renderLegs(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), packedLight, cuboids, sprite);
        if (stack.hasFoil()) {
            renderLegs(poseStack, humanoidModel, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, cuboids, sprite);
        }
    }

    private void renderLeggingsParts(PoseStack poseStack, HumanoidModel<?> humanoidModel, VertexConsumer consumer, int packedLight, ArmorConstructionData construction) {
        renderLegs(poseStack, humanoidModel, consumer, packedLight, ModularLowerArmourGeometry.LEGGING_LEGS, sprite(construction.skullMaterial()));
        construction.combMaterial().ifPresent(material -> renderLegs(poseStack, humanoidModel, consumer, packedLight, ModularLowerArmourGeometry.LEGGING_KNEES, sprite(material)));
        construction.visorMaterial().ifPresent(material -> renderBody(poseStack, humanoidModel.body, consumer, packedLight, ModularLowerArmourGeometry.LEGGING_TASSETS, sprite(material)));
    }

    private void renderLegs(PoseStack poseStack, HumanoidModel<?> humanoidModel, VertexConsumer consumer, int packedLight, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        renderLeg(poseStack, humanoidModel.rightLeg, consumer, packedLight, cuboids, sprite, ModularLowerArmourGeometry.LegSide.RIGHT);
        renderLeg(poseStack, humanoidModel.leftLeg, consumer, packedLight, cuboids, sprite, ModularLowerArmourGeometry.LegSide.LEFT);
    }

    private void renderLeg(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int packedLight, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite, ModularLowerArmourGeometry.LegSide side) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        ModularLowerArmourGeometry.renderLegCuboids(cuboids, side, parentPart.x, parentPart.y, parentPart.z, poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderBody(PoseStack poseStack, ModelPart parentPart, VertexConsumer consumer, int packedLight, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        poseStack.pushPose();
        parentPart.translateAndRotate(poseStack);
        ModularLowerArmourGeometry.renderBodyCuboids(cuboids, parentPart.x, parentPart.y, parentPart.z, poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private TextureAtlasSprite sprite(ResourceLocation materialId) {
        return ArmorMaterialTextureManager.INSTANCE.sprite(materialId);
    }
}

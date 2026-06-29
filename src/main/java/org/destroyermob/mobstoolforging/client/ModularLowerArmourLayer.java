package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
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
        if (livingEntity.isInvisible() || !(getParentModel() instanceof HumanoidModel<?>)) {
            return;
        }
        renderSlot(poseStack, bufferSource, packedLight, livingEntity, EquipmentSlot.LEGS, ArmorConstructionData.LEGGINGS_TYPE, ModularLowerArmourGeometry.LEGGINGS);
        renderSlot(poseStack, bufferSource, packedLight, livingEntity, EquipmentSlot.FEET, ArmorConstructionData.BOOTS_TYPE, ModularLowerArmourGeometry.BOOTS);
    }

    private void renderSlot(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, EquipmentSlot slot, ResourceLocation armorType, List<ModularLowerArmourGeometry.Cuboid> cuboids) {
        ItemStack stack = livingEntity.getItemBySlot(slot);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !armorType.equals(construction.armorType())) {
            return;
        }
        TextureAtlasSprite sprite = sprite(construction.skullMaterial());
        renderCuboids(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), packedLight, cuboids, sprite);
        if (stack.hasFoil()) {
            renderCuboids(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, cuboids, sprite);
        }
    }

    private void renderCuboids(PoseStack poseStack, VertexConsumer consumer, int packedLight, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        ModularLowerArmourGeometry.renderCuboids(cuboids, poseStack, consumer, sprite, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private TextureAtlasSprite sprite(ResourceLocation materialId) {
        return ArmorMaterialTextureManager.INSTANCE.sprite(materialId);
    }
}

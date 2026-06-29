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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ModularHelmetModel;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;

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
        if (stack.hasFoil()) {
            renderGlint(poseStack, bufferSource, packedLight, construction);
        }
        poseStack.popPose();
    }

    private void renderParts(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction) {
        renderSkull(poseStack, spriteConsumer(bufferSource, construction.skullMaterial()), packedLight);
        construction.combMaterial().ifPresent(material -> helmetModel.renderComb(poseStack, spriteConsumer(bufferSource, material), packedLight, OverlayTexture.NO_OVERLAY));
        construction.visorMaterial().ifPresent(material -> helmetModel.renderVisor(poseStack, spriteConsumer(bufferSource, material), packedLight, OverlayTexture.NO_OVERLAY));
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorConstructionData construction) {
        VertexConsumer glint = bufferSource.getBuffer(RenderType.armorEntityGlint());
        renderSkull(poseStack, glint, packedLight);
        if (construction.combMaterial().isPresent()) {
            helmetModel.renderComb(poseStack, glint, packedLight, OverlayTexture.NO_OVERLAY);
        }
        if (construction.visorMaterial().isPresent()) {
            helmetModel.renderVisor(poseStack, glint, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    private void renderSkull(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        helmetModel.renderSkull(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private VertexConsumer spriteConsumer(MultiBufferSource bufferSource, ResourceLocation materialId) {
        return ArmorMaterialTextureManager.INSTANCE.sprite(materialId)
                .wrap(bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)));
    }
}

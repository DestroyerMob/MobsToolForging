package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;

public final class ModularCapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final float MODULAR_CHESTPLATE_CAPE_OFFSET = 0.25F;

    public ModularCapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!usesOffsetCape(player) || player.isInvisible() || !player.isModelPartShown(PlayerModelPart.CAPE)) {
            return;
        }
        PlayerSkin skin = player.getSkin();
        ResourceLocation capeTexture = skin.capeTexture();
        if (capeTexture == null) {
            return;
        }
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.is(Items.ELYTRA)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, MODULAR_CHESTPLATE_CAPE_OFFSET);
        applyCapeMotion(poseStack, player, partialTick);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(capeTexture));
        getParentModel().renderCloak(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    public static boolean usesOffsetCape(AbstractClientPlayer player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        ArmorConstructionData construction = chestStack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        return construction != null && ArmorConstructionData.CHESTPLATE_TYPE.equals(construction.armorType());
    }

    private static void applyCapeMotion(PoseStack poseStack, AbstractClientPlayer player, float partialTick) {
        double cloakX = Mth.lerp(partialTick, player.xCloakO, player.xCloak) - Mth.lerp(partialTick, player.xo, player.getX());
        double cloakY = Mth.lerp(partialTick, player.yCloakO, player.yCloak) - Mth.lerp(partialTick, player.yo, player.getY());
        double cloakZ = Mth.lerp(partialTick, player.zCloakO, player.zCloak) - Mth.lerp(partialTick, player.zo, player.getZ());
        float bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double bodySin = Mth.sin(bodyRot * Mth.DEG_TO_RAD);
        double bodyCos = -Mth.cos(bodyRot * Mth.DEG_TO_RAD);
        float verticalMotion = Mth.clamp((float) cloakY * 10.0F, -6.0F, 32.0F);
        float backMotion = Mth.clamp((float) (cloakX * bodySin + cloakZ * bodyCos) * 100.0F, 0.0F, 150.0F);
        float sideMotion = Mth.clamp((float) (cloakX * bodyCos - cloakZ * bodySin) * 100.0F, -20.0F, 20.0F);
        if (backMotion < 0.0F) {
            backMotion = 0.0F;
        }
        float bob = Mth.lerp(partialTick, player.oBob, player.bob);
        verticalMotion += Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * bob;
        if (player.isCrouching()) {
            verticalMotion += 25.0F;
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + backMotion / 2.0F + verticalMotion));
        poseStack.mulPose(Axis.ZP.rotationDegrees(sideMotion / 2.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - sideMotion / 2.0F));
    }
}

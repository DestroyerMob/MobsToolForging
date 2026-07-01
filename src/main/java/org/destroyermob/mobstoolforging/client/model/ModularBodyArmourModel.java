package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class ModularBodyArmourModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "modular_body_armour"), "main");

    public ModularBodyArmourModel(ModelPart root) {
    }

    public void renderChainmailBody(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        ModularBodyArmourGeometry.renderCuboids(ModularBodyArmourGeometry.CHAINMAIL_BODY, poseStack, consumer, sprite, packedLight, packedOverlay);
    }

    public void renderChainmailRightArm(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        ModularBodyArmourGeometry.renderCuboids(ModularBodyArmourGeometry.CHAINMAIL_RIGHT_ARM, poseStack, consumer, sprite, packedLight, packedOverlay);
    }

    public void renderChainmailLeftArm(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        ModularBodyArmourGeometry.renderCuboids(ModularBodyArmourGeometry.CHAINMAIL_LEFT_ARM, poseStack, consumer, sprite, packedLight, packedOverlay);
    }

    public void renderBody(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        ModularBodyArmourGeometry.renderCuboids(ModularBodyArmourGeometry.BODY, poseStack, consumer, sprite, packedLight, packedOverlay);
    }

    public void renderRightArm(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        ModularBodyArmourGeometry.renderCuboids(ModularBodyArmourGeometry.RIGHT_ARM, poseStack, consumer, sprite, packedLight, packedOverlay);
    }

    public void renderLeftArm(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        ModularBodyArmourGeometry.renderCuboids(ModularBodyArmourGeometry.LEFT_ARM, poseStack, consumer, sprite, packedLight, packedOverlay);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}

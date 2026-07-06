package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class ModularHelmetModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "modular_helmet"), "main");
    public static final ModelLayerLocation BLANK_ARMOR_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "blank_modular_armor"), "main");
    private static final String CHAINMAIL_HELMET = "ChainmailHelmet";
    private static final String MATERIAL_HELMET = "MaterialHelmet";
    private final ModelPart chainmailHelmet;
    private final ModelPart materialHelmet;

    public ModularHelmetModel(ModelPart root) {
        this.chainmailHelmet = root.getChild(CHAINMAIL_HELMET);
        this.materialHelmet = root.getChild(MATERIAL_HELMET);
    }

    public void renderChainmail(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        chainmailHelmet.render(poseStack, consumer, packedLight, packedOverlay);
    }

    public void renderMaterial(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        renderMaterial(poseStack, consumer, 0xFFFFFFFF, packedLight, packedOverlay);
    }

    public void renderMaterial(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        materialHelmet.render(poseStack, new TintingVertexConsumer(consumer, color), packedLight, packedOverlay);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        PartPose headLocalPose = PartPose.offset(0.0F, 2.0F, 5.0F);
        CubeDeformation chainmailDeformation = new CubeDeformation(0.05F);
        CubeDeformation materialDeformation = new CubeDeformation(0.12F);

        root.addOrReplaceChild(CHAINMAIL_HELMET, CubeListBuilder.create()
                .texOffs(4, 8).addBox(-5.0F, -3.0F, -8.0F, 0.0F, 1.0F, 6.0F, chainmailDeformation)
                .texOffs(0, 4).addBox(-5.0F, -5.0F, -10.0F, 0.0F, 2.0F, 10.0F, chainmailDeformation)
                .texOffs(10, 14).addBox(-5.0F, -6.0F, 0.0F, 10.0F, 2.0F, 0.0F, chainmailDeformation)
                .texOffs(10, 14).addBox(-5.0F, -6.0F, -10.0F, 10.0F, 2.0F, 0.0F, chainmailDeformation)
                .texOffs(10, 14).addBox(-5.0F, -4.0F, -10.0F, 5.0F, 1.0F, 0.0F, chainmailDeformation)
                .texOffs(10, 14).addBox(-5.0F, -4.0F, 0.0F, 5.0F, 1.0F, 0.0F, chainmailDeformation), headLocalPose);

        root.addOrReplaceChild(MATERIAL_HELMET, CubeListBuilder.create()
                .texOffs(-6, 2).addBox(-5.0F, -10.0F, 0.0F, 10.0F, 4.0F, 0.0F, materialDeformation)
                .texOffs(-16, -8).addBox(-5.0F, -10.0F, -10.0F, 0.0F, 5.0F, 10.0F, materialDeformation)
                .texOffs(0, -6).addBox(5.0F, -10.0F, -10.0F, 0.0F, 3.0F, 10.0F, materialDeformation)
                .texOffs(-10, 0).addBox(-5.0F, -10.0F, -10.0F, 10.0F, 0.0F, 10.0F, materialDeformation)
                .texOffs(0, 0).addBox(-5.0F, -10.0F, -10.0F, 10.0F, 4.0F, 0.0F, materialDeformation), headLocalPose);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public static LayerDefinition createBlankArmorLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}

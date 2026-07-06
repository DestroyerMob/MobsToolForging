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

public final class ModularBodyArmourModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "modular_body_armour"), "main");
    private static final String CHAINMAIL_BODY = "ChainmailBody";
    private static final String CHAINMAIL_RIGHT_ARM = "ChainmailRightArm";
    private static final String CHAINMAIL_LEFT_ARM = "ChainmailLeftArm";
    private static final String MATERIAL_BODY = "MaterialBody";
    private static final String MATERIAL_RIGHT_ARM = "MaterialRightArm";
    private static final String MATERIAL_LEFT_ARM = "MaterialLeftArm";
    private final ModelPart chainmailBody;
    private final ModelPart chainmailRightArm;
    private final ModelPart chainmailLeftArm;
    private final ModelPart materialBody;
    private final ModelPart materialRightArm;
    private final ModelPart materialLeftArm;

    public ModularBodyArmourModel(ModelPart root) {
        this.chainmailBody = root.getChild(CHAINMAIL_BODY);
        this.chainmailRightArm = root.getChild(CHAINMAIL_RIGHT_ARM);
        this.chainmailLeftArm = root.getChild(CHAINMAIL_LEFT_ARM);
        this.materialBody = root.getChild(MATERIAL_BODY);
        this.materialRightArm = root.getChild(MATERIAL_RIGHT_ARM);
        this.materialLeftArm = root.getChild(MATERIAL_LEFT_ARM);
    }

    public void renderChainmailBody(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailBody, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderChainmailRightArm(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailRightArm, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderChainmailLeftArm(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailLeftArm, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderBody(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        renderBody(poseStack, consumer, 0xFFFFFFFF, packedLight, packedOverlay);
    }

    public void renderBody(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialBody, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderRightArm(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        renderRightArm(poseStack, consumer, 0xFFFFFFFF, packedLight, packedOverlay);
    }

    public void renderRightArm(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialRightArm, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderLeftArm(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        renderLeftArm(poseStack, consumer, 0xFFFFFFFF, packedLight, packedOverlay);
    }

    public void renderLeftArm(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialLeftArm, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        CubeDeformation chainmailDeformation = new CubeDeformation(1.0F);
        CubeDeformation materialDeformation = new CubeDeformation(1.15F);

        addBody(root, CHAINMAIL_BODY, chainmailDeformation);
        addRightArm(root, CHAINMAIL_RIGHT_ARM, chainmailDeformation);
        addLeftArm(root, CHAINMAIL_LEFT_ARM, chainmailDeformation);
        addBody(root, MATERIAL_BODY, materialDeformation);
        addRightArm(root, MATERIAL_RIGHT_ARM, materialDeformation);
        addLeftArm(root, MATERIAL_LEFT_ARM, materialDeformation);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    private static void addBody(PartDefinition root, String name, CubeDeformation deformation) {
        root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation), PartPose.ZERO);
    }

    private static void addRightArm(PartDefinition root, String name, CubeDeformation deformation) {
        root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation), PartPose.ZERO);
    }

    private static void addLeftArm(PartDefinition root, String name, CubeDeformation deformation) {
        root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(40, 16)
                .mirror()
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation), PartPose.ZERO);
    }

    private static void render(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        part.render(poseStack, new TintingVertexConsumer(consumer, color), packedLight, packedOverlay);
    }
}

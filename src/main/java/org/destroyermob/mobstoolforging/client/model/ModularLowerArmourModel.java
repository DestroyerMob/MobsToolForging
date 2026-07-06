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

public final class ModularLowerArmourModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "modular_lower_armour"), "main");
    private static final String CHAINMAIL_RIGHT_LEGGINGS = "ChainmailRightLeggings";
    private static final String CHAINMAIL_LEFT_LEGGINGS = "ChainmailLeftLeggings";
    private static final String MATERIAL_RIGHT_LEGGINGS = "MaterialRightLeggings";
    private static final String MATERIAL_LEFT_LEGGINGS = "MaterialLeftLeggings";
    private static final String CHAINMAIL_RIGHT_BOOT = "ChainmailRightBoot";
    private static final String CHAINMAIL_LEFT_BOOT = "ChainmailLeftBoot";
    private static final String MATERIAL_RIGHT_BOOT = "MaterialRightBoot";
    private static final String MATERIAL_LEFT_BOOT = "MaterialLeftBoot";
    private final ModelPart chainmailRightLeggings;
    private final ModelPart chainmailLeftLeggings;
    private final ModelPart materialRightLeggings;
    private final ModelPart materialLeftLeggings;
    private final ModelPart chainmailRightBoot;
    private final ModelPart chainmailLeftBoot;
    private final ModelPart materialRightBoot;
    private final ModelPart materialLeftBoot;

    public ModularLowerArmourModel(ModelPart root) {
        this.chainmailRightLeggings = root.getChild(CHAINMAIL_RIGHT_LEGGINGS);
        this.chainmailLeftLeggings = root.getChild(CHAINMAIL_LEFT_LEGGINGS);
        this.materialRightLeggings = root.getChild(MATERIAL_RIGHT_LEGGINGS);
        this.materialLeftLeggings = root.getChild(MATERIAL_LEFT_LEGGINGS);
        this.chainmailRightBoot = root.getChild(CHAINMAIL_RIGHT_BOOT);
        this.chainmailLeftBoot = root.getChild(CHAINMAIL_LEFT_BOOT);
        this.materialRightBoot = root.getChild(MATERIAL_RIGHT_BOOT);
        this.materialLeftBoot = root.getChild(MATERIAL_LEFT_BOOT);
    }

    public void renderChainmailRightLeggings(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailRightLeggings, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderChainmailLeftLeggings(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailLeftLeggings, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderRightLeggings(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialRightLeggings, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderLeftLeggings(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialLeftLeggings, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderChainmailRightBoot(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailRightBoot, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderChainmailLeftBoot(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(chainmailLeftBoot, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderRightBoot(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialRightBoot, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public void renderLeftBoot(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        render(materialLeftBoot, poseStack, consumer, color, packedLight, packedOverlay);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        CubeDeformation leggingsChainmailDeformation = new CubeDeformation(0.5F);
        CubeDeformation leggingsMaterialDeformation = new CubeDeformation(0.65F);
        CubeDeformation bootChainmailDeformation = new CubeDeformation(1.0F);
        CubeDeformation bootMaterialDeformation = new CubeDeformation(1.15F);

        addLeggingLeg(root, CHAINMAIL_RIGHT_LEGGINGS, leggingsChainmailDeformation);
        addLeggingLeg(root, CHAINMAIL_LEFT_LEGGINGS, leggingsChainmailDeformation);
        addLeggingLeg(root, MATERIAL_RIGHT_LEGGINGS, leggingsMaterialDeformation);
        addLeggingLeg(root, MATERIAL_LEFT_LEGGINGS, leggingsMaterialDeformation);
        addBootLeg(root, CHAINMAIL_RIGHT_BOOT, bootChainmailDeformation);
        addBootLeg(root, CHAINMAIL_LEFT_BOOT, bootChainmailDeformation);
        addBootLeg(root, MATERIAL_RIGHT_BOOT, bootMaterialDeformation);
        addBootLeg(root, MATERIAL_LEFT_BOOT, bootMaterialDeformation);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    private static void addLeggingLeg(PartDefinition root, String name, CubeDeformation deformation) {
        root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation), PartPose.ZERO);
    }

    private static void addBootLeg(PartDefinition root, String name, CubeDeformation deformation) {
        root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(24, 15)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation), PartPose.ZERO);
    }

    private static void render(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        part.render(poseStack, new TintingVertexConsumer(consumer, color), packedLight, packedOverlay);
    }
}

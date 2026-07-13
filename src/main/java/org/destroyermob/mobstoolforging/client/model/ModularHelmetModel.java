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
    private static final String CHAINMAIL_TRIM_HELMET = "ChainmailTrimHelmet";
    private static final String MATERIAL_TRIM_HELMET = "MaterialTrimHelmet";
    private final ModelPart chainmailHelmet;
    private final ModelPart materialHelmet;
    private final ModelPart chainmailTrimHelmet;
    private final ModelPart materialTrimHelmet;

    public ModularHelmetModel(ModelPart root) {
        this.chainmailHelmet = root.getChild(CHAINMAIL_HELMET);
        this.materialHelmet = root.getChild(MATERIAL_HELMET);
        this.chainmailTrimHelmet = root.getChild(CHAINMAIL_TRIM_HELMET);
        this.materialTrimHelmet = root.getChild(MATERIAL_TRIM_HELMET);
    }

    public void renderChainmail(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        renderChainmail(poseStack, consumer, 0xFFFFFFFF, packedLight, packedOverlay);
    }

    public void renderChainmail(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        chainmailHelmet.render(poseStack, new TintingVertexConsumer(consumer, color), packedLight, packedOverlay);
    }

    public void renderMaterial(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        renderMaterial(poseStack, consumer, 0xFFFFFFFF, packedLight, packedOverlay);
    }

    public void renderMaterial(PoseStack poseStack, VertexConsumer consumer, int color, int packedLight, int packedOverlay) {
        materialHelmet.render(poseStack, new TintingVertexConsumer(consumer, color), packedLight, packedOverlay);
    }

    public void renderTrim(PoseStack poseStack, VertexConsumer consumer, boolean plated, int packedLight, int packedOverlay) {
        ModelPart trimShell = plated ? materialTrimHelmet : chainmailTrimHelmet;
        trimShell.render(poseStack, consumer, packedLight, packedOverlay);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        CubeDeformation chainmailDeformation = new CubeDeformation(0.5F);
        CubeDeformation materialDeformation = new CubeDeformation(0.65F);
        CubeDeformation chainmailTrimDeformation = new CubeDeformation(0.53F);
        CubeDeformation materialTrimDeformation = new CubeDeformation(0.68F);

        root.addOrReplaceChild(CHAINMAIL_HELMET, CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, chainmailDeformation), PartPose.ZERO);

        root.addOrReplaceChild(MATERIAL_HELMET, CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, materialDeformation), PartPose.ZERO);

        root.addOrReplaceChild(CHAINMAIL_TRIM_HELMET, CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, chainmailTrimDeformation), PartPose.ZERO);

        root.addOrReplaceChild(MATERIAL_TRIM_HELMET, CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, materialTrimDeformation), PartPose.ZERO);

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

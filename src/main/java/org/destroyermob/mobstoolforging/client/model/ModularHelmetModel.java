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

    private final ModelPart skull;
    private final ModelPart comb;
    private final ModelPart visor;

    public ModularHelmetModel(ModelPart root) {
        this.skull = root.getChild("skull");
        this.comb = root.getChild("comb");
        this.visor = root.getChild("visor");
    }

    public void renderSkull(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        skull.render(poseStack, consumer, packedLight, packedOverlay);
    }

    public void renderComb(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        comb.render(poseStack, consumer, packedLight, packedOverlay);
    }

    public void renderVisor(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        visor.render(poseStack, consumer, packedLight, packedOverlay);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("skull", CubeListBuilder.create()
                .texOffs(-6, -6).addBox(-5.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-3, 1).addBox(-5.0F, -8.0F, 4.0F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-3, 1).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -8).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("comb", CubeListBuilder.create()
                .texOffs(-4, 0).addBox(-5.0F, -10.0F, -1.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -10.0F, -1.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("visor", CubeListBuilder.create()
                .texOffs(-7, -8).addBox(5.0F, -1.0F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-7, -8).addBox(5.0F, -5.0F, -5.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-7, -8).addBox(5.0F, -9.0F, -5.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(1, 0).addBox(5.0F, -6.0F, 3.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(1, 0).addBox(5.0F, -6.0F, -5.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -3.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -3.0F, 4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -3.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -2.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -3.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -3.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(5.0F, -2.0F, 2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-5, 1).addBox(-2.0F, -7.0F, -6.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-5, 1).addBox(-2.0F, -7.0F, 5.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 16, 16);
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

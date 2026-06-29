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

        CubeListBuilder skullBuilder = CubeListBuilder.create();
        addHeadAlignedBox(skullBuilder, -6, -6, -5.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F);
        addHeadAlignedBox(skullBuilder, -3, 1, -5.0F, -8.0F, 4.0F, 10.0F, 8.0F, 1.0F);
        addHeadAlignedBox(skullBuilder, -3, 1, -5.0F, -8.0F, -5.0F, 10.0F, 8.0F, 1.0F);
        addHeadAlignedBox(skullBuilder, -8, -8, -5.0F, -9.0F, -5.0F, 10.0F, 1.0F, 10.0F);
        partdefinition.addOrReplaceChild("skull", skullBuilder, PartPose.ZERO);

        CubeListBuilder combBuilder = CubeListBuilder.create();
        addHeadAlignedBox(combBuilder, -4, 0, -5.0F, -10.0F, -1.0F, 10.0F, 1.0F, 2.0F);
        addHeadAlignedBox(combBuilder, 0, 0, -6.0F, -10.0F, -1.0F, 1.0F, 10.0F, 2.0F);
        partdefinition.addOrReplaceChild("comb", combBuilder, PartPose.ZERO);

        CubeListBuilder visorBuilder = CubeListBuilder.create();
        addHeadAlignedBox(visorBuilder, -7, -8, 5.0F, -1.0F, -5.0F, 1.0F, 1.0F, 10.0F);
        addHeadAlignedBox(visorBuilder, -7, -8, 5.0F, -5.0F, -5.0F, 1.0F, 2.0F, 10.0F);
        addHeadAlignedBox(visorBuilder, -7, -8, 5.0F, -9.0F, -5.0F, 1.0F, 3.0F, 10.0F);
        addHeadAlignedBox(visorBuilder, 1, 0, 5.0F, -6.0F, 3.0F, 1.0F, 1.0F, 2.0F);
        addHeadAlignedBox(visorBuilder, 1, 0, 5.0F, -6.0F, -5.0F, 1.0F, 1.0F, 2.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -3.0F, -5.0F, 1.0F, 2.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -3.0F, 4.0F, 1.0F, 2.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -3.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -2.0F, -4.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -3.0F, -3.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -3.0F, -1.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, 2, 1, 5.0F, -2.0F, 2.0F, 1.0F, 1.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, -5, 1, -2.0F, -7.0F, -6.0F, 8.0F, 3.0F, 1.0F);
        addHeadAlignedBox(visorBuilder, -5, 1, -2.0F, -7.0F, 5.0F, 8.0F, 3.0F, 1.0F);
        partdefinition.addOrReplaceChild("visor", visorBuilder, PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static void addHeadAlignedBox(CubeListBuilder builder, int texU, int texV, float x, float y, float z, float dx, float dy, float dz) {
        // The exported helmet faces +X; Minecraft humanoid heads face -Z.
        float minX = z;
        float minZ = -(x + dx);
        builder.texOffs(texU, texV).addBox(minX, y, minZ, dz, dy, dx, new CubeDeformation(0.0F));
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

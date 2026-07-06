package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;

public final class ModularLowerArmourItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularLowerArmourItemModel() {
    }

    public static ResolvedArmorItemModel composeLeggings(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite chainmailSprite = ArmorMaterialTextureManager.INSTANCE.geometrySprite(key.leggingsChainmailMaterial(), ArmorPartData.LEGGINGS_CHAINMAIL);
        List<BakedQuad> quads = new ArrayList<>();
        addForwardCuboids(quads, ModularLowerArmourGeometry.LEGGING_RIGHT_LEG, chainmailSprite);
        addForwardCuboids(quads, ModularLowerArmourGeometry.LEGGING_LEFT_LEG, chainmailSprite);
        key.leggingsPlateMaterial().ifPresent(material -> {
            TextureAtlasSprite plateSprite = ArmorMaterialTextureManager.INSTANCE.geometrySprite(material, ArmorPartData.LEGGINGS_PLATE);
            addForwardCuboids(quads, ModularLowerArmourGeometry.LEGGING_RIGHT_PLATE, plateSprite);
            addForwardCuboids(quads, ModularLowerArmourGeometry.LEGGING_LEFT_PLATE, plateSprite);
        });
        return new ResolvedArmorItemModel(List.copyOf(quads), chainmailSprite, transforms);
    }

    public static ResolvedArmorItemModel composeBoots(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite sprite = ArmorMaterialTextureManager.INSTANCE.geometrySprite(key.bootsChainmailMaterial(), ArmorPartData.BOOTS_CHAINMAIL);
        List<BakedQuad> quads = new ArrayList<>();
        addCuboids(quads, ModularLowerArmourGeometry.BOOTS, sprite);
        key.bootsPlateMaterial().ifPresent(material -> addCuboids(quads, ModularLowerArmourGeometry.BOOTS_PLATE, ArmorMaterialTextureManager.INSTANCE.geometrySprite(material, ArmorPartData.BOOTS_PLATE)));
        return new ResolvedArmorItemModel(List.copyOf(quads), sprite, transforms);
    }

    private static void addCuboids(List<BakedQuad> quads, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        for (ModularLowerArmourGeometry.Cuboid cuboid : cuboids) {
            for (Direction direction : cuboid.itemRenderDirections()) {
                quads.add(bakeFace(cuboid, direction, sprite));
            }
        }
    }

    private static void addForwardCuboids(List<BakedQuad> quads, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        for (ModularLowerArmourGeometry.Cuboid cuboid : cuboids) {
            for (Direction direction : cuboid.forwardItemRenderDirections()) {
                quads.add(bakeForwardFace(cuboid, direction, sprite));
            }
        }
    }

    private static BakedQuad bakeFace(ModularLowerArmourGeometry.Cuboid cuboid, Direction direction, TextureAtlasSprite sprite) {
        BlockElementFace face = new BlockElementFace(null, BlockElementFace.NO_TINT, "#texture", cuboid.blockFaceUv(direction));
        return FACE_BAKERY.bakeQuad(
                cuboid.itemFrom(),
                cuboid.itemTo(),
                face,
                sprite,
                direction,
                BlockModelRotation.X0_Y0,
                null,
                true
        );
    }

    private static BakedQuad bakeForwardFace(ModularLowerArmourGeometry.Cuboid cuboid, Direction direction, TextureAtlasSprite sprite) {
        BlockElementFace face = new BlockElementFace(null, BlockElementFace.NO_TINT, "#texture", cuboid.forwardBlockFaceUv(direction));
        return FACE_BAKERY.bakeQuad(
                cuboid.forwardItemFrom(),
                cuboid.forwardItemTo(),
                face,
                sprite,
                direction,
                BlockModelRotation.X0_Y0,
                null,
                true
        );
    }
}

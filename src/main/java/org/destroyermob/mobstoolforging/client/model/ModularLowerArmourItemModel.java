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
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;

public final class ModularLowerArmourItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularLowerArmourItemModel() {
    }

    public static ResolvedArmorItemModel composeLeggings(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite legsSprite = ArmorMaterialTextureManager.INSTANCE.sprite(key.skullMaterial());
        List<BakedQuad> quads = new ArrayList<>();
        addCuboids(quads, ModularLowerArmourGeometry.LEGGING_LEGS, legsSprite);
        key.combMaterial().ifPresent(material -> addCuboids(quads, ModularLowerArmourGeometry.LEGGING_KNEES, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        key.visorMaterial().ifPresent(material -> addCuboids(quads, ModularLowerArmourGeometry.LEGGING_TASSETS, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        return new ResolvedArmorItemModel(List.copyOf(quads), legsSprite, transforms);
    }

    public static ResolvedArmorItemModel composeBoots(ArmorVisualKey key, ItemTransforms transforms) {
        return compose(key, ModularLowerArmourGeometry.BOOTS, transforms);
    }

    private static ResolvedArmorItemModel compose(ArmorVisualKey key, List<ModularLowerArmourGeometry.Cuboid> cuboids, ItemTransforms transforms) {
        TextureAtlasSprite sprite = ArmorMaterialTextureManager.INSTANCE.sprite(key.skullMaterial());
        List<BakedQuad> quads = new ArrayList<>();
        addCuboids(quads, cuboids, sprite);
        return new ResolvedArmorItemModel(List.copyOf(quads), sprite, transforms);
    }

    private static void addCuboids(List<BakedQuad> quads, List<ModularLowerArmourGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        for (ModularLowerArmourGeometry.Cuboid cuboid : cuboids) {
            for (Direction direction : cuboid.itemRenderDirections()) {
                quads.add(bakeFace(cuboid, direction, sprite));
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
}

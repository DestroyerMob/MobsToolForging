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

public final class ModularHelmetItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularHelmetItemModel() {
    }

    public static ResolvedArmorItemModel compose(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite skullSprite = ArmorMaterialTextureManager.INSTANCE.sprite(key.skullMaterial());
        List<BakedQuad> quads = new ArrayList<>();
        addCuboids(quads, ModularHelmetGeometry.SKULL, skullSprite);
        key.combMaterial().ifPresent(material -> addCuboids(quads, ModularHelmetGeometry.COMB, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        key.visorMaterial().ifPresent(material -> addCuboids(quads, ModularHelmetGeometry.VISOR, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        return new ResolvedArmorItemModel(List.copyOf(quads), skullSprite, transforms);
    }

    private static void addCuboids(List<BakedQuad> quads, List<ModularHelmetGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        for (ModularHelmetGeometry.Cuboid cuboid : cuboids) {
            for (Direction direction : Direction.values()) {
                quads.add(bakeFace(cuboid, direction, sprite));
            }
        }
    }

    private static BakedQuad bakeFace(ModularHelmetGeometry.Cuboid cuboid, Direction direction, TextureAtlasSprite sprite) {
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

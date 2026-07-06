package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;

public final class ModularHelmetItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularHelmetItemModel() {
    }

    public static ResolvedArmorItemModel compose(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite chainmailSprite = ArmorMaterialTextureManager.INSTANCE.geometrySprite(key.helmetChainmailMaterial(), ArmorPartData.HELMET_CHAINMAIL);
        List<BakedQuad> quads = new ArrayList<>();
        addCuboids(quads, ModularHelmetGeometry.CHAINMAIL, chainmailSprite);
        key.helmetPlateMaterial().ifPresent(material -> addCuboids(quads, ModularHelmetGeometry.MATERIAL, ArmorMaterialTextureManager.INSTANCE.geometrySprite(material, ArmorPartData.HELMET_PLATE)));
        return new ResolvedArmorItemModel(List.copyOf(quads), chainmailSprite, transforms);
    }

    private static void addCuboids(List<BakedQuad> quads, List<ModularHelmetGeometry.Cuboid> cuboids, TextureAtlasSprite sprite) {
        for (ModularHelmetGeometry.Cuboid cuboid : cuboids) {
            for (net.minecraft.core.Direction direction : cuboid.renderDirections()) {
                quads.add(bakeFace(cuboid, direction, sprite));
            }
        }
    }

    private static BakedQuad bakeFace(ModularHelmetGeometry.Cuboid cuboid, net.minecraft.core.Direction direction, TextureAtlasSprite sprite) {
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

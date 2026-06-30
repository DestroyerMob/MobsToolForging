package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;

public final class ModularHelmetItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularHelmetItemModel() {
    }

    public static ResolvedArmorItemModel compose(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite skullSprite = ArmorMaterialTextureManager.INSTANCE.sprite(key.skullMaterial());
        List<BakedQuad> quads = new ArrayList<>();
        addElements(quads, ModularHelmetItemGeometry.SKULL, skullSprite);
        key.combMaterial().ifPresent(material -> addElements(quads, ModularHelmetItemGeometry.COMB, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        key.visorMaterial().ifPresent(material -> addElements(quads, ModularHelmetItemGeometry.VISOR, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        return new ResolvedArmorItemModel(List.copyOf(quads), skullSprite, transforms);
    }

    private static void addElements(List<BakedQuad> quads, List<ModularHelmetItemGeometry.Element> elements, TextureAtlasSprite sprite) {
        for (ModularHelmetItemGeometry.Element element : elements) {
            for (ModularHelmetItemGeometry.Face face : element.faces()) {
                if (element.hasArea(face.direction())) {
                    quads.add(bakeFace(element, face, sprite));
                }
            }
        }
    }

    private static BakedQuad bakeFace(ModularHelmetItemGeometry.Element element, ModularHelmetItemGeometry.Face itemFace, TextureAtlasSprite sprite) {
        BlockElementFace face = new BlockElementFace(null, BlockElementFace.NO_TINT, "#texture", itemFace.uv());
        return FACE_BAKERY.bakeQuad(
                element.from(),
                element.to(),
                face,
                sprite,
                itemFace.direction(),
                BlockModelRotation.X0_Y0,
                null,
                true
        );
    }
}

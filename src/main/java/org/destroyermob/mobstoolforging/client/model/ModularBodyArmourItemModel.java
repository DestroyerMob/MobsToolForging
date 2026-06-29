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

public final class ModularBodyArmourItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularBodyArmourItemModel() {
    }

    public static ResolvedArmorItemModel compose(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite bodySprite = ArmorMaterialTextureManager.INSTANCE.sprite(key.skullMaterial());
        List<BakedQuad> quads = new ArrayList<>();
        for (ModularBodyArmourGeometry.ItemCuboid cuboid : ModularBodyArmourGeometry.ITEM) {
            for (Direction direction : Direction.values()) {
                quads.add(bakeFace(cuboid, direction, bodySprite));
            }
        }
        return new ResolvedArmorItemModel(List.copyOf(quads), bodySprite, transforms);
    }

    private static BakedQuad bakeFace(ModularBodyArmourGeometry.ItemCuboid cuboid, Direction direction, TextureAtlasSprite sprite) {
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

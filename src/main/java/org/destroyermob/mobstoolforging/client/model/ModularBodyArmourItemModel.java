package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;

public final class ModularBodyArmourItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private ModularBodyArmourItemModel() {
    }

    public static BakedModel compose(ArmorVisualKey key, BakedModel fallback) {
        boolean customBase = !MaterialCatalog.IRON.equals(key.chestplateChainmailMaterial());
        boolean hasPlate = key.chestplatePlateMaterial().isPresent();
        if (!customBase && !hasPlate) {
            return fallback;
        }

        List<BakedQuad> quads = customBase ? new ArrayList<>() : new ArrayList<>(fallback.getQuads(null, null, RandomSource.create(42L)));
        TextureAtlasSprite particle = fallback.getParticleIcon();
        if (customBase) {
            TextureAtlasSprite baseSprite = ArmorMaterialTextureManager.INSTANCE.geometrySprite(key.chestplateChainmailMaterial(), ArmorPartData.CHESTPLATE_CHAINMAIL);
            particle = baseSprite;
            for (ModularBodyArmourGeometry.ItemCuboid cuboid : ModularBodyArmourGeometry.CHAINMAIL_ITEM) {
                for (Direction direction : Direction.values()) {
                    quads.add(bakeFace(cuboid, direction, baseSprite));
                }
            }
        }
        if (hasPlate) {
            TextureAtlasSprite bodySprite = ArmorMaterialTextureManager.INSTANCE.geometrySprite(key.chestplatePlateMaterial().orElseThrow(), ArmorPartData.CHESTPLATE_BODY);
            particle = bodySprite;
            for (ModularBodyArmourGeometry.ItemCuboid cuboid : ModularBodyArmourGeometry.ITEM) {
                for (Direction direction : Direction.values()) {
                    quads.add(bakeFace(cuboid, direction, bodySprite));
                }
            }
        }
        return new ResolvedArmorItemModel(List.copyOf(quads), particle, fallback.getTransforms());
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

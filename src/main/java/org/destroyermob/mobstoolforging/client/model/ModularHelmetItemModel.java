package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;
import org.joml.Vector3f;

public final class ModularHelmetItemModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final float ITEM_X_OFFSET = 8.0F;
    private static final float ITEM_Y_OFFSET = 3.0F;
    private static final float ITEM_Z_OFFSET = 8.0F;
    private static final List<Cuboid> SKULL = List.of(
            cuboid(-5, -8, -4, -4, 0, 4),
            cuboid(-5, -8, 4, 5, 0, 5),
            cuboid(-5, -8, -5, 5, 0, -4),
            cuboid(-5, -9, -5, 5, -8, 5)
    );
    private static final List<Cuboid> COMB = List.of(
            cuboid(-5, -10, -1, 5, -9, 1),
            cuboid(-6, -10, -1, -5, 0, 1)
    );
    private static final List<Cuboid> VISOR = List.of(
            cuboid(5, -1, -5, 6, 0, 5),
            cuboid(5, -5, -5, 6, -3, 5),
            cuboid(5, -9, -5, 6, -6, 5),
            cuboid(5, -6, 3, 6, -5, 5),
            cuboid(5, -6, -5, 6, -5, -3),
            cuboid(5, -3, -5, 6, -1, -4),
            cuboid(5, -3, 4, 6, -1, 5),
            cuboid(5, -3, 1, 6, -2, 2),
            cuboid(5, -3, 3, 6, -2, 4),
            cuboid(5, -2, -4, 6, -1, -3),
            cuboid(5, -3, -3, 6, -2, -2),
            cuboid(5, -3, -1, 6, -2, 0),
            cuboid(5, -2, -2, 6, -1, -1),
            cuboid(5, -2, 0, 6, -1, 1),
            cuboid(5, -2, 2, 6, -1, 3),
            cuboid(-2, -7, -6, 6, -4, -5),
            cuboid(-2, -7, 5, 6, -4, 6)
    );

    private ModularHelmetItemModel() {
    }

    public static ResolvedArmorItemModel compose(ArmorVisualKey key, ItemTransforms transforms) {
        TextureAtlasSprite skullSprite = ArmorMaterialTextureManager.INSTANCE.sprite(key.skullMaterial());
        List<BakedQuad> quads = new ArrayList<>();
        addCuboids(quads, SKULL, skullSprite);
        key.combMaterial().ifPresent(material -> addCuboids(quads, COMB, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        key.visorMaterial().ifPresent(material -> addCuboids(quads, VISOR, ArmorMaterialTextureManager.INSTANCE.sprite(material)));
        return new ResolvedArmorItemModel(List.copyOf(quads), skullSprite, transforms);
    }

    private static void addCuboids(List<BakedQuad> quads, List<Cuboid> cuboids, TextureAtlasSprite sprite) {
        for (Cuboid cuboid : cuboids) {
            for (Direction direction : Direction.values()) {
                quads.add(bakeFace(cuboid, direction, sprite));
            }
        }
    }

    private static BakedQuad bakeFace(Cuboid cuboid, Direction direction, TextureAtlasSprite sprite) {
        BlockElementFace face = new BlockElementFace(null, BlockElementFace.NO_TINT, "#texture", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0));
        return FACE_BAKERY.bakeQuad(
                cuboid.from(),
                cuboid.to(),
                face,
                sprite,
                direction,
                BlockModelRotation.X0_Y0,
                null,
                true
        );
    }

    private static Cuboid cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        float rotatedMinX = minZ;
        float rotatedMaxX = maxZ;
        float rotatedMinZ = -maxX;
        float rotatedMaxZ = -minX;
        return new Cuboid(
                new Vector3f(rotatedMinX + ITEM_X_OFFSET, ITEM_Y_OFFSET - maxY, rotatedMinZ + ITEM_Z_OFFSET),
                new Vector3f(rotatedMaxX + ITEM_X_OFFSET, ITEM_Y_OFFSET - minY, rotatedMaxZ + ITEM_Z_OFFSET)
        );
    }

    private record Cuboid(Vector3f from, Vector3f to) {
    }
}

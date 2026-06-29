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
    private static final float ITEM_Y_OFFSET = 12.0F;
    private static final float ITEM_Z_OFFSET = 8.0F;
    private static final List<Cuboid> SKULL = List.of(
            cuboid(-4, -7, -4, 4, -6, 4),
            cuboid(-4, -6, -5, 4, -1, -4),
            cuboid(4, -6, -4, 5, -1, 4),
            cuboid(-5, -6, -4, -4, -1, 4)
    );
    private static final List<Cuboid> COMB = List.of(
            cuboid(-1, -9, -1, 1, -1, 1),
            cuboid(-1, -1, -1, 1, 0, 1)
    );
    private static final List<Cuboid> VISOR = List.of(
            cuboid(1, -1, -6, 3, 0, -5),
            cuboid(-3, -1, -6, -1, 0, -5),
            cuboid(0, -1, -6, 2, 0, -5),
            cuboid(-2, -1, -6, 0, 0, -5),
            cuboid(-4, -1, -6, -2, 0, -5),
            cuboid(2, -1, -6, 4, 0, -5),
            cuboid(3, -2, -6, 4, -1, -5),
            cuboid(-5, -2, -6, -4, -1, -5),
            cuboid(-4, -3, -6, -3, -2, -5),
            cuboid(2, -3, -6, 3, -2, -5),
            cuboid(-3, -4, -6, -2, -3, -5),
            cuboid(1, -4, -6, 2, -3, -5),
            cuboid(-2, -5, -6, 2, -4, -5)
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
        return new Cuboid(
                new Vector3f(minX + ITEM_X_OFFSET, minY + ITEM_Y_OFFSET, minZ + ITEM_Z_OFFSET),
                new Vector3f(maxX + ITEM_X_OFFSET, maxY + ITEM_Y_OFFSET, maxZ + ITEM_Z_OFFSET)
        );
    }

    private record Cuboid(Vector3f from, Vector3f to) {
    }
}

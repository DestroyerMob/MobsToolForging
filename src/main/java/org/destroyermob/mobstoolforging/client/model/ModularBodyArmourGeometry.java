package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class ModularBodyArmourGeometry {
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final float ITEM_X_SCALE = 16.0F / 18.0F;
    private static final int WHITE = 0xFFFFFFFF;

    public static final List<Cuboid> CHAINMAIL_BODY = List.of(
            cuboid(-4.15F, 0, -2.35F, 4.15F, 12, -2.1F, 0, 0),
            cuboid(-4.15F, 0, 2.1F, 4.15F, 12, 2.35F, 0, 0),
            cuboid(4.1F, 0, -2.25F, 4.35F, 12, 2.25F, 0, 0),
            cuboid(-4.35F, 0, -2.25F, -4.1F, 12, 2.25F, 0, 0)
    );
    public static final List<Cuboid> CHAINMAIL_RIGHT_ARM = List.of(
            cuboid(-3.35F, -2, -2.35F, 1.1F, 4, -2.1F, 0, 0),
            cuboid(-3.35F, -2, 2.1F, 1.1F, 4, 2.35F, 0, 0),
            cuboid(-3.6F, -2, -2.25F, -3.35F, 4, 2.25F, 0, 0),
            cuboid(1.1F, -2, -2.25F, 1.35F, 4, 2.25F, 0, 0)
    );
    public static final List<Cuboid> CHAINMAIL_LEFT_ARM = List.of(
            cuboid(-1.1F, -2, -2.35F, 3.35F, 4, -2.1F, 0, 0),
            cuboid(-1.1F, -2, 2.1F, 3.35F, 4, 2.35F, 0, 0),
            cuboid(-1.35F, -2, -2.25F, -1.1F, 4, 2.25F, 0, 0),
            cuboid(3.35F, -2, -2.25F, 3.6F, 4, 2.25F, 0, 0)
    );
    public static final List<Cuboid> BODY = List.of(
            cuboid(-4, 0, -3, 4, 12, -2, -5, 1),
            cuboid(-4, 0, 2, 4, 12, 3, -5, 1),
            cuboid(4, 4, -3, 5, 12, 3, -3, -4),
            cuboid(-5, 4, -3, -4, 12, 3, -3, -4)
    );
    public static final List<Cuboid> RIGHT_ARM = List.of(
            cuboid(-3, -2, 2, 1, 2, 3, -1, 1),
            cuboid(-4, -3, -3, 1, -2, 3, -4, -4),
            cuboid(-4, -2, -3, -3, 2, 3, -3, -4),
            cuboid(-3, -2, -3, 1, 2, -2, -1, 1)
    );
    public static final List<Cuboid> LEFT_ARM = List.of(
            cuboid(-1, -2, -3, 3, 2, -2, -1, 1),
            cuboid(-1, -2, 2, 3, 2, 3, -1, 1),
            cuboid(3, -2, -3, 4, 2, 3, -3, -4),
            cuboid(-1, -3, -3, 4, -2, 3, -4, -4)
    );
    public static final List<ItemCuboid> ITEM = List.of(
            itemCuboid(-4, 12, -3, 4, 24, -2, -5, 1),
            itemCuboid(-4, 12, 2, 4, 24, 3, -5, 1),
            itemCuboid(4, 16, -3, 5, 24, 3, -3, -4),
            itemCuboid(-5, 16, -3, -4, 24, 3, -3, -4),
            itemCuboid(-8, 12, 2, -4, 16, 3, -1, 1),
            itemCuboid(-9, 11, -3, -4, 12, 3, -4, -4),
            itemCuboid(-9, 12, -3, -8, 16, 3, -3, -4),
            itemCuboid(-8, 12, -3, -4, 16, -2, -1, 1),
            itemCuboid(4, 12, -3, 8, 16, -2, -1, 1),
            itemCuboid(4, 12, 2, 8, 16, 3, -1, 1),
            itemCuboid(8, 12, -3, 9, 16, 3, -3, -4),
            itemCuboid(4, 11, -3, 9, 12, 3, -4, -4)
    );
    public static final List<ItemCuboid> CHAINMAIL_ITEM = List.of(
            itemCuboid(-4.15F, 12, -2.35F, 4.15F, 24, -2.1F, 0, 0),
            itemCuboid(-4.15F, 12, 2.1F, 4.15F, 24, 2.35F, 0, 0),
            itemCuboid(4.1F, 12, -2.25F, 4.35F, 24, 2.25F, 0, 0),
            itemCuboid(-4.35F, 12, -2.25F, -4.1F, 24, 2.25F, 0, 0),
            itemCuboid(-3.35F, 12, -2.35F, -1.1F, 18, -2.1F, 0, 0),
            itemCuboid(-3.35F, 12, 2.1F, -1.1F, 18, 2.35F, 0, 0),
            itemCuboid(-3.6F, 12, -2.25F, -3.35F, 18, 2.25F, 0, 0),
            itemCuboid(-1.1F, 12, -2.25F, -0.85F, 18, 2.25F, 0, 0),
            itemCuboid(1.1F, 12, -2.35F, 3.35F, 18, -2.1F, 0, 0),
            itemCuboid(1.1F, 12, 2.1F, 3.35F, 18, 2.35F, 0, 0),
            itemCuboid(0.85F, 12, -2.25F, 1.1F, 18, 2.25F, 0, 0),
            itemCuboid(3.35F, 12, -2.25F, 3.6F, 18, 2.25F, 0, 0)
    );

    private ModularBodyArmourGeometry() {
    }

    public static void renderCuboids(List<Cuboid> cuboids, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            renderBox(poseStack, consumer, sprite, light, overlay, cuboid);
        }
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay, Cuboid cuboid) {
        if (cuboid.maxX <= cuboid.minX || cuboid.maxY <= cuboid.minY || cuboid.maxZ <= cuboid.minZ) {
            return;
        }
        float minX = cuboid.minX * MODEL_SCALE;
        float minY = cuboid.minY * MODEL_SCALE;
        float minZ = cuboid.minZ * MODEL_SCALE;
        float maxX = cuboid.maxX * MODEL_SCALE;
        float maxY = cuboid.maxY * MODEL_SCALE;
        float maxZ = cuboid.maxZ * MODEL_SCALE;
        ArmorCubeUv.Face up = cuboid.faceUv(Direction.UP);
        ArmorCubeUv.Face down = cuboid.faceUv(Direction.DOWN);
        ArmorCubeUv.Face north = cuboid.faceUv(Direction.NORTH);
        ArmorCubeUv.Face south = cuboid.faceUv(Direction.SOUTH);
        ArmorCubeUv.Face west = cuboid.faceUv(Direction.WEST);
        ArmorCubeUv.Face east = cuboid.faceUv(Direction.EAST);
        quad(poseStack, consumer, Direction.UP.step(), light, overlay, minX, maxY, minZ, u(sprite, up.u0()), v(sprite, up.v1()), minX, maxY, maxZ, u(sprite, up.u0()), v(sprite, up.v0()), maxX, maxY, maxZ, u(sprite, up.u1()), v(sprite, up.v0()), maxX, maxY, minZ, u(sprite, up.u1()), v(sprite, up.v1()));
        quad(poseStack, consumer, Direction.DOWN.step(), light, overlay, minX, minY, maxZ, u(sprite, down.u0()), v(sprite, down.v1()), minX, minY, minZ, u(sprite, down.u0()), v(sprite, down.v0()), maxX, minY, minZ, u(sprite, down.u1()), v(sprite, down.v0()), maxX, minY, maxZ, u(sprite, down.u1()), v(sprite, down.v1()));
        quad(poseStack, consumer, Direction.NORTH.step(), light, overlay, minX, minY, minZ, u(sprite, north.u0()), v(sprite, north.v1()), minX, maxY, minZ, u(sprite, north.u0()), v(sprite, north.v0()), maxX, maxY, minZ, u(sprite, north.u1()), v(sprite, north.v0()), maxX, minY, minZ, u(sprite, north.u1()), v(sprite, north.v1()));
        quad(poseStack, consumer, Direction.SOUTH.step(), light, overlay, maxX, minY, maxZ, u(sprite, south.u0()), v(sprite, south.v1()), maxX, maxY, maxZ, u(sprite, south.u0()), v(sprite, south.v0()), minX, maxY, maxZ, u(sprite, south.u1()), v(sprite, south.v0()), minX, minY, maxZ, u(sprite, south.u1()), v(sprite, south.v1()));
        quad(poseStack, consumer, Direction.WEST.step(), light, overlay, minX, minY, maxZ, u(sprite, west.u0()), v(sprite, west.v1()), minX, maxY, maxZ, u(sprite, west.u0()), v(sprite, west.v0()), minX, maxY, minZ, u(sprite, west.u1()), v(sprite, west.v0()), minX, minY, minZ, u(sprite, west.u1()), v(sprite, west.v1()));
        quad(poseStack, consumer, Direction.EAST.step(), light, overlay, maxX, minY, minZ, u(sprite, east.u0()), v(sprite, east.v1()), maxX, maxY, minZ, u(sprite, east.u0()), v(sprite, east.v0()), maxX, maxY, maxZ, u(sprite, east.u1()), v(sprite, east.v0()), maxX, minY, maxZ, u(sprite, east.u1()), v(sprite, east.v1()));
    }

    private static void quad(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int light, int overlay, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3, float v3) {
        vertex(poseStack, consumer, normal, light, overlay, x0, y0, z0, u0, v0);
        vertex(poseStack, consumer, normal, light, overlay, x1, y1, z1, u1, v1);
        vertex(poseStack, consumer, normal, light, overlay, x2, y2, z2, u2, v2);
        vertex(poseStack, consumer, normal, light, overlay, x3, y3, z3, u3, v3);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int light, int overlay, float x, float y, float z, float u, float v) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(WHITE)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }

    private static float u(TextureAtlasSprite sprite, float textureU) {
        return sprite.getU(textureU / 16.0F);
    }

    private static float v(TextureAtlasSprite sprite, float textureV) {
        return sprite.getV(textureV / 16.0F);
    }

    private static Cuboid cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float textureU, float textureV) {
        return new Cuboid(minX, minY, minZ, maxX, maxY, maxZ, textureU, textureV);
    }

    private static ItemCuboid itemCuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float textureU, float textureV) {
        return new ItemCuboid(minX, minY, minZ, maxX, maxY, maxZ, textureU, textureV);
    }

    public record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float textureU, float textureV) {
        public ArmorCubeUv.Face faceUv(Direction direction) {
            return ArmorCubeUv.face(direction, textureU, textureV, maxX - minX, maxY - minY, maxZ - minZ);
        }
    }

    public record ItemCuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float textureU, float textureV) {
        public Vector3f itemFrom() {
            return new Vector3f((minX + 9.0F) * ITEM_X_SCALE, 2.0F + 24.0F - maxY, minZ + 8.0F);
        }

        public Vector3f itemTo() {
            return new Vector3f((maxX + 9.0F) * ITEM_X_SCALE, 2.0F + 24.0F - minY, maxZ + 8.0F);
        }

        public BlockFaceUV blockFaceUv(Direction direction) {
            return ArmorCubeUv.blockFaceUv(direction, textureU, textureV, maxX - minX, maxY - minY, maxZ - minZ);
        }
    }
}

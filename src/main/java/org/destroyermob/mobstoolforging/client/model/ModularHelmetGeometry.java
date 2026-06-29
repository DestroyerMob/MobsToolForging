package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class ModularHelmetGeometry {
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final int WHITE = 0xFFFFFFFF;

    public static final List<Cuboid> SKULL = List.of(
            cuboid(-5, -9, -5, 5, -9, 5, -8, -8),
            cuboid(-5, -9, 5, 5, -2, 5, -3, 2),
            cuboid(-2, -2, 5, 2, -1, 5, 1, 2),
            cuboid(-5, -9, -5, 5, -5, -5, -3, 2),
            cuboid(5, -9, -5, 5, -3, 5, -8, -8),
            cuboid(-5, -9, -5, -5, -3, 5, -8, -8),
            cuboid(-5, -3, 1, -5, -2, 5, -2, -2),
            cuboid(5, -3, 1, 5, -2, 5, -2, -2)
    );
    public static final List<Cuboid> COMB = List.of(
            cuboid(-1, -10, -6, 1, -9, 6, -10, -10),
            cuboid(-1, -9, -6, 1, -4, -5, 1, 1),
            cuboid(-1, -9, 5, 1, -4, 6, 1, 1)
    );
    public static final List<Cuboid> VISOR = List.of(
            cuboid(-6, -3, -6, -5, -1, 2, -5, -6),
            cuboid(5, -3, -6, 6, -1, 2, -5, -6),
            cuboid(-5, -2, -6, 5, 0, -5, -7, 1)
    );

    private ModularHelmetGeometry() {
    }

    public static void renderCuboids(List<Cuboid> cuboids, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            renderBox(poseStack, consumer, sprite, light, overlay, cuboid.headAligned());
        }
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay, Cuboid cuboid) {
        if (cuboid.maxX < cuboid.minX || cuboid.maxY < cuboid.minY || cuboid.maxZ < cuboid.minZ || cuboid.hasNoSurface()) {
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

    public record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float textureU, float textureV) {
        public Cuboid headAligned() {
            // Helmet3(1).java has been converted from root entity coordinates into humanoid head space.
            return this;
        }

        private boolean hasNoSurface() {
            int flatAxes = 0;
            if (maxX == minX) {
                flatAxes++;
            }
            if (maxY == minY) {
                flatAxes++;
            }
            if (maxZ == minZ) {
                flatAxes++;
            }
            return flatAxes >= 2;
        }

        public Vector3f itemFrom() {
            Cuboid aligned = headAligned();
            return new Vector3f(aligned.minX + 8.0F, 3.0F - aligned.maxY, aligned.minZ + 8.0F);
        }

        public Vector3f itemTo() {
            Cuboid aligned = headAligned();
            return new Vector3f(aligned.maxX + 8.0F, 3.0F - aligned.minY, aligned.maxZ + 8.0F);
        }

        public ArmorCubeUv.Face faceUv(Direction direction) {
            Cuboid aligned = headAligned();
            return ArmorCubeUv.face(direction, textureU, textureV, aligned.maxX - aligned.minX, aligned.maxY - aligned.minY, aligned.maxZ - aligned.minZ);
        }

        public BlockFaceUV blockFaceUv(Direction direction) {
            Cuboid aligned = headAligned();
            return ArmorCubeUv.blockFaceUv(direction, textureU, textureV, aligned.maxX - aligned.minX, aligned.maxY - aligned.minY, aligned.maxZ - aligned.minZ);
        }
    }
}

package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class ModularLowerArmourGeometry {
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final float ITEM_SCALE = 16.0F / 10.0F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final List<Direction> FULL_CUBE_DIRECTIONS = List.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public static final List<Cuboid> LEGGING_LEFT_LEG = List.of(
            cuboid(3, 12, -1, 3, 16, 5, -2, -4),
            cuboid(-3, 17, 5, 3, 20, 5, -2, 2),
            cuboid(-3, 17, -1, 3, 20, -1, -2, 2),
            cuboid(-3, 17, -1, -3, 20, 5, -2, -4),
            cuboid(3, 17, -1, 3, 20, 5, -2, -4),
            cuboid(-3, 12, -1, -3, 16, 5, -2, -4),
            cuboid(-3, 12, -1, 3, 16, -1, -2, 2),
            cuboid(-3, 12, 5, 3, 16, 5, -2, 2)
    );
    public static final List<Cuboid> LEGGING_RIGHT_LEG = List.of(
            cuboid(-3, 12, -5, 3, 16, -5, -2, 2),
            cuboid(-3, 17, 1, 3, 20, 1, -2, 2),
            cuboid(3, 17, -5, 3, 20, 1, -2, -4),
            cuboid(-3, 17, -5, -3, 20, 1, -2, -4),
            cuboid(-3, 17, -5, 3, 20, -5, -2, 2),
            cuboid(-3, 12, -5, -3, 16, 1, -2, -4),
            cuboid(3, 12, -5, 3, 16, 1, -2, -4),
            cuboid(-3, 12, 1, 3, 16, 1, -2, 2)
    );
    public static final List<Cuboid> LEGGING_RIGHT_KNEE = List.of(
            cuboid(-4, 15, -5, -3, 18, 0, -3, -3)
    );
    public static final List<Cuboid> LEGGING_LEFT_KNEE = List.of(
            cuboid(-4, 15, 0, -3, 18, 5, -3, -3)
    );
    public static final List<Cuboid> LEGGING_TASSETS = List.of(
            cuboid(-4, 11, 3, -3, 13, 5, 1, 0),
            cuboid(-4, 11, 5, 4, 19, 6, -2, 1),
            cuboid(3, 11, 3, 4, 13, 5, 1, 0),
            cuboid(-4, 11, -6, 4, 19, -5, -2, 1),
            cuboid(-4, 11, -5, -3, 13, -3, 1, 0),
            cuboid(3, 11, -5, 4, 13, -3, 1, 0)
    );
    public static final List<Cuboid> BOOTS = List.of(
            cuboid(2, 20, -4, 3, 24, 0, -1, -2),
            cuboid(-3, 20, -5, 3, 24, -4, -3, 1),
            cuboid(-3, 20, -4, -2, 24, 0, -1, -2),
            cuboid(2, 20, 0, 3, 24, 4, -1, -2),
            cuboid(-3, 20, 4, 3, 24, 5, -3, 1),
            cuboid(-3, 20, 0, -2, 24, 4, -1, -2)
    );

    private ModularLowerArmourGeometry() {
    }

    public static void renderCuboids(List<Cuboid> cuboids, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            renderBox(poseStack, consumer, sprite, light, overlay, cuboid.yRotatedQuarter(), 0.0F, 0.0F, 0.0F);
        }
    }

    public static void renderBodyCuboids(List<Cuboid> cuboids, float parentX, float parentY, float parentZ, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            renderBox(poseStack, consumer, sprite, light, overlay, cuboid.yRotatedQuarter(), parentX, parentY, parentZ);
        }
    }

    public static void renderForwardBodyCuboids(List<Cuboid> cuboids, float parentX, float parentY, float parentZ, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            renderBox(poseStack, consumer, sprite, light, overlay, cuboid.yRotatedQuarterFacingForward(), parentX, parentY, parentZ);
        }
    }

    public static void renderForwardLegCuboids(List<Cuboid> cuboids, float parentX, float parentY, float parentZ, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            renderBox(poseStack, consumer, sprite, light, overlay, cuboid.yRotatedQuarterFacingForward(), parentX, parentY, parentZ);
        }
    }

    public static void renderLegCuboids(List<Cuboid> cuboids, LegSide side, float parentX, float parentY, float parentZ, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay) {
        for (Cuboid cuboid : cuboids) {
            Cuboid rotated = cuboid.yRotatedQuarter();
            if (side.includes(rotated)) {
                renderBox(poseStack, consumer, sprite, light, overlay, rotated, parentX, parentY, parentZ);
            }
        }
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int light, int overlay, Cuboid cuboid, float parentX, float parentY, float parentZ) {
        if (cuboid.maxX < cuboid.minX || cuboid.maxY < cuboid.minY || cuboid.maxZ < cuboid.minZ || cuboid.hasNoSurface()) {
            return;
        }
        float minX = (cuboid.minX - parentX) * MODEL_SCALE;
        float minY = (cuboid.minY - parentY) * MODEL_SCALE;
        float minZ = (cuboid.minZ - parentZ) * MODEL_SCALE;
        float maxX = (cuboid.maxX - parentX) * MODEL_SCALE;
        float maxY = (cuboid.maxY - parentY) * MODEL_SCALE;
        float maxZ = (cuboid.maxZ - parentZ) * MODEL_SCALE;
        List<Direction> directions = cuboid.renderDirections();
        if (directions.contains(Direction.UP)) {
            ArmorCubeUv.Face up = cuboid.faceUv(Direction.UP);
            quad(poseStack, consumer, Direction.UP.step(), light, overlay, minX, maxY, minZ, u(sprite, up.u0()), v(sprite, up.v1()), minX, maxY, maxZ, u(sprite, up.u0()), v(sprite, up.v0()), maxX, maxY, maxZ, u(sprite, up.u1()), v(sprite, up.v0()), maxX, maxY, minZ, u(sprite, up.u1()), v(sprite, up.v1()));
        }
        if (directions.contains(Direction.DOWN)) {
            ArmorCubeUv.Face down = cuboid.faceUv(Direction.DOWN);
            quad(poseStack, consumer, Direction.DOWN.step(), light, overlay, minX, minY, maxZ, u(sprite, down.u0()), v(sprite, down.v1()), minX, minY, minZ, u(sprite, down.u0()), v(sprite, down.v0()), maxX, minY, minZ, u(sprite, down.u1()), v(sprite, down.v0()), maxX, minY, maxZ, u(sprite, down.u1()), v(sprite, down.v1()));
        }
        if (directions.contains(Direction.NORTH)) {
            ArmorCubeUv.Face north = cuboid.faceUv(Direction.NORTH);
            quad(poseStack, consumer, Direction.NORTH.step(), light, overlay, minX, minY, minZ, u(sprite, north.u0()), v(sprite, north.v1()), minX, maxY, minZ, u(sprite, north.u0()), v(sprite, north.v0()), maxX, maxY, minZ, u(sprite, north.u1()), v(sprite, north.v0()), maxX, minY, minZ, u(sprite, north.u1()), v(sprite, north.v1()));
        }
        if (directions.contains(Direction.SOUTH)) {
            ArmorCubeUv.Face south = cuboid.faceUv(Direction.SOUTH);
            quad(poseStack, consumer, Direction.SOUTH.step(), light, overlay, maxX, minY, maxZ, u(sprite, south.u0()), v(sprite, south.v1()), maxX, maxY, maxZ, u(sprite, south.u0()), v(sprite, south.v0()), minX, maxY, maxZ, u(sprite, south.u1()), v(sprite, south.v0()), minX, minY, maxZ, u(sprite, south.u1()), v(sprite, south.v1()));
        }
        if (directions.contains(Direction.WEST)) {
            ArmorCubeUv.Face west = cuboid.faceUv(Direction.WEST);
            quad(poseStack, consumer, Direction.WEST.step(), light, overlay, minX, minY, maxZ, u(sprite, west.u0()), v(sprite, west.v1()), minX, maxY, maxZ, u(sprite, west.u0()), v(sprite, west.v0()), minX, maxY, minZ, u(sprite, west.u1()), v(sprite, west.v0()), minX, minY, minZ, u(sprite, west.u1()), v(sprite, west.v1()));
        }
        if (directions.contains(Direction.EAST)) {
            ArmorCubeUv.Face east = cuboid.faceUv(Direction.EAST);
            quad(poseStack, consumer, Direction.EAST.step(), light, overlay, maxX, minY, minZ, u(sprite, east.u0()), v(sprite, east.v1()), maxX, maxY, minZ, u(sprite, east.u0()), v(sprite, east.v0()), maxX, maxY, maxZ, u(sprite, east.u1()), v(sprite, east.v0()), maxX, minY, maxZ, u(sprite, east.u1()), v(sprite, east.v1()));
        }
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

    public enum LegSide {
        RIGHT {
            @Override
            boolean includes(Cuboid cuboid) {
                return cuboid.centerX() < 0.0F;
            }
        },
        LEFT {
            @Override
            boolean includes(Cuboid cuboid) {
                return cuboid.centerX() >= 0.0F;
            }
        };

        abstract boolean includes(Cuboid cuboid);
    }

    public record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float textureU, float textureV) {
        public Cuboid yRotatedQuarter() {
            return new Cuboid(minZ, minY, -maxX, maxZ, maxY, -minX, textureU, textureV);
        }

        public Cuboid yRotatedQuarterFacingForward() {
            return new Cuboid(minZ, minY, minX, maxZ, maxY, maxX, textureU, textureV);
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

        public List<Direction> renderDirections() {
            if (hasNoSurface()) {
                return List.of();
            }
            if (maxX == minX) {
                return List.of(minX + maxX >= 0.0F ? Direction.EAST : Direction.WEST);
            }
            if (maxY == minY) {
                return List.of(minY + maxY <= 24.0F ? Direction.UP : Direction.DOWN);
            }
            if (maxZ == minZ) {
                return List.of(minZ + maxZ <= 0.0F ? Direction.NORTH : Direction.SOUTH);
            }
            return FULL_CUBE_DIRECTIONS;
        }

        public List<Direction> itemRenderDirections() {
            return yRotatedQuarter().renderDirections();
        }

        public List<Direction> forwardItemRenderDirections() {
            return yRotatedQuarterFacingForward().renderDirections();
        }

        private float centerX() {
            return (minX + maxX) * 0.5F;
        }

        public Vector3f itemFrom() {
            Cuboid rotated = yRotatedQuarter();
            return new Vector3f((rotated.minX + 5.0F) * ITEM_SCALE, 2.0F + 24.0F - rotated.maxY, (rotated.minZ + 5.0F) * ITEM_SCALE);
        }

        public Vector3f forwardItemFrom() {
            Cuboid rotated = yRotatedQuarterFacingForward();
            return new Vector3f((rotated.minX + 5.0F) * ITEM_SCALE, 2.0F + 24.0F - rotated.maxY, (rotated.minZ + 5.0F) * ITEM_SCALE);
        }

        public Vector3f itemTo() {
            Cuboid rotated = yRotatedQuarter();
            return new Vector3f((rotated.maxX + 5.0F) * ITEM_SCALE, 2.0F + 24.0F - rotated.minY, (rotated.maxZ + 5.0F) * ITEM_SCALE);
        }

        public Vector3f forwardItemTo() {
            Cuboid rotated = yRotatedQuarterFacingForward();
            return new Vector3f((rotated.maxX + 5.0F) * ITEM_SCALE, 2.0F + 24.0F - rotated.minY, (rotated.maxZ + 5.0F) * ITEM_SCALE);
        }

        public ArmorCubeUv.Face faceUv(Direction direction) {
            return ArmorCubeUv.face(direction, textureU, textureV, maxX - minX, maxY - minY, maxZ - minZ);
        }

        public BlockFaceUV blockFaceUv(Direction direction) {
            Cuboid rotated = yRotatedQuarter();
            return ArmorCubeUv.blockFaceUv(direction, textureU, textureV, rotated.maxX - rotated.minX, rotated.maxY - rotated.minY, rotated.maxZ - rotated.minZ);
        }

        public BlockFaceUV forwardBlockFaceUv(Direction direction) {
            Cuboid rotated = yRotatedQuarterFacingForward();
            return ArmorCubeUv.blockFaceUv(direction, textureU, textureV, rotated.maxX - rotated.minX, rotated.maxY - rotated.minY, rotated.maxZ - rotated.minZ);
        }
    }
}

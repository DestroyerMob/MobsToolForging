package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class ModularLowerArmourGeometry {
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final float ITEM_SCALE = 16.0F / 10.0F;
    private static final int WHITE = 0xFFFFFFFF;

    public static final List<Cuboid> LEGGINGS = List.of(
            cuboid(-3, 12, 0, -2, 20, 4),
            cuboid(2, 12, 0, 3, 20, 4),
            cuboid(-3, 12, 4, 3, 20, 5),
            cuboid(-3, 12, -4, -2, 20, 0),
            cuboid(-3, 12, -5, 3, 20, -4),
            cuboid(2, 12, -4, 3, 20, 0)
    );
    public static final List<Cuboid> BOOTS = List.of(
            cuboid(2, 20, -4, 3, 24, 0),
            cuboid(-3, 20, -5, 3, 24, -4),
            cuboid(-3, 20, -4, -2, 24, 0),
            cuboid(2, 20, 0, 3, 24, 4),
            cuboid(-3, 20, 4, 3, 24, 5),
            cuboid(-3, 20, 0, -2, 24, 4)
    );

    private ModularLowerArmourGeometry() {
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
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        quad(poseStack, consumer, Direction.UP.step(), light, overlay, minX, maxY, minZ, u0, v1, minX, maxY, maxZ, u0, v0, maxX, maxY, maxZ, u1, v0, maxX, maxY, minZ, u1, v1);
        quad(poseStack, consumer, Direction.DOWN.step(), light, overlay, minX, minY, maxZ, u0, v1, minX, minY, minZ, u0, v0, maxX, minY, minZ, u1, v0, maxX, minY, maxZ, u1, v1);
        quad(poseStack, consumer, Direction.NORTH.step(), light, overlay, minX, minY, minZ, u0, v1, minX, maxY, minZ, u0, v0, maxX, maxY, minZ, u1, v0, maxX, minY, minZ, u1, v1);
        quad(poseStack, consumer, Direction.SOUTH.step(), light, overlay, maxX, minY, maxZ, u0, v1, maxX, maxY, maxZ, u0, v0, minX, maxY, maxZ, u1, v0, minX, minY, maxZ, u1, v1);
        quad(poseStack, consumer, Direction.WEST.step(), light, overlay, minX, minY, maxZ, u0, v1, minX, maxY, maxZ, u0, v0, minX, maxY, minZ, u1, v0, minX, minY, minZ, u1, v1);
        quad(poseStack, consumer, Direction.EAST.step(), light, overlay, maxX, minY, minZ, u0, v1, maxX, maxY, minZ, u0, v0, maxX, maxY, maxZ, u1, v0, maxX, minY, maxZ, u1, v1);
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

    private static Cuboid cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new Cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        public Vector3f itemFrom() {
            return new Vector3f((minX + 5.0F) * ITEM_SCALE, 2.0F + 24.0F - maxY, (minZ + 5.0F) * ITEM_SCALE);
        }

        public Vector3f itemTo() {
            return new Vector3f((maxX + 5.0F) * ITEM_SCALE, 2.0F + 24.0F - minY, (maxZ + 5.0F) * ITEM_SCALE);
        }
    }
}

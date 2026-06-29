package org.destroyermob.mobstoolforging.client.model;

import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;

public final class ArmorCubeUv {
    private static final float TEXTURE_SIZE = 16.0F;

    private ArmorCubeUv() {
    }

    public static Face face(Direction direction, float textureU, float textureV, float width, float height, float depth) {
        return switch (direction) {
            case DOWN -> face(textureU + depth, textureV, width, depth);
            case UP -> face(textureU + depth + width, textureV, width, depth);
            case NORTH -> face(textureU + depth, textureV + depth, width, height);
            case SOUTH -> face(textureU + depth + width + depth, textureV + depth, width, height);
            case WEST -> face(textureU, textureV + depth, depth, height);
            case EAST -> face(textureU + depth + width, textureV + depth, depth, height);
        };
    }

    public static BlockFaceUV blockFaceUv(Direction direction, float textureU, float textureV, float width, float height, float depth) {
        Face face = face(direction, textureU, textureV, width, height, depth);
        return new BlockFaceUV(new float[]{face.u0(), face.v0(), face.u1(), face.v1()}, 0);
    }

    private static Face face(float u, float v, float width, float height) {
        float boundedWidth = Math.min(width, TEXTURE_SIZE);
        float boundedHeight = Math.min(height, TEXTURE_SIZE);
        float boundedU = boundedStart(u, boundedWidth);
        float boundedV = boundedStart(v, boundedHeight);
        return new Face(boundedU, boundedV, boundedU + boundedWidth, boundedV + boundedHeight);
    }

    private static float boundedStart(float value, float span) {
        if (span >= TEXTURE_SIZE) {
            return 0.0F;
        }
        float wrapped = value % TEXTURE_SIZE;
        if (wrapped < 0.0F) {
            wrapped += TEXTURE_SIZE;
        }
        return Math.min(wrapped, TEXTURE_SIZE - span);
    }

    public record Face(float u0, float v0, float u1, float v1) {
    }
}

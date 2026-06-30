package org.destroyermob.mobstoolforging.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class ModularHelmetItemGeometry {
    public static final List<Element> COMB = List.of(
            element(7, 3, 2, 9, 9, 3,
                    face(Direction.NORTH, 0, 0, 2, 6),
                    face(Direction.EAST, 0, 0, 1, 6),
                    face(Direction.SOUTH, 0, 0, 2, 6),
                    face(Direction.WEST, 1, 0, 2, 6),
                    face(Direction.UP, 0, 1, 2, 2),
                    face(Direction.DOWN, 0, 0, 2, 1)),
            element(7, 3, 13, 9, 9, 14,
                    face(Direction.NORTH, 0, 0, 2, 6),
                    face(Direction.EAST, 0, 0, 1, 6),
                    face(Direction.SOUTH, 0, 0, 2, 6),
                    face(Direction.WEST, 1, 0, 2, 6),
                    face(Direction.UP, 0, 1, 2, 2),
                    face(Direction.DOWN, 0, 0, 2, 1)),
            element(7, 9, 2, 9, 10, 14,
                    face(Direction.NORTH, 0, 0, 2, 1),
                    face(Direction.EAST, 0, 0, 12, 1),
                    face(Direction.SOUTH, 0, 0, 2, 1),
                    face(Direction.WEST, 0, 0, 12, 1),
                    face(Direction.UP, 0, 0, 2, 12),
                    face(Direction.DOWN, 0, 0, 2, 12))
    );

    public static final List<Element> SKULL = List.of(
            element(3, 3, 3, 3, 9, 13,
                    face(Direction.NORTH, 1, 0, 1, 6),
                    face(Direction.EAST, 0, 0, 10, 6),
                    face(Direction.SOUTH, 1, 0, 1, 6),
                    face(Direction.WEST, 0, 0, 10, 6),
                    face(Direction.UP, 1, 0, 1, 10),
                    face(Direction.DOWN, 1, 0, 1, 10)),
            element(13, 3, 3, 13, 9, 13,
                    face(Direction.NORTH, 1, 0, 1, 6),
                    face(Direction.EAST, 0, 0, 10, 6),
                    face(Direction.SOUTH, 1, 0, 1, 6),
                    face(Direction.WEST, 0, 0, 10, 6),
                    face(Direction.UP, 1, 0, 1, 10),
                    face(Direction.DOWN, 1, 0, 1, 10)),
            element(13, 2, 8, 13, 3, 13,
                    face(Direction.NORTH, 1, 4, 1, 5),
                    face(Direction.EAST, 0, 4, 5, 5),
                    face(Direction.SOUTH, 1, 4, 1, 5),
                    face(Direction.WEST, 5, 4, 10, 5),
                    face(Direction.UP, 1, 5, 1, 10),
                    face(Direction.DOWN, 1, 0, 1, 5)),
            element(3, 2, 8, 3, 3, 13,
                    face(Direction.NORTH, 1, 4, 1, 5),
                    face(Direction.EAST, 0, 4, 5, 5),
                    face(Direction.SOUTH, 1, 4, 1, 5),
                    face(Direction.WEST, 5, 4, 10, 5),
                    face(Direction.UP, 1, 5, 1, 10),
                    face(Direction.DOWN, 1, 0, 1, 5)),
            element(3, 2, 13, 13, 9, 13,
                    face(Direction.NORTH, 0, 0, 10, 7),
                    face(Direction.EAST, 1, 0, 1, 7),
                    face(Direction.SOUTH, 0, 0, 10, 7),
                    face(Direction.WEST, 1, 0, 1, 7),
                    face(Direction.UP, 0, 1, 10, 1),
                    face(Direction.DOWN, 0, 1, 10, 1)),
            element(6, 1, 13, 10, 2, 13,
                    face(Direction.NORTH, 3, 6, 7, 7),
                    face(Direction.EAST, 1, 6, 1, 7),
                    face(Direction.SOUTH, 3, 6, 7, 7),
                    face(Direction.WEST, 1, 6, 1, 7),
                    face(Direction.UP, 3, 1, 7, 1),
                    face(Direction.DOWN, 3, 1, 7, 1)),
            element(3, 5, 3, 13, 9, 3,
                    face(Direction.NORTH, 0, 0, 10, 4),
                    face(Direction.EAST, 1, 0, 1, 4),
                    face(Direction.SOUTH, 0, 0, 10, 4),
                    face(Direction.WEST, 1, 0, 1, 4),
                    face(Direction.UP, 0, 1, 10, 1),
                    face(Direction.DOWN, 0, 1, 10, 1)),
            element(3, 9, 3, 13, 9, 13,
                    face(Direction.NORTH, 0, 2, 10, 2),
                    face(Direction.EAST, 0, 2, 10, 2),
                    face(Direction.SOUTH, 0, 2, 10, 2),
                    face(Direction.WEST, 0, 2, 10, 2),
                    face(Direction.UP, 0, 0, 10, 10),
                    face(Direction.DOWN, 0, 0, 10, 10))
    );

    public static final List<Element> VISOR = List.of(
            element(2, 1, 3, 3, 3, 10,
                    face(Direction.NORTH, 0, 0, 1, 2),
                    face(Direction.EAST, 0, 0, 7, 2),
                    face(Direction.SOUTH, 1, 0, 2, 2),
                    face(Direction.WEST, 0, 0, 7, 2),
                    face(Direction.UP, 1, 0, 2, 7),
                    face(Direction.DOWN, 1, 0, 2, 7)),
            element(13, 1, 3, 14, 3, 10,
                    face(Direction.NORTH, 0, 0, 1, 2),
                    face(Direction.EAST, 0, 0, 7, 2),
                    face(Direction.SOUTH, 1, 0, 2, 2),
                    face(Direction.WEST, 0, 0, 7, 2),
                    face(Direction.UP, 1, 0, 2, 7),
                    face(Direction.DOWN, 1, 0, 2, 7)),
            element(2, 0, 2, 14, 2, 3,
                    face(Direction.NORTH, 2, 0, 14, 2),
                    face(Direction.EAST, 0, 0, 1, 2),
                    face(Direction.SOUTH, 0, 0, 12, 2),
                    face(Direction.WEST, 1, 0, 2, 2),
                    face(Direction.UP, 0, 1, 12, 2),
                    face(Direction.DOWN, 0, 0, 12, 1))
    );

    private ModularHelmetItemGeometry() {
    }

    private static Element element(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Face... faces) {
        return new Element(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ), List.of(faces));
    }

    private static Face face(Direction direction, float u0, float v0, float u1, float v1) {
        return new Face(direction, new BlockFaceUV(new float[]{u0, v0, u1, v1}, 0));
    }

    public record Element(Vector3f from, Vector3f to, List<Face> faces) {
        public boolean hasArea(Direction direction) {
            float width = to.x() - from.x();
            float height = to.y() - from.y();
            float depth = to.z() - from.z();
            return switch (direction) {
                case DOWN, UP -> width != 0.0F && depth != 0.0F;
                case NORTH, SOUTH -> width != 0.0F && height != 0.0F;
                case WEST, EAST -> depth != 0.0F && height != 0.0F;
            };
        }
    }

    public record Face(Direction direction, BlockFaceUV uv) {
    }
}

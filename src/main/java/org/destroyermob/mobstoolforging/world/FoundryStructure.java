package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModTags;

/**
 * Discovers and validates an open-topped foundry around a controller in its
 * lowest wall layer. The interior may be from 1x1 to 9x9 and up to eight
 * blocks tall. The interior floor and four straight wall faces must belong to
 * the datapack tag for foundry structure blocks. Wall corners are optional,
 * matching the open-corner construction used by Tinkers-style foundries.
 */
public record FoundryStructure(
        boolean formed,
        BlockPos interiorMin,
        BlockPos interiorMax,
        int width,
        int depth,
        int height
) {
    private static final int MAX_INTERIOR_SPAN = 9;
    private static final int MAX_INTERIOR_HEIGHT = 8;
    public static final FoundryStructure UNFORMED = new FoundryStructure(false, BlockPos.ZERO, BlockPos.ZERO, 0, 0, 0);

    public static FoundryStructure find(LevelReader level, BlockPos controllerPos, BlockState controllerState) {
        if (!controllerState.is(ModBlocks.FOUNDRY_FORGE.get()) || !controllerState.hasProperty(FoundryForgeBlock.FACING)) {
            return UNFORMED;
        }

        Direction outward = controllerState.getValue(FoundryForgeBlock.FACING);
        Direction inward = outward.getOpposite();
        Direction lateral = outward.getClockWise();
        BlockPos origin = controllerPos.relative(inward);
        if (!level.getBlockState(origin).isAir()) {
            return UNFORMED;
        }

        int negativeWall = findWall(level, origin, lateral.getOpposite());
        int positiveWall = findWall(level, origin, lateral);
        int backWall = findWall(level, origin, inward);
        if (negativeWall < 1 || positiveWall < 1 || backWall < 1) {
            return UNFORMED;
        }

        int width = negativeWall + positiveWall - 1;
        int depth = backWall;
        if (width > MAX_INTERIOR_SPAN || depth > MAX_INTERIOR_SPAN) {
            return UNFORMED;
        }

        BlockPos frontLeft = origin.relative(outward).relative(lateral, -negativeWall);
        if (!validInteriorFloor(level, frontLeft, lateral, inward, width + 2, depth + 2)
                || !validWallLayer(level, controllerPos, frontLeft, lateral, inward, width + 2, depth + 2, 0)) {
            return UNFORMED;
        }

        int height = 1;
        while (height < MAX_INTERIOR_HEIGHT) {
            LayerResult result = inspectWallLayer(level, controllerPos, frontLeft, lateral, inward, width + 2, depth + 2, height);
            if (result == LayerResult.EMPTY) {
                break;
            }
            if (result == LayerResult.INVALID) {
                return UNFORMED;
            }
            height++;
        }

        BlockPos firstInterior = frontLeft.relative(lateral).relative(inward);
        BlockPos oppositeInterior = firstInterior.relative(lateral, width - 1).relative(inward, depth - 1).above(height - 1);
        BlockPos min = new BlockPos(
                Math.min(firstInterior.getX(), oppositeInterior.getX()),
                firstInterior.getY(),
                Math.min(firstInterior.getZ(), oppositeInterior.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(firstInterior.getX(), oppositeInterior.getX()),
                oppositeInterior.getY(),
                Math.max(firstInterior.getZ(), oppositeInterior.getZ())
        );
        return new FoundryStructure(true, min, max, width, depth, height);
    }

    public int interiorVolume() {
        return width * depth * height;
    }

    private static int findWall(LevelReader level, BlockPos origin, Direction direction) {
        for (int distance = 1; distance <= MAX_INTERIOR_SPAN + 1; distance++) {
            BlockState state = level.getBlockState(origin.relative(direction, distance));
            if (isShell(state)) {
                return distance;
            }
            if (!state.isAir()) {
                return -1;
            }
        }
        return -1;
    }

    private static boolean validInteriorFloor(LevelReader level, BlockPos frontLeft, Direction lateral, Direction inward, int outerWidth, int outerDepth) {
        for (int x = 1; x < outerWidth - 1; x++) {
            for (int z = 1; z < outerDepth - 1; z++) {
                if (!isShell(level.getBlockState(frontLeft.relative(lateral, x).relative(inward, z).below()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean validWallLayer(LevelReader level, BlockPos controllerPos, BlockPos frontLeft, Direction lateral, Direction inward, int outerWidth, int outerDepth, int y) {
        return inspectWallLayer(level, controllerPos, frontLeft, lateral, inward, outerWidth, outerDepth, y) == LayerResult.COMPLETE;
    }

    private static LayerResult inspectWallLayer(LevelReader level, BlockPos controllerPos, BlockPos frontLeft, Direction lateral, Direction inward, int outerWidth, int outerDepth, int y) {
        boolean anyBlock = false;
        for (int x = 0; x < outerWidth; x++) {
            for (int z = 0; z < outerDepth; z++) {
                BlockPos pos = frontLeft.relative(lateral, x).relative(inward, z).above(y);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir()) {
                    anyBlock = true;
                }
            }
        }
        if (!anyBlock) {
            return LayerResult.EMPTY;
        }
        for (int x = 0; x < outerWidth; x++) {
            for (int z = 0; z < outerDepth; z++) {
                BlockPos pos = frontLeft.relative(lateral, x).relative(inward, z).above(y);
                BlockState state = level.getBlockState(pos);
                boolean onWidthEdge = x == 0 || x == outerWidth - 1;
                boolean onDepthEdge = z == 0 || z == outerDepth - 1;
                boolean corner = onWidthEdge && onDepthEdge;
                boolean wallFace = (onWidthEdge || onDepthEdge) && !corner;
                if (wallFace) {
                    boolean currentController = pos.equals(controllerPos) && state.is(ModBlocks.FOUNDRY_FORGE.get());
                    if (!currentController && !isShell(state)) {
                        return LayerResult.INVALID;
                    }
                    if (!currentController && state.is(ModBlocks.FOUNDRY_FORGE.get())) {
                        return LayerResult.INVALID;
                    }
                } else if (corner) {
                    if (!state.isAir() && !isShell(state)) {
                        return LayerResult.INVALID;
                    }
                    if (state.is(ModBlocks.FOUNDRY_FORGE.get())) {
                        return LayerResult.INVALID;
                    }
                } else if (!state.isAir()) {
                    return LayerResult.INVALID;
                }
            }
        }
        return LayerResult.COMPLETE;
    }

    private static boolean isShell(BlockState state) {
        return state.is(ModTags.Blocks.FOUNDRY_STRUCTURE_BLOCKS);
    }

    private enum LayerResult {
        EMPTY,
        COMPLETE,
        INVALID
    }
}

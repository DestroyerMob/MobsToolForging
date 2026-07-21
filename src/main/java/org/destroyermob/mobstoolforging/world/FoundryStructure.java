package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
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
        return diagnose(level, controllerPos, controllerState).structure();
    }

    /** Returns the first actionable construction problem as well as any dimensions that were discoverable. */
    public static Diagnosis diagnose(LevelReader level, BlockPos controllerPos, BlockState controllerState) {
        if (!controllerState.is(ModBlocks.FOUNDRY_FORGE.get()) || !controllerState.hasProperty(FoundryForgeBlock.FACING)) {
            return Diagnosis.failure(Failure.INVALID_CONTROLLER, controllerPos);
        }

        Direction outward = controllerState.getValue(FoundryForgeBlock.FACING);
        Direction inward = outward.getOpposite();
        Direction lateral = outward.getClockWise();
        BlockPos origin = controllerPos.relative(inward);
        if (!level.getBlockState(origin).isAir()) {
            return Diagnosis.failure(Failure.ENTRANCE_BLOCKED, origin);
        }

        WallSearch negativeWall = findWall(level, origin, lateral.getOpposite());
        if (!negativeWall.found()) {
            return Diagnosis.failure(negativeWall.blocked() ? Failure.WALL_PATH_BLOCKED : Failure.SIDE_WALL_MISSING,
                    negativeWall.problemPos());
        }
        WallSearch positiveWall = findWall(level, origin, lateral);
        if (!positiveWall.found()) {
            return Diagnosis.failure(positiveWall.blocked() ? Failure.WALL_PATH_BLOCKED : Failure.SIDE_WALL_MISSING,
                    positiveWall.problemPos());
        }
        WallSearch backWall = findWall(level, origin, inward);
        if (!backWall.found()) {
            return Diagnosis.failure(backWall.blocked() ? Failure.WALL_PATH_BLOCKED : Failure.BACK_WALL_MISSING,
                    backWall.problemPos());
        }

        int width = negativeWall.distance() + positiveWall.distance() - 1;
        int depth = backWall.distance();
        if (width > MAX_INTERIOR_SPAN) {
            return Diagnosis.failure(Failure.TOO_WIDE, origin);
        }
        if (depth > MAX_INTERIOR_SPAN) {
            return Diagnosis.failure(Failure.TOO_DEEP, origin);
        }

        BlockPos frontLeft = origin.relative(outward).relative(lateral, -negativeWall.distance());
        Optional<Problem> floorProblem = inspectInteriorFloor(level, frontLeft, lateral, inward, width + 2, depth + 2);
        if (floorProblem.isPresent()) {
            return Diagnosis.failure(floorProblem.get().failure(), floorProblem.get().pos());
        }
        LayerInspection base = inspectWallLayer(level, controllerPos, frontLeft, lateral, inward, width + 2, depth + 2, 0);
        if (base.result() != LayerResult.COMPLETE) {
            return Diagnosis.failure(base.failure(), base.problemPos().orElse(controllerPos));
        }

        int height = 1;
        while (height < MAX_INTERIOR_HEIGHT) {
            LayerInspection result = inspectWallLayer(level, controllerPos, frontLeft, lateral, inward,
                    width + 2, depth + 2, height);
            if (result.result() == LayerResult.EMPTY) {
                break;
            }
            if (result.result() == LayerResult.INVALID) {
                return Diagnosis.failure(result.failure(), result.problemPos().orElse(controllerPos.above(height)));
            }
            height++;
        }

        // A maximum-height foundry still needs a completely open ninth layer.
        if (height == MAX_INTERIOR_HEIGHT) {
            Optional<BlockPos> roof = firstNonAir(level, frontLeft, lateral, inward, width + 2, depth + 2, height);
            if (roof.isPresent()) {
                return Diagnosis.failure(Failure.TOP_BLOCKED, roof.get());
            }
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
        return Diagnosis.success(new FoundryStructure(true, min, max, width, depth, height));
    }

    public int interiorVolume() {
        return width * depth * height;
    }

    private static WallSearch findWall(LevelReader level, BlockPos origin, Direction direction) {
        for (int distance = 1; distance <= MAX_INTERIOR_SPAN + 1; distance++) {
            BlockPos pos = origin.relative(direction, distance);
            BlockState state = level.getBlockState(pos);
            if (isShell(state)) {
                return WallSearch.found(distance, pos);
            }
            if (!state.isAir()) {
                return WallSearch.blocked(pos);
            }
        }
        return WallSearch.missing(origin.relative(direction, MAX_INTERIOR_SPAN + 1));
    }

    private static Optional<Problem> inspectInteriorFloor(
            LevelReader level,
            BlockPos frontLeft,
            Direction lateral,
            Direction inward,
            int outerWidth,
            int outerDepth
    ) {
        for (int x = 1; x < outerWidth - 1; x++) {
            for (int z = 1; z < outerDepth - 1; z++) {
                BlockPos pos = frontLeft.relative(lateral, x).relative(inward, z).below();
                BlockState state = level.getBlockState(pos);
                if (!isShell(state)) {
                    return Optional.of(new Problem(state.isAir() ? Failure.FLOOR_MISSING : Failure.FLOOR_INVALID, pos));
                }
            }
        }
        return Optional.empty();
    }

    private static LayerInspection inspectWallLayer(
            LevelReader level,
            BlockPos controllerPos,
            BlockPos frontLeft,
            Direction lateral,
            Direction inward,
            int outerWidth,
            int outerDepth,
            int y
    ) {
        Optional<BlockPos> firstBlock = firstNonAir(level, frontLeft, lateral, inward, outerWidth, outerDepth, y);
        if (firstBlock.isEmpty()) {
            return LayerInspection.empty();
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
                    if (!currentController && state.is(ModBlocks.FOUNDRY_FORGE.get())) {
                        return LayerInspection.invalid(Failure.EXTRA_CONTROLLER, pos);
                    }
                    if (!currentController && !isShell(state)) {
                        return LayerInspection.invalid(state.isAir() ? Failure.WALL_MISSING : Failure.WALL_INVALID, pos);
                    }
                } else if (corner) {
                    if (state.is(ModBlocks.FOUNDRY_FORGE.get())) {
                        return LayerInspection.invalid(Failure.EXTRA_CONTROLLER, pos);
                    }
                    if (!state.isAir() && !isShell(state)) {
                        return LayerInspection.invalid(Failure.WALL_INVALID, pos);
                    }
                } else if (!state.isAir()) {
                    return LayerInspection.invalid(Failure.INTERIOR_BLOCKED, pos);
                }
            }
        }
        return LayerInspection.complete();
    }

    private static Optional<BlockPos> firstNonAir(
            LevelReader level,
            BlockPos frontLeft,
            Direction lateral,
            Direction inward,
            int outerWidth,
            int outerDepth,
            int y
    ) {
        for (int x = 0; x < outerWidth; x++) {
            for (int z = 0; z < outerDepth; z++) {
                BlockPos pos = frontLeft.relative(lateral, x).relative(inward, z).above(y);
                if (!level.getBlockState(pos).isAir()) {
                    return Optional.of(pos);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isShell(BlockState state) {
        return state.is(ModTags.Blocks.FOUNDRY_STRUCTURE_BLOCKS);
    }

    public enum Failure {
        NONE,
        INVALID_CONTROLLER,
        ENTRANCE_BLOCKED,
        SIDE_WALL_MISSING,
        BACK_WALL_MISSING,
        WALL_PATH_BLOCKED,
        TOO_WIDE,
        TOO_DEEP,
        FLOOR_MISSING,
        FLOOR_INVALID,
        WALL_MISSING,
        WALL_INVALID,
        EXTRA_CONTROLLER,
        INTERIOR_BLOCKED,
        TOP_BLOCKED,
        CONTENTS_EXCEED_CAPACITY
    }

    public record Diagnosis(FoundryStructure structure, Failure failure, Optional<BlockPos> problemPos) {
        public Diagnosis {
            problemPos = problemPos.map(BlockPos::immutable);
        }

        public boolean formed() {
            return failure == Failure.NONE && structure.formed();
        }

        public static Diagnosis success(FoundryStructure structure) {
            return new Diagnosis(structure, Failure.NONE, Optional.empty());
        }

        public static Diagnosis failure(Failure failure, BlockPos problemPos) {
            return new Diagnosis(UNFORMED, failure, Optional.of(problemPos));
        }

        public Diagnosis withFailure(Failure nextFailure, BlockPos nextProblemPos) {
            return new Diagnosis(structure, nextFailure, Optional.of(nextProblemPos));
        }
    }

    private record Problem(Failure failure, BlockPos pos) {
    }

    private record WallSearch(int distance, boolean blocked, BlockPos problemPos) {
        private boolean found() {
            return distance > 0;
        }

        private static WallSearch found(int distance, BlockPos pos) {
            return new WallSearch(distance, false, pos);
        }

        private static WallSearch blocked(BlockPos pos) {
            return new WallSearch(-1, true, pos);
        }

        private static WallSearch missing(BlockPos pos) {
            return new WallSearch(-1, false, pos);
        }
    }

    private enum LayerResult {
        EMPTY,
        COMPLETE,
        INVALID
    }

    private record LayerInspection(LayerResult result, Failure failure, Optional<BlockPos> problemPos) {
        private static LayerInspection empty() {
            return new LayerInspection(LayerResult.EMPTY, Failure.NONE, Optional.empty());
        }

        private static LayerInspection complete() {
            return new LayerInspection(LayerResult.COMPLETE, Failure.NONE, Optional.empty());
        }

        private static LayerInspection invalid(Failure failure, BlockPos pos) {
            return new LayerInspection(LayerResult.INVALID, failure, Optional.of(pos));
        }
    }
}

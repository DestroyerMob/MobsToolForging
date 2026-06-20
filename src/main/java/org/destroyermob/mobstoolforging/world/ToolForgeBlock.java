package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ToolForgeBlock extends ToolWorkstationBlock {
    public static final MapCodec<ToolForgeBlock> CODEC = simpleCodec(ToolForgeBlock::new);

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 4.0, 14.0, 1.0, 12.0),
            Block.box(4.0, 1.0, 5.0, 12.0, 2.0, 11.0),
            Block.box(5.0, 2.0, 6.0, 11.0, 8.0, 10.0),
            Block.box(3.0, 9.0, 11.0, 16.0, 11.0, 12.0),
            Block.box(2.0, 9.0, 5.0, 16.0, 11.0, 11.0),
            Block.box(0.0, 9.0, 7.0, 1.0, 11.0, 9.0),
            Block.box(1.0, 9.0, 6.0, 2.0, 11.0, 10.0),
            Block.box(3.0, 9.0, 4.0, 16.0, 11.0, 5.0),
            Block.box(2.0, 8.0, 6.0, 3.0, 9.0, 10.0),
            Block.box(1.0, 8.0, 7.0, 2.0, 9.0, 9.0),
            Block.box(3.0, 8.0, 5.0, 15.0, 9.0, 11.0)
    ).optimize();
    private static final VoxelShape EAST_SHAPE = rotateClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateClockwise(SOUTH_SHAPE);

    public ToolForgeBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.TOOL_FORGE);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape shapeForState(BlockState state) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case NORTH, UP, DOWN -> NORTH_SHAPE;
        };
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape rotated = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            rotated = Shapes.or(
                    rotated,
                    Shapes.box(1.0 - box.maxZ, box.minY, box.minX, 1.0 - box.minZ, box.maxY, box.maxX)
            );
        }
        return rotated.optimize();
    }
}

package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrudeAnvilBlock extends ToolForgeBlock {
    public static final MapCodec<CrudeAnvilBlock> CODEC = simpleCodec(CrudeAnvilBlock::new);

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(3.1, 0.0, 5.0, 13.1, 2.0, 11.0),
            Block.box(5.0, 2.0, 6.0, 11.0, 7.0, 10.0),
            Block.box(2.1, 7.0, 4.0, 14.1, 11.0, 12.0)
    ).optimize();
    private static final VoxelShape EAST_SHAPE = rotateClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateClockwise(SOUTH_SHAPE);

    public CrudeAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.CRUDE_ANVIL);
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

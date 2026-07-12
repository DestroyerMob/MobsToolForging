package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class LavaHeatingForgeBlock extends HeatingForgeBlock {
    public static final MapCodec<LavaHeatingForgeBlock> CODEC = simpleCodec(LavaHeatingForgeBlock::new);
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.box(0.0, 8.0, 0.0, 2.0, 16.0, 14.0),
            Block.box(14.0, 8.0, 2.0, 16.0, 16.0, 16.0),
            Block.box(2.0, 8.0, 0.0, 16.0, 16.0, 2.0),
            Block.box(0.0, 8.0, 14.0, 14.0, 16.0, 16.0),
            Block.box(0.0, 2.0, 0.0, 2.0, 8.0, 2.0),
            Block.box(14.0, 2.0, 0.0, 16.0, 8.0, 2.0),
            Block.box(0.0, 2.0, 14.0, 2.0, 8.0, 16.0),
            Block.box(14.0, 2.0, 14.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 2.0, 2.0, 2.0, 4.0, 14.0),
            Block.box(14.0, 2.0, 2.0, 16.0, 4.0, 14.0),
            Block.box(2.0, 2.0, 0.0, 14.0, 4.0, 2.0),
            Block.box(2.0, 2.0, 14.0, 14.0, 4.0, 16.0),
            Block.box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0)
    ).optimize();
    private static final VoxelShape EAST_SHAPE = rotateClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateClockwise(SOUTH_SHAPE);

    public LavaHeatingForgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LavaHeatingForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(
                blockEntityType,
                ModBlockEntities.LAVA_HEATING_FORGE.get(),
                HeatingForgeBlockEntity::serverTick
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> SOUTH_SHAPE;
            case SOUTH -> WEST_SHAPE;
            case WEST -> NORTH_SHAPE;
            default -> EAST_SHAPE;
        };
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.toAabbs().forEach(box -> rotated[0] = Shapes.or(
                rotated[0],
                Shapes.box(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX)
        ));
        return rotated[0].optimize();
    }
}

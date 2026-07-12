package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ToolmakersBenchBlock extends ToolWorkstationBlock {
    public static final MapCodec<ToolmakersBenchBlock> CODEC = simpleCodec(ToolmakersBenchBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0),
            Block.box(6.0, 1.0, 6.0, 10.0, 11.0, 10.0),
            Block.box(0.0, 11.0, 0.0, 16.0, 13.0, 16.0)
    ).optimize();

    public ToolmakersBenchBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.TOOLMAKERS_BENCH);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape shapeForState(BlockState state) {
        return SHAPE;
    }
}

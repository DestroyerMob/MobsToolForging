package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LapidaryTableBlock extends ToolWorkstationBlock {
    public static final MapCodec<LapidaryTableBlock> CODEC = simpleCodec(LapidaryTableBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 3.0, 9.0, 3.0),
            Block.box(13.0, 0.0, 1.0, 15.0, 9.0, 3.0),
            Block.box(1.0, 0.0, 13.0, 3.0, 9.0, 15.0),
            Block.box(13.0, 0.0, 13.0, 15.0, 9.0, 15.0),
            Block.box(0.0, 9.0, 0.0, 16.0, 10.0, 16.0),
            Block.box(1.0, 10.0, 15.0, 16.0, 12.0, 16.0),
            Block.box(15.0, 10.0, 0.0, 16.0, 12.0, 15.0),
            Block.box(0.0, 10.0, 1.0, 1.0, 12.0, 16.0),
            Block.box(0.0, 10.0, 0.0, 15.0, 12.0, 1.0),
            Block.box(5.0, 11.0, 13.0, 11.0, 12.0, 14.0),
            Block.box(3.0, 11.0, 4.0, 13.0, 12.0, 5.0),
            Block.box(4.0, 11.0, 3.0, 12.0, 12.0, 4.0),
            Block.box(5.0, 11.0, 2.0, 11.0, 12.0, 3.0),
            Block.box(4.0, 11.0, 12.0, 12.0, 12.0, 13.0),
            Block.box(3.0, 11.0, 11.0, 13.0, 12.0, 12.0),
            Block.box(2.0, 11.0, 10.0, 14.0, 12.0, 11.0),
            Block.box(2.0, 11.0, 9.0, 14.0, 12.0, 10.0),
            Block.box(2.0, 11.0, 8.0, 14.0, 12.0, 9.0),
            Block.box(2.0, 11.0, 7.0, 14.0, 12.0, 8.0),
            Block.box(2.0, 11.0, 6.0, 14.0, 12.0, 7.0),
            Block.box(2.0, 11.0, 5.0, 14.0, 12.0, 6.0)
    ).optimize();

    public LapidaryTableBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.LAPIDARY_TABLE);
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

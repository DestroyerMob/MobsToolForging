package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LapidaryTableBlock extends ToolWorkstationBlock {
    public static final MapCodec<LapidaryTableBlock> CODEC = simpleCodec(LapidaryTableBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

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

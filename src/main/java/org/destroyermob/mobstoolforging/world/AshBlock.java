package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AshBlock extends Block {
    public static final MapCodec<AshBlock> CODEC = simpleCodec(AshBlock::new);
    public static final int MAX_LAYERS = 16;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYERS);
    private static final VoxelShape[] SHAPE_BY_LAYER = shapesByLayer();

    public AshBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        int layers = state.getValue(LAYERS);
        if (!useContext.getItemInHand().is(asItem()) || layers >= MAX_LAYERS) {
            return layers == 1;
        }
        return useContext.replacingClickedOnBlock() ? useContext.getClickedFace() == Direction.UP : true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            return existing.setValue(LAYERS, Math.min(MAX_LAYERS, existing.getValue(LAYERS) + 1));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return Block.isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)
                || belowState.is(this) && belowState.getValue(LAYERS) == MAX_LAYERS;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    private static VoxelShape[] shapesByLayer() {
        VoxelShape[] shapes = new VoxelShape[MAX_LAYERS + 1];
        for (int layer = 0; layer <= MAX_LAYERS; layer++) {
            shapes[layer] = layer == 0
                    ? Block.box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)
                    : Block.box(0.0D, 0.0D, 0.0D, 16.0D, layer, 16.0D);
        }
        return shapes;
    }
}

package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeatherStationBlock extends ToolWorkstationBlock {
    public static final MapCodec<LeatherStationBlock> CODEC = simpleCodec(LeatherStationBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    private static final VoxelShape SHAPE = Shapes.block();

    public LeatherStationBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.LEATHER_STATION);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == BedPart.FOOT ? super.newBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos otherPos = context.getClickedPos().relative(otherHalfDirection(facing));
        Level level = context.getLevel();
        if (!level.getWorldBorder().isWithinBounds(otherPos) || !level.getBlockState(otherPos).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    public static Direction otherHalfDirection(BlockState state) {
        return otherHalfDirection(state.getValue(FACING));
    }

    public static Direction otherHalfDirection(Direction facing) {
        return facing.getClockWise();
    }

    public static BlockPos footPos(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(otherHalfDirection(state).getOpposite());
    }

    public static BlockPos otherHalfPos(BlockPos pos, BlockState state) {
        Direction direction = otherHalfDirection(state);
        return state.getValue(PART) == BedPart.FOOT ? pos.relative(direction) : pos.relative(direction.getOpposite());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos otherPos = otherHalfPos(pos, state);
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(PART) != state.getValue(PART)) {
                if (player.isCreative() && level.getBlockEntity(otherPos) instanceof ToolForgeBlockEntity forge) {
                    forge.reset();
                }
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
            if (!player.isCreative() && state.getValue(PART) == BedPart.HEAD) {
                popResource(level, pos, new ItemStack(this));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction otherDirection = state.getValue(PART) == BedPart.FOOT ? otherHalfDirection(state) : otherHalfDirection(state).getOpposite();
        if (direction == otherDirection && (!neighborState.is(this) || neighborState.getValue(PART) == state.getValue(PART))) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        StationTarget target = stationTarget(level, pos, state);
        if (target == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, target.state(), level, target.pos(), player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        StationTarget target = stationTarget(level, pos, state);
        if (target == null) {
            return InteractionResult.PASS;
        }
        return super.useWithoutItem(target.state(), level, target.pos(), player, hitResult);
    }

    @Nullable
    private StationTarget stationTarget(Level level, BlockPos pos, BlockState state) {
        BlockPos footPos = footPos(pos, state);
        BlockState footState = level.getBlockState(footPos);
        if (!footState.is(this) || footState.getValue(PART) != BedPart.FOOT) {
            return null;
        }
        return new StationTarget(footPos, footState);
    }

    @Override
    protected VoxelShape shapeForState(BlockState state) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    private record StationTarget(BlockPos pos, BlockState state) {
    }
}

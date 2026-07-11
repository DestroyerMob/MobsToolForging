package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;

public class PatternRackBlock extends BaseEntityBlock {
    public static final MapCodec<PatternRackBlock> CODEC = simpleCodec(PatternRackBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 12.0D, 16.0D, 2.0D, 16.0D),
            Block.box(1.0D, 2.0D, 13.0D, 15.0D, 16.0D, 15.0D),
            Block.box(1.0D, 0.0D, 13.0D, 3.0D, 12.0D, 15.0D),
            Block.box(13.0D, 0.0D, 13.0D, 15.0D, 12.0D, 15.0D)
    ).optimize();
    private static final VoxelShape EAST_SHAPE = rotateClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateClockwise(SOUTH_SHAPE);

    private static final double GRID_MIN_U = 1.0D / 16.0D;
    private static final double GRID_MAX_U = 15.0D / 16.0D;
    private static final double GRID_MIN_Y = 1.0D / 16.0D;
    private static final double GRID_MAX_Y = 15.0D / 16.0D;
    private static final double FIRST_SLOT_SPLIT = 5.5D / 16.0D;
    private static final double SECOND_SLOT_SPLIT = 10.5D / 16.0D;

    public PatternRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PatternRackBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof PatternRackBlockEntity rack && SmithingHammerLevel.isHammer(stack)) {
            if (!MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_rack_disabled"));
                }
                return ItemInteractionResult.CONSUME;
            }
            if (player.isShiftKeyDown()) {
                PatternRackSelection.beginRackLink(player, level, pos);
                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 1.25F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            return itemResult(handlePatternUse(rack, state, level, pos, player, hitResult), level);
        }
        if (!(level.getBlockEntity(pos) instanceof PatternRackBlockEntity rack) || !PatternRackBlockEntity.isValidPattern(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()) {
            if (!level.isClientSide) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_rack_disabled"));
            }
            return ItemInteractionResult.CONSUME;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        int installedSlot = rack.installPattern(slotFromHit(state, pos, hitResult), stack, player.getAbilities().instabuild);
        if (installedSlot < 0) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_rack_full"));
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.05F);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_installed", rack.patternStack(installedSlot).getHoverName()));
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PatternRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown() && EmptyMainHandInteractions.shouldDeferToOffhand(
                player,
                stack -> MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()
                        && (SmithingHammerLevel.isHammer(stack) || PatternRackBlockEntity.isValidPattern(stack))
        )) {
            return InteractionResult.PASS;
        }
        if (!MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()) {
            if (!level.isClientSide) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_rack_disabled"));
            }
            return InteractionResult.CONSUME;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return handlePatternUse(rack, state, level, pos, player, hitResult);
    }

    private static InteractionResult handlePatternUse(PatternRackBlockEntity rack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int slot = slotFromHit(state, pos, hitResult);
        if (player.isShiftKeyDown()) {
            ItemStack removed = rack.removePattern(slot);
            if (removed.isEmpty()) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.no_pattern_selected"));
                return InteractionResult.CONSUME;
            }
            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.65F, 1.1F);
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_removed"));
            return InteractionResult.CONSUME;
        }
        ItemStack pattern = rack.patternStack(slot);
        if (PatternRackSelection.assignFromRack(player, level, pos, slot, pattern)) {
            return InteractionResult.CONSUME;
        }
        if (PatternRackSelection.assignToLinkedStations(player, level, pos, slot, pattern)) {
            return InteractionResult.CONSUME;
        }
        inspectPattern(player, pattern);
        return InteractionResult.CONSUME;
    }

    private static ItemInteractionResult itemResult(InteractionResult result, Level level) {
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static void inspectPattern(Player player, ItemStack pattern) {
        ForgeTemplateDefinition template = PatternRackBlockEntity.template(pattern).orElse(null);
        if (template == null) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.no_pattern_selected"));
            return;
        }
        DebugFeedback.actionBar(player, Component.translatable(
                "message.mobstoolforging.pattern_rack_inspect",
                template.displayName(),
                PatternRackSelection.compatibleStations(pattern, template)
        ));
    }

    static int slotFromHit(BlockState state, BlockPos pos, BlockHitResult hitResult) {
        Vec3 local = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        Direction facing = state.getValue(FACING);
        double modelX = switch (facing) {
            case SOUTH -> 1.0D - local.x;
            case EAST -> local.z;
            case WEST -> 1.0D - local.z;
            case NORTH, UP, DOWN -> local.x;
        };
        double y = local.y;
        if (modelX < GRID_MIN_U || modelX > GRID_MAX_U || y < GRID_MIN_Y || y > GRID_MAX_Y) {
            return -1;
        }
        int column = slotIndex(modelX);
        int row = 2 - slotIndex(y);
        return row * 3 + column;
    }

    private static int slotIndex(double coordinate) {
        if (coordinate < FIRST_SLOT_SPLIT) {
            return 0;
        }
        return coordinate < SECOND_SLOT_SPLIT ? 1 : 2;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PatternRackBlockEntity rack) {
            for (ItemStack pattern : rack.dropStacks()) {
                Block.popResource(level, pos, pattern);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case NORTH, UP, DOWN -> NORTH_SHAPE;
        };
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape rotated = Shapes.empty();
        for (var box : shape.toAabbs()) {
            rotated = Shapes.or(
                    rotated,
                    Shapes.box(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX)
            );
        }
        return rotated.optimize();
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
        builder.add(FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}

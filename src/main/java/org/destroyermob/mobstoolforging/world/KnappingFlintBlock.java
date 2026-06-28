package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModTags;

public class KnappingFlintBlock extends BaseEntityBlock {
    public static final MapCodec<KnappingFlintBlock> CODEC = simpleCodec(KnappingFlintBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);

    public KnappingFlintBlock(BlockBehaviour.Properties properties) {
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
        return new KnappingFlintBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get() || !stack.is(ModTags.Items.KNAPPING_TOOLS)) {
            return pickUpWork(level, pos);
        }
        if (!(level.getBlockEntity(pos) instanceof KnappingFlintBlockEntity knapping)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        boolean complete = knapping.hit();
        playChipEffects(level, pos);
        if (complete) {
            ItemStack output = knapping.outputStack();
            level.setBlock(pos, ModBlocks.GROUND_TOOL_ASSEMBLY.get().defaultBlockState().setValue(GroundToolAssemblyBlock.FACING, state.getValue(FACING)), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof GroundToolAssemblyBlockEntity assembly) {
                assembly.seed(output);
            } else if (!output.isEmpty()) {
                Block.popResource(level, pos, output);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 1.15F);
            player.displayClientMessage(Component.translatable("message.mobstoolforging.knapping_complete", output.getHoverName()), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.knapping_status", knapping.target().displayName(), knapping.hitCount(), KnappingFlintBlockEntity.REQUIRED_HITS), true);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.55F, 1.0F);
        return InteractionResult.CONSUME;
    }

    private static ItemInteractionResult pickUpWork(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.55F, 1.0F);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !newState.is(ModBlocks.GROUND_TOOL_ASSEMBLY.get())) {
            Block.popResource(level, pos, new ItemStack(Items.FLINT));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void playChipEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.35F, 1.25F + level.random.nextFloat() * 0.2F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.FLINT)),
                    pos.getX() + 0.5,
                    pos.getY() + 0.18,
                    pos.getZ() + 0.5,
                    6,
                    0.1,
                    0.04,
                    0.1,
                    0.015
            );
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return true;
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
}

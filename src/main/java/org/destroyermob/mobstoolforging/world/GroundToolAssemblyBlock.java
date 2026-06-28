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
import org.destroyermob.mobstoolforging.registry.ModItems;

public class GroundToolAssemblyBlock extends BaseEntityBlock {
    public static final MapCodec<GroundToolAssemblyBlock> CODEC = simpleCodec(GroundToolAssemblyBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public GroundToolAssemblyBlock(BlockBehaviour.Properties properties) {
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
        return new GroundToolAssemblyBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof GroundToolAssemblyBlockEntity assembly)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(ModItems.PLANT_FIBER.get())) {
            return assemble(stack, assembly, level, pos, player);
        }
        if (assembly.canAdd(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (assembly.addStack(stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 1.05F + level.random.nextFloat() * 0.1F);
                player.displayClientMessage(Component.translatable("message.mobstoolforging.ground_assembly_part_placed"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        return pickUpWork(level, pos);
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

    private ItemInteractionResult assemble(ItemStack fiber, GroundToolAssemblyBlockEntity assembly, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        ItemStack output = assembly.assemble(level.registryAccess());
        if (output.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.ground_assembly_needs_parts"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (!player.getAbilities().instabuild) {
            fiber.shrink(1);
        }
        assembly.removeStacks();
        level.removeBlock(pos, false);
        if (!player.getInventory().add(output)) {
            player.drop(output, false);
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.6F, 1.15F);
        return ItemInteractionResult.CONSUME;
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
        if (!level.isClientSide && !state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof GroundToolAssemblyBlockEntity assembly) {
            for (ItemStack stack : assembly.removeStacks()) {
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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

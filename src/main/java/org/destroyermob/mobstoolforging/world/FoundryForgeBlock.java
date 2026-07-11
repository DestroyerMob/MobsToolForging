package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class FoundryForgeBlock extends BaseEntityBlock {
    public static final MapCodec<FoundryForgeBlock> CODEC = simpleCodec(FoundryForgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FoundryForgeBlock(BlockBehaviour.Properties properties) {
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
        return new FoundryForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.FOUNDRY_FORGE.get(), FoundryForgeBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge) || !forge.isLit()) {
            return;
        }
        if (random.nextFloat() < 0.55F) {
            level.addParticle(
                    ParticleTypes.FLAME,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.24D,
                    pos.getY() + 0.42D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.24D,
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
        if (random.nextFloat() < 0.35F) {
            level.addParticle(
                    ParticleTypes.LAVA,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D,
                    pos.getY() + 0.55D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand) && !canHandleItem(stack, forge)) {
            return EmptyMainHandInteractions.itemResult(useWithoutItem(state, level, pos, player, hitResult), level);
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            forge.acceptLavaFuel();
            consumeLavaBucket(player, hand, stack);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 0.8F, 1.0F);
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.foundry_lava_added"));
            return ItemInteractionResult.CONSUME;
        }
        if (stack.is(ModItems.CRUCIBLE.get())) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (forge.acceptCrucible(stack)) {
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.55F, 0.9F);
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.foundry_crucible_added"));
            } else {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.foundry_crucible_busy"));
            }
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (EmptyMainHandInteractions.shouldDeferToOffhand(player, stack -> canHandleItem(stack, forge))) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (forge.hasCrucible()) {
            ItemStack crucible = forge.removeCrucible();
            if (!player.getInventory().add(crucible)) {
                player.drop(crucible, false);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.CONSUME;
        }
        DebugFeedback.actionBar(player, Component.translatable(forge.isLit() ? "message.mobstoolforging.foundry_lit" : "message.mobstoolforging.foundry_status"));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge) {
            ItemStack crucible = forge.crucibleDropStack();
            if (!crucible.isEmpty()) {
                Block.popResource(level, pos, crucible);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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

    private static boolean canHandleItem(ItemStack stack, FoundryForgeBlockEntity forge) {
        return stack.is(Items.LAVA_BUCKET) || forge.canAcceptCrucible(stack);
    }

    private static void consumeLavaBucket(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return;
        }
        if (stack.getCount() == 1) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            return;
        }
        stack.shrink(1);
        ItemStack bucket = new ItemStack(Items.BUCKET);
        if (!player.getInventory().add(bucket)) {
            player.drop(bucket, false);
        }
    }
}

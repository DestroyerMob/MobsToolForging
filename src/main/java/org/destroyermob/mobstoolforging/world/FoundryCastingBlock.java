package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class FoundryCastingBlock extends StatefulFoundryBlock {
    public static final MapCodec<FoundryCastingBlock> CODEC = simpleCodec(FoundryCastingBlock::new);
    private static final VoxelShape TABLE_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 2.0D, 10.0D, 2.0D),
            Block.box(14.0D, 0.0D, 0.0D, 16.0D, 10.0D, 2.0D),
            Block.box(0.0D, 0.0D, 14.0D, 2.0D, 10.0D, 16.0D),
            Block.box(14.0D, 0.0D, 14.0D, 16.0D, 10.0D, 16.0D),
            Block.box(0.0D, 10.0D, 0.0D, 16.0D, 12.0D, 16.0D)
    );
    private static final VoxelShape BASIN_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public FoundryCastingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryCastingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.FOUNDRY_CASTING.get(),
                level.isClientSide ? FoundryCastingBlockEntity::clientTick : FoundryCastingBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return FoundryCastingBlockEntity.isBasin(state) ? BASIN_SHAPE : TABLE_SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (casting.takeOutput(player)) {
            return InteractionResult.CONSUME;
        }
        return casting.takeForm(player) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting) || !casting.canInsertForm(stack)) {
            if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand)) {
                return EmptyMainHandInteractions.itemResult(useWithoutItem(state, level, pos, player, hitResult), level);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            casting.insertForm(stack);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 0.55F, 1.05F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

}

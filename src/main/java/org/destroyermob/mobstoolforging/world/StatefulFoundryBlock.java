package org.destroyermob.mobstoolforging.world;

import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;

/** Shared removal protocol for foundry blocks whose meaningful contents can travel with the item. */
abstract class StatefulFoundryBlock extends BaseEntityBlock {
    protected StatefulFoundryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        return FoundryBlockDrops.preserveBlockEntity(super.getDrops(state, loot), loot);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        FoundryBlockDrops.preserveCreativeContents(level, pos, player, level.getBlockEntity(pos));
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean onDestroyedByPlayer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            boolean willHarvest,
            FluidState fluid
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        FoundryBlockDrops.beginHarvestedRemoval(blockEntity, willHarvest && !level.isClientSide);
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (!removed) {
            FoundryBlockDrops.cancelHarvestedRemoval(blockEntity);
        }
        return removed;
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        try {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
        } finally {
            FoundryBlockDrops.finishHarvestedRemoval(blockEntity);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        FoundryBlockDrops.recoverOnRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void onExplosionHit(
            BlockState state,
            Level level,
            BlockPos pos,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> dropConsumer
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        FoundryBlockDrops.beginExplosionRemoval(blockEntity);
        boolean[] emittedSelfDrop = {false};
        try {
            super.onExplosionHit(state, level, pos, explosion, (stack, dropPos) -> {
                if (FoundryBlockDrops.prepareExplosionDrop(stack, blockEntity, level.registryAccess())) {
                    emittedSelfDrop[0] = true;
                    if (stack.getCount() > 1) {
                        ItemStack plainRemainder = stack.copyWithCount(stack.getCount() - 1);
                        plainRemainder.remove(DataComponents.BLOCK_ENTITY_DATA);
                        stack.setCount(1);
                        dropConsumer.accept(stack, dropPos);
                        dropConsumer.accept(plainRemainder, dropPos);
                        return;
                    }
                }
                dropConsumer.accept(stack, dropPos);
            });
        } finally {
            boolean removed = !level.getBlockState(pos).is(state.getBlock());
            FoundryBlockDrops.finishExplosionRemoval(blockEntity, removed, emittedSelfDrop[0], dropConsumer, pos);
        }
    }
}

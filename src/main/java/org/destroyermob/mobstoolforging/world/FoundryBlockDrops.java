package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Gives each stateful foundry removal path exactly one owner for its contents. */
public final class FoundryBlockDrops {
    private static final Set<BlockEntity> DEFERRED_REMOVALS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<BlockEntity> GENERATED_SELF_DROPS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<BlockEntity> HANDLED_DROP_EVENTS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<BlockEntity, Long> SUPPRESS_NEXT_FALLBACK = new WeakHashMap<>();

    private FoundryBlockDrops() {
    }

    /**
     * Adds portable state to the first self drop without changing the block entity. Loot previews
     * are allowed callers, so this method must remain free of removal-side effects.
     */
    static List<ItemStack> preserveBlockEntity(List<ItemStack> drops, LootParams.Builder loot) {
        BlockEntity blockEntity = loot.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!isStatefulFoundry(blockEntity)) {
            return drops;
        }
        List<ItemStack> preparedDrops = new ArrayList<>(drops);
        for (ItemStack drop : preparedDrops) {
            if (!drop.is(blockEntity.getBlockState().getBlock().asItem())) {
                continue;
            }
            if (drop.getCount() > 1) {
                ItemStack plainRemainder = drop.copyWithCount(drop.getCount() - 1);
                plainRemainder.remove(DataComponents.BLOCK_ENTITY_DATA);
                drop.setCount(1);
                preparedDrops.add(plainRemainder);
            }
            prepareSelfDrop(drop, blockEntity, loot.getLevel().registryAccess());
            if (DEFERRED_REMOVALS.contains(blockEntity)) {
                GENERATED_SELF_DROPS.add(blockEntity);
            }
            break;
        }
        return preparedDrops;
    }

    static void beginHarvestedRemoval(@Nullable BlockEntity blockEntity, boolean willHarvest) {
        if (willHarvest && isStatefulFoundry(blockEntity)) {
            DEFERRED_REMOVALS.add(blockEntity);
        }
    }

    static void cancelHarvestedRemoval(@Nullable BlockEntity blockEntity) {
        if (blockEntity != null) {
            DEFERRED_REMOVALS.remove(blockEntity);
            GENERATED_SELF_DROPS.remove(blockEntity);
            HANDLED_DROP_EVENTS.remove(blockEntity);
        }
    }

    static void finishHarvestedRemoval(@Nullable BlockEntity blockEntity) {
        if (blockEntity == null) {
            return;
        }
        boolean deferred = DEFERRED_REMOVALS.remove(blockEntity);
        boolean generatedSelfDrop = GENERATED_SELF_DROPS.remove(blockEntity);
        boolean eventHandled = HANDLED_DROP_EVENTS.remove(blockEntity);
        if (deferred && !generatedSelfDrop && !eventHandled) {
            dropFallbackItems(blockEntity);
        }
    }

    /** Emits a shulker-style state item for meaningful creative-mode contents. */
    static void preserveCreativeContents(Level level, BlockPos pos, Player player, @Nullable BlockEntity blockEntity) {
        if (level.isClientSide || !player.isCreative() || !isStatefulFoundry(blockEntity)) {
            return;
        }
        CompoundTag portableState = portableState(blockEntity, level.registryAccess());
        if (portableState.isEmpty()) {
            return;
        }
        ItemStack drop = new ItemStack(blockEntity.getBlockState().getBlock());
        BlockItem.setBlockEntityData(drop, blockEntity.getType(), portableState);
        SUPPRESS_NEXT_FALLBACK.put(blockEntity, level.getGameTime());
        Block.popResource(level, pos, drop);
    }

    static void beginExplosionRemoval(@Nullable BlockEntity blockEntity) {
        if (isStatefulFoundry(blockEntity)) {
            DEFERRED_REMOVALS.add(blockEntity);
        }
    }

    static boolean prepareExplosionDrop(ItemStack drop, @Nullable BlockEntity blockEntity, HolderLookup.Provider registries) {
        return isStatefulFoundry(blockEntity) && prepareSelfDrop(drop, blockEntity, registries);
    }

    static void finishExplosionRemoval(
            @Nullable BlockEntity blockEntity,
            boolean removed,
            boolean emittedSelfDrop,
            java.util.function.BiConsumer<ItemStack, BlockPos> dropConsumer,
            BlockPos pos
    ) {
        if (blockEntity == null) {
            return;
        }
        DEFERRED_REMOVALS.remove(blockEntity);
        GENERATED_SELF_DROPS.remove(blockEntity);
        HANDLED_DROP_EVENTS.remove(blockEntity);
        if (removed && !emittedSelfDrop) {
            takeFallbackItems(blockEntity).forEach(stack -> dropConsumer.accept(stack, pos));
        }
    }

    static void recoverOnRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (state.is(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!isStatefulFoundry(blockEntity)) {
            return;
        }
        Long suppressionTick = SUPPRESS_NEXT_FALLBACK.remove(blockEntity);
        boolean suppressionIsCurrent = suppressionTick != null && suppressionTick == level.getGameTime();
        if (DEFERRED_REMOVALS.contains(blockEntity) || suppressionIsCurrent) {
            return;
        }
        dropFallbackItems(blockEntity);
    }

    /**
     * Finalizes actual non-explosion loot after other modifiers have had a chance to replace it.
     * Register this at LOWEST priority.
     */
    public static void finalizeBlockDrops(BlockDropsEvent event) {
        BlockEntity blockEntity = event.getBlockEntity();
        if (!isStatefulFoundry(blockEntity)) {
            return;
        }

        boolean blockStillPresent = event.getLevel().getBlockState(event.getPos()).is(event.getState().getBlock());
        if (blockStillPresent) {
            SUPPRESS_NEXT_FALLBACK.put(blockEntity, event.getLevel().getGameTime());
        }
        if (DEFERRED_REMOVALS.contains(blockEntity)) {
            HANDLED_DROP_EVENTS.add(blockEntity);
        }
        if (event.isCanceled() || !event.getLevel().getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            return;
        }

        ItemEntity stateOwner = null;
        List<ItemEntity> plainRemainders = new ArrayList<>();
        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.is(event.getState().getBlock().asItem())) {
                continue;
            }
            if (stateOwner == null) {
                stateOwner = itemEntity;
                if (stack.getCount() > 1) {
                    ItemStack plainRemainder = stack.copyWithCount(stack.getCount() - 1);
                    plainRemainder.remove(DataComponents.BLOCK_ENTITY_DATA);
                    stack.setCount(1);
                    ItemEntity remainderEntity = new ItemEntity(
                            event.getLevel(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), plainRemainder
                    );
                    remainderEntity.setDefaultPickUpDelay();
                    plainRemainders.add(remainderEntity);
                }
                prepareSelfDrop(stack, blockEntity, event.getLevel().registryAccess());
            } else {
                stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            }
        }
        if (stateOwner != null) {
            event.getDrops().addAll(plainRemainders);
            return;
        }

        for (ItemStack stack : takeFallbackItems(blockEntity)) {
            ItemEntity itemEntity = new ItemEntity(
                    event.getLevel(),
                    event.getPos().getX() + 0.5D,
                    event.getPos().getY() + 0.5D,
                    event.getPos().getZ() + 0.5D,
                    stack
            );
            itemEntity.setDefaultPickUpDelay();
            event.getDrops().add(itemEntity);
        }
    }

    private static boolean prepareSelfDrop(ItemStack drop, BlockEntity blockEntity, HolderLookup.Provider registries) {
        if (!drop.is(blockEntity.getBlockState().getBlock().asItem())) {
            return false;
        }
        BlockItem.setBlockEntityData(drop, blockEntity.getType(), portableState(blockEntity, registries));
        return true;
    }

    private static CompoundTag portableState(BlockEntity blockEntity, HolderLookup.Provider registries) {
        if (blockEntity instanceof FoundryForgeBlockEntity forge) {
            return forge.savePortableState(registries);
        }
        if (blockEntity instanceof FoundryCastingBlockEntity casting) {
            return casting.savePortableState(registries);
        }
        if (blockEntity instanceof FoundryFuelTankBlockEntity tank) {
            return tank.savePortableState(registries);
        }
        return new CompoundTag();
    }

    private static List<ItemStack> takeFallbackItems(BlockEntity blockEntity) {
        if (blockEntity instanceof FoundryForgeBlockEntity forge) {
            return forge.takeFallbackItems();
        }
        if (blockEntity instanceof FoundryCastingBlockEntity casting) {
            return casting.takeFallbackItems();
        }
        return List.of();
    }

    private static void dropFallbackItems(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        takeFallbackItems(blockEntity).forEach(stack -> Block.popResource(level, blockEntity.getBlockPos(), stack));
    }

    private static boolean isStatefulFoundry(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof FoundryForgeBlockEntity
                || blockEntity instanceof FoundryCastingBlockEntity
                || blockEntity instanceof FoundryFuelTankBlockEntity;
    }
}

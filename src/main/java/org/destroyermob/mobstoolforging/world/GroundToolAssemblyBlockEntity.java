package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class GroundToolAssemblyBlockEntity extends BlockEntity {
    private static final String STACKS_TAG = "Stacks";
    private static final int MAX_STACKS = 9;

    private final List<ItemStack> stacks = new ArrayList<>();

    public GroundToolAssemblyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GROUND_TOOL_ASSEMBLY.get(), pos, blockState);
    }

    public List<ItemStack> stacks() {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    public boolean seed(ItemStack stack) {
        if (!stacks.isEmpty() || stack.isEmpty() || !GroundAssemblyRecipeRegistry.canStart(stack)) {
            return false;
        }
        stacks.add(stack.copyWithCount(1));
        sync();
        return true;
    }

    public boolean canAdd(ItemStack stack) {
        return stacks.size() < MAX_STACKS && GroundAssemblyRecipeRegistry.canAccept(stacks, stack);
    }

    public boolean addStack(ItemStack stack) {
        if (!canAdd(stack)) {
            return false;
        }
        stacks.add(stack.copyWithCount(1));
        stack.shrink(1);
        sync();
        return true;
    }

    public ItemStack assemble(HolderLookup.Provider registries) {
        return GroundAssemblyRecipeRegistry.assemble(stacks, registries);
    }

    public List<ItemStack> removeStacks() {
        List<ItemStack> removed = stacks();
        stacks.clear();
        sync();
        return removed;
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!stacks.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    list.add(stack.saveOptional(registries));
                }
            }
            tag.put(STACKS_TAG, list);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stacks.clear();
        if (tag.contains(STACKS_TAG)) {
            ListTag list = tag.getList(STACKS_TAG, Tag.TAG_COMPOUND);
            int limit = Math.min(MAX_STACKS, list.size());
            for (int index = 0; index < limit; index++) {
                ItemStack stack = ItemStack.parseOptional(registries, list.getCompound(index));
                if (!stack.isEmpty()) {
                    stacks.add(stack.copyWithCount(1));
                }
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}

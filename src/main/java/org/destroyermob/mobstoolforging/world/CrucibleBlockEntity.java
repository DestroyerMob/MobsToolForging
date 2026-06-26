package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class CrucibleBlockEntity extends BlockEntity {
    private static final String CONTENTS_TAG = "Contents";

    private CrucibleContents contents = CrucibleContents.EMPTY;

    public CrucibleBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, blockState);
    }

    public CrucibleContents contents() {
        return contents;
    }

    public void setContents(CrucibleContents contents) {
        this.contents = contents == null ? CrucibleContents.EMPTY : contents;
        sync();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public boolean acceptItem(ItemStack stack) {
        if (!isEmpty() || stack.isEmpty() || stack.is(ModItems.CRUCIBLE.get())) {
            return false;
        }
        setContents(CrucibleContents.ofItem(stack.split(1)));
        return true;
    }

    public ItemStack removeItem() {
        if (!contents.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack item = contents.item().copy();
        setContents(CrucibleContents.EMPTY);
        return item;
    }

    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(ModItems.CRUCIBLE.get());
        if (!contents.isEmpty()) {
            stack.set(ModDataComponents.CRUCIBLE_CONTENTS.get(), contents);
        }
        return stack;
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
        if (!contents.isEmpty()) {
            tag.put(CONTENTS_TAG, CrucibleContents.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), contents).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(CONTENTS_TAG)) {
            contents = CrucibleContents.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag.get(CONTENTS_TAG)).result().orElse(CrucibleContents.EMPTY);
        } else {
            contents = CrucibleContents.EMPTY;
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

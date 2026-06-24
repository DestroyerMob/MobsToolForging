package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class PatternCreationStationBlockEntity extends BlockEntity implements Container, MenuProvider {
    private static final String PAPER_TAG = "Paper";
    private static final Component CONTAINER_TITLE = Component.translatable("container.mobstoolforging.pattern_creation_station");

    private final List<Runnable> changeListeners = new ArrayList<>();
    private ItemStack paperStack = ItemStack.EMPTY;

    public PatternCreationStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PATTERN_CREATION_STATION.get(), pos, blockState);
    }

    public List<ForgeTemplateDefinition> availablePatterns() {
        return ToolTypeRegistry.patternStationTemplates();
    }

    public ItemStack paperDropStack() {
        return paperStack.copy();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (Runnable listener : List.copyOf(changeListeners)) {
            listener.run();
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return paperStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? paperStack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || amount <= 0 || paperStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = paperStack.split(amount);
        if (paperStack.isEmpty()) {
            paperStack = ItemStack.EMPTY;
        }
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = paperStack;
        paperStack = ItemStack.EMPTY;
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        paperStack = stack.isEmpty() || !stack.is(Items.PAPER) ? ItemStack.EMPTY : stack.copy();
        if (!paperStack.isEmpty() && paperStack.getCount() > getMaxStackSize()) {
            paperStack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.is(Items.PAPER);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.canInteractWithBlock(worldPosition, 4.0);
    }

    @Override
    public void clearContent() {
        paperStack = ItemStack.EMPTY;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return CONTAINER_TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PatternCreationStationMenu(containerId, playerInventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        PatternCreationStationMenu.writeClientData(buffer, worldPosition, availablePatterns());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!paperStack.isEmpty()) {
            tag.put(PAPER_TAG, paperStack.saveOptional(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        paperStack = tag.contains(PAPER_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(PAPER_TAG)) : ItemStack.EMPTY;
    }
}

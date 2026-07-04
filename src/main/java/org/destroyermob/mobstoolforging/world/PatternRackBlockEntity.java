package org.destroyermob.mobstoolforging.world;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class PatternRackBlockEntity extends BlockEntity {
    public static final int SLOT_COUNT = 9;
    private static final String PATTERNS_TAG = "Patterns";
    private static final String STACK_TAG = "Stack";
    private static final String SLOT_TAG = "Slot";

    private final NonNullList<ItemStack> patterns = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public PatternRackBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PATTERN_RACK.get(), pos, blockState);
    }

    public static Optional<ForgeTemplateDefinition> template(ItemStack stack) {
        if (!(stack.getItem() instanceof ToolTemplateItem templateItem)) {
            return Optional.empty();
        }
        return templateItem.template(stack);
    }

    public static Optional<ResourceLocation> templateId(ItemStack stack) {
        if (!(stack.getItem() instanceof ToolTemplateItem templateItem)) {
            return Optional.empty();
        }
        return templateItem.templateId(stack).filter(id -> ToolTypeRegistry.template(id).isPresent());
    }

    public static boolean isValidPattern(ItemStack stack) {
        return template(stack).isPresent();
    }

    public ItemStack patternStack(int slot) {
        return isValidSlot(slot) ? patterns.get(slot).copy() : ItemStack.EMPTY;
    }

    public List<ItemStack> patternStacks() {
        return patterns.stream().map(ItemStack::copy).toList();
    }

    public Optional<ResourceLocation> templateId(int slot) {
        return isValidSlot(slot) ? templateId(patterns.get(slot)) : Optional.empty();
    }

    public int occupiedSlots() {
        int count = 0;
        for (ItemStack pattern : patterns) {
            if (!pattern.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int installPattern(int preferredSlot, ItemStack heldStack, boolean creative) {
        if (!isValidPattern(heldStack)) {
            return -1;
        }
        int slot = isValidSlot(preferredSlot) && patterns.get(preferredSlot).isEmpty() ? preferredSlot : firstEmptySlot();
        if (slot < 0) {
            return -1;
        }
        patterns.set(slot, heldStack.copyWithCount(1));
        if (!creative) {
            heldStack.shrink(1);
        }
        sync();
        return slot;
    }

    public ItemStack removePattern(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = patterns.get(slot);
        if (removed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        patterns.set(slot, ItemStack.EMPTY);
        sync();
        return removed.copy();
    }

    public List<ItemStack> dropStacks() {
        return patternStacks().stream().filter(stack -> !stack.isEmpty()).toList();
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < patterns.size(); slot++) {
            if (patterns.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < SLOT_COUNT;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag storedPatterns = new ListTag();
        for (int slot = 0; slot < patterns.size(); slot++) {
            ItemStack pattern = patterns.get(slot);
            if (pattern.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putByte(SLOT_TAG, (byte) slot);
            entry.put(STACK_TAG, pattern.saveOptional(registries));
            storedPatterns.add(entry);
        }
        tag.put(PATTERNS_TAG, storedPatterns);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        patterns.replaceAll(ignored -> ItemStack.EMPTY);
        if (!tag.contains(PATTERNS_TAG)) {
            return;
        }
        ListTag storedPatterns = tag.getList(PATTERNS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < storedPatterns.size(); index++) {
            CompoundTag entry = storedPatterns.getCompound(index);
            int slot = entry.getByte(SLOT_TAG) & 255;
            ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound(STACK_TAG));
            if (stack.isEmpty()) {
                continue;
            }
            if (isValidSlot(slot)) {
                patterns.set(slot, stack);
            } else {
                int fallbackSlot = firstEmptySlot();
                if (fallbackSlot >= 0) {
                    patterns.set(fallbackSlot, stack);
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

package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModMenuTypes;

public class PatternCreationStationMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    private static final int INVENTORY_SLOT_START = 2;
    private static final int INVENTORY_SLOT_END = 29;
    private static final int HOTBAR_SLOT_START = 29;
    private static final int HOTBAR_SLOT_END = 38;

    private final ContainerLevelAccess access;
    private final DataSlot selectedPatternIndex = DataSlot.standalone();
    private final List<ResourceLocation> patternIds;
    private final Slot inputSlot;
    private final Slot resultSlot;
    private final Container inputContainer;
    private final boolean persistentInput;
    @Nullable
    private final PatternCreationStationBlockEntity station;
    private final ResultContainer resultContainer = new ResultContainer();
    private final Runnable inputChangeListener = this::inputContainerChanged;

    private ItemStack input = ItemStack.EMPTY;
    private long lastSoundTime;
    private Runnable slotUpdateListener = () -> {
    };

    public PatternCreationStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL, newInputContainer(), defaultPatternIds(), false, null);
    }

    public PatternCreationStationMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, readClientData(playerInventory, buffer));
    }

    public PatternCreationStationMenu(int containerId, Inventory playerInventory, PatternCreationStationBlockEntity station) {
        this(
                containerId,
                playerInventory,
                station.getLevel() == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(station.getLevel(), station.getBlockPos()),
                station,
                station.availablePatterns().stream().map(ForgeTemplateDefinition::id).toList(),
                true,
                station
        );
    }

    private PatternCreationStationMenu(int containerId, Inventory playerInventory, ClientMenuData data) {
        this(containerId, playerInventory, data.access(), newInputContainer(), data.patternIds(), false, null);
    }

    private PatternCreationStationMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, Container inputContainer, List<ResourceLocation> patternIds, boolean persistentInput, @Nullable PatternCreationStationBlockEntity station) {
        super(ModMenuTypes.PATTERN_CREATION_STATION.get(), containerId);
        this.access = access;
        this.inputContainer = inputContainer;
        this.patternIds = List.copyOf(patternIds);
        this.persistentInput = persistentInput;
        this.station = station;

        this.inputSlot = addSlot(new Slot(inputContainer, 0, 20, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.PAPER);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                if (PatternCreationStationMenu.this.station == null) {
                    PatternCreationStationMenu.this.inputContainerChanged();
                }
            }
        });
        this.resultSlot = addSlot(new Slot(resultContainer, 1, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                ItemStack consumed = PatternCreationStationMenu.this.inputSlot.remove(selectedPatternPaperCost());
                if (!consumed.isEmpty()) {
                    PatternCreationStationMenu.this.setupResultSlot();
                }

                access.execute((level, pos) -> {
                    long gameTime = level.getGameTime();
                    if (PatternCreationStationMenu.this.lastSoundTime != gameTime) {
                        level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        PatternCreationStationMenu.this.lastSoundTime = gameTime;
                    }
                });
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlot(selectedPatternIndex);
        if (station != null) {
            station.addChangeListener(inputChangeListener);
        }
    }

    public int getSelectedPatternIndex() {
        return selectedPatternIndex.get();
    }

    public int getNumPatterns() {
        return patternIds.size();
    }

    public ItemStack getPatternStack(int index) {
        if (!isValidPatternIndex(index)) {
            return ItemStack.EMPTY;
        }
        return ToolTemplateItem.createPatternStack(patternIds.get(index));
    }

    public int getPatternPaperCost(int index) {
        return pattern(index).map(ForgeTemplateDefinition::patternStationPaperCost).orElse(1);
    }

    public boolean hasInputItem() {
        return inputSlot.hasItem() && inputSlot.getItem().is(Items.PAPER) && !patternIds.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.PATTERN_CREATION_STATION.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.isSpectator()) {
            return false;
        }
        if (hasInputItem() && isValidPatternIndex(id)) {
            selectedPatternIndex.set(id);
            setupResultSlot();
        }
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack stack = inputSlot.getItem();
        boolean itemChanged = !ItemStack.isSameItemSameComponents(stack, input);
        input = stack.copy();
        if (itemChanged) {
            setupPatternList(stack);
        } else {
            setupResultSlot();
        }
    }

    private void setupPatternList(ItemStack stack) {
        selectedPatternIndex.set(-1);
        resultSlot.set(ItemStack.EMPTY);
        if (!stack.is(Items.PAPER)) {
            input = ItemStack.EMPTY;
        }
        broadcastChanges();
    }

    private void setupResultSlot() {
        if (hasInputItem() && isValidPatternIndex(selectedPatternIndex.get()) && inputSlot.getItem().getCount() >= selectedPatternPaperCost()) {
            resultSlot.set(ToolTemplateItem.createPatternStack(patternIds.get(selectedPatternIndex.get())));
        } else {
            resultSlot.set(ItemStack.EMPTY);
        }
        broadcastChanges();
    }

    private boolean isValidPatternIndex(int index) {
        return index >= 0 && index < patternIds.size();
    }

    private Optional<ForgeTemplateDefinition> pattern(int index) {
        return isValidPatternIndex(index) ? ToolTypeRegistry.template(patternIds.get(index)) : Optional.empty();
    }

    private int selectedPatternPaperCost() {
        return getPatternPaperCost(selectedPatternIndex.get());
    }

    public void registerUpdateListener(Runnable listener) {
        slotUpdateListener = listener;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            Item item = slotStack.getItem();
            movedStack = slotStack.copy();
            if (index == RESULT_SLOT) {
                item.onCraftedBy(slotStack, player.level(), player);
                if (!moveItemStackTo(slotStack, INVENTORY_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, movedStack);
            } else if (index == INPUT_SLOT) {
                if (!moveItemStackTo(slotStack, INVENTORY_SLOT_START, HOTBAR_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.is(Items.PAPER)) {
                if (!moveItemStackTo(slotStack, INPUT_SLOT, RESULT_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INVENTORY_SLOT_START && index < INVENTORY_SLOT_END) {
                if (!moveItemStackTo(slotStack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_SLOT_START && index < HOTBAR_SLOT_END && !moveItemStackTo(slotStack, INVENTORY_SLOT_START, INVENTORY_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (slotStack.getCount() == movedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
            broadcastChanges();
        }

        return movedStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        resultContainer.removeItemNoUpdate(1);
        if (station != null) {
            station.removeChangeListener(inputChangeListener);
        }
        if (!persistentInput) {
            access.execute((level, pos) -> clearContainer(player, inputContainer));
        }
    }

    public static void writeClientData(RegistryFriendlyByteBuf buffer, BlockPos pos, List<ForgeTemplateDefinition> patterns) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(patterns.size());
        for (ForgeTemplateDefinition pattern : patterns) {
            buffer.writeResourceLocation(pattern.id());
        }
    }

    private static ClientMenuData readClientData(Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        if (buffer == null) {
            return new ClientMenuData(ContainerLevelAccess.NULL, defaultPatternIds());
        }
        BlockPos pos = buffer.readBlockPos();
        int count = Math.max(0, Math.min(1024, buffer.readVarInt()));
        List<ResourceLocation> patternIds = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            patternIds.add(buffer.readResourceLocation());
        }
        return new ClientMenuData(ContainerLevelAccess.create(playerInventory.player.level(), pos), patternIds);
    }

    private static Container newInputContainer() {
        return new SimpleContainer(1);
    }

    private static List<ResourceLocation> defaultPatternIds() {
        return ToolTypeRegistry.patternStationTemplates().stream()
                .filter(template -> !ToolTemplateItem.createPatternStack(template).isEmpty())
                .map(ForgeTemplateDefinition::id)
                .toList();
    }

    private void inputContainerChanged() {
        slotsChanged(inputContainer);
        slotUpdateListener.run();
    }

    private record ClientMenuData(ContainerLevelAccess access, List<ResourceLocation> patternIds) {
    }
}

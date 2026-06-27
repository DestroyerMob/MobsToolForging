package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public class HeatingForgeBlockEntity extends BlockEntity {
    private static final int WORKPIECE_SLOTS = 2;
    private static final String FUEL_TAG = "Fuel";
    private static final String WORKPIECE_TAG = "Workpiece";
    private static final String BURN_TIME_TAG = "BurnTime";
    private static final String BURN_DURATION_TAG = "BurnDuration";
    private static final String HEAT_PROGRESS_TAG = "HeatProgress";
    private static final String IGNITED_TAG = "Ignited";

    private ItemStack fuelStack = ItemStack.EMPTY;
    private final ItemStack[] workpieceStacks = new ItemStack[WORKPIECE_SLOTS];
    private final int[] heatProgress = new int[WORKPIECE_SLOTS];
    private int burnTime;
    private int burnDuration;
    private boolean ignited;

    public HeatingForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HEATING_FORGE.get(), pos, blockState);
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            workpieceStacks[slot] = ItemStack.EMPTY;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeatingForgeBlockEntity forge) {
        boolean changed = false;
        boolean sync = false;
        int requiredTicks = MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get();
        if (forge.burnTime > 0) {
            forge.burnTime--;
            changed = true;
        }
        if (forge.ignited && forge.burnTime <= 0) {
            if (forge.consumeFuel()) {
                changed = true;
            } else {
                forge.ignited = false;
                forge.burnDuration = 0;
                changed = true;
            }
        }
        boolean canAdvanceHeat = forge.workpieceCount() < WORKPIECE_SLOTS || level.getGameTime() % 3L != 0L;
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            ItemStack workpiece = forge.workpieceStacks[slot];
            if (workpiece.isEmpty()) {
                continue;
            }
            WorkpieceHeat.clearIfCooled(workpiece, level);
            if (forge.burnTime > 0) {
                if (canAdvanceHeat && forge.heatProgress[slot] < requiredTicks) {
                    forge.heatProgress[slot]++;
                    changed = true;
                    sync = level.getGameTime() % 10L == 0L;
                }
                if (forge.heatProgress[slot] >= requiredTicks) {
                    if (!WorkpieceHeat.isHot(workpiece, level) || level.getGameTime() % 20L == 0L) {
                        WorkpieceHeat.heat(workpiece, level);
                        changed = true;
                        sync = true;
                    }
                } else if (sync) {
                    WorkpieceHeat.setTemperature(workpiece, level, forge.heatProgressFraction(slot), false);
                }
            } else if (forge.heatProgress[slot] > 0) {
                int visualProgress = forge.coolingVisualProgress(level, requiredTicks, slot);
                if (visualProgress != forge.heatProgress[slot]) {
                    forge.heatProgress[slot] = visualProgress;
                    changed = true;
                    sync = level.getGameTime() % 10L == 0L || visualProgress == 0;
                }
            }
        }
        if (changed) {
            forge.setChanged();
            if (sync || forge.burnTime == 0) {
                forge.sync();
            }
        }
    }

    public ItemStack fuelStack() {
        return fuelStack;
    }

    public ItemStack workpieceStack() {
        int slot = firstOccupiedSlot();
        return slot >= 0 ? workpieceStacks[slot] : ItemStack.EMPTY;
    }

    public ItemStack workpieceStack(int slot) {
        return validWorkpieceSlot(slot) ? workpieceStacks[slot] : ItemStack.EMPTY;
    }

    public int workpieceSlots() {
        return WORKPIECE_SLOTS;
    }

    public int workpieceCount() {
        int count = 0;
        for (ItemStack workpiece : workpieceStacks) {
            if (!workpiece.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public boolean isLit() {
        return burnTime > 0;
    }

    public boolean hasFuel() {
        return !fuelStack.isEmpty();
    }

    public boolean hasWorkpiece() {
        return firstOccupiedSlot() >= 0;
    }

    public int heatProgress() {
        int progress = 0;
        for (int slotProgress : heatProgress) {
            progress = Math.max(progress, slotProgress);
        }
        return progress;
    }

    public float heatProgressFraction() {
        return Math.min(1.0F, heatProgress() / (float) MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get());
    }

    public float heatProgressFraction(int slot) {
        if (!validWorkpieceSlot(slot)) {
            return 0.0F;
        }
        return Math.min(1.0F, heatProgress[slot] / (float) MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get());
    }

    public boolean canAcceptFuel(ItemStack stack) {
        return !stack.isEmpty() && fuelBurnTime(stack) > 0 && (fuelStack.isEmpty() || ItemStack.isSameItemSameComponents(fuelStack, stack) && fuelStack.getCount() < fuelStack.getMaxStackSize());
    }

    public boolean acceptFuel(ItemStack stack) {
        if (!canAcceptFuel(stack)) {
            return false;
        }
        if (!fuelStack.isEmpty() && (!ItemStack.isSameItemSameComponents(fuelStack, stack) || fuelStack.getCount() >= fuelStack.getMaxStackSize())) {
            return false;
        }
        ItemStack inserted = stack.split(1);
        if (fuelStack.isEmpty()) {
            fuelStack = inserted;
        } else {
            fuelStack.grow(1);
        }
        sync();
        return true;
    }

    public boolean canAcceptWorkpiece(ItemStack stack) {
        return !stack.isEmpty() && firstEmptySlot() >= 0 && isHeatableWorkpiece(stack);
    }

    public boolean acceptWorkpiece(ItemStack stack) {
        int slot = firstEmptySlot();
        if (!canAcceptWorkpiece(stack)) {
            return false;
        }
        workpieceStacks[slot] = stack.split(1);
        heatProgress[slot] = Math.round(WorkpieceHeat.temperature(workpieceStacks[slot], levelOrThrow()) * MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get());
        sync();
        return true;
    }

    public boolean ignite() {
        if (burnTime > 0) {
            ignited = true;
            sync();
            return true;
        }
        if (!consumeFuel()) {
            return false;
        }
        ignited = true;
        sync();
        return true;
    }

    public ItemStack removeWorkpiece() {
        int slot = lastOccupiedSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        applyStoredHeat(slot);
        ItemStack result = workpieceStacks[slot];
        workpieceStacks[slot] = ItemStack.EMPTY;
        heatProgress[slot] = 0;
        compactWorkpieces();
        sync();
        return result;
    }

    public ItemStack removeFuel() {
        ItemStack result = fuelStack;
        fuelStack = ItemStack.EMPTY;
        sync();
        return result;
    }

    public ItemStack workpieceDropStack() {
        return workpieceStack().copy();
    }

    public List<ItemStack> workpieceDropStacks() {
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack workpiece : workpieceStacks) {
            if (!workpiece.isEmpty()) {
                drops.add(workpiece.copy());
            }
        }
        return drops;
    }

    public ItemStack fuelDropStack() {
        return fuelStack.copy();
    }

    public static boolean isHeatableWorkpiece(ItemStack stack) {
        ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
        if (partData != null) {
            return MaterialCatalog.definition(partData.materialId())
                    .filter(definition -> definition.category() == MaterialCategory.METAL)
                    .isPresent();
        }
        return MaterialCatalog.resolve(stack)
                .filter(definition -> definition.category() == MaterialCategory.METAL)
                .isPresent();
    }

    public static int fuelBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    private boolean consumeFuel() {
        if (fuelStack.isEmpty()) {
            return false;
        }
        int burn = fuelBurnTime(fuelStack);
        if (burn <= 0) {
            return false;
        }
        fuelStack.shrink(1);
        if (fuelStack.isEmpty()) {
            fuelStack = ItemStack.EMPTY;
        }
        burnTime = burn;
        burnDuration = burn;
        return true;
    }

    private Level levelOrThrow() {
        if (level == null) {
            throw new IllegalStateException("Heating forge level is not available");
        }
        return level;
    }

    private int coolingVisualProgress(Level level, int requiredTicks, int slot) {
        return Math.round(WorkpieceHeat.temperature(workpieceStacks[slot], level) * requiredTicks);
    }

    private void applyStoredHeat(int slot) {
        if (level == null || workpieceStacks[slot].isEmpty() || heatProgress[slot] <= 0) {
            return;
        }
        int requiredTicks = MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get();
        if (heatProgress[slot] >= requiredTicks) {
            WorkpieceHeat.heat(workpieceStacks[slot], level);
        } else {
            WorkpieceHeat.setTemperature(workpieceStacks[slot], level, heatProgressFraction(slot), false);
        }
    }

    private int firstOccupiedSlot() {
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            if (!workpieceStacks[slot].isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private int lastOccupiedSlot() {
        for (int slot = WORKPIECE_SLOTS - 1; slot >= 0; slot--) {
            if (!workpieceStacks[slot].isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            if (workpieceStacks[slot].isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void compactWorkpieces() {
        if (workpieceStacks[0].isEmpty() && !workpieceStacks[1].isEmpty()) {
            workpieceStacks[0] = workpieceStacks[1];
            heatProgress[0] = heatProgress[1];
            workpieceStacks[1] = ItemStack.EMPTY;
            heatProgress[1] = 0;
        }
    }

    private static boolean validWorkpieceSlot(int slot) {
        return slot >= 0 && slot < WORKPIECE_SLOTS;
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
        if (!fuelStack.isEmpty()) {
            tag.put(FUEL_TAG, fuelStack.saveOptional(registries));
        }
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            if (!workpieceStacks[slot].isEmpty()) {
                tag.put(WORKPIECE_TAG + slot, workpieceStacks[slot].saveOptional(registries));
                tag.putInt(HEAT_PROGRESS_TAG + slot, heatProgress[slot]);
            }
        }
        tag.putInt(BURN_TIME_TAG, burnTime);
        tag.putInt(BURN_DURATION_TAG, burnDuration);
        tag.putBoolean(IGNITED_TAG, ignited);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelStack = tag.contains(FUEL_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(FUEL_TAG)) : ItemStack.EMPTY;
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            workpieceStacks[slot] = tag.contains(WORKPIECE_TAG + slot) ? ItemStack.parseOptional(registries, tag.getCompound(WORKPIECE_TAG + slot)) : ItemStack.EMPTY;
            heatProgress[slot] = tag.getInt(HEAT_PROGRESS_TAG + slot);
        }
        if (tag.contains(WORKPIECE_TAG) && workpieceStacks[0].isEmpty()) {
            workpieceStacks[0] = ItemStack.parseOptional(registries, tag.getCompound(WORKPIECE_TAG));
            heatProgress[0] = tag.getInt(HEAT_PROGRESS_TAG);
        }
        burnTime = tag.getInt(BURN_TIME_TAG);
        burnDuration = tag.getInt(BURN_DURATION_TAG);
        ignited = tag.getBoolean(IGNITED_TAG);
        compactWorkpieces();
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

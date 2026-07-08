package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.items.IItemHandler;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class HeatingForgeBlockEntity extends BlockEntity {
    public static final int FUEL_BED_SLOTS = 4;
    public static final int WORKPIECE_SLOTS = 4;
    public static final int MAX_ASH_LAYERS = 3;
    public static final int HEATED_WORKPIECE_TICKS = 1600;
    public static final int FULL_BED_BURN_TICKS = 6000;
    // A full coal bed provides four heat units per tick; active billets share that budget.
    private static final int HEAT_UNITS_PER_TICK = WORKPIECE_SLOTS;

    private static final int ITEM_HANDLER_SLOTS = FUEL_BED_SLOTS + WORKPIECE_SLOTS + 1;
    private static final int WORKPIECE_HANDLER_SLOT_START = FUEL_BED_SLOTS;
    private static final int ASH_HANDLER_SLOT = FUEL_BED_SLOTS + WORKPIECE_SLOTS;
    private static final String FUEL_TAG = "Fuel";
    private static final String WORKPIECE_TAG = "Workpiece";
    private static final String BURN_TIME_TAG = "BurnTime";
    private static final String BURN_DURATION_TAG = "BurnDuration";
    private static final String HEAT_PROGRESS_TAG = "HeatProgress";
    private static final String IGNITED_TAG = "Ignited";
    private static final String LAST_HOT_GAME_TIME_TAG = "LastHotGameTime";
    private static final String ASH_LAYERS_TAG = "AshLayers";
    private static final String SPENT_FUEL_BED_TAG = "SpentFuelBed";

    private final IItemHandler itemHandler = new ForgeItemHandler();
    private ItemStack fuelStack = ItemStack.EMPTY;
    private final ItemStack[] workpieceStacks = new ItemStack[WORKPIECE_SLOTS];
    private final int[] heatProgress = new int[WORKPIECE_SLOTS];
    private int burnTime;
    private int burnDuration;
    private int ashLayers;
    private long lastHotGameTime;
    private boolean ignited;
    private boolean spentFuelBed;

    public HeatingForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HEATING_FORGE.get(), pos, blockState);
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            workpieceStacks[slot] = ItemStack.EMPTY;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeatingForgeBlockEntity forge) {
        boolean changed = false;
        boolean sync = false;
        if (forge.isFuelBurning()) {
            forge.burnTime--;
            forge.lastHotGameTime = level.getGameTime();
            changed = true;
            sync = level.getGameTime() % 20L == 0L;
            if (forge.burnTime <= 0) {
                forge.finishFuelBedUse();
                sync = true;
            }
        }
        boolean canHeat = forge.isFuelBurning();
        int[] heatingSlots = new int[WORKPIECE_SLOTS];
        int heatingSlotCount = 0;
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            ItemStack workpiece = forge.workpieceStacks[slot];
            if (workpiece.isEmpty()) {
                continue;
            }
            WorkpieceHeat.clearIfCooled(workpiece, level);
            if (canHeat && forge.heatProgress[slot] < forge.requiredHeatTicks(workpiece)) {
                heatingSlots[heatingSlotCount++] = slot;
            }
        }
        if (canHeat && heatingSlotCount > 0) {
            for (int unit = 0; unit < HEAT_UNITS_PER_TICK; unit++) {
                int slot = heatingSlots[(int) ((level.getGameTime() + unit) % heatingSlotCount)];
                if (forge.heatProgress[slot] < forge.requiredHeatTicks(forge.workpieceStacks[slot])) {
                    forge.heatProgress[slot]++;
                    changed = true;
                }
            }
            sync = sync || level.getGameTime() % 10L == 0L;
        }
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            ItemStack workpiece = forge.workpieceStacks[slot];
            if (workpiece.isEmpty()) {
                continue;
            }
            if (canHeat) {
                if (forge.heatProgress[slot] >= forge.requiredHeatTicks(workpiece)) {
                    if (!WorkpieceHeat.isHot(workpiece, level) || level.getGameTime() % 20L == 0L) {
                        forge.applyTargetHeat(slot);
                        changed = true;
                        sync = true;
                    }
                } else if (sync) {
                    WorkpieceHeat.setTemperature(workpiece, level, forge.progressTemperature(slot), false);
                }
            } else if (forge.heatProgress[slot] > 0) {
                int visualProgress = forge.coolingVisualProgress(level, slot);
                if (visualProgress != forge.heatProgress[slot]) {
                    forge.heatProgress[slot] = visualProgress;
                    changed = true;
                    sync = sync || level.getGameTime() % 10L == 0L || visualProgress == 0;
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

    public IItemHandler itemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public ItemStack fuelStack() {
        return fuelStack;
    }

    public int fuelBedCount() {
        return spentFuelBed ? FUEL_BED_SLOTS : fuelStack.getCount();
    }

    public boolean isFuelBedFull() {
        return fuelStack.getCount() >= FUEL_BED_SLOTS;
    }

    public boolean hasSpentFuelBed() {
        return spentFuelBed;
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
        return isFuelBurning();
    }

    public boolean isWorkshopHot(Level level) {
        return isFuelBurning();
    }

    public boolean hasFuel() {
        return !fuelStack.isEmpty();
    }

    public boolean hasWorkpiece() {
        return firstOccupiedSlot() >= 0;
    }

    public int ashLayers() {
        return ashLayers;
    }

    public boolean hasAsh() {
        return ashLayers > 0;
    }

    public boolean ashTrayFull() {
        return ashLayers >= MAX_ASH_LAYERS;
    }

    public int heatProgress() {
        int progress = 0;
        for (int slotProgress : heatProgress) {
            progress = Math.max(progress, slotProgress);
        }
        return progress;
    }

    public float heatProgressFraction() {
        float progress = 0.0F;
        for (int slot = 0; slot < WORKPIECE_SLOTS; slot++) {
            progress = Math.max(progress, heatProgressFraction(slot));
        }
        return progress;
    }

    public float heatProgressFraction(int slot) {
        if (!validWorkpieceSlot(slot)) {
            return 0.0F;
        }
        return Math.min(1.0F, heatProgress[slot] / (float) requiredHeatTicks(workpieceStacks[slot]));
    }

    public int heatProgress(int slot) {
        return validWorkpieceSlot(slot) ? heatProgress[slot] : 0;
    }

    public int requiredHeatTicks(int slot) {
        return validWorkpieceSlot(slot) ? requiredHeatTicks(workpieceStacks[slot]) : configuredHeatTicks();
    }

    public float workpieceTargetTemperature(int slot) {
        return validWorkpieceSlot(slot) ? targetTemperature(workpieceStacks[slot]) : 0.0F;
    }

    public float workpieceProgressTemperature(int slot) {
        return validWorkpieceSlot(slot) ? progressTemperature(slot) : 0.0F;
    }

    public float fuelTemperatureFraction() {
        if (!isFuelBurning()) {
            return 0.0F;
        }
        int duration = burnDuration <= 0 ? FULL_BED_BURN_TICKS : burnDuration;
        return Math.min(1.0F, burnTime / (float) duration);
    }

    public boolean canAcceptFuel(ItemStack stack) {
        return !stack.isEmpty()
                && fuelBurnTime(stack) > 0
                && !spentFuelBed
                && !ashTrayFull()
                && (fuelStack.isEmpty() || ItemStack.isSameItemSameComponents(fuelStack, stack))
                && fuelStack.getCount() < FUEL_BED_SLOTS;
    }

    public boolean acceptFuel(ItemStack stack) {
        if (!canAcceptFuel(stack)) {
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
        return !stack.isEmpty() && isFuelBedFull() && !spentFuelBed && !ashTrayFull() && firstEmptySlot() >= 0 && isHeatableWorkpiece(stack);
    }

    public boolean canAcceptWorkpiece(ItemStack stack, int slot) {
        return validWorkpieceSlot(slot)
                && !stack.isEmpty()
                && isFuelBedFull()
                && !spentFuelBed
                && !ashTrayFull()
                && workpieceStacks[slot].isEmpty()
                && isHeatableWorkpiece(stack);
    }

    public boolean acceptWorkpiece(ItemStack stack) {
        int slot = firstEmptySlot();
        if (!canAcceptWorkpiece(stack)) {
            return false;
        }
        return acceptWorkpiece(stack, slot);
    }

    public boolean acceptWorkpiece(ItemStack stack, int slot) {
        if (!canAcceptWorkpiece(stack, slot)) {
            return false;
        }
        workpieceStacks[slot] = stack.split(1);
        heatProgress[slot] = progressForTemperature(workpieceStacks[slot], WorkpieceHeat.temperature(workpieceStacks[slot], levelOrThrow()));
        sync();
        return true;
    }

    public boolean canIgnite() {
        return !spentFuelBed && !ashTrayFull() && isFuelBedFull();
    }

    public boolean ignite() {
        if (isFuelBurning()) {
            ignited = true;
            sync();
            return true;
        }
        if (!canIgnite()) {
            return false;
        }
        burnTime = FULL_BED_BURN_TICKS;
        burnDuration = FULL_BED_BURN_TICKS;
        ignited = true;
        sync();
        return true;
    }

    public ItemStack removeWorkpiece() {
        int slot = lastOccupiedSlot();
        return removeWorkpiece(slot);
    }

    public ItemStack removeWorkpiece(int slot) {
        if (!validWorkpieceSlot(slot) || workpieceStacks[slot].isEmpty()) {
            return ItemStack.EMPTY;
        }
        applyStoredHeat(slot);
        ItemStack result = workpieceStacks[slot];
        workpieceStacks[slot] = ItemStack.EMPTY;
        heatProgress[slot] = 0;
        sync();
        return result;
    }

    public ItemStack removeFuel() {
        if (isLit()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = fuelStack;
        fuelStack = ItemStack.EMPTY;
        burnTime = 0;
        burnDuration = 0;
        ignited = false;
        sync();
        return result;
    }

    public void clearSpentFuelBed() {
        if (!spentFuelBed) {
            return;
        }
        spentFuelBed = false;
        burnTime = 0;
        burnDuration = 0;
        ignited = false;
        sync();
    }

    public ItemStack collectAsh() {
        if (ashLayers <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(ModItems.ASH.get(), ashLayers);
        ashLayers = 0;
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

    public ItemStack ashDropStack() {
        return ashLayers <= 0 ? ItemStack.EMPTY : new ItemStack(ModItems.ASH.get(), ashLayers);
    }

    public static boolean isHeatableWorkpiece(ItemStack stack) {
        return HeatingRecipeRegistry.isHeatable(HeatingSource.FORGE, stack);
    }

    public static int fuelBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    private boolean isFuelBurning() {
        return ignited && burnTime > 0 && !spentFuelBed && isFuelBedFull();
    }

    private void finishFuelBedUse() {
        burnTime = 0;
        burnDuration = FULL_BED_BURN_TICKS;
        fuelStack = ItemStack.EMPTY;
        spentFuelBed = true;
        ignited = false;
        if (ashLayers < MAX_ASH_LAYERS) {
            ashLayers++;
        }
    }

    private Level levelOrThrow() {
        if (level == null) {
            throw new IllegalStateException("Heating forge level is not available");
        }
        return level;
    }

    private int coolingVisualProgress(Level level, int slot) {
        return progressForTemperature(workpieceStacks[slot], WorkpieceHeat.temperature(workpieceStacks[slot], level));
    }

    private void applyStoredHeat(int slot) {
        if (level == null || workpieceStacks[slot].isEmpty() || heatProgress[slot] <= 0) {
            return;
        }
        if (heatProgress[slot] >= requiredHeatTicks(workpieceStacks[slot])) {
            applyTargetHeat(slot);
        } else {
            WorkpieceHeat.setTemperature(workpieceStacks[slot], level, progressTemperature(slot), false);
        }
    }

    private void applyTargetHeat(int slot) {
        if (level == null || workpieceStacks[slot].isEmpty()) {
            return;
        }
        HeatingRecipe recipe = heatingRecipe(workpieceStacks[slot]).orElse(null);
        if (recipe == null) {
            WorkpieceHeat.heat(workpieceStacks[slot], level);
            return;
        }
        WorkpieceHeat.setTemperature(workpieceStacks[slot], level, recipe.targetTemperature(), recipe.workable());
    }

    private float progressTemperature(int slot) {
        if (!validWorkpieceSlot(slot) || workpieceStacks[slot].isEmpty()) {
            return 0.0F;
        }
        return Math.min(targetTemperature(workpieceStacks[slot]), heatProgressFraction(slot) * targetTemperature(workpieceStacks[slot]));
    }

    private int progressForTemperature(ItemStack stack, float temperature) {
        float targetTemperature = targetTemperature(stack);
        if (targetTemperature <= 0.0F) {
            return 0;
        }
        return Math.min(requiredHeatTicks(stack), Math.round(Math.max(0.0F, temperature) / targetTemperature * requiredHeatTicks(stack)));
    }

    private int requiredHeatTicks(ItemStack stack) {
        return heatingRecipe(stack)
                .map(HeatingRecipe::ticks)
                .orElse(configuredHeatTicks());
    }

    private float targetTemperature(ItemStack stack) {
        return heatingRecipe(stack)
                .map(HeatingRecipe::targetTemperature)
                .orElse(1.0F);
    }

    private Optional<HeatingRecipe> heatingRecipe(ItemStack stack) {
        return HeatingRecipeRegistry.find(HeatingSource.FORGE, stack);
    }

    private static int configuredHeatTicks() {
        return Math.max(1, MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get());
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
        tag.putInt(ASH_LAYERS_TAG, ashLayers);
        tag.putBoolean(SPENT_FUEL_BED_TAG, spentFuelBed);
        tag.putBoolean(IGNITED_TAG, ignited);
        tag.putLong(LAST_HOT_GAME_TIME_TAG, lastHotGameTime);
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
        burnDuration = tag.contains(BURN_DURATION_TAG) ? tag.getInt(BURN_DURATION_TAG) : FULL_BED_BURN_TICKS;
        ashLayers = Math.max(0, Math.min(MAX_ASH_LAYERS, tag.getInt(ASH_LAYERS_TAG)));
        spentFuelBed = tag.getBoolean(SPENT_FUEL_BED_TAG);
        ignited = tag.getBoolean(IGNITED_TAG);
        lastHotGameTime = tag.getLong(LAST_HOT_GAME_TIME_TAG);
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

    private final class ForgeItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return ITEM_HANDLER_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < FUEL_BED_SLOTS) {
                return slot < fuelStack.getCount() ? fuelStack.copyWithCount(1) : ItemStack.EMPTY;
            }
            int workpieceSlot = slot - WORKPIECE_HANDLER_SLOT_START;
            if (validWorkpieceSlot(workpieceSlot)) {
                return workpieceStacks[workpieceSlot].copy();
            }
            if (slot == ASH_HANDLER_SLOT && ashLayers > 0) {
                return new ItemStack(ModItems.ASH.get(), ashLayers);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (slot >= 0 && slot < FUEL_BED_SLOTS) {
                return insertFuelFromHandler(slot, stack, simulate);
            }
            int workpieceSlot = slot - WORKPIECE_HANDLER_SLOT_START;
            if (validWorkpieceSlot(workpieceSlot)) {
                return insertWorkpieceFromHandler(workpieceSlot, stack, simulate);
            }
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != ASH_HANDLER_SLOT || amount <= 0 || ashLayers <= 0) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, ashLayers);
            ItemStack result = new ItemStack(ModItems.ASH.get(), extracted);
            if (!simulate) {
                ashLayers -= extracted;
                sync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == ASH_HANDLER_SLOT) {
                return MAX_ASH_LAYERS;
            }
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= 0 && slot < FUEL_BED_SLOTS) {
                return canAcceptFuel(stack) && slot == fuelStack.getCount();
            }
            int workpieceSlot = slot - WORKPIECE_HANDLER_SLOT_START;
            return validWorkpieceSlot(workpieceSlot)
                    && canAcceptWorkpiece(stack)
                    && workpieceSlot == firstEmptySlot();
        }

        private ItemStack insertFuelFromHandler(int slot, ItemStack stack, boolean simulate) {
            if (slot != fuelStack.getCount() || !canAcceptFuel(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                if (fuelStack.isEmpty()) {
                    fuelStack = stack.copyWithCount(1);
                } else {
                    fuelStack.grow(1);
                }
                sync();
            }
            return remainder;
        }

        private ItemStack insertWorkpieceFromHandler(int slot, ItemStack stack, boolean simulate) {
            if (slot != firstEmptySlot() || !canAcceptWorkpiece(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                workpieceStacks[slot] = stack.copyWithCount(1);
                heatProgress[slot] = progressForTemperature(workpieceStacks[slot], WorkpieceHeat.temperature(workpieceStacks[slot], levelOrThrow()));
                sync();
            }
            return remainder;
        }
    }
}

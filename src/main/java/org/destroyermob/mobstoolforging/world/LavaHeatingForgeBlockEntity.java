package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModTags;

public class LavaHeatingForgeBlockEntity extends HeatingForgeBlockEntity {
    public static final int TANK_CAPACITY = 4_000;
    private static final String FLUID_TANK_TAG = "HeatingFluidTank";
    private static final String RESERVED_HEAT_TAG = "ReservedHeatUnits";
    private static final String ACTIVE_TAG = "ActivelyHeating";

    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY, LavaHeatingForgeBlockEntity::isHeatingFluid) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private final IItemHandler workpieceHandler = new WorkpieceItemHandler();
    private int reservedHeatUnits;
    private boolean activelyHeating;

    public LavaHeatingForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LAVA_HEATING_FORGE.get(), pos, blockState);
    }

    @Override
    public boolean usesFluidFuel() {
        return true;
    }

    @Override
    public boolean isLit() {
        return activelyHeating;
    }

    @Override
    public boolean isWorkshopHot(Level level) {
        return activelyHeating;
    }

    @Override
    public boolean hasFuel() {
        return hasAvailableHeatSource();
    }

    @Override
    public boolean canAcceptFuel(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canAcceptWorkpiece(ItemStack stack) {
        return !stack.isEmpty()
                && hasAvailableHeatSource()
                && firstEmptyWorkpieceSlot() >= 0
                && isHeatableWorkpiece(stack);
    }

    @Override
    public boolean canAcceptWorkpiece(ItemStack stack, int slot) {
        return slot >= 0
                && slot < workpieceSlots()
                && !stack.isEmpty()
                && hasAvailableHeatSource()
                && workpieceStack(slot).isEmpty()
                && isHeatableWorkpiece(stack);
    }

    @Override
    public IItemHandler itemHandler(@Nullable Direction side) {
        return workpieceHandler;
    }

    public IFluidHandler fluidHandler(@Nullable Direction side) {
        return fluidTank;
    }

    public FluidStack fluidStack() {
        return fluidTank.getFluid().copy();
    }

    public int fluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public int tankCapacity() {
        return fluidTank.getCapacity();
    }

    public float fluidFillFraction() {
        return fluidTank.getFluidAmount() / (float) fluidTank.getCapacity();
    }

    @Override
    protected boolean hasAvailableHeatSource() {
        return reservedHeatUnits > 0 || !fluidTank.isEmpty();
    }

    @Override
    protected boolean consumeHeatUnit() {
        if (reservedHeatUnits <= 0) {
            FluidStack drained = fluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                return false;
            }
            reservedHeatUnits = Math.max(1, MobsToolForgingConfig.FLUID_FORGE_HEAT_UNITS_PER_MB.get());
        }
        reservedHeatUnits--;
        return true;
    }

    @Override
    protected boolean setActivelyHeating(boolean active) {
        if (activelyHeating == active) {
            return false;
        }
        activelyHeating = active;
        return true;
    }

    private static boolean isHeatingFluid(FluidStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Fluids.HEATING_FLUIDS);
    }

    private int firstEmptyWorkpieceSlot() {
        for (int slot = 0; slot < workpieceSlots(); slot++) {
            if (workpieceStack(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(FLUID_TANK_TAG, fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt(RESERVED_HEAT_TAG, reservedHeatUnits);
        tag.putBoolean(ACTIVE_TAG, activelyHeating);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(FLUID_TANK_TAG)) {
            fluidTank.readFromNBT(registries, tag.getCompound(FLUID_TANK_TAG));
        }
        reservedHeatUnits = Math.max(0, tag.getInt(RESERVED_HEAT_TAG));
        activelyHeating = tag.getBoolean(ACTIVE_TAG);
    }

    private final class WorkpieceItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return workpieceSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return workpieceStack(slot).copy();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!canAcceptWorkpiece(stack, slot)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                ItemStack inserted = stack.copyWithCount(1);
                acceptWorkpiece(inserted, slot);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return canAcceptWorkpiece(stack, slot);
        }
    }
}

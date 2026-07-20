package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class FoundryFuelTankBlockEntity extends BlockEntity {
    public static final int BUCKET_MB = 1000;
    public static final int CAPACITY_MB = 4000;
    private static final String LAVA_TAG = "Lava";
    private static final String FLUID_TAG = "FuelFluid";
    private static final String AMOUNT_TAG = "FuelAmount";

    @Nullable
    private ResourceLocation fluidId;
    private int fluidAmountMb;
    private final IFluidHandler fluidHandler = new TankFluidHandler();

    public FoundryFuelTankBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_FUEL_TANK.get(), pos, blockState);
    }

    public int lavaMb() {
        return fluidAmountMb;
    }

    public int fluidAmountMb() {
        return fluidAmountMb;
    }

    public FluidStack fluidStack() {
        if (fluidId == null || fluidAmountMb <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(BuiltInRegistries.FLUID.get(fluidId), fluidAmountMb);
    }

    public IFluidHandler fluidHandler() {
        return fluidHandler;
    }

    public float fuelTemperatureC() {
        return FoundryFuelRegistry.find(fluidStack()).map(FoundryFuelRecipe::temperatureC).orElse(0.0F);
    }

    public int capacityMb() {
        return CAPACITY_MB;
    }

    public float lavaVisualFraction() {
        return fluidAmountMb / (float) CAPACITY_MB;
    }

    public int lavaBuckets() {
        return fluidAmountMb / BUCKET_MB;
    }

    public boolean acceptLavaBucket() {
        return fluidHandler.fill(new FluidStack(Fluids.LAVA, BUCKET_MB), IFluidHandler.FluidAction.EXECUTE) == BUCKET_MB;
    }

    public boolean drainLavaBucket() {
        FluidStack drained = fluidHandler.drain(BUCKET_MB, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount() == BUCKET_MB;
    }

    public @Nullable FuelUse drainFuel(float requiredTemperatureC) {
        FluidStack contents = fluidStack();
        FoundryFuelRecipe recipe = FoundryFuelRegistry.find(contents).orElse(null);
        if (recipe == null || recipe.temperatureC() < requiredTemperatureC || contents.getAmount() < recipe.amountMb()) {
            return null;
        }
        FluidStack drained = fluidHandler.drain(recipe.amountMb(), IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() != recipe.amountMb()) {
            return null;
        }
        return new FuelUse(BuiltInRegistries.FLUID.getKey(drained.getFluid()), recipe.temperatureC(), recipe.burnTicks());
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
        if (fluidId != null && fluidAmountMb > 0) {
            tag.putString(FLUID_TAG, fluidId.toString());
            tag.putInt(AMOUNT_TAG, fluidAmountMb);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidId = ResourceLocation.tryParse(tag.getString(FLUID_TAG));
        fluidAmountMb = Math.max(0, Math.min(CAPACITY_MB, tag.getInt(AMOUNT_TAG)));
        if ((fluidId == null || fluidAmountMb == 0) && tag.getInt(LAVA_TAG) > 0) {
            fluidId = ResourceLocation.withDefaultNamespace("lava");
            fluidAmountMb = Math.max(0, Math.min(CAPACITY_MB, tag.getInt(LAVA_TAG)));
        }
        if (fluidAmountMb == 0) {
            fluidId = null;
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

    public record FuelUse(ResourceLocation fluid, float temperatureC, int burnTicks) {
    }

    private final class TankFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? fluidStack() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? CAPACITY_MB : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && FoundryFuelRegistry.find(stack).isPresent();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(0, resource)) {
                return 0;
            }
            ResourceLocation offeredId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
            if (fluidAmountMb > 0 && !offeredId.equals(fluidId)) {
                return 0;
            }
            int accepted = Math.min(resource.getAmount(), CAPACITY_MB - fluidAmountMb);
            if (accepted > 0 && action.execute()) {
                fluidId = offeredId;
                fluidAmountMb += accepted;
                sync();
            }
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack contents = fluidStack();
            if (resource.isEmpty() || contents.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, contents)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack contents = fluidStack();
            if (contents.isEmpty() || maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            int drainedAmount = Math.min(maxDrain, fluidAmountMb);
            FluidStack drained = contents.copyWithAmount(drainedAmount);
            if (action.execute()) {
                fluidAmountMb -= drainedAmount;
                if (fluidAmountMb == 0) {
                    fluidId = null;
                }
                sync();
            }
            return drained;
        }
    }
}

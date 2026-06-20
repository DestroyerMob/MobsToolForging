package org.destroyermob.mobstoolforging.world;

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
    private static final String FUEL_TAG = "Fuel";
    private static final String WORKPIECE_TAG = "Workpiece";
    private static final String BURN_TIME_TAG = "BurnTime";
    private static final String BURN_DURATION_TAG = "BurnDuration";
    private static final String HEAT_PROGRESS_TAG = "HeatProgress";
    private static final String IGNITED_TAG = "Ignited";

    private ItemStack fuelStack = ItemStack.EMPTY;
    private ItemStack workpieceStack = ItemStack.EMPTY;
    private int burnTime;
    private int burnDuration;
    private int heatProgress;
    private boolean ignited;

    public HeatingForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HEATING_FORGE.get(), pos, blockState);
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
        if (!forge.workpieceStack.isEmpty()) {
            WorkpieceHeat.clearIfCooled(forge.workpieceStack, level);
        }
        if (forge.burnTime > 0 && !forge.workpieceStack.isEmpty()) {
            if (forge.heatProgress < requiredTicks) {
                forge.heatProgress++;
                changed = true;
                sync = level.getGameTime() % 10L == 0L;
            }
            if (forge.heatProgress >= requiredTicks) {
                if (!WorkpieceHeat.isHot(forge.workpieceStack, level) || level.getGameTime() % 20L == 0L) {
                    WorkpieceHeat.heat(forge.workpieceStack, level);
                    changed = true;
                    sync = true;
                }
            }
        } else if (!forge.workpieceStack.isEmpty() && forge.heatProgress > 0) {
            int visualProgress = forge.coolingVisualProgress(level, requiredTicks);
            if (visualProgress != forge.heatProgress) {
                forge.heatProgress = visualProgress;
                changed = true;
                sync = level.getGameTime() % 10L == 0L || visualProgress == 0;
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
        return workpieceStack;
    }

    public boolean isLit() {
        return burnTime > 0;
    }

    public boolean hasFuel() {
        return !fuelStack.isEmpty();
    }

    public boolean hasWorkpiece() {
        return !workpieceStack.isEmpty();
    }

    public int heatProgress() {
        return heatProgress;
    }

    public float heatProgressFraction() {
        return Math.min(1.0F, heatProgress / (float) MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get());
    }

    public boolean acceptFuel(ItemStack stack) {
        if (stack.isEmpty() || fuelBurnTime(stack) <= 0) {
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

    public boolean acceptWorkpiece(ItemStack stack) {
        if (stack.isEmpty() || hasWorkpiece() || !isHeatableWorkpiece(stack)) {
            return false;
        }
        workpieceStack = stack.split(1);
        heatProgress = WorkpieceHeat.isHot(workpieceStack, levelOrThrow()) ? MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get() : 0;
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
        ItemStack result = workpieceStack;
        workpieceStack = ItemStack.EMPTY;
        heatProgress = 0;
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
        return workpieceStack.copy();
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

    private int coolingVisualProgress(Level level, int requiredTicks) {
        long remainingTicks = WorkpieceHeat.remainingTicks(workpieceStack, level);
        if (remainingTicks > 0L) {
            int coolingTicks = MobsToolForgingConfig.COOLING_TICKS.get();
            return Math.min(requiredTicks, (int) Math.ceil(requiredTicks * (remainingTicks / (double) coolingTicks)));
        }
        return Math.max(0, heatProgress - 1);
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
        if (!workpieceStack.isEmpty()) {
            tag.put(WORKPIECE_TAG, workpieceStack.saveOptional(registries));
        }
        tag.putInt(BURN_TIME_TAG, burnTime);
        tag.putInt(BURN_DURATION_TAG, burnDuration);
        tag.putInt(HEAT_PROGRESS_TAG, heatProgress);
        tag.putBoolean(IGNITED_TAG, ignited);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelStack = tag.contains(FUEL_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(FUEL_TAG)) : ItemStack.EMPTY;
        workpieceStack = tag.contains(WORKPIECE_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(WORKPIECE_TAG)) : ItemStack.EMPTY;
        burnTime = tag.getInt(BURN_TIME_TAG);
        burnDuration = tag.getInt(BURN_DURATION_TAG);
        heatProgress = tag.getInt(HEAT_PROGRESS_TAG);
        ignited = tag.getBoolean(IGNITED_TAG);
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

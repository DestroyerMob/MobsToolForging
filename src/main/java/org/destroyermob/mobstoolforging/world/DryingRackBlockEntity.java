package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class DryingRackBlockEntity extends BlockEntity {
    private static final String STACK_TAG = "Stack";
    private static final String RECIPE_TAG = "Recipe";
    private static final String DRYING_TIME_TAG = "DryingTime";
    private static final String DRYING_DURATION_TAG = "DryingDuration";

    private final IItemHandler itemHandler = new RackItemHandler();
    private ItemStack stack = ItemStack.EMPTY;
    @Nullable
    private ResourceLocation recipeId;
    private int dryingTime;
    private int dryingDuration;

    public DryingRackBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity rack) {
        if (rack.stack.isEmpty() || rack.recipeId == null) {
            return;
        }
        DryingRecipe recipe = DryingRecipeRegistry.recipe(rack.recipeId)
                .or(() -> DryingRecipeRegistry.find(rack.stack))
                .orElse(null);
        if (recipe == null) {
            rack.recipeId = null;
            rack.dryingTime = 0;
            rack.dryingDuration = 0;
            rack.sync();
            return;
        }
        rack.recipeId = recipe.id();
        rack.dryingDuration = recipe.ticks();
        rack.dryingTime++;
        if (rack.dryingTime >= rack.dryingDuration) {
            rack.stack = recipe.outputCopy();
            rack.recipeId = null;
            rack.dryingTime = 0;
            rack.dryingDuration = 0;
            rack.sync();
        } else if (level.getGameTime() % 20L == 0L) {
            rack.sync();
        } else {
            rack.setChanged();
        }
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public boolean hasItem() {
        return !stack.isEmpty();
    }

    public boolean isDrying() {
        return hasItem() && recipeId != null;
    }

    public ItemStack displayStack() {
        return stack.copy();
    }

    public float progressFraction() {
        if (dryingDuration <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, (float) dryingTime / (float) dryingDuration);
    }

    public int dryingTicksRemaining() {
        if (!isDrying()) {
            return 0;
        }
        return Math.max(0, dryingDuration - dryingTime);
    }

    public boolean canPlace(ItemStack heldStack) {
        return stack.isEmpty() && DryingRecipeRegistry.find(heldStack).isPresent();
    }

    public boolean place(ItemStack heldStack, boolean creative) {
        if (!stack.isEmpty()) {
            return false;
        }
        Optional<DryingRecipe> recipe = DryingRecipeRegistry.find(heldStack);
        if (recipe.isEmpty()) {
            return false;
        }
        DryingRecipe dryingRecipe = recipe.get();
        stack = dryingRecipe.inputCopy(heldStack);
        recipeId = dryingRecipe.id();
        dryingTime = 0;
        dryingDuration = dryingRecipe.ticks();
        if (!creative) {
            heldStack.shrink(dryingRecipe.input().count());
        }
        sync();
        return true;
    }

    public ItemStack removeItem() {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.copy();
        clear();
        return removed;
    }

    public ItemStack dropStack() {
        return stack.copy();
    }

    private void clear() {
        stack = ItemStack.EMPTY;
        recipeId = null;
        dryingTime = 0;
        dryingDuration = 0;
        sync();
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
        if (!stack.isEmpty()) {
            tag.put(STACK_TAG, stack.saveOptional(registries));
        }
        if (recipeId != null) {
            tag.putString(RECIPE_TAG, recipeId.toString());
        }
        tag.putInt(DRYING_TIME_TAG, dryingTime);
        tag.putInt(DRYING_DURATION_TAG, dryingDuration);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stack = tag.contains(STACK_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(STACK_TAG)) : ItemStack.EMPTY;
        recipeId = tag.contains(RECIPE_TAG) ? ResourceLocation.parse(tag.getString(RECIPE_TAG)) : null;
        dryingTime = Math.max(0, tag.getInt(DRYING_TIME_TAG));
        dryingDuration = Math.max(0, tag.getInt(DRYING_DURATION_TAG));
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

    private final class RackItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? stack.copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack insertedStack, boolean simulate) {
            if (slot != 0 || insertedStack.isEmpty() || !stack.isEmpty()) {
                return insertedStack;
            }
            Optional<DryingRecipe> recipe = DryingRecipeRegistry.find(insertedStack);
            if (recipe.isEmpty()) {
                return insertedStack;
            }
            DryingRecipe dryingRecipe = recipe.get();
            ItemStack remainder = insertedStack.copy();
            remainder.shrink(dryingRecipe.input().count());
            if (!simulate) {
                stack = dryingRecipe.inputCopy(insertedStack);
                recipeId = dryingRecipe.id();
                dryingTime = 0;
                dryingDuration = dryingRecipe.ticks();
                sync();
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (recipeId != null && amount < stack.getCount()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, stack.getCount());
            ItemStack result = stack.copyWithCount(extracted);
            if (!simulate) {
                if (extracted >= stack.getCount()) {
                    clear();
                } else {
                    stack.shrink(extracted);
                    sync();
                }
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack candidate) {
            return slot == 0 && stack.isEmpty() && DryingRecipeRegistry.find(candidate).isPresent();
        }
    }
}

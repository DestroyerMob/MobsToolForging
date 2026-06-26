package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class FoundryForgeBlockEntity extends BlockEntity {
    private static final String CRUCIBLE_TAG = "Crucible";
    private static final String BURN_TIME_TAG = "BurnTime";
    private static final String HEAT_PROGRESS_TAG = "HeatProgress";
    private static final int LAVA_BURN_TICKS = 2400;
    private static final int MELT_TICKS = 400;

    private ItemStack crucibleStack = ItemStack.EMPTY;
    private int burnTime;
    private int heatProgress;

    public FoundryForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_FORGE.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryForgeBlockEntity forge) {
        boolean changed = false;
        boolean sync = false;
        if (forge.burnTime > 0) {
            forge.burnTime--;
            changed = true;
        }

        CrucibleContents contents = forge.crucibleContents();
        if (forge.burnTime > 0 && !forge.crucibleStack.isEmpty()) {
            if (contents.hasMoltenMaterial()) {
                if (!contents.isWhiteHot()) {
                    forge.setCrucibleContents(contents.withHeat(1.0F));
                    sync = true;
                }
                forge.heatProgress = MELT_TICKS;
                changed = true;
            } else {
                Optional<ResourceLocation> meltable = forge.meltableMaterial(contents);
                if (meltable.isPresent()) {
                    if (forge.heatProgress < MELT_TICKS) {
                        forge.heatProgress++;
                        forge.setCrucibleContents(contents.withHeat(forge.heatProgress / (float) MELT_TICKS));
                        changed = true;
                        sync = level.getGameTime() % 10L == 0L;
                    }
                    if (forge.heatProgress >= MELT_TICKS) {
                        forge.setCrucibleContents(contents.melt(meltable.get()));
                        sync = true;
                    }
                }
            }
        }

        if (forge.crucibleStack.isEmpty() || (!contents.hasItem() && !contents.hasMoltenMaterial())) {
            if (forge.heatProgress != 0) {
                forge.heatProgress = 0;
                changed = true;
            }
        }

        if (changed) {
            forge.setChanged();
            if (sync || forge.burnTime == 0) {
                forge.sync();
            }
        }
    }

    public boolean isLit() {
        return burnTime > 0;
    }

    public boolean hasCrucible() {
        return !crucibleStack.isEmpty();
    }

    public ItemStack crucibleStack() {
        return crucibleStack;
    }

    public float heatProgressFraction() {
        return Math.min(1.0F, heatProgress / (float) MELT_TICKS);
    }

    public float lavaVisualFraction() {
        return Math.min(1.0F, burnTime / (float) LAVA_BURN_TICKS);
    }

    public CrucibleContents crucibleContents() {
        CrucibleContents contents = crucibleStack.get(ModDataComponents.CRUCIBLE_CONTENTS.get());
        return contents == null ? CrucibleContents.EMPTY : contents;
    }

    public boolean acceptLavaFuel() {
        burnTime += LAVA_BURN_TICKS;
        sync();
        return true;
    }

    public boolean acceptCrucible(ItemStack stack) {
        if (!crucibleStack.isEmpty() || stack.isEmpty() || !stack.is(ModItems.CRUCIBLE.get())) {
            return false;
        }
        crucibleStack = stack.split(1);
        heatProgress = Math.round(crucibleContents().heat() * MELT_TICKS);
        sync();
        return true;
    }

    public ItemStack removeCrucible() {
        ItemStack result = crucibleStack;
        crucibleStack = ItemStack.EMPTY;
        heatProgress = 0;
        sync();
        return result;
    }

    public ItemStack crucibleDropStack() {
        return crucibleStack.copy();
    }

    private Optional<ResourceLocation> meltableMaterial(CrucibleContents contents) {
        if (!contents.hasItem()) {
            return Optional.empty();
        }
        ItemStack item = contents.item();
        if (item.is(Items.NETHERITE_SCRAP)) {
            return Optional.of(MaterialCatalog.NETHERITE);
        }
        return MaterialCatalog.resolve(item)
                .filter(definition -> definition.category() == MaterialCategory.METAL)
                .map(ToolMaterialDefinition::id);
    }

    private void setCrucibleContents(CrucibleContents contents) {
        if (crucibleStack.isEmpty()) {
            return;
        }
        if (contents.isEmpty()) {
            crucibleStack.remove(ModDataComponents.CRUCIBLE_CONTENTS.get());
        } else {
            crucibleStack.set(ModDataComponents.CRUCIBLE_CONTENTS.get(), contents);
        }
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
        if (!crucibleStack.isEmpty()) {
            tag.put(CRUCIBLE_TAG, crucibleStack.saveOptional(registries));
        }
        tag.putInt(BURN_TIME_TAG, burnTime);
        tag.putInt(HEAT_PROGRESS_TAG, heatProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        crucibleStack = tag.contains(CRUCIBLE_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(CRUCIBLE_TAG)) : ItemStack.EMPTY;
        burnTime = tag.getInt(BURN_TIME_TAG);
        heatProgress = tag.getInt(HEAT_PROGRESS_TAG);
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

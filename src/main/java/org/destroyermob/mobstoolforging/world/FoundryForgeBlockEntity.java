package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public class FoundryForgeBlockEntity extends BlockEntity {
    public static final int INGOT_MB = 90;
    public static final int BLOCK_MB = INGOT_MB * 9;
    public static final int DEFAULT_MELT_TICKS = 400;
    private static final String SOLID_INPUTS_TAG = "SolidInputs";
    private static final String MOLTEN_FLUIDS_TAG = "MoltenFluids";
    private static final String STACK_TAG = "Stack";
    private static final String MATERIAL_TAG = "Material";
    private static final String AMOUNT_TAG = "Amount";
    private static final String LEGACY_CRUCIBLE_TAG = "Crucible";
    private static final String BURN_TIME_TAG = "BurnTime";
    private static final String FUEL_TEMPERATURE_C_TAG = "FuelTemperatureC";
    private static final String LEGACY_FUEL_TEMPERATURE_TAG = "FuelTemperature";
    private static final String ACTIVE_FUEL_TAG = "ActiveFuel";
    private static final String MELT_PROGRESS_TAG = "HeatProgress";
    private static final String FORMED_TAG = "Formed";
    private static final String INTERIOR_MIN_X_TAG = "InteriorMinX";
    private static final String INTERIOR_MIN_Y_TAG = "InteriorMinY";
    private static final String INTERIOR_MIN_Z_TAG = "InteriorMinZ";
    private static final String INTERIOR_MAX_X_TAG = "InteriorMaxX";
    private static final String INTERIOR_MAX_Y_TAG = "InteriorMaxY";
    private static final String INTERIOR_MAX_Z_TAG = "InteriorMaxZ";
    private static final String STRUCTURE_WIDTH_TAG = "StructureWidth";
    private static final String STRUCTURE_DEPTH_TAG = "StructureDepth";
    private static final String STRUCTURE_HEIGHT_TAG = "StructureHeight";
    private static final int FLUID_CAPACITY_MB_PER_INTERIOR_BLOCK = 1000;

    private final List<ItemStack> solidInputs = new ArrayList<>();
    private final List<MoltenLayer> moltenLayers = new ArrayList<>();
    private int burnTime;
    private float activeFuelTemperatureC;
    @Nullable
    private ResourceLocation activeFuel;
    private int meltProgress;
    private boolean formed;
    private BlockPos interiorMin = BlockPos.ZERO;
    private BlockPos interiorMax = BlockPos.ZERO;
    private int structureWidth;
    private int structureDepth;
    private int structureHeight;
    private int structureCheckTicks;

    public FoundryForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FOUNDRY_FORGE.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryForgeBlockEntity forge) {
        if (forge.structureCheckTicks-- <= 0) {
            forge.refreshStructure();
            forge.structureCheckTicks = 20;
        }

        boolean changed = false;
        boolean sync = false;
        if (forge.formed && level.getGameTime() % 5L == 0L && forge.absorbDroppedItems()) {
            changed = true;
            sync = true;
        }
        if (forge.burnTime > 0) {
            forge.burnTime--;
            if (forge.burnTime == 0) {
                forge.activeFuelTemperatureC = 0.0F;
                forge.activeFuel = null;
                sync = true;
            }
            changed = true;
        }

        if (forge.formed && !forge.solidInputs.isEmpty()) {
            float requiredTemperatureC = forge.currentRequiredTemperatureC();
            if (forge.burnTime <= 0) {
                FoundryFuelTankBlockEntity.FuelUse fuel = forge.drawFuel(requiredTemperatureC);
                if (fuel != null) {
                    forge.burnTime = fuel.burnTicks();
                    forge.activeFuelTemperatureC = fuel.temperatureC();
                    forge.activeFuel = fuel.fluid();
                    changed = true;
                    sync = true;
                }
            }
            if (forge.burnTime > 0 && forge.activeFuelTemperatureC >= requiredTemperatureC) {
                forge.meltProgress++;
                changed = true;
                sync = level.getGameTime() % 10L == 0L;
                int requiredTicks = forge.currentMeltingRecipe().map(FoundryMeltingRecipe::ticks).orElse(DEFAULT_MELT_TICKS);
                if (forge.meltProgress >= requiredTicks) {
                    forge.meltNextItem();
                    forge.meltProgress = 0;
                    sync = true;
                }
            }
        } else if (forge.meltProgress != 0) {
            forge.meltProgress = 0;
            changed = true;
            sync = true;
        }

        if (changed) {
            forge.setChanged();
            if (sync || forge.burnTime == 0) {
                forge.sync();
            }
        }
    }

    public boolean isLit() {
        return formed && burnTime > 0;
    }

    public float activeFuelTemperatureC() {
        return activeFuelTemperatureC;
    }

    public Optional<ResourceLocation> activeFuel() {
        return Optional.ofNullable(activeFuel);
    }

    public float currentMeltingPointC() {
        return currentRequiredTemperatureC();
    }

    public boolean hasSufficientTemperature() {
        return solidInputs.isEmpty() || activeFuelTemperatureC >= currentRequiredTemperatureC();
    }

    public boolean isFormed() {
        return formed;
    }

    public int structureWidth() {
        return structureWidth;
    }

    public int structureDepth() {
        return structureDepth;
    }

    public int structureHeight() {
        return structureHeight;
    }

    public int solidItemCount() {
        return solidInputs.stream().mapToInt(ItemStack::getCount).sum();
    }

    public List<ItemStack> solidRenderStacks() {
        return solidInputs.stream().map(ItemStack::copy).toList();
    }

    public List<MoltenLayer> moltenLayers() {
        return List.copyOf(moltenLayers);
    }

    public int moltenAmountMb() {
        return moltenLayers.stream().mapToInt(MoltenLayer::amountMb).sum();
    }

    public int fluidCapacityMb() {
        return formed ? structureWidth * structureDepth * structureHeight * FLUID_CAPACITY_MB_PER_INTERIOR_BLOCK : 0;
    }

    public float moltenVisualFraction() {
        int capacity = fluidCapacityMb();
        return capacity == 0 ? 0.0F : Math.min(1.0F, moltenAmountMb() / (float) capacity);
    }

    public Optional<ResourceLocation> visibleMoltenMaterial() {
        return moltenLayers.isEmpty() ? Optional.empty() : Optional.of(moltenLayers.getLast().material());
    }

    public Optional<ResourceLocation> bottomMoltenMaterial() {
        return moltenLayers.isEmpty() ? Optional.empty() : Optional.of(moltenLayers.getFirst().material());
    }

    public Optional<MoltenLayer> bottomMoltenLayer() {
        return moltenLayers.isEmpty() ? Optional.empty() : Optional.of(moltenLayers.getFirst());
    }

    public int moltenLayerCount() {
        return moltenLayers.size();
    }

    public int moltenAmount(ResourceLocation material) {
        return moltenLayers.stream()
                .filter(layer -> layer.material().equals(material))
                .mapToInt(MoltenLayer::amountMb)
                .sum();
    }

    public float meltProgressFraction() {
        int requiredTicks = currentMeltingRecipe().map(FoundryMeltingRecipe::ticks).orElse(DEFAULT_MELT_TICKS);
        return Math.min(1.0F, meltProgress / (float) requiredTicks);
    }

    public int connectedTankCount() {
        return connectedTanks().size();
    }

    public int connectedFuelBuckets() {
        return connectedTanks().stream().mapToInt(FoundryFuelTankBlockEntity::lavaBuckets).sum();
    }

    public int connectedFuelMb() {
        return connectedTanks().stream().mapToInt(FoundryFuelTankBlockEntity::fluidAmountMb).sum();
    }

    public static boolean isMeltable(ItemStack stack) {
        return FoundryMeltingRegistry.find(stack).isPresent() || recyclingMaterial(stack).isPresent();
    }

    /** Inserts as many metal items as the formed foundry can reserve fluid space for. */
    public int acceptSolid(ItemStack offered) {
        refreshStructure();
        return acceptSolidInFormedFoundry(offered);
    }

    private int acceptSolidInFormedFoundry(ItemStack offered) {
        Optional<FoundryMeltingRecipe> recipe = FoundryMeltingRegistry.find(offered);
        int amountPerItem = recipe.map(FoundryMeltingRecipe::amountMb).orElseGet(() -> recyclingYieldMb(offered));
        if (!formed || offered.isEmpty() || amountPerItem <= 0) {
            return 0;
        }
        int reservedMb = moltenAmountMb() + solidReservedMb();
        int accepted = Math.min(offered.getCount(), Math.max(0, fluidCapacityMb() - reservedMb) / amountPerItem);
        if (accepted <= 0) {
            return 0;
        }
        ItemStack inserted = offered.split(accepted);
        for (ItemStack stored : solidInputs) {
            if (ItemStack.isSameItemSameComponents(stored, inserted) && stored.getCount() + inserted.getCount() <= stored.getMaxStackSize()) {
                stored.grow(inserted.getCount());
                sync();
                return accepted;
            }
        }
        solidInputs.add(inserted);
        sync();
        return accepted;
    }

    private boolean absorbDroppedItems() {
        if (!formed || level == null) {
            return false;
        }
        AABB interior = new AABB(
                interiorMin.getX(), interiorMin.getY(), interiorMin.getZ(),
                interiorMax.getX() + 1.0D, interiorMax.getY() + 1.0D, interiorMax.getZ() + 1.0D
        );
        boolean acceptedAny = false;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, interior, ItemEntity::isAlive)) {
            ItemStack stack = entity.getItem();
            int accepted = acceptSolidInFormedFoundry(stack);
            if (accepted <= 0) {
                continue;
            }
            acceptedAny = true;
            if (stack.isEmpty()) {
                entity.discard();
            } else {
                entity.setItem(stack);
            }
        }
        if (acceptedAny) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 0.7F);
        }
        return acceptedAny;
    }

    public int drainBottom(ResourceLocation material, int maximumAmount) {
        if (!formed || maximumAmount <= 0 || moltenLayers.isEmpty()) {
            return 0;
        }
        MoltenLayer bottom = moltenLayers.getFirst();
        if (!bottom.material().equals(material)) {
            return 0;
        }
        int drained = Math.min(maximumAmount, bottom.amountMb());
        int remaining = bottom.amountMb() - drained;
        if (remaining == 0) {
            moltenLayers.removeFirst();
        } else {
            moltenLayers.set(0, new MoltenLayer(bottom.material(), remaining));
        }
        sync();
        return drained;
    }

    public int addMoltenLayer(ResourceLocation material, int amountMb) {
        if (!formed || amountMb <= 0) {
            return 0;
        }
        int accepted = Math.min(amountMb, Math.max(0, fluidCapacityMb() - moltenAmountMb() - solidReservedMb()));
        if (accepted <= 0) {
            return 0;
        }
        appendMolten(material, accepted);
        sync();
        return accepted;
    }

    public boolean refreshStructure() {
        if (level == null) {
            return formed;
        }
        FoundryStructure structure = FoundryStructure.find(level, worldPosition, getBlockState());
        boolean nextFormed = structure.formed();
        BlockPos nextMin = nextFormed ? structure.interiorMin() : BlockPos.ZERO;
        BlockPos nextMax = nextFormed ? structure.interiorMax() : BlockPos.ZERO;
        int nextWidth = nextFormed ? structure.width() : 0;
        int nextDepth = nextFormed ? structure.depth() : 0;
        int nextHeight = nextFormed ? structure.height() : 0;
        boolean changed = formed != nextFormed
                || !interiorMin.equals(nextMin)
                || !interiorMax.equals(nextMax)
                || structureWidth != nextWidth
                || structureDepth != nextDepth
                || structureHeight != nextHeight;
        formed = nextFormed;
        interiorMin = nextMin;
        interiorMax = nextMax;
        structureWidth = nextWidth;
        structureDepth = nextDepth;
        structureHeight = nextHeight;
        if (changed) {
            sync();
        }
        return formed;
    }

    public float interiorMinRenderX() {
        return interiorMin.getX() - worldPosition.getX() + 0.04F;
    }

    public float interiorMinRenderZ() {
        return interiorMin.getZ() - worldPosition.getZ() + 0.04F;
    }

    public float interiorMaxRenderX() {
        return interiorMax.getX() - worldPosition.getX() + 0.96F;
    }

    public float interiorMaxRenderZ() {
        return interiorMax.getZ() - worldPosition.getZ() + 0.96F;
    }

    public float interiorRenderHeight() {
        return structureHeight;
    }

    public boolean containsShellPosition(BlockPos pos) {
        if (!formed || pos.getY() < interiorMin.getY() || pos.getY() > interiorMax.getY()) {
            return false;
        }
        boolean xFace = (pos.getX() == interiorMin.getX() - 1 || pos.getX() == interiorMax.getX() + 1)
                && pos.getZ() >= interiorMin.getZ() && pos.getZ() <= interiorMax.getZ();
        boolean zFace = (pos.getZ() == interiorMin.getZ() - 1 || pos.getZ() == interiorMax.getZ() + 1)
                && pos.getX() >= interiorMin.getX() && pos.getX() <= interiorMax.getX();
        return xFace || zFace;
    }

    private List<FoundryFuelTankBlockEntity> connectedTanks() {
        if (!formed || level == null) {
            return List.of();
        }
        List<FoundryFuelTankBlockEntity> tanks = new ArrayList<>();
        int minX = interiorMin.getX() - 1;
        int maxX = interiorMax.getX() + 1;
        int minZ = interiorMin.getZ() - 1;
        int maxZ = interiorMax.getZ() + 1;
        for (int y = interiorMin.getY(); y <= interiorMax.getY(); y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean perimeter = x == minX || x == maxX || z == minZ || z == maxZ;
                    if (perimeter && level.getBlockEntity(new BlockPos(x, y, z)) instanceof FoundryFuelTankBlockEntity tank) {
                        tanks.add(tank);
                    }
                }
            }
        }
        return tanks;
    }

    private @Nullable FoundryFuelTankBlockEntity.FuelUse drawFuel(float requiredTemperatureC) {
        for (FoundryFuelTankBlockEntity tank : connectedTanks()) {
            FoundryFuelTankBlockEntity.FuelUse fuel = tank.drainFuel(requiredTemperatureC);
            if (fuel != null) {
                return fuel;
            }
        }
        return null;
    }

    private void meltNextItem() {
        if (solidInputs.isEmpty()) {
            return;
        }
        ItemStack input = solidInputs.getFirst();
        Optional<FoundryMeltingRecipe> recipe = FoundryMeltingRegistry.find(input);
        ResourceLocation material = recipe.map(FoundryMeltingRecipe::material).orElseGet(() -> recyclingMaterial(input).orElse(null));
        int amount = recipe.map(FoundryMeltingRecipe::amountMb).orElseGet(() -> recyclingYieldMb(input));
        if (material == null || amount <= 0) {
            solidInputs.removeFirst();
            return;
        }
        input.shrink(1);
        if (input.isEmpty()) {
            solidInputs.removeFirst();
        }
        appendMolten(material, amount);
    }

    private Optional<FoundryMeltingRecipe> currentMeltingRecipe() {
        return solidInputs.isEmpty() ? Optional.empty() : FoundryMeltingRegistry.find(solidInputs.getFirst());
    }

    private float currentRequiredTemperatureC() {
        if (solidInputs.isEmpty()) {
            return 0.0F;
        }
        ResourceLocation material = currentMeltingRecipe().map(FoundryMeltingRecipe::material)
                .orElseGet(() -> recyclingMaterial(solidInputs.getFirst()).orElse(null));
        return material == null ? FoundryMeltingPointRegistry.DEFAULT_MELTING_POINT_C
                : FoundryMeltingPointRegistry.celsius(material);
    }

    public int solidReservedMb() {
        int reserved = 0;
        for (ItemStack stack : solidInputs) {
            int amount = FoundryMeltingRegistry.find(stack).map(FoundryMeltingRecipe::amountMb).orElseGet(() -> recyclingYieldMb(stack));
            reserved += stack.getCount() * amount;
        }
        return reserved;
    }

    private void appendMolten(ResourceLocation material, int amountMb) {
        if (amountMb <= 0) {
            return;
        }
        appendRawMolten(material, amountMb);
        mixAvailableAlloys();
    }

    private void appendRawMolten(ResourceLocation material, int amountMb) {
        if (!moltenLayers.isEmpty() && moltenLayers.getLast().material().equals(material)) {
            MoltenLayer top = moltenLayers.getLast();
            moltenLayers.set(moltenLayers.size() - 1, new MoltenLayer(material, top.amountMb() + amountMb));
        } else {
            moltenLayers.add(new MoltenLayer(material, amountMb));
        }
    }

    private void mixAvailableAlloys() {
        int transformations = 0;
        boolean mixed = false;
        while (transformations++ < 64) {
            FoundryAlloyRecipe recipe = FoundryAlloyRegistry.findCraftable(this::moltenAmount).orElse(null);
            if (recipe == null) {
                break;
            }
            int batches = recipe.craftableBatches(this::moltenAmount);
            recipe.inputs().forEach((material, amount) -> consumeMolten(material, amount * batches));
            appendRawMolten(recipe.result(), recipe.outputAmountMb() * batches);
            mixed = true;
        }
        if (mixed && level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.7F, 0.8F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.1D,
                        worldPosition.getZ() + 0.5D, 8, 0.25D, 0.08D, 0.25D, 0.01D);
            }
        }
    }

    public static int recyclingYieldMb(ItemStack stack) {
        ResourceLocation material = recyclingMaterial(stack).orElse(null);
        if (material == null) {
            return 0;
        }
        String partType = recyclablePartType(stack);
        int baseAmount = FoundryCastRegistry.recipes().stream()
                .filter(recipe -> ToolTypeRegistry.template(recipe.template())
                        .map(template -> template.partType().equals(partType))
                        .orElse(false))
                .mapToInt(FoundryCastRecipe::amountMb)
                .min()
                .orElse(INGOT_MB);
        int quality = recyclableQuality(stack);
        float condition = ToolPartWear.remainingDurabilityPercent(stack) / 100.0F;
        float recovery = 0.55F + ForgingQuality.clampScore(quality) / 500.0F;
        int recovered = (int) Math.floor(baseAmount * condition * recovery / 10.0F) * 10;
        return Math.max(10, recovered);
    }

    private static Optional<ResourceLocation> recyclingMaterial(ItemStack stack) {
        ToolPartData tool = stack.get(ModDataComponents.TOOL_PART.get());
        ArmorPartData armor = stack.get(ModDataComponents.ARMOR_PART.get());
        ResourceLocation material = tool != null && !tool.isCoated() ? tool.materialId()
                : armor != null && !armor.isCoated() ? armor.materialId() : null;
        if (material == null || MaterialCatalog.definition(material)
                .filter(definition -> definition.category() == MaterialCategory.METAL).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(material);
    }

    private static String recyclablePartType(ItemStack stack) {
        ToolPartData tool = stack.get(ModDataComponents.TOOL_PART.get());
        if (tool != null) {
            return tool.partType();
        }
        ArmorPartData armor = stack.get(ModDataComponents.ARMOR_PART.get());
        return armor == null ? "" : armor.partType();
    }

    private static int recyclableQuality(ItemStack stack) {
        ToolPartData tool = stack.get(ModDataComponents.TOOL_PART.get());
        if (tool != null) {
            return Metallurgy.adjustedQuality(stack, tool.effectiveQuality());
        }
        ArmorPartData armor = stack.get(ModDataComponents.ARMOR_PART.get());
        return armor == null ? ForgingQuality.DEFAULT_SCORE : Metallurgy.adjustedQuality(stack, armor.quality());
    }

    private void consumeMolten(ResourceLocation material, int amountMb) {
        int remaining = amountMb;
        for (int index = 0; index < moltenLayers.size() && remaining > 0;) {
            MoltenLayer layer = moltenLayers.get(index);
            if (!layer.material().equals(material)) {
                index++;
                continue;
            }
            int consumed = Math.min(remaining, layer.amountMb());
            remaining -= consumed;
            int layerRemaining = layer.amountMb() - consumed;
            if (layerRemaining <= 0) {
                moltenLayers.remove(index);
            } else {
                moltenLayers.set(index, new MoltenLayer(material, layerRemaining));
                index++;
            }
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
        ListTag inputs = new ListTag();
        for (ItemStack stack : solidInputs) {
            CompoundTag entry = new CompoundTag();
            entry.put(STACK_TAG, stack.saveOptional(registries));
            inputs.add(entry);
        }
        tag.put(SOLID_INPUTS_TAG, inputs);

        ListTag fluids = new ListTag();
        moltenLayers.forEach(layer -> {
            CompoundTag entry = new CompoundTag();
            entry.putString(MATERIAL_TAG, layer.material().toString());
            entry.putInt(AMOUNT_TAG, layer.amountMb());
            fluids.add(entry);
        });
        tag.put(MOLTEN_FLUIDS_TAG, fluids);
        tag.putInt(BURN_TIME_TAG, burnTime);
        tag.putFloat(FUEL_TEMPERATURE_C_TAG, activeFuelTemperatureC);
        if (activeFuel != null) {
            tag.putString(ACTIVE_FUEL_TAG, activeFuel.toString());
        }
        tag.putInt(MELT_PROGRESS_TAG, meltProgress);
        tag.putBoolean(FORMED_TAG, formed);
        tag.putInt(INTERIOR_MIN_X_TAG, interiorMin.getX());
        tag.putInt(INTERIOR_MIN_Y_TAG, interiorMin.getY());
        tag.putInt(INTERIOR_MIN_Z_TAG, interiorMin.getZ());
        tag.putInt(INTERIOR_MAX_X_TAG, interiorMax.getX());
        tag.putInt(INTERIOR_MAX_Y_TAG, interiorMax.getY());
        tag.putInt(INTERIOR_MAX_Z_TAG, interiorMax.getZ());
        tag.putInt(STRUCTURE_WIDTH_TAG, structureWidth);
        tag.putInt(STRUCTURE_DEPTH_TAG, structureDepth);
        tag.putInt(STRUCTURE_HEIGHT_TAG, structureHeight);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        solidInputs.clear();
        ListTag inputs = tag.getList(SOLID_INPUTS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < inputs.size(); index++) {
            ItemStack stack = ItemStack.parseOptional(registries, inputs.getCompound(index).getCompound(STACK_TAG));
            if (!stack.isEmpty()) {
                solidInputs.add(stack);
            }
        }

        moltenLayers.clear();
        ListTag fluids = tag.getList(MOLTEN_FLUIDS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < fluids.size(); index++) {
            CompoundTag entry = fluids.getCompound(index);
            ResourceLocation material = ResourceLocation.tryParse(entry.getString(MATERIAL_TAG));
            int amount = entry.getInt(AMOUNT_TAG);
            if (material != null && amount > 0) {
                appendMolten(material, amount);
            }
        }

        migrateLegacyCrucible(tag, registries);
        burnTime = Math.max(0, tag.getInt(BURN_TIME_TAG));
        if (tag.contains(FUEL_TEMPERATURE_C_TAG)) {
            activeFuelTemperatureC = Math.max(0.0F, tag.getFloat(FUEL_TEMPERATURE_C_TAG));
        } else if (tag.contains(LEGACY_FUEL_TEMPERATURE_TAG)) {
            activeFuelTemperatureC = Math.max(0.0F, tag.getFloat(LEGACY_FUEL_TEMPERATURE_TAG))
                    * FoundryFuelRegistry.LAVA_TEMPERATURE_C;
        } else {
            activeFuelTemperatureC = 0.0F;
        }
        activeFuel = ResourceLocation.tryParse(tag.getString(ACTIVE_FUEL_TAG));
        if (burnTime > 0 && activeFuelTemperatureC <= 0.0F) {
            activeFuelTemperatureC = FoundryFuelRegistry.LAVA_TEMPERATURE_C;
            activeFuel = ResourceLocation.withDefaultNamespace("lava");
        }
        if (burnTime == 0) {
            activeFuelTemperatureC = 0.0F;
            activeFuel = null;
        }
        meltProgress = Math.max(0, tag.getInt(MELT_PROGRESS_TAG));
        formed = tag.getBoolean(FORMED_TAG);
        interiorMin = new BlockPos(tag.getInt(INTERIOR_MIN_X_TAG), tag.getInt(INTERIOR_MIN_Y_TAG), tag.getInt(INTERIOR_MIN_Z_TAG));
        interiorMax = new BlockPos(tag.getInt(INTERIOR_MAX_X_TAG), tag.getInt(INTERIOR_MAX_Y_TAG), tag.getInt(INTERIOR_MAX_Z_TAG));
        structureWidth = tag.getInt(STRUCTURE_WIDTH_TAG);
        structureDepth = tag.getInt(STRUCTURE_DEPTH_TAG);
        structureHeight = tag.getInt(STRUCTURE_HEIGHT_TAG);
    }

    private void migrateLegacyCrucible(CompoundTag tag, HolderLookup.Provider registries) {
        if (!solidInputs.isEmpty() || !moltenLayers.isEmpty() || !tag.contains(LEGACY_CRUCIBLE_TAG)) {
            return;
        }
        ItemStack legacyCrucible = ItemStack.parseOptional(registries, tag.getCompound(LEGACY_CRUCIBLE_TAG));
        CrucibleContents contents = legacyCrucible.get(ModDataComponents.CRUCIBLE_CONTENTS.get());
        if (contents == null) {
            return;
        }
        if (contents.hasItem()) {
            solidInputs.add(contents.item().copy());
        } else if (contents.hasMoltenMaterial()) {
            contents.moltenMaterial().ifPresent(material -> appendMolten(material, Math.max(1, contents.moltenAmount()) * INGOT_MB));
        }
    }

    public record MoltenLayer(ResourceLocation material, int amountMb) {
        public MoltenLayer {
            amountMb = Math.max(1, amountMb);
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

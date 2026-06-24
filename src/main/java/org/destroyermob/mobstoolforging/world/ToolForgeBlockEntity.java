package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModTags;

public class ToolForgeBlockEntity extends BlockEntity {
    private static final String TEMPLATE_TAG = "Template";
    private static final String MATERIAL_COUNT_TAG = "MaterialCount";
    private static final String HIT_COUNT_TAG = "HitCount";
    private static final String MATERIAL_ID_TAG = "MaterialId";
    private static final String MATERIAL_ITEM_ID_TAG = "MaterialItemId";
    private static final String DISPLAY_ROTATION_TAG = "DisplayRotation";
    private static final String MATERIAL_HEAT_EXPIRES_TAG = "MaterialHeatExpires";
    private static final String MATERIAL_HEAT_TEMPERATURE_TAG = "MaterialHeatTemperature";
    private static final String MATERIAL_HEAT_LAST_UPDATE_TAG = "MaterialHeatLastUpdate";
    private static final String MATERIAL_HEAT_WORKABLE_TAG = "MaterialHeatWorkable";
    private static final String DIRECT_OUTPUT_TAG = "DirectOutput";
    private static final String LOOSE_WORK_RECIPE_TAG = "LooseWorkRecipe";
    private static final String ABRASIVE_STACK_TAG = "AbrasiveStack";
    private static final String BENCH_STACKS_TAG = "BenchStacks";
    private static final int MAX_BENCH_STACKS = 9;

    @Nullable
    private ResourceLocation templateId;
    @Nullable
    private ResourceLocation materialId;
    @Nullable
    private ResourceLocation materialItemId;
    @Nullable
    private HeatedWorkpieceData materialHeatData;
    @Nullable
    private ResourceLocation looseWorkRecipeId;
    private ItemStack directOutputStack = ItemStack.EMPTY;
    private ItemStack abrasiveStack = ItemStack.EMPTY;
    private final List<ItemStack> benchStacks = new ArrayList<>();
    private int materialCount;
    private int hitCount;
    private float displayRotationDegrees;

    public ToolForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TOOL_WORKSTATION.get(), pos, blockState);
    }

    @Nullable
    public ForgeTemplateDefinition template() {
        return templateId == null ? null : ToolTypeRegistry.template(templateId).orElse(null);
    }

    @Nullable
    public ResourceLocation templateId() {
        return templateId;
    }

    @Nullable
    public StationWorkRecipe looseWorkRecipe() {
        return looseWorkRecipeId == null ? null : StationWorkRecipeRegistry.recipe(looseWorkRecipeId).orElse(null);
    }

    public int materialCount() {
        return materialCount;
    }

    public int hitCount() {
        return hitCount;
    }

    public float displayRotationDegrees() {
        return displayRotationDegrees;
    }

    public boolean hasAbrasive() {
        return !abrasiveStack.isEmpty();
    }

    public ItemStack abrasiveStack() {
        return abrasiveStack.copy();
    }

    public List<ItemStack> benchStacks() {
        return benchStacks.stream().map(ItemStack::copy).toList();
    }

    public boolean hasBenchStacks() {
        return !benchStacks.isEmpty();
    }

    @Nullable
    public ResourceLocation materialId() {
        return materialId;
    }

    public float progress() {
        ForgeTemplateDefinition template = template();
        StationWorkRecipe looseRecipe = looseWorkRecipe();
        if (looseRecipe != null && materialItemId != null) {
            return Math.min(1.0F, hitCount / (float) looseRecipe.requiredHits());
        }
        if (template == null) {
            return 0.0F;
        }
        return Math.min(1.0F, hitCount / (float) template.requiredHits());
    }

    public boolean isEmpty() {
        return directOutputStack.isEmpty() && abrasiveStack.isEmpty() && benchStacks.isEmpty() && templateId == null && materialId == null && materialItemId == null && materialHeatData == null && looseWorkRecipeId == null && materialCount == 0 && hitCount == 0;
    }

    public boolean isComplete() {
        if (!directOutputStack.isEmpty()) {
            return true;
        }
        ForgeTemplateDefinition template = template();
        return template != null && materialCount >= template.requiredMaterials() && hitCount >= template.requiredHits();
    }

    public boolean canChangeTemplate() {
        return directOutputStack.isEmpty() && benchStacks.isEmpty() && materialId == null && materialItemId == null && materialHeatData == null && looseWorkRecipeId == null && materialCount == 0 && hitCount == 0;
    }

    public boolean hasPlacedWork() {
        return !abrasiveStack.isEmpty() || !benchStacks.isEmpty() || materialId != null || materialItemId != null || materialHeatData != null || looseWorkRecipeId != null || materialCount > 0 || hitCount > 0;
    }

    public boolean selectTemplate(ForgeTemplateDefinition template) {
        return setTemplateFromItem(template);
    }

    public boolean setTemplateFromItem(ForgeTemplateDefinition template) {
        if (this.templateId != null && this.templateId.equals(template.id())) {
            return true;
        }
        if (!canChangeTemplate()) {
            return false;
        }
        this.templateId = template.id();
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        looseWorkRecipeId = null;
        directOutputStack = ItemStack.EMPTY;
        materialCount = 0;
        hitCount = 0;
        displayRotationDegrees = 0.0F;
        sync();
        return true;
    }

    public boolean clearTemplate() {
        if (templateId == null || !canChangeTemplate()) {
            return false;
        }
        templateId = null;
        displayRotationDegrees = 0.0F;
        sync();
        return true;
    }

    public int remainingMaterials() {
        ForgeTemplateDefinition template = template();
        return template == null ? 0 : Math.max(0, template.requiredMaterials() - materialCount);
    }

    public int acceptMaterials(ItemStack stack, ToolMaterialDefinition material) {
        if (template() == null || isComplete()) {
            return 0;
        }
        if (materialId != null && !materialId.equals(material.id())) {
            return 0;
        }
        int taken = Math.min(stack.getCount(), remainingMaterials());
        if (taken > 0) {
            if (materialId == null) {
                materialId = material.id();
                materialItemId = BuiltInRegistries.ITEM.getKey(material.displayItem());
            }
            captureMaterialHeat(stack);
            stack.shrink(taken);
            materialCount += taken;
            sync();
        }
        return taken;
    }

    public boolean canHammer() {
        ForgeTemplateDefinition template = template();
        return template != null && materialCount >= template.requiredMaterials() && !isComplete();
    }

    public boolean hammer() {
        if (!canHammer()) {
            return false;
        }
        hitCount++;
        randomizeDisplayRotation();
        sync();
        return true;
    }

    public ItemStack outputStack() {
        if (!directOutputStack.isEmpty()) {
            return directOutputStack.copy();
        }
        ForgeTemplateDefinition template = template();
        return isComplete() && template != null && materialId != null ? applyMaterialHeat(template.outputStack(materialId, workQuality(template))) : ItemStack.EMPTY;
    }

    public ItemStack displayMaterialStack() {
        if (!directOutputStack.isEmpty()) {
            return directOutputStack.copy();
        }
        if (isComplete()) {
            return outputStack();
        }
        if (materialCount <= 0 || materialItemId == null) {
            return ItemStack.EMPTY;
        }
        return applyMaterialHeat(new ItemStack(BuiltInRegistries.ITEM.get(materialItemId)));
    }

    public ItemStack materialDropStack() {
        if (materialCount <= 0 || materialItemId == null) {
            return ItemStack.EMPTY;
        }
        return applyMaterialHeat(new ItemStack(BuiltInRegistries.ITEM.get(materialItemId), materialCount));
    }

    public ItemStack removeOutput() {
        ItemStack output = outputStack();
        if (!output.isEmpty()) {
            resetWork();
        }
        return output;
    }

    public List<ItemStack> removePlacedWorkStacks() {
        if (!hasPlacedWork()) {
            return List.of();
        }

        List<ItemStack> removed = new ArrayList<>();
        ItemStack materialDrop = materialDropStack();
        if (!materialDrop.isEmpty()) {
            removed.add(materialDrop);
        }
        if (!abrasiveStack.isEmpty()) {
            removed.add(abrasiveStack.copy());
        }
        for (ItemStack benchStack : benchStacks) {
            if (!benchStack.isEmpty()) {
                removed.add(benchStack.copy());
            }
        }

        clearWorkState();
        abrasiveStack = ItemStack.EMPTY;
        sync();
        return removed;
    }

    public boolean canPlaceAbrasive(ItemStack stack) {
        return workstationKind() == WorkstationKind.LAPIDARY_TABLE
                && stack.is(ModTags.Items.LAPIDARY_ABRASIVES)
                && abrasiveStack.isEmpty()
                && directOutputStack.isEmpty()
                && looseWorkRecipeId == null;
    }

    public boolean placeAbrasive(ItemStack stack) {
        if (!canPlaceAbrasive(stack)) {
            return false;
        }
        abrasiveStack = stack.copyWithCount(1);
        stack.shrink(1);
        sync();
        return true;
    }

    public boolean canPlaceToolmakerStack(ItemStack stack) {
        boolean finishedTool = ToolmakerBenchAssembly.isFinishedTool(stack);
        return workstationKind() == WorkstationKind.TOOLMAKERS_BENCH
                && !stack.isEmpty()
                && benchStacks.size() < MAX_BENCH_STACKS
                && (finishedTool ? benchStacks.isEmpty() : benchStacks.stream().noneMatch(ToolmakerBenchAssembly::isFinishedTool))
                && directOutputStack.isEmpty()
                && templateId == null
                && abrasiveStack.isEmpty()
                && materialId == null
                && materialItemId == null
                && materialHeatData == null
                && looseWorkRecipeId == null
                && materialCount == 0
                && hitCount == 0
                && ToolmakerBenchAssembly.isPlaceable(stack);
    }

    public boolean placeToolmakerStack(ItemStack stack) {
        if (!canPlaceToolmakerStack(stack)) {
            return false;
        }
        benchStacks.add(stack.copyWithCount(1));
        stack.shrink(1);
        sync();
        return true;
    }

    public void clearToolmakerStacks() {
        if (benchStacks.isEmpty()) {
            return;
        }
        benchStacks.clear();
        sync();
    }

    public void setDirectOutput(ItemStack output) {
        clearWorkState();
        if (output.isEmpty()) {
            directOutputStack = ItemStack.EMPTY;
        } else {
            directOutputStack = output.copy();
        }
        sync();
    }

    public void reset() {
        clearWorkState();
        abrasiveStack = ItemStack.EMPTY;
        sync();
    }

    private void resetWork() {
        clearWorkState();
        sync();
    }

    private void clearWorkState() {
        templateId = null;
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        looseWorkRecipeId = null;
        directOutputStack = ItemStack.EMPTY;
        benchStacks.clear();
        materialCount = 0;
        hitCount = 0;
        displayRotationDegrees = 0.0F;
    }

    public boolean canPlaceLooseWork(StationWorkRecipe recipe, ItemStack stack) {
        return recipe.canStart(workstationKind(), templateId, stack)
                && directOutputStack.isEmpty()
                && materialId == null
                && materialItemId == null
                && materialHeatData == null
                && looseWorkRecipeId == null
                && benchStacks.isEmpty()
                && materialCount == 0
                && hitCount == 0;
    }

    public boolean placeLooseWork(StationWorkRecipe recipe, ItemStack stack) {
        if (stack.isEmpty() || !canPlaceLooseWork(recipe, stack)) {
            return false;
        }
        looseWorkRecipeId = recipe.id();
        materialItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        materialCount = 1;
        stack.shrink(1);
        sync();
        return true;
    }

    public boolean hasLooseWork() {
        return looseWorkRecipeId != null && directOutputStack.isEmpty() && materialId == null && materialCount == 1 && materialItemId != null;
    }

    public boolean hammerLooseWork(StationWorkRecipe recipe) {
        if (!hasLooseWork() || !recipe.id().equals(looseWorkRecipeId)) {
            return false;
        }
        hitCount++;
        randomizeDisplayRotation();
        if (hitCount < recipe.requiredHits()) {
            sync();
            return true;
        }
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        looseWorkRecipeId = null;
        materialCount = 0;
        hitCount = 0;
        displayRotationDegrees = 0.0F;
        directOutputStack = recipe.outputCopy();
        sync();
        return true;
    }

    public boolean materialIsForgeReady() {
        ForgeTemplateDefinition template = template();
        return materialHeatData != null && level != null
                && template != null
                && materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) >= template.minimumTemperature();
    }

    public boolean hasMaterialHeat() {
        return materialHeatData != null && materialHeatTemperature() > 0.0F;
    }

    public float materialHeatTemperature() {
        if (materialHeatData == null) {
            return 0.0F;
        }
        return level == null
                ? Math.max(0.0F, Math.min(1.0F, materialHeatData.temperature()))
                : materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
    }

    public boolean materialHeatWasWorkable() {
        return materialHeatData != null && materialHeatData.workable();
    }

    public String materialHeatStatusKey() {
        ForgeTemplateDefinition template = template();
        float minimum = template == null ? MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue() : template.minimumTemperature();
        return WorkpieceHeat.statusKey(materialHeatTemperature(), materialHeatWasWorkable(), minimum);
    }

    private void captureMaterialHeat(ItemStack stack) {
        HeatedWorkpieceData heatData = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        if (heatData == null) {
            return;
        }
        if (level == null) {
            materialHeatData = heatData;
            return;
        }
        float stackTemperature = heatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        float existingTemperature = materialHeatData == null ? 0.0F : materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        boolean workable = heatData.workable() || materialHeatData != null && materialHeatData.workable();
        if (stackTemperature >= existingTemperature) {
            materialHeatData = refreshedHeatData(stackTemperature, workable);
        } else if (materialHeatData != null && workable && !materialHeatData.workable()) {
            materialHeatData = refreshedHeatData(existingTemperature, true);
        }
    }

    private ItemStack applyMaterialHeat(ItemStack stack) {
        if (stack.isEmpty() || materialHeatData == null) {
            return stack;
        }
        if (level == null) {
            stack.set(ModDataComponents.HEATED_WORKPIECE.get(), materialHeatData);
            return stack;
        }
        float temperature = materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        if (temperature > 0.0F) {
            stack.set(ModDataComponents.HEATED_WORKPIECE.get(), refreshedHeatData(temperature, materialHeatData.workable()));
        }
        return stack;
    }

    private HeatedWorkpieceData refreshedHeatData(float temperature, boolean workable) {
        long gameTime = level == null ? 0L : level.getGameTime();
        return new HeatedWorkpieceData(
                gameTime + Math.max(1L, Math.round(temperature * MobsToolForgingConfig.COOLING_TICKS.get())),
                temperature,
                gameTime,
                workable
        );
    }

    private int workQuality(ForgeTemplateDefinition template) {
        int quality = ToolPartData.DEFAULT_QUALITY;
        WorkstationKind kind = workstationKind();
        if (kind == WorkstationKind.TOOL_FORGE && materialHeatData != null && level != null) {
            float temperature = materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
            float minimum = template.minimumTemperature();
            if (temperature >= minimum) {
                float headroom = Math.max(0.01F, 1.0F - minimum);
                quality += Math.round(Math.min(1.0F, (temperature - minimum) / headroom) * 10.0F);
            } else {
                quality -= Math.round((minimum - temperature) * 20.0F);
            }
        }
        return Math.max(90, Math.min(110, quality));
    }

    private void randomizeDisplayRotation() {
        if (level == null) {
            return;
        }
        float direction = level.random.nextBoolean() ? 1.0F : -1.0F;
        float amount = 4.0F + level.random.nextFloat() * 8.0F;
        displayRotationDegrees = wrapDegrees(displayRotationDegrees + direction * amount);
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public WorkstationKind workstationKind() {
        return getBlockState().getBlock() instanceof ToolWorkstationBlock workstation ? workstation.kind() : WorkstationKind.TOOL_FORGE;
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
        if (templateId != null) {
            tag.putString(TEMPLATE_TAG, templateId.toString());
        }
        if (materialId != null) {
            tag.putString(MATERIAL_ID_TAG, materialId.toString());
        }
        if (materialItemId != null) {
            tag.putString(MATERIAL_ITEM_ID_TAG, materialItemId.toString());
        }
        if (materialHeatData != null) {
            tag.putLong(MATERIAL_HEAT_EXPIRES_TAG, materialHeatData.expiresAtGameTime());
            tag.putFloat(MATERIAL_HEAT_TEMPERATURE_TAG, materialHeatData.temperature());
            tag.putLong(MATERIAL_HEAT_LAST_UPDATE_TAG, materialHeatData.lastUpdateGameTime());
            tag.putBoolean(MATERIAL_HEAT_WORKABLE_TAG, materialHeatData.workable());
        }
        if (!directOutputStack.isEmpty()) {
            tag.put(DIRECT_OUTPUT_TAG, directOutputStack.saveOptional(registries));
        }
        if (looseWorkRecipeId != null) {
            tag.putString(LOOSE_WORK_RECIPE_TAG, looseWorkRecipeId.toString());
        }
        if (!abrasiveStack.isEmpty()) {
            tag.put(ABRASIVE_STACK_TAG, abrasiveStack.saveOptional(registries));
        }
        if (!benchStacks.isEmpty()) {
            ListTag stacks = new ListTag();
            for (ItemStack benchStack : benchStacks) {
                if (!benchStack.isEmpty()) {
                    stacks.add(benchStack.saveOptional(registries));
                }
            }
            tag.put(BENCH_STACKS_TAG, stacks);
        }
        tag.putInt(MATERIAL_COUNT_TAG, materialCount);
        tag.putInt(HIT_COUNT_TAG, hitCount);
        tag.putFloat(DISPLAY_ROTATION_TAG, displayRotationDegrees);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        templateId = tag.contains(TEMPLATE_TAG) ? ResourceLocation.parse(tag.getString(TEMPLATE_TAG)) : null;
        materialId = tag.contains(MATERIAL_ID_TAG) ? ResourceLocation.parse(tag.getString(MATERIAL_ID_TAG)) : null;
        materialItemId = tag.contains(MATERIAL_ITEM_ID_TAG) ? ResourceLocation.parse(tag.getString(MATERIAL_ITEM_ID_TAG)) : null;
        materialHeatData = tag.contains(MATERIAL_HEAT_TEMPERATURE_TAG)
                ? new HeatedWorkpieceData(
                tag.getLong(MATERIAL_HEAT_EXPIRES_TAG),
                tag.getFloat(MATERIAL_HEAT_TEMPERATURE_TAG),
                tag.getLong(MATERIAL_HEAT_LAST_UPDATE_TAG),
                tag.getBoolean(MATERIAL_HEAT_WORKABLE_TAG)
        )
                : null;
        directOutputStack = tag.contains(DIRECT_OUTPUT_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(DIRECT_OUTPUT_TAG)) : ItemStack.EMPTY;
        looseWorkRecipeId = tag.contains(LOOSE_WORK_RECIPE_TAG) ? ResourceLocation.parse(tag.getString(LOOSE_WORK_RECIPE_TAG)) : null;
        abrasiveStack = tag.contains(ABRASIVE_STACK_TAG) ? ItemStack.parseOptional(registries, tag.getCompound(ABRASIVE_STACK_TAG)) : ItemStack.EMPTY;
        benchStacks.clear();
        if (tag.contains(BENCH_STACKS_TAG)) {
            ListTag stacks = tag.getList(BENCH_STACKS_TAG, Tag.TAG_COMPOUND);
            int limit = Math.min(MAX_BENCH_STACKS, stacks.size());
            for (int index = 0; index < limit; index++) {
                ItemStack stack = ItemStack.parseOptional(registries, stacks.getCompound(index));
                if (!stack.isEmpty()) {
                    benchStacks.add(stack);
                }
            }
        }
        materialCount = tag.getInt(MATERIAL_COUNT_TAG);
        hitCount = tag.getInt(HIT_COUNT_TAG);
        displayRotationDegrees = tag.getFloat(DISPLAY_ROTATION_TAG);
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

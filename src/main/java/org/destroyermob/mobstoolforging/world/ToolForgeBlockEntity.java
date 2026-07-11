package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ModularArmorPartItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class ToolForgeBlockEntity extends BlockEntity {
    private static final String TEMPLATE_TAG = "Template";
    private static final String SOURCE_RACK_POS_TAG = "SourceRackPos";
    private static final String SOURCE_RACK_SLOT_TAG = "SourceRackSlot";
    private static final String LINKED_RACKS_TAG = "LinkedRacks";
    private static final String MATERIAL_COUNT_TAG = "MaterialCount";
    private static final String HIT_COUNT_TAG = "HitCount";
    private static final String MATERIAL_ID_TAG = "MaterialId";
    private static final String MATERIAL_ITEM_ID_TAG = "MaterialItemId";
    private static final String DISPLAY_ROTATION_TAG = "DisplayRotation";
    private static final String MATERIAL_HEAT_EXPIRES_TAG = "MaterialHeatExpires";
    private static final String MATERIAL_HEAT_TEMPERATURE_TAG = "MaterialHeatTemperature";
    private static final String MATERIAL_HEAT_LAST_UPDATE_TAG = "MaterialHeatLastUpdate";
    private static final String MATERIAL_HEAT_WORKABLE_TAG = "MaterialHeatWorkable";
    private static final String STARTING_HEAT_LEVEL_TAG = "StartingHeatLevel";
    private static final String QUALITY_SCORE_TAG = "QualityScore";
    private static final String NEXT_GOOD_HIT_GAME_TIME_TAG = "NextGoodHitGameTime";
    private static final String DIRECT_OUTPUT_TAG = "DirectOutput";
    private static final String LOOSE_WORK_RECIPE_TAG = "LooseWorkRecipe";
    private static final String ABRASIVE_STACK_TAG = "AbrasiveStack";
    private static final String BENCH_STACKS_TAG = "BenchStacks";
    private static final int MAX_BENCH_STACKS = 9;
    private static final int MAX_LINKED_PATTERN_RACKS = 8;
    private static final int TIMED_HIT_QUALITY_BONUS = 9;
    private static final int PRECISION_TIMED_HIT_QUALITY_BONUS = 12;
    private static final int MISSED_TIMING_QUALITY_PENALTY = 4;

    @Nullable
    private ResourceLocation templateId;
    @Nullable
    private BlockPos sourceRackPos;
    private int sourceRackSlot = -1;
    private final List<BlockPos> linkedPatternRacks = new ArrayList<>();
    @Nullable
    private ResourceLocation materialId;
    @Nullable
    private ResourceLocation materialItemId;
    @Nullable
    private HeatedWorkpieceData materialHeatData;
    private HeatLevel startingHeatLevel = HeatLevel.NONE;
    @Nullable
    private ResourceLocation looseWorkRecipeId;
    private ItemStack directOutputStack = ItemStack.EMPTY;
    private ItemStack abrasiveStack = ItemStack.EMPTY;
    private final List<ItemStack> benchStacks = new ArrayList<>();
    private int materialCount;
    private int hitCount;
    private int qualityScore = ForgingQuality.DEFAULT_SCORE;
    private long nextGoodHitGameTime = -1L;
    private float displayRotationDegrees;

    public ToolForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TOOL_WORKSTATION.get(), pos, blockState);
    }

    @Nullable
    public ForgeTemplateDefinition template() {
        if (selectedPatternMissing()) {
            return null;
        }
        return templateId == null ? null : ToolTypeRegistry.template(templateId).orElse(null);
    }

    @Nullable
    public ResourceLocation templateId() {
        return templateId;
    }

    @Nullable
    public BlockPos sourceRackPos() {
        return sourceRackPos;
    }

    public int sourceRackSlot() {
        return sourceRackSlot;
    }

    public boolean hasPatternSource() {
        return sourceRackPos != null && sourceRackSlot >= 0;
    }

    public static int maxLinkedPatternRacks() {
        return MAX_LINKED_PATTERN_RACKS;
    }

    public int linkedRackCount() {
        return linkedPatternRacks.size();
    }

    public List<BlockPos> linkedRackPositions() {
        return linkedPatternRacks.stream().map(BlockPos::immutable).toList();
    }

    public boolean hasLinkedRack(BlockPos rackPos) {
        return linkedPatternRacks.stream().anyMatch(pos -> pos.equals(rackPos));
    }

    public PatternRackLinkResult linkPatternRack(BlockPos rackPos) {
        BlockPos linkedPos = rackPos.immutable();
        if (hasLinkedRack(linkedPos)) {
            return PatternRackLinkResult.ALREADY_LINKED;
        }
        if (linkedPatternRacks.size() >= MAX_LINKED_PATTERN_RACKS) {
            return PatternRackLinkResult.FULL;
        }
        linkedPatternRacks.add(linkedPos);
        sync();
        return PatternRackLinkResult.LINKED;
    }

    public boolean selectedPatternMissing() {
        return templateId != null
                && hasPatternSource()
                && directOutputStack.isEmpty()
                && !hasPlacedWork()
                && !sourcePatternMatches();
    }

    private boolean sourcePatternMatches() {
        if (level == null || sourceRackPos == null || sourceRackSlot < 0) {
            return true;
        }
        if (!(level.getBlockEntity(sourceRackPos) instanceof PatternRackBlockEntity rack)) {
            return false;
        }
        return rack.templateId(sourceRackSlot).filter(templateId::equals).isPresent();
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

    public static int maxBenchStacks() {
        return MAX_BENCH_STACKS;
    }

    @Nullable
    public ResourceLocation materialId() {
        return materialId;
    }

    public float progress() {
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork != null) {
            return Math.min(1.0F, hitCount / (float) coatingWork.template().requiredHits());
        }
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
        return directOutputStack.isEmpty() && abrasiveStack.isEmpty() && benchStacks.isEmpty() && templateId == null && materialId == null && materialItemId == null && materialHeatData == null && startingHeatLevel == HeatLevel.NONE && looseWorkRecipeId == null && materialCount == 0 && hitCount == 0;
    }

    public boolean isComplete() {
        if (!directOutputStack.isEmpty()) {
            return true;
        }
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork != null) {
            return materialId != null
                    && materialCount >= coatingWork.template().requiredMaterials()
                    && hitCount >= coatingWork.template().requiredHits();
        }
        ForgeTemplateDefinition template = template();
        return template != null && materialCount >= template.requiredMaterials() && hitCount >= template.requiredHits();
    }

    public boolean canChangeTemplate() {
        return directOutputStack.isEmpty() && benchStacks.isEmpty() && materialId == null && materialItemId == null && materialHeatData == null && startingHeatLevel == HeatLevel.NONE && looseWorkRecipeId == null && materialCount == 0 && hitCount == 0;
    }

    public boolean hasPlacedWork() {
        return !abrasiveStack.isEmpty() || !benchStacks.isEmpty() || materialId != null || materialItemId != null || materialHeatData != null || startingHeatLevel != HeatLevel.NONE || looseWorkRecipeId != null || materialCount > 0 || hitCount > 0;
    }

    public boolean selectTemplate(ForgeTemplateDefinition template) {
        return setTemplateFromItem(template);
    }

    public boolean setTemplateFromItem(ForgeTemplateDefinition template) {
        return setTemplate(template, null, -1);
    }

    public boolean setTemplateFromRack(ForgeTemplateDefinition template, BlockPos rackPos, int rackSlot) {
        return setTemplate(template, rackPos.immutable(), rackSlot);
    }

    private boolean setTemplate(ForgeTemplateDefinition template, @Nullable BlockPos rackPos, int rackSlot) {
        if (this.templateId != null && this.templateId.equals(template.id())) {
            sourceRackPos = rackPos;
            sourceRackSlot = rackSlot;
            sync();
            return true;
        }
        if (!canChangeTemplate()) {
            return false;
        }
        this.templateId = template.id();
        sourceRackPos = rackPos;
        sourceRackSlot = rackSlot;
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        startingHeatLevel = HeatLevel.NONE;
        qualityScore = ForgingQuality.DEFAULT_SCORE;
        nextGoodHitGameTime = -1L;
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
        sourceRackPos = null;
        sourceRackSlot = -1;
        displayRotationDegrees = 0.0F;
        sync();
        return true;
    }

    public int remainingMaterials() {
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork != null) {
            return Math.max(0, coatingWork.template().requiredMaterials() - materialCount);
        }
        ForgeTemplateDefinition template = template();
        return template == null ? 0 : Math.max(0, template.requiredMaterials() - materialCount);
    }

    public int acceptMaterials(ItemStack stack, ToolMaterialDefinition material) {
        ForgeTemplateDefinition template = template();
        if (template == null || isComplete()) {
            return 0;
        }
        if (workstationKind() == WorkstationKind.LAPIDARY_TABLE && !hasRequiredLapidaryAbrasive(material)) {
            return 0;
        }
        if (materialId != null && !materialId.equals(material.id())) {
            return 0;
        }
        int taken = Math.min(stack.getCount(), remainingMaterials());
        if (taken > 0) {
            if (materialId == null) {
                materialId = material.id();
                materialItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                captureStartingHeat(stack, material);
                initializeQuality(material);
            }
            captureMaterialHeat(stack);
            stack.shrink(taken);
            materialCount += taken;
            scheduleFirstGoodHitIfReady();
            sync();
        }
        return taken;
    }

    public boolean canHammer() {
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork != null) {
            return materialId != null
                    && materialCount >= coatingWork.template().requiredMaterials()
                    && !isComplete();
        }
        ForgeTemplateDefinition template = template();
        return template != null
                && materialCount >= template.requiredMaterials()
                && !isComplete();
    }

    public boolean hammer(boolean precisionTool) {
        if (!canHammer()) {
            return false;
        }
        hitCount++;
        applyTimingQuality(precisionTool);
        randomizeDisplayRotation();
        if (lapidaryCoatingWork().isEmpty()) {
            completeArmorAttachmentIfReady();
        }
        sync();
        return true;
    }

    public ItemStack outputStack() {
        if (!directOutputStack.isEmpty()) {
            return directOutputStack.copy();
        }
        if (isComplete() && lapidaryCoatingWork().isPresent()) {
            return lapidaryCoatingOutput();
        }
        ForgeTemplateDefinition template = template();
        return isComplete() && template != null && materialId != null ? applyMaterialHeat(template.outputStack(materialId, completedQualityScore())) : ItemStack.EMPTY;
    }

    public ItemStack displayMaterialStack() {
        if (!directOutputStack.isEmpty()) {
            return directOutputStack.copy();
        }
        if (isComplete()) {
            return outputStack();
        }
        if (hasLapidaryCoatingBase() && (materialCount <= 0 || materialItemId == null)) {
            return lapidaryCoatingBaseStack();
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
            clearWorkState(false);
            consumeAbrasiveAfterLapidaryCraft();
            sync();
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

        clearWorkState(false);
        abrasiveStack = ItemStack.EMPTY;
        sync();
        return removed;
    }

    public boolean canPlaceAbrasive(ItemStack stack) {
        return workstationKind() == WorkstationKind.LAPIDARY_TABLE
                && LapidaryAbrasives.isAbrasive(stack)
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

    public boolean hasRequiredLapidaryAbrasive(ToolMaterialDefinition material) {
        return missingRequiredLapidaryAbrasive(material).isEmpty();
    }

    public Optional<ResourceLocation> missingRequiredLapidaryAbrasive(ToolMaterialDefinition material) {
        if (material == null) {
            return Optional.empty();
        }
        return material.requiredLapidaryAbrasiveTier()
                .filter(tier -> !LapidaryAbrasives.satisfiesTier(abrasiveStack, tier));
    }

    public boolean hasLapidaryCoatingBase() {
        return lapidaryCoatingWork().isPresent();
    }

    public ItemStack lapidaryCoatingBaseStack() {
        return lapidaryCoatingWork()
                .map(work -> work.baseStack().copyWithCount(1))
                .orElse(ItemStack.EMPTY);
    }

    public int lapidaryCoatingRequiredMaterials() {
        return lapidaryCoatingWork().map(work -> work.template().requiredMaterials()).orElse(0);
    }

    public int lapidaryCoatingRequiredHits() {
        return lapidaryCoatingWork().map(work -> work.template().requiredHits()).orElse(0);
    }

    public boolean canPlaceLapidaryBasePart(ItemStack stack) {
        return workstationKind() == WorkstationKind.LAPIDARY_TABLE
                && directOutputStack.isEmpty()
                && templateId == null
                && materialId == null
                && materialItemId == null
                && materialHeatData == null
                && startingHeatLevel == HeatLevel.NONE
                && looseWorkRecipeId == null
                && benchStacks.isEmpty()
                && materialCount == 0
                && hitCount == 0
                && isLapidaryCoatablePart(stack)
                && level != null
                && WorkpieceHeat.isHot(stack, level);
    }

    public boolean placeLapidaryBasePart(ItemStack stack) {
        if (!canPlaceLapidaryBasePart(stack)) {
            return false;
        }
        benchStacks.add(stack.copyWithCount(1));
        stack.shrink(1);
        sync();
        return true;
    }

    public boolean canPlaceLapidaryCoatingMaterial(ItemStack stack, ToolMaterialDefinition material) {
        if (stack.isEmpty()
                || material == null
                || workstationKind() != WorkstationKind.LAPIDARY_TABLE
                || material.category() != MaterialCategory.GEM
                || !hasRequiredLapidaryAbrasive(material)
                || remainingMaterials() <= 0
                || isComplete()) {
            return false;
        }
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork == null || !coatingWork.template().allowsMaterial(material.id())) {
            return false;
        }
        return materialId == null || materialId.equals(material.id());
    }

    public int acceptLapidaryCoatingMaterial(ItemStack stack, ToolMaterialDefinition material) {
        if (!canPlaceLapidaryCoatingMaterial(stack, material)) {
            return 0;
        }
        int taken = Math.min(stack.getCount(), remainingMaterials());
        if (taken <= 0) {
            return 0;
        }
        if (materialId == null) {
            materialId = material.id();
            materialItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            initializeQuality(material);
        }
        stack.shrink(taken);
        materialCount += taken;
        scheduleFirstGoodHitIfReady();
        sync();
        return taken;
    }

    public ItemStack lapidaryCoatingMaterialPreviewStack() {
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork == null) {
            return ItemStack.EMPTY;
        }
        ResourceLocation previewMaterial = materialId != null ? materialId : lapidaryCoatingPreviewMaterial(coatingWork);
        return previewMaterial == null ? ItemStack.EMPTY : MaterialCatalog.displayStack(previewMaterial);
    }

    public boolean canPlaceRepairTool(ItemStack stack) {
        return canUseRepairStacks()
                && benchStacks.isEmpty()
                && isRepairableAtThisStation(stack);
    }

    public boolean placeRepairTool(ItemStack stack) {
        if (!canPlaceRepairTool(stack)) {
            return false;
        }
        benchStacks.add(stack.copyWithCount(1));
        stack.shrink(1);
        sync();
        return true;
    }

    public boolean canPlaceRepairMaterial(ItemStack stack) {
        return canUseRepairStacks()
                && benchStacks.size() == 1
                && isRepairMaterialFor(benchStacks.get(0), stack);
    }

    public boolean placeRepairMaterial(ItemStack stack) {
        if (!canPlaceRepairMaterial(stack)) {
            return false;
        }
        benchStacks.add(stack.copyWithCount(1));
        stack.shrink(1);
        sync();
        return true;
    }

    public boolean hasRepairWork() {
        return canUseRepairStacks()
                && benchStacks.size() == 2
                && isRepairMaterialFor(benchStacks.get(0), benchStacks.get(1));
    }

    public boolean hasRepairStacks() {
        return canUseRepairStacks()
                && !benchStacks.isEmpty();
    }

    public boolean canPlaceArmorAttachmentTarget(ItemStack stack) {
        ForgeTemplateDefinition template = template();
        return ArmorForgeAttachment.isAttachmentStation(workstationKind())
                && ArmorForgeAttachment.isAttachmentTemplate(template)
                && ArmorForgeAttachment.isCompatibleTarget(template, stack)
                && directOutputStack.isEmpty()
                && (workstationKind() == WorkstationKind.LAPIDARY_TABLE || abrasiveStack.isEmpty())
                && benchStacks.isEmpty()
                && materialId == null
                && materialItemId == null
                && materialHeatData == null
                && startingHeatLevel == HeatLevel.NONE
                && looseWorkRecipeId == null
                && materialCount == 0
                && hitCount == 0;
    }

    public boolean placeArmorAttachmentTarget(ItemStack stack) {
        if (!canPlaceArmorAttachmentTarget(stack)) {
            return false;
        }
        benchStacks.add(stack.copyWithCount(1));
        stack.shrink(1);
        sync();
        return true;
    }

    public boolean hasArmorAttachmentTarget() {
        ForgeTemplateDefinition template = template();
        return ArmorForgeAttachment.isAttachmentStation(workstationKind())
                && ArmorForgeAttachment.isAttachmentTemplate(template)
                && benchStacks.size() == 1
                && directOutputStack.isEmpty()
                && ArmorForgeAttachment.isCompatibleTarget(template, benchStacks.get(0));
    }

    public ItemStack armorAttachmentTarget() {
        return hasArmorAttachmentTarget() ? benchStacks.get(0).copy() : ItemStack.EMPTY;
    }

    public ItemStack repairToolWithPlacedMaterial() {
        if (!hasRepairWork()) {
            return ItemStack.EMPTY;
        }
        ItemStack target = benchStacks.get(0);
        ItemStack repaired = ToolRepairing.isRepairableModularTool(target)
                ? ToolRepairing.repairWithOneMaterial(target)
                : ArmorRepairing.repairWithOneMaterial(target);
        if (repaired.isEmpty()) {
            return ItemStack.EMPTY;
        }
        clearWorkState();
        randomizeDisplayRotation();
        if (isRepairableAtThisStation(repaired)) {
            benchStacks.add(repaired.copy());
        } else {
            directOutputStack = repaired.copy();
        }
        sync();
        return repaired.copy();
    }

    private boolean canUseRepairStacks() {
        return (workstationKind().isSmithingAnvilLike() || workstationKind() == WorkstationKind.LEATHER_STATION)
                && directOutputStack.isEmpty()
                && templateId == null
                && abrasiveStack.isEmpty()
                && materialId == null
                && materialItemId == null
                && materialHeatData == null
                && startingHeatLevel == HeatLevel.NONE
                && looseWorkRecipeId == null
                && materialCount == 0
                && hitCount == 0;
    }

    private boolean isRepairableAtThisStation(ItemStack stack) {
        return workstationKind().isSmithingAnvilLike() && ToolRepairing.isRepairableModularTool(stack)
                || ArmorRepairing.canRepairAt(stack, workstationKind());
    }

    private boolean isRepairMaterialFor(ItemStack target, ItemStack candidate) {
        return workstationKind().isSmithingAnvilLike()
                && ToolRepairing.isRepairableModularTool(target)
                && ToolRepairing.isRepairMaterial(target, candidate)
                || ArmorRepairing.canRepairAt(target, workstationKind())
                && ArmorRepairing.isRepairMaterial(target, candidate);
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
                && startingHeatLevel == HeatLevel.NONE
                && looseWorkRecipeId == null
                && materialCount == 0
                && hitCount == 0
                && ToolmakerBenchAssembly.canPlace(benchStacks, stack);
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

    public void setToolmakerStacks(List<ItemStack> stacks) {
        if (workstationKind() != WorkstationKind.TOOLMAKERS_BENCH) {
            return;
        }

        benchStacks.clear();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (benchStacks.size() >= MAX_BENCH_STACKS) {
                break;
            }
            benchStacks.add(stack.copyWithCount(1));
        }
        sync();
    }

    public boolean replaceToolmakerAssemblyStack(int assemblyIndex, ItemStack replacement) {
        if (workstationKind() != WorkstationKind.TOOLMAKERS_BENCH || assemblyIndex < 0 || replacement.isEmpty()) {
            return false;
        }

        int currentAssemblyIndex = 0;
        for (int stackIndex = 0; stackIndex < benchStacks.size(); stackIndex++) {
            ItemStack existing = benchStacks.get(stackIndex);
            if (existing.isEmpty() || existing.is(ModItems.PLANT_FIBER.get())) {
                continue;
            }
            if (currentAssemblyIndex == assemblyIndex) {
                benchStacks.set(stackIndex, replacement.copyWithCount(1));
                sync();
                return true;
            }
            currentAssemblyIndex++;
        }
        return false;
    }

    public boolean replaceToolmakerStack(int stackIndex, ItemStack replacement) {
        if (workstationKind() != WorkstationKind.TOOLMAKERS_BENCH
                || stackIndex < 0
                || stackIndex >= benchStacks.size()
                || replacement.isEmpty()) {
            return false;
        }
        benchStacks.set(stackIndex, replacement.copyWithCount(1));
        sync();
        return true;
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

    private void consumeAbrasiveAfterLapidaryCraft() {
        if (workstationKind() != WorkstationKind.LAPIDARY_TABLE || abrasiveStack.isEmpty()) {
            return;
        }
        abrasiveStack.shrink(1);
        if (abrasiveStack.isEmpty()) {
            abrasiveStack = ItemStack.EMPTY;
        }
    }

    private void clearWorkState() {
        clearWorkState(true);
    }

    private void clearWorkState(boolean clearTemplate) {
        if (clearTemplate) {
            templateId = null;
            sourceRackPos = null;
            sourceRackSlot = -1;
        }
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        startingHeatLevel = HeatLevel.NONE;
        looseWorkRecipeId = null;
        directOutputStack = ItemStack.EMPTY;
        benchStacks.clear();
        materialCount = 0;
        hitCount = 0;
        qualityScore = ForgingQuality.DEFAULT_SCORE;
        nextGoodHitGameTime = -1L;
        displayRotationDegrees = 0.0F;
    }

    public boolean canPlaceLooseWork(StationWorkRecipe recipe, ItemStack stack) {
        return recipe.canStart(workstationKind(), templateId, stack)
                && directOutputStack.isEmpty()
                && materialId == null
                && materialItemId == null
                && materialHeatData == null
                && startingHeatLevel == HeatLevel.NONE
                && looseWorkRecipeId == null
                && benchStacks.isEmpty()
                && materialCount == 0
                && hitCount == 0;
    }

    public boolean placeLooseWork(StationWorkRecipe recipe, ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() < recipe.input().count() || !canPlaceLooseWork(recipe, stack)) {
            return false;
        }
        looseWorkRecipeId = recipe.id();
        materialItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        materialCount = recipe.input().count();
        qualityScore = ForgingQuality.DEFAULT_SCORE;
        stack.shrink(recipe.input().count());
        if (workstationKind() == WorkstationKind.LEATHER_STATION) {
            scheduleFirstGoodHitIfReady();
        }
        sync();
        return true;
    }

    public boolean hasLooseWork() {
        return looseWorkRecipeId != null && directOutputStack.isEmpty() && materialId == null && materialCount > 0 && materialItemId != null;
    }

    public boolean hammerLooseWork(StationWorkRecipe recipe) {
        if (!hasLooseWork() || !recipe.id().equals(looseWorkRecipeId)) {
            return false;
        }
        hitCount++;
        if (workstationKind() == WorkstationKind.LEATHER_STATION) {
            applyTimingQuality(false);
        }
        randomizeDisplayRotation();
        if (hitCount < recipe.requiredHits()) {
            sync();
            return true;
        }
        ItemStack output = applyLooseWorkQuality(recipe.outputCopy());
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        looseWorkRecipeId = null;
        materialCount = 0;
        hitCount = 0;
        displayRotationDegrees = 0.0F;
        directOutputStack = output;
        sync();
        return true;
    }

    private ItemStack applyLooseWorkQuality(ItemStack output) {
        if (output.isEmpty() || workstationKind() != WorkstationKind.LEATHER_STATION) {
            return output;
        }
        int quality = completedQualityScore();
        ArmorPartData partData = output.get(ModDataComponents.ARMOR_PART.get());
        if (partData != null) {
            output.set(ModDataComponents.ARMOR_PART.get(), new ArmorPartData(
                    partData.partType(),
                    partData.materialId(),
                    quality,
                    partData.coatingBaseMaterial()
            ));
        }
        ArmorConstructionData armorData = output.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (armorData != null) {
            output.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), new ArmorConstructionData(
                    armorData.armorType(),
                    armorData.skullMaterial(),
                    armorData.combMaterial(),
                    armorData.overlayBaseMaterial(),
                    armorData.visorMaterial(),
                    quality
            ));
        }
        return output;
    }

    private Optional<LapidaryCoatingWork> lapidaryCoatingWork() {
        if (workstationKind() != WorkstationKind.LAPIDARY_TABLE || benchStacks.size() != 1 || !directOutputStack.isEmpty()) {
            return Optional.empty();
        }
        ItemStack baseStack = benchStacks.get(0);
        ToolPartData baseData = baseStack.get(ModDataComponents.TOOL_PART.get());
        if (baseData != null) {
            return lapidaryCoatingWork(baseStack, baseData);
        }
        ArmorPartData armorData = baseStack.get(ModDataComponents.ARMOR_PART.get());
        return armorData == null ? Optional.empty() : lapidaryCoatingWork(baseStack, armorData);
    }

    public static boolean isLapidaryCoatablePart(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data != null) {
            return lapidaryCoatingWork(stack, data).isPresent();
        }
        ArmorPartData armorData = stack.get(ModDataComponents.ARMOR_PART.get());
        return armorData != null && lapidaryCoatingWork(stack, armorData).isPresent();
    }

    public static ItemStack lapidaryCoatingPreview(ItemStack baseStack, ResourceLocation coatingMaterial) {
        if (baseStack.isEmpty() || coatingMaterial == null) {
            return ItemStack.EMPTY;
        }
        if (MaterialCatalog.definition(coatingMaterial)
                .filter(definition -> definition.category() == MaterialCategory.GEM)
                .isEmpty()) {
            return ItemStack.EMPTY;
        }
        LapidaryCoatingWork coatingWork;
        ToolPartData toolData = baseStack.get(ModDataComponents.TOOL_PART.get());
        if (toolData != null) {
            coatingWork = lapidaryCoatingWork(baseStack, toolData).orElse(null);
        } else {
            ArmorPartData armorData = baseStack.get(ModDataComponents.ARMOR_PART.get());
            coatingWork = armorData == null ? null : lapidaryCoatingWork(baseStack, armorData).orElse(null);
        }
        if (coatingWork == null
                || !coatingWork.template().allowsMaterial(coatingMaterial)
                || !coatingWork.canCreatePart(coatingMaterial)) {
            return ItemStack.EMPTY;
        }
        int quality = MobsToolForgingConfig.ENABLE_QUALITY.get()
                ? ForgingQuality.clampScore(Math.round((coatingWork.baseQuality() + ForgingQuality.DEFAULT_SCORE) / 2.0F))
                : ForgingQuality.DEFAULT_SCORE;
        return createLapidaryCoatingOutput(coatingWork, coatingMaterial, quality);
    }

    private static Optional<LapidaryCoatingWork> lapidaryCoatingWork(ItemStack stack, ToolPartData data) {
        if (data.coatingBaseMaterial().isPresent()) {
            return Optional.empty();
        }
        if (MaterialCatalog.definition(data.materialId())
                .filter(definition -> definition.category() == MaterialCategory.METAL)
                .isEmpty()) {
            return Optional.empty();
        }
        return ToolTypeRegistry.templates().stream()
                .filter(template -> data.partType().equals(template.partType()))
                .filter(template -> isCoatableToolType(template.toolType()))
                .flatMap(template -> ToolTypeRegistry.toolType(template.toolType())
                        .filter(definition -> definition.matchesPartItem(data.partType(), data.materialId(), stack))
                        .map(definition -> LapidaryCoatingWork.tool(stack, data, template, definition))
                        .stream())
                .min(Comparator
                        .comparingInt((LapidaryCoatingWork work) -> work.template().requiredMaterials())
                        .thenComparing(work -> work.template().id().toString()));
    }

    private static Optional<LapidaryCoatingWork> lapidaryCoatingWork(ItemStack stack, ArmorPartData data) {
        if (data.coatingBaseMaterial().isPresent() || !isArmorPlatePart(data.partType())) {
            return Optional.empty();
        }
        if (MaterialCatalog.definition(data.materialId())
                .filter(definition -> definition.category() == MaterialCategory.METAL)
                .isEmpty()) {
            return Optional.empty();
        }
        return ToolTypeRegistry.templates().stream()
                .filter(template -> data.partType().equals(template.partType()))
                .filter(ArmorForgeAttachment::isAttachmentTemplate)
                .filter(template -> {
                    ItemStack output = template.outputStack(data.materialId(), data.quality());
                    return !output.isEmpty() && output.is(stack.getItem());
                })
                .map(template -> LapidaryCoatingWork.armor(stack, data, template))
                .min(Comparator
                        .comparingInt((LapidaryCoatingWork work) -> work.template().requiredMaterials())
                        .thenComparing(work -> work.template().id().toString()));
    }

    private static boolean isCoatableToolType(ResourceLocation toolType) {
        return !ToolTypeRegistry.SMITHING_HAMMER_TOOL_TYPE.equals(toolType)
                && !ToolTypeRegistry.SCREWDRIVER_TOOL_TYPE.equals(toolType)
                && !ToolTypeRegistry.GEM_CUTTERS_KNIFE_TOOL_TYPE.equals(toolType);
    }

    private static boolean isArmorPlatePart(String partType) {
        return ArmorPartData.HELMET_PLATE.equals(partType)
                || ArmorPartData.CHESTPLATE_BODY.equals(partType)
                || ArmorPartData.LEGGINGS_PLATE.equals(partType)
                || ArmorPartData.BOOTS_PLATE.equals(partType);
    }

    @Nullable
    private ResourceLocation lapidaryCoatingPreviewMaterial(LapidaryCoatingWork coatingWork) {
        List<ResourceLocation> materials = MaterialCatalog.starterMaterialIds().stream()
                .filter(coatingWork.template()::allowsMaterial)
                .filter(material -> MaterialCatalog.definition(material)
                        .filter(definition -> definition.category() == MaterialCategory.GEM)
                        .isPresent())
                .filter(coatingWork::canCreatePart)
                .toList();
        if (materials.isEmpty()) {
            return null;
        }
        long gameTime = level == null ? 0L : level.getGameTime();
        return materials.get((int) (gameTime / 40L % materials.size()));
    }

    private ItemStack lapidaryCoatingOutput() {
        LapidaryCoatingWork coatingWork = lapidaryCoatingWork().orElse(null);
        if (coatingWork == null || materialId == null) {
            return ItemStack.EMPTY;
        }
        int outputQuality = MobsToolForgingConfig.ENABLE_QUALITY.get()
                ? ForgingQuality.clampScore(Math.round((coatingWork.baseQuality() + completedQualityScore()) / 2.0F))
                : ForgingQuality.DEFAULT_SCORE;
        return createLapidaryCoatingOutput(coatingWork, materialId, outputQuality);
    }

    private static ItemStack createLapidaryCoatingOutput(LapidaryCoatingWork coatingWork, ResourceLocation materialId, int outputQuality) {
        ItemStack output = coatingWork.createPart(materialId, outputQuality);
        if (output.isEmpty()) {
            output = coatingWork.baseStack().copyWithCount(1);
        }
        if (coatingWork.baseToolData() != null) {
            ToolPartData coatedData = coatingWork.baseToolData().withCoating(materialId, outputQuality);
            output.set(ModDataComponents.TOOL_PART.get(), coatedData);
            Integer wear = coatingWork.baseStack().get(ModDataComponents.TOOL_PART_WEAR.get());
            if (wear != null) {
                output.set(ModDataComponents.TOOL_PART_WEAR.get(), wear);
            }
            if (!(output.getItem() instanceof ModularToolPartItem)) {
                ToolStackNames.applyCoatedPartName(output, coatedData.partType(), coatingWork.baseMaterial(), materialId);
            }
        } else if (coatingWork.baseArmorData() != null) {
            ArmorPartData coatedData = coatingWork.baseArmorData().withCoating(materialId, outputQuality);
            output.set(ModDataComponents.ARMOR_PART.get(), coatedData);
            if (!(output.getItem() instanceof ModularArmorPartItem)) {
                ToolStackNames.applyCoatedPartName(output, coatedData.partType(), coatingWork.baseMaterial(), materialId);
            }
        }
        output.remove(ModDataComponents.HEATED_WORKPIECE.get());
        ToolExternalComponents.copyCompatibleExternalComponents(coatingWork.baseStack(), output);
        return output;
    }

    private void completeArmorAttachmentIfReady() {
        ForgeTemplateDefinition template = template();
        if (template == null
                || !ArmorForgeAttachment.isAttachmentTemplate(template)
                || materialId == null
                || materialCount < template.requiredMaterials()
                || hitCount < template.requiredHits()
                || !hasArmorAttachmentTarget()) {
            return;
        }
        ItemStack output = ArmorForgeAttachment.apply(benchStacks.get(0), template.id(), materialId, completedQualityScore());
        if (output.isEmpty()) {
            return;
        }
        templateId = null;
        sourceRackPos = null;
        sourceRackSlot = -1;
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        startingHeatLevel = HeatLevel.NONE;
        looseWorkRecipeId = null;
        materialCount = 0;
        hitCount = 0;
        qualityScore = ForgingQuality.DEFAULT_SCORE;
        nextGoodHitGameTime = -1L;
        benchStacks.clear();
        displayRotationDegrees = 0.0F;
        directOutputStack = output;
    }

    private record LapidaryCoatingWork(
            ItemStack baseStack,
            @Nullable ToolPartData baseToolData,
            @Nullable ArmorPartData baseArmorData,
            ForgeTemplateDefinition template,
            @Nullable ToolTypeDefinition definition
    ) {
        private static LapidaryCoatingWork tool(ItemStack stack, ToolPartData data, ForgeTemplateDefinition template, ToolTypeDefinition definition) {
            return new LapidaryCoatingWork(stack, data, null, template, definition);
        }

        private static LapidaryCoatingWork armor(ItemStack stack, ArmorPartData data, ForgeTemplateDefinition template) {
            return new LapidaryCoatingWork(stack, null, data, template, null);
        }

        private String partType() {
            return baseToolData == null ? baseArmorData.partType() : baseToolData.partType();
        }

        private ResourceLocation baseMaterial() {
            return baseToolData == null ? baseArmorData.materialId() : baseToolData.materialId();
        }

        private int baseQuality() {
            return baseToolData == null ? baseArmorData.quality() : baseToolData.quality();
        }

        private boolean canCreatePart(ResourceLocation material) {
            if (definition != null) {
                return !definition.createPart(partType(), material).isEmpty();
            }
            return !template.outputStack(material).isEmpty();
        }

        private ItemStack createPart(ResourceLocation material, int quality) {
            if (definition != null) {
                return definition.createPart(partType(), material, quality);
            }
            return template.outputStack(material, quality);
        }
    }

    public boolean materialIsForgeReady() {
        ForgeTemplateDefinition template = template();
        if (MobsToolForgingConfig.REQUIRE_HEAT_AT_JOB_START_ONLY.get() && !MobsToolForgingConfig.WORKPIECE_COOLS_MID_CRAFT.get()) {
            return startingHeatLevel != HeatLevel.NONE;
        }
        return materialHeatData != null && level != null
                && template != null
                && materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) >= materialMinimumForgeTemperature();
    }

    public boolean hasMaterialHeat() {
        return startingHeatLevel != HeatLevel.NONE || materialHeatData != null && materialHeatTemperature() > 0.0F;
    }

    public float materialHeatTemperature() {
        if (startingHeatLevel != HeatLevel.NONE && MobsToolForgingConfig.REQUIRE_HEAT_AT_JOB_START_ONLY.get() && !MobsToolForgingConfig.WORKPIECE_COOLS_MID_CRAFT.get()) {
            return materialHeatData == null ? startingHeatLevel.temperature() : Math.max(0.0F, Math.min(1.0F, materialHeatData.temperature()));
        }
        if (materialHeatData == null) {
            return 0.0F;
        }
        return level == null
                ? Math.max(0.0F, Math.min(1.0F, materialHeatData.temperature()))
                : materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
    }

    public boolean materialHeatWasWorkable() {
        return startingHeatLevel != HeatLevel.NONE || materialHeatData != null && materialHeatData.workable();
    }

    public String materialHeatStatusKey() {
        return WorkpieceHeat.statusKey(materialHeatTemperature(), materialHeatWasWorkable(), materialMinimumForgeTemperature());
    }

    public boolean canStartMetalWork(ItemStack stack, ToolMaterialDefinition material) {
        if (level == null || !workstationKind().isSmithingAnvilLike()) {
            return false;
        }
        HeatLevel heat = WorkshopHeat.heatForJob(level, worldPosition, stack, workstationKind(), material);
        return WorkshopHeat.canStartMetalWork(heat, WorkshopHeat.stackTemperature(level, stack), material, template());
    }

    public HeatLevel startingHeatLevel() {
        return startingHeatLevel;
    }

    public int completedQualityScore() {
        if (!MobsToolForgingConfig.ENABLE_QUALITY.get()) {
            return ForgingQuality.DEFAULT_SCORE;
        }
        ForgingQuality cap = workstationKind().maxQuality();
        if (workstationKind().isSmithingAnvilLike() && startingHeatLevel == HeatLevel.LOW && cap.ordinal() > ForgingQuality.WORKED.ordinal()) {
            cap = ForgingQuality.WORKED;
        }
        return ForgingQuality.clampScore(Math.min(qualityScore, cap.score()));
    }

    public ForgingQuality completedQuality() {
        return ForgingQuality.fromScore(completedQualityScore());
    }

    public boolean isTimingQualityWindow() {
        if (level == null || !MobsToolForgingConfig.ENABLE_QUALITY.get() || !MobsToolForgingConfig.ENABLE_TIMING_QUALITY.get() || nextGoodHitGameTime < 0L || isComplete()) {
            return false;
        }
        long delta = Math.abs(level.getGameTime() - nextGoodHitGameTime);
        return delta <= MobsToolForgingConfig.TIMING_QUALITY_WINDOW_TICKS.get();
    }

    private void captureStartingHeat(ItemStack stack, ToolMaterialDefinition material) {
        if (level == null || !workstationKind().isSmithingAnvilLike()) {
            startingHeatLevel = HeatLevel.NONE;
            return;
        }
        startingHeatLevel = WorkshopHeat.heatForJob(level, worldPosition, stack, workstationKind(), material);
        if (startingHeatLevel.atLeast(HeatLevel.HOT) && WorkshopHeat.stackTemperature(level, stack) <= 0.0F) {
            materialHeatData = refreshedHeatData(startingHeatLevel.temperature(), true);
        }
    }

    public float materialMinimumForgeTemperature() {
        ForgeTemplateDefinition template = template();
        ToolMaterialDefinition material = materialId == null ? null : MaterialCatalog.definition(materialId).orElse(null);
        if (material != null) {
            return WorkshopHeat.minimumForgeTemperature(material, template);
        }
        return template == null ? MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue() : template.minimumTemperature();
    }

    private void initializeQuality(ToolMaterialDefinition material) {
        if (!MobsToolForgingConfig.ENABLE_QUALITY.get()) {
            qualityScore = ForgingQuality.DEFAULT_SCORE;
            return;
        }
        int score = ForgingQuality.DEFAULT_SCORE + workstationKind().setupQualityBonus() + startingHeatLevel.qualityBonus() + materialDifficultyPenalty(material);
        if (workstationKind() == WorkstationKind.LAPIDARY_TABLE) {
            score += LapidaryAbrasives.qualityBonus(abrasiveStack);
        }
        qualityScore = ForgingQuality.clampScore(score);
    }

    private int materialDifficultyPenalty(ToolMaterialDefinition material) {
        if (MaterialCatalog.DIAMOND.equals(material.id())
                || MaterialCatalog.EMERALD.equals(material.id())
                || MaterialCatalog.RUBY.equals(material.id())
                || MaterialCatalog.SAPPHIRE.equals(material.id())) {
            return -8;
        }
        if (MaterialCatalog.IRON.equals(material.id())) {
            return -4;
        }
        return 0;
    }

    private void applyTimingQuality(boolean precisionTool) {
        if (level == null || !MobsToolForgingConfig.ENABLE_QUALITY.get() || !MobsToolForgingConfig.ENABLE_TIMING_QUALITY.get()) {
            return;
        }
        if (nextGoodHitGameTime < 0L) {
            scheduleNextGoodHit();
            return;
        }
        boolean goodTiming = Math.abs(level.getGameTime() - nextGoodHitGameTime) <= MobsToolForgingConfig.TIMING_QUALITY_WINDOW_TICKS.get();
        if (goodTiming) {
            qualityScore = ForgingQuality.clampScore(qualityScore + (precisionTool ? PRECISION_TIMED_HIT_QUALITY_BONUS : TIMED_HIT_QUALITY_BONUS));
            playTimingHitSound(precisionTool);
        } else {
            qualityScore = ForgingQuality.clampScore(qualityScore - (precisionTool ? 0 : MISSED_TIMING_QUALITY_PENALTY));
        }
        scheduleNextGoodHit();
    }

    private void scheduleFirstGoodHitIfReady() {
        if (nextGoodHitGameTime >= 0L
                || !MobsToolForgingConfig.ENABLE_QUALITY.get()
                || !MobsToolForgingConfig.ENABLE_TIMING_QUALITY.get()
                || !canHammer() && !hasLooseWork()) {
            return;
        }
        scheduleNextGoodHit();
    }

    private void playTimingHitSound(boolean precisionTool) {
        if (level == null) {
            return;
        }
        level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.65F, precisionTool ? 1.45F : 1.2F);
    }

    private void scheduleNextGoodHit() {
        if (level == null || !MobsToolForgingConfig.ENABLE_TIMING_QUALITY.get()) {
            nextGoodHitGameTime = -1L;
            return;
        }
        int baseDelay = workstationKind() == WorkstationKind.CRUDE_ANVIL ? 18 : 14;
        nextGoodHitGameTime = level.getGameTime() + baseDelay + level.random.nextInt(12);
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
        if (stack.isEmpty()) {
            return stack;
        }
        if (materialHeatData == null && startingHeatLevel == HeatLevel.NONE) {
            return stack;
        }
        if (level == null) {
            stack.set(ModDataComponents.HEATED_WORKPIECE.get(), materialHeatData == null ? refreshedHeatData(startingHeatLevel.temperature(), true) : materialHeatData);
            return stack;
        }
        boolean freezeActiveWorkpieceHeat = MobsToolForgingConfig.REQUIRE_HEAT_AT_JOB_START_ONLY.get() && !MobsToolForgingConfig.WORKPIECE_COOLS_MID_CRAFT.get();
        float temperature;
        if (materialHeatData == null) {
            temperature = startingHeatLevel.temperature();
        } else if (freezeActiveWorkpieceHeat) {
            temperature = Math.max(0.0F, Math.min(1.0F, materialHeatData.temperature()));
        } else {
            temperature = materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        }
        if (temperature > 0.0F) {
            stack.set(ModDataComponents.HEATED_WORKPIECE.get(), refreshedHeatData(temperature, materialHeatData == null || materialHeatData.workable()));
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
        if (sourceRackPos != null && sourceRackSlot >= 0) {
            tag.putLong(SOURCE_RACK_POS_TAG, sourceRackPos.asLong());
            tag.putInt(SOURCE_RACK_SLOT_TAG, sourceRackSlot);
        }
        if (!linkedPatternRacks.isEmpty()) {
            tag.putLongArray(LINKED_RACKS_TAG, linkedPatternRacks.stream().mapToLong(BlockPos::asLong).toArray());
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
        if (startingHeatLevel != HeatLevel.NONE) {
            tag.putString(STARTING_HEAT_LEVEL_TAG, startingHeatLevel.getSerializedName());
        }
        if (qualityScore != ForgingQuality.DEFAULT_SCORE) {
            tag.putInt(QUALITY_SCORE_TAG, qualityScore);
        }
        if (nextGoodHitGameTime >= 0L) {
            tag.putLong(NEXT_GOOD_HIT_GAME_TIME_TAG, nextGoodHitGameTime);
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
        sourceRackPos = tag.contains(SOURCE_RACK_POS_TAG) ? BlockPos.of(tag.getLong(SOURCE_RACK_POS_TAG)) : null;
        sourceRackSlot = tag.contains(SOURCE_RACK_SLOT_TAG) ? tag.getInt(SOURCE_RACK_SLOT_TAG) : -1;
        linkedPatternRacks.clear();
        if (tag.contains(LINKED_RACKS_TAG)) {
            for (long rackPos : tag.getLongArray(LINKED_RACKS_TAG)) {
                if (linkedPatternRacks.size() >= MAX_LINKED_PATTERN_RACKS) {
                    break;
                }
                BlockPos linkedPos = BlockPos.of(rackPos).immutable();
                if (!hasLinkedRack(linkedPos)) {
                    linkedPatternRacks.add(linkedPos);
                }
            }
        }
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
        startingHeatLevel = tag.contains(STARTING_HEAT_LEVEL_TAG) ? HeatLevel.parse(tag.getString(STARTING_HEAT_LEVEL_TAG), HeatLevel.NONE) : HeatLevel.NONE;
        qualityScore = tag.contains(QUALITY_SCORE_TAG) ? ForgingQuality.clampScore(tag.getInt(QUALITY_SCORE_TAG)) : ForgingQuality.DEFAULT_SCORE;
        nextGoodHitGameTime = tag.contains(NEXT_GOOD_HIT_GAME_TIME_TAG) ? tag.getLong(NEXT_GOOD_HIT_GAME_TIME_TAG) : -1L;
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

    public enum PatternRackLinkResult {
        LINKED,
        ALREADY_LINKED,
        FULL
    }
}

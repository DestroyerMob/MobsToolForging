package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;

public final class ToolmakerBenchAssembly {
    private ToolmakerBenchAssembly() {
    }

    public static boolean isPlaceable(ItemStack stack) {
        if (stack.isEmpty() || WorkpieceHeat.hasHeat(stack)) {
            return false;
        }
        if (stack.get(ModDataComponents.TOOL_CONSTRUCTION.get()) != null) {
            return true;
        }
        ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
        if (partData != null && ToolTypeRegistry.toolTypes().stream().anyMatch(definition -> isKnownAssemblyPart(definition, stack, partData))) {
            return true;
        }
        return stack.is(ModTags.Items.TOOL_HANDLES);
    }

    public static boolean canPlace(List<ItemStack> existingStacks, ItemStack stack) {
        if (isFinishedTool(stack)) {
            return existingStacks.isEmpty();
        }
        if (isPlantFiber(stack)) {
            return needsPlantFiber(existingStacks);
        }
        return !hasPlantFiber(existingStacks) && isPlaceable(stack);
    }

    public static boolean needsPlantFiber(List<ItemStack> stacks) {
        return plantFiberCount(stacks) == 0 && hasCompleteConstructionRequiringPlantFiber(stacks);
    }

    public static ItemStack assemble(List<ItemStack> stacks, HolderLookup.Provider registries) {
        int plantFiberCount = plantFiberCount(stacks);
        if (plantFiberCount > 1) {
            return ItemStack.EMPTY;
        }
        List<ItemStack> assemblyStacks = assemblyStacks(stacks);
        if (assemblyStacks.isEmpty() || assemblyStacks.stream().anyMatch(ToolmakerBenchAssembly::isFinishedTool)) {
            return ItemStack.EMPTY;
        }
        for (ToolTypeDefinition definition : ToolTypeRegistry.toolTypes()) {
            Parts parts = findParts(stacks, definition);
            Optional<ToolConstructionData> construction = parts.validConstruction(definition);
            if (construction.isEmpty()) {
                continue;
            }
            if ((plantFiberCount == 1) != requiresPlantFiber(construction.get())) {
                continue;
            }
            ItemStack output = definition.createTool(construction.get());
            if (output.isEmpty()) {
                continue;
            }
            ToolPartWear.applyStoredWear(output, parts.part());
            if (ToolAssemblyEnchantments.mergeOnto(output, parts.enchantmentSources(), registries)) {
                ToolExternalComponents.copyPrimaryHeadComponentsToTool(parts.part(), output);
                output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(assemblyStacks));
                ToolAssemblyEnchantments.syncRoutedToolEnchantments(output, registries);
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public static Optional<List<ItemStack>> disassemble(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null) {
            return Optional.empty();
        }
        ToolTypeDefinition definition = ToolTypeRegistry.toolType(construction.toolType()).orElse(null);
        if (definition == null) {
            return Optional.empty();
        }
        ToolAssemblyParts storedParts = stack.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (storedParts != null && !storedParts.stacks().isEmpty()) {
            List<ItemStack> parts = ToolPartWear.copyWithWearFromTool(definition, stack, storedParts.copyStacks());
            if (!ToolAssemblyEnchantments.hasEnchantments(parts)) {
                parts = ToolAssemblyEnchantments.copyToolEnchantmentsToViableParts(stack, parts);
            }
            parts = normalizedDisassemblyParts(definition, construction, parts);
            return Optional.of(ToolExternalComponents.copyToolComponentsToPrimaryHead(definition, stack, parts));
        }

        List<ItemStack> parts = new ArrayList<>();
        ItemStack primary = definition.createPart(definition.primaryPartType(), construction.headMaterial(), construction.quality());
        if (primary.isEmpty()) {
            return Optional.empty();
        }
        primary = copyWithConstructionTreatment(primary, construction);
        parts.add(primary);

        ResourceLocation requiredPartMaterial = construction.guardMaterial().orElse(construction.headMaterial());
        for (String partType : definition.requiredAssemblyParts()) {
            ItemStack part = definition.createPart(partType, requiredPartMaterial, construction.quality());
            if (part.isEmpty()) {
                return Optional.empty();
            }
            parts.add(part);
        }

        parts.add(handleStack(construction.handleMaterial()));
        List<ItemStack> result = parts.stream().filter(item -> !item.isEmpty()).map(ItemStack::copy).toList();
        List<ItemStack> wornParts = ToolPartWear.copyWithWearFromTool(definition, stack, result);
        wornParts = ToolAssemblyEnchantments.copyToolEnchantmentsToViableParts(stack, wornParts);
        return Optional.of(ToolExternalComponents.copyToolComponentsToPrimaryHead(definition, stack, wornParts));
    }

    public static boolean isFinishedTool(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return construction != null && ToolTypeRegistry.toolType(construction.toolType()).isPresent();
    }

    private static Parts findParts(List<ItemStack> stacks, ToolTypeDefinition definition) {
        ItemStack part = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        Map<String, ItemStack> requiredParts = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (isPlantFiber(stack)) {
                continue;
            }
            if (WorkpieceHeat.hasHeat(stack)) {
                return Parts.invalid();
            }
            ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
            if (matchesPart(definition, stack, partData, definition.primaryPartType())) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty() || !part.isEmpty()) {
                    return Parts.invalid();
                }
                part = stack;
                continue;
            }
            String requiredPart = matchingRequiredPart(definition, stack, partData).orElse(null);
            if (requiredPart != null) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty() || requiredParts.containsKey(requiredPart)) {
                    return Parts.invalid();
                }
                requiredParts.put(requiredPart, stack);
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_HANDLES)) {
                if (!handle.isEmpty()) {
                    return Parts.invalid();
                }
                handle = stack;
                continue;
            }
            return Parts.invalid();
        }
        return new Parts(false, part, handle, requiredParts);
    }

    private static boolean isKnownAssemblyPart(ToolTypeDefinition definition, ItemStack stack, ToolPartData partData) {
        return matchesPart(definition, stack, partData, definition.primaryPartType())
                || matchingRequiredPart(definition, stack, partData).isPresent();
    }

    private static boolean matchesPart(ToolTypeDefinition definition, ItemStack stack, ToolPartData partData, String partType) {
        return partData != null
                && partType.equals(partData.partType())
                && definition.matchesPartItem(partType, partData.materialId(), stack);
    }

    private static Optional<String> matchingRequiredPart(ToolTypeDefinition definition, ItemStack stack, ToolPartData partData) {
        return definition.requiredAssemblyParts().stream()
                .filter(partType -> matchesPart(definition, stack, partData, partType))
                .findFirst();
    }

    private static boolean hasCompleteConstructionRequiringPlantFiber(List<ItemStack> stacks) {
        if (stacks.isEmpty() || stacks.stream().anyMatch(ToolmakerBenchAssembly::isFinishedTool)) {
            return false;
        }
        for (ToolTypeDefinition definition : ToolTypeRegistry.toolTypes()) {
            Optional<ToolConstructionData> construction = findParts(stacks, definition).validConstruction(definition);
            if (construction.filter(ToolmakerBenchAssembly::requiresPlantFiber).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static boolean requiresPlantFiber(ToolConstructionData construction) {
        return MaterialCatalog.FLINT.equals(construction.headMaterial());
    }

    private static List<ItemStack> assemblyStacks(List<ItemStack> stacks) {
        return stacks.stream()
                .filter(stack -> !isPlantFiber(stack))
                .map(ItemStack::copy)
                .toList();
    }

    private static boolean hasPlantFiber(List<ItemStack> stacks) {
        return stacks.stream().anyMatch(ToolmakerBenchAssembly::isPlantFiber);
    }

    private static int plantFiberCount(List<ItemStack> stacks) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (isPlantFiber(stack)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isPlantFiber(ItemStack stack) {
        return stack.is(ModItems.PLANT_FIBER.get());
    }

    private static ItemStack handleStack(ResourceLocation material) {
        if (MaterialCatalog.BLAZE.equals(material)) {
            return new ItemStack(Items.BLAZE_ROD);
        }
        if (MaterialCatalog.BREEZE.equals(material)) {
            return new ItemStack(Items.BREEZE_ROD);
        }
        return new ItemStack(Items.STICK);
    }

    private static List<ItemStack> normalizedDisassemblyParts(ToolTypeDefinition definition, ToolConstructionData construction, List<ItemStack> stacks) {
        List<ItemStack> parts = new ArrayList<>();
        boolean hasPrimary = false;
        boolean hasHandle = false;
        Map<String, Boolean> requiredSeen = new LinkedHashMap<>();
        for (String partType : definition.requiredAssemblyParts()) {
            requiredSeen.put(partType, false);
        }

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
            if (!hasPrimary && matchesPart(definition, stack, data, definition.primaryPartType())) {
                parts.add(copyWithConstructionTreatment(stack, construction));
                hasPrimary = true;
                continue;
            }
            Optional<String> requiredPart = matchingRequiredPart(definition, stack, data)
                    .filter(partType -> !requiredSeen.getOrDefault(partType, true));
            if (requiredPart.isPresent()) {
                requiredSeen.put(requiredPart.get(), true);
                parts.add(stack.copyWithCount(1));
                continue;
            }
            if (!hasHandle && stack.is(ModTags.Items.TOOL_HANDLES)) {
                parts.add(stack.copyWithCount(1));
                hasHandle = true;
            }
        }
        return List.copyOf(parts);
    }

    private static ItemStack copyWithConstructionTreatment(ItemStack stack, ToolConstructionData construction) {
        ItemStack copy = stack.copyWithCount(1);
        ToolPartData data = copy.get(ModDataComponents.TOOL_PART.get());
        if (data != null && data.treatment().isEmpty() && construction.treatment().isPresent()) {
            copy.set(ModDataComponents.TOOL_PART.get(), data.withTreatment(construction.treatment().get()));
        }
        return copy;
    }

    private record Parts(
            boolean failed,
            ItemStack part,
            ItemStack handle,
            Map<String, ItemStack> requiredParts
    ) {
        private static Parts invalid() {
            return new Parts(true, ItemStack.EMPTY, ItemStack.EMPTY, Map.of());
        }

        private Optional<ToolConstructionData> validConstruction(ToolTypeDefinition definition) {
            if (failed) {
                return Optional.empty();
            }
            Optional<ToolConstructionData> construction = construction(definition);
            return construction.filter(data -> definition.canAssemble(data, partDataByType()));
        }

        private Optional<ToolConstructionData> construction(ToolTypeDefinition definition) {
            ToolPartData partData = part.get(ModDataComponents.TOOL_PART.get());
            if (partData == null || handle.isEmpty() || !requiredParts.keySet().containsAll(definition.requiredAssemblyParts())) {
                return Optional.empty();
            }
            return Optional.of(new ToolConstructionData(
                    definition.id(),
                    partData.materialId(),
                    MaterialCatalog.handleMaterial(handle),
                    guardMaterial(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    partData.treatment(),
                    quality(definition)
            ));
        }

        private Map<String, ToolPartData> partDataByType() {
            Map<String, ToolPartData> values = new LinkedHashMap<>();
            ToolPartData primary = part.get(ModDataComponents.TOOL_PART.get());
            if (primary != null) {
                values.put(primary.partType(), primary);
            }
            requiredParts.forEach((partType, stack) -> {
                ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
                if (data != null) {
                    values.put(partType, data);
                }
            });
            return values;
        }

        private Optional<ResourceLocation> guardMaterial() {
            return requiredParts.values().stream().findFirst().map(MaterialCatalog::bindingMaterial);
        }

        private int quality(ToolTypeDefinition definition) {
            if (!MobsToolForgingConfig.ENABLE_QUALITY.get()) {
                return ToolConstructionData.DEFAULT_QUALITY;
            }
            ToolPartData primary = part.get(ModDataComponents.TOOL_PART.get());
            List<ToolPartData> requiredData = requiredParts.values().stream()
                    .map(stack -> stack.get(ModDataComponents.TOOL_PART.get()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            return definition.assembledQuality(primary, requiredData);
        }

        private List<ItemStack> enchantmentSources() {
            List<ItemStack> sources = new ArrayList<>(ToolAssemblyEnchantments.copySources(part, requiredParts.values()));
            if (!handle.isEmpty()) {
                sources.add(handle);
            }
            return List.copyOf(sources);
        }

    }
}

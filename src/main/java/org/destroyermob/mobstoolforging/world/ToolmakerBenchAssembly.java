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
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
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
        return stack.is(ModTags.Items.TOOL_HANDLES)
                || stack.is(ModTags.Items.TOOL_BINDINGS)
                || stack.is(ModTags.Items.TOOL_WRAPS)
                || stack.is(ModTags.Items.TOOL_FOCI)
                || stack.is(ModTags.Items.TREATMENT_CATALYSTS);
    }

    public static ItemStack assemble(List<ItemStack> stacks, HolderLookup.Provider registries) {
        if (stacks.isEmpty() || stacks.stream().anyMatch(ToolmakerBenchAssembly::isFinishedTool)) {
            return ItemStack.EMPTY;
        }
        for (ToolTypeDefinition definition : ToolTypeRegistry.toolTypes()) {
            Parts parts = findParts(stacks, definition);
            Optional<ToolConstructionData> construction = parts.validConstruction(definition);
            if (construction.isEmpty()) {
                continue;
            }
            ItemStack output = definition.createTool(construction.get());
            if (output.isEmpty()) {
                continue;
            }
            ToolPartWear.applyStoredWear(output, parts.part());
            if (ToolAssemblyEnchantments.mergeOnto(output, parts.enchantmentSources(), registries)) {
                output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(stacks));
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
            return Optional.of(ToolPartWear.copyWithWearFromTool(definition, stack, storedParts.copyStacks()));
        }

        List<ItemStack> parts = new ArrayList<>();
        ItemStack primary = definition.createPart(definition.primaryPartType(), construction.headMaterial(), construction.quality());
        if (primary.isEmpty()) {
            return Optional.empty();
        }
        if (construction.treatment().filter(MaterialCatalog.NETHERITE::equals).isPresent()) {
            ToolPartData primaryData = primary.get(ModDataComponents.TOOL_PART.get());
            if (primaryData != null) {
                primary.set(ModDataComponents.TOOL_PART.get(), primaryData.withTreatment(MaterialCatalog.NETHERITE));
            }
        }
        parts.add(primary);

        ResourceLocation requiredPartMaterial = construction.bindingMaterial().orElse(construction.headMaterial());
        for (String partType : definition.requiredAssemblyParts()) {
            ItemStack part = definition.createPart(partType, requiredPartMaterial, construction.quality());
            if (part.isEmpty()) {
                return Optional.empty();
            }
            parts.add(part);
        }

        parts.add(handleStack(construction.handleMaterial()));
        if (definition.requiredAssemblyParts().isEmpty()) {
            construction.bindingMaterial().map(MaterialCatalog::displayStack).ifPresent(parts::add);
        }
        construction.wrapMaterial().map(ToolmakerBenchAssembly::wrapStack).ifPresent(parts::add);
        construction.focusMaterial().map(ToolmakerBenchAssembly::focusStack).ifPresent(parts::add);
        construction.treatment()
                .filter(treatment -> !MaterialCatalog.NETHERITE.equals(treatment))
                .map(ToolmakerBenchAssembly::treatmentStack)
                .ifPresent(parts::add);
        List<ItemStack> result = parts.stream().filter(item -> !item.isEmpty()).map(ItemStack::copy).toList();
        return Optional.of(ToolPartWear.copyWithWearFromTool(definition, stack, result));
    }

    public static boolean isFinishedTool(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return construction != null && ToolTypeRegistry.toolType(construction.toolType()).isPresent();
    }

    private static Parts findParts(List<ItemStack> stacks, ToolTypeDefinition definition) {
        ItemStack part = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        ItemStack binding = ItemStack.EMPTY;
        ItemStack wrap = ItemStack.EMPTY;
        ItemStack focus = ItemStack.EMPTY;
        ItemStack treatment = ItemStack.EMPTY;
        Map<String, ItemStack> requiredParts = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
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
            if (stack.is(ModTags.Items.TOOL_BINDINGS)) {
                if (!definition.requiredAssemblyParts().isEmpty() || !binding.isEmpty()) {
                    return Parts.invalid();
                }
                binding = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_WRAPS)) {
                if (!wrap.isEmpty()) {
                    return Parts.invalid();
                }
                wrap = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_FOCI)) {
                if (!focus.isEmpty()) {
                    return Parts.invalid();
                }
                focus = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TREATMENT_CATALYSTS)) {
                if (!treatment.isEmpty()) {
                    return Parts.invalid();
                }
                treatment = stack;
                continue;
            }
            return Parts.invalid();
        }
        return new Parts(false, part, handle, binding, requiredParts, wrap, focus, treatment);
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

    private static Optional<ResourceLocation> material(ItemStack stack, MaterialResolver resolver) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(resolver.resolve(stack));
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

    private static ItemStack wrapStack(ResourceLocation material) {
        if (MaterialCatalog.LEATHER.equals(material)) {
            return new ItemStack(Items.LEATHER);
        }
        return MaterialCatalog.displayStack(material);
    }

    private static ItemStack focusStack(ResourceLocation material) {
        if (MaterialCatalog.AMETHYST.equals(material)) {
            return new ItemStack(Items.AMETHYST_SHARD);
        }
        return MaterialCatalog.displayStack(material);
    }

    private static ItemStack treatmentStack(ResourceLocation material) {
        if (MaterialCatalog.NETHER.equals(material)) {
            return new ItemStack(Items.NETHERITE_SCRAP);
        }
        if (MaterialCatalog.SCULK.equals(material)) {
            return new ItemStack(Items.SCULK_CATALYST);
        }
        return MaterialCatalog.displayStack(material);
    }

    @FunctionalInterface
    private interface MaterialResolver {
        ResourceLocation resolve(ItemStack stack);
    }

    private record Parts(
            boolean failed,
            ItemStack part,
            ItemStack handle,
            ItemStack binding,
            Map<String, ItemStack> requiredParts,
            ItemStack wrap,
            ItemStack focus,
            ItemStack treatment
    ) {
        private static Parts invalid() {
            return new Parts(true, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, Map.of(), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
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
                    bindingMaterial(),
                    material(wrap, MaterialCatalog::wrapMaterial),
                    material(focus, MaterialCatalog::focusMaterial),
                    partData.treatment().or(() -> material(treatment, MaterialCatalog::treatmentMaterial)),
                    quality()
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

        private Optional<ResourceLocation> bindingMaterial() {
            if (!requiredParts.isEmpty()) {
                return requiredParts.values().stream().findFirst().map(MaterialCatalog::bindingMaterial);
            }
            return binding.isEmpty() ? Optional.empty() : Optional.of(MaterialCatalog.bindingMaterial(binding));
        }

        private int quality() {
            int total = partQuality(part);
            int count = 1;
            for (ItemStack requiredPart : requiredParts.values()) {
                total += partQuality(requiredPart);
                count++;
            }
            return Math.round(total / (float) count);
        }

        private List<ItemStack> enchantmentSources() {
            List<ItemStack> sources = new ArrayList<>(ToolAssemblyEnchantments.copySources(part, requiredParts.values()));
            if (!handle.isEmpty()) {
                sources.add(handle);
            }
            return List.copyOf(sources);
        }

        private static int partQuality(ItemStack stack) {
            ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
            return data == null ? ToolPartData.DEFAULT_QUALITY : data.quality();
        }
    }
}

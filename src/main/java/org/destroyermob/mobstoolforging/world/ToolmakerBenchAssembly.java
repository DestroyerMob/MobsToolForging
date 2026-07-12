package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ModularArmorPartItem;
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
        if (stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null) {
            return true;
        }
        ArmorPartData armorPart = stack.get(ModDataComponents.ARMOR_PART.get());
        if (armorPart != null && ArmorAssemblyKind.isKnownPart(stack, armorPart)) {
            return true;
        }
        ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
        if (CrossbowAssembly.isCrossbowAssemblyComponent(stack)) {
            return true;
        }
        if (SmithingHammerAssembly.isHeadIngredient(stack)) {
            return true;
        }
        if (partData != null && ToolTypeRegistry.toolTypes().stream().anyMatch(definition -> isKnownAssemblyPart(definition, stack, partData))) {
            return true;
        }
        return stack.is(ModTags.Items.TOOL_HANDLES);
    }

    public static boolean canPlace(List<ItemStack> existingStacks, ItemStack stack) {
        if (isFinishedTool(stack)) {
            return existingStacks.isEmpty();
        }
        return isPlaceable(stack);
    }

    public static ItemStack assemble(List<ItemStack> stacks, HolderLookup.Provider registries) {
        if (stacks.isEmpty() || stacks.stream().anyMatch(ToolmakerBenchAssembly::isFinishedTool)) {
            return ItemStack.EMPTY;
        }
        ItemStack hammer = SmithingHammerAssembly.assemble(stacks);
        if (!hammer.isEmpty()) {
            return hammer;
        }
        ItemStack crossbow = CrossbowAssembly.assemble(stacks);
        if (!crossbow.isEmpty()) {
            return crossbow;
        }
        ItemStack armor = assembleArmor(stacks, registries);
        if (!armor.isEmpty()) {
            return armor;
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
                ToolExternalComponents.copyPrimaryHeadComponentsToTool(parts.part(), output);
                output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(stacks));
                CompositeAffixCompatibility.syncCompatibilityMirror(output);
                ToolAssemblyEnchantments.syncRoutedToolEnchantments(output, registries);
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public static Optional<List<ItemStack>> disassemble(ItemStack stack) {
        Optional<List<ItemStack>> hammerParts = SmithingHammerAssembly.disassemble(stack);
        if (hammerParts.isPresent()) {
            return hammerParts;
        }
        Optional<List<ItemStack>> crossbowParts = CrossbowAssembly.disassemble(stack);
        if (crossbowParts.isPresent()) {
            return crossbowParts;
        }
        ArmorConstructionData armorConstruction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (armorConstruction != null) {
            return disassembleArmor(stack, armorConstruction);
        }

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
            return Optional.of(ToolExternalComponents.copyToolComponentsToPrimaryHeadWithoutAffixes(definition, stack, parts));
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
        if (SmithingHammerAssembly.isHammer(stack)) {
            return true;
        }
        if (CrossbowAssembly.isCrossbow(stack.get(ModDataComponents.TOOL_CONSTRUCTION.get()))) {
            return true;
        }
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction != null && ToolTypeRegistry.toolType(construction.toolType()).isPresent()) {
            return true;
        }
        ArmorConstructionData armorConstruction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        return armorConstruction != null && ArmorAssemblyKind.fromArmorType(armorConstruction.armorType()).isPresent();
    }

    private static ItemStack assembleArmor(List<ItemStack> stacks, HolderLookup.Provider registries) {
        ArmorAssemblyParts parts = findArmorParts(stacks);
        if (!parts.valid()) {
            return ItemStack.EMPTY;
        }
        ItemStack output = parts.kind().create(parts);
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!ToolAssemblyEnchantments.mergeOnto(output, parts.enchantmentSources(), registries)) {
            return ItemStack.EMPTY;
        }
        ArmorExternalComponents.copyArmorPartComponentsToArmor(parts.base(), parts.plate(), output);
        output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(parts.stacks()));
        CompositeAffixCompatibility.syncCompatibilityMirror(output);
        ToolAssemblyEnchantments.syncRoutedToolEnchantments(output, registries);
        return output;
    }

    private static ArmorAssemblyParts findArmorParts(List<ItemStack> stacks) {
        ArmorAssemblyParts parts = new ArmorAssemblyParts();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (WorkpieceHeat.hasHeat(stack)) {
                return ArmorAssemblyParts.invalid();
            }
            ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
            if (data == null || !ArmorStatsCatalog.isSupportedArmorMaterial(data.materialId())) {
                return ArmorAssemblyParts.invalid();
            }
            Optional<ArmorAssemblyKind> kind = ArmorAssemblyKind.fromPart(stack, data);
            if (kind.isEmpty() || !kind.get().accept(parts, stack, data)) {
                return ArmorAssemblyParts.invalid();
            }
        }
        return parts;
    }

    private static Optional<List<ItemStack>> disassembleArmor(ItemStack armor, ArmorConstructionData construction) {
        ToolAssemblyParts storedParts = armor.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (storedParts != null) {
            List<ItemStack> parts = storedParts.copyStacks();
            if (!parts.isEmpty()) {
                return Optional.of(ArmorExternalComponents.copyArmorComponentsToPrimaryPartWithoutAffixes(armor, parts));
            }
        }
        return ArmorAssemblyKind.fromArmorType(construction.armorType()).map(kind -> {
            List<ItemStack> parts = new ArrayList<>();
            ItemStack base = kind.createBasePart(construction.chainmailMaterial(), construction.quality());
            if (!base.isEmpty()) {
                parts.add(base);
            }
            construction.overlayMaterial().ifPresent(material -> {
                ItemStack plate = kind.createPlatePart(material, construction.overlayBaseMaterial(), construction.quality());
                if (!plate.isEmpty()) {
                    parts.add(plate);
                }
            });
            List<ItemStack> result = List.copyOf(parts);
            if (!ToolAssemblyEnchantments.hasEnchantments(result)) {
                result = ToolAssemblyEnchantments.copyToolEnchantmentsToViableParts(armor, result);
            }
            return ArmorExternalComponents.copyArmorComponentsToPrimaryPart(armor, result);
        }).filter(parts -> !parts.isEmpty());
    }

    private static Parts findParts(List<ItemStack> stacks, ToolTypeDefinition definition) {
        ItemStack part = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
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
            data = data.withTreatment(construction.treatment().get());
        }
        if (data != null && data.coatingBaseMaterial().isEmpty() && construction.headBaseMaterial().isPresent()) {
            data = data.withCoatingBaseMaterial(construction.headBaseMaterial().get());
        }
        if (data != null) {
            copy.set(ModDataComponents.TOOL_PART.get(), data);
        }
        return copy;
    }

    private enum ArmorAssemblyKind {
        HELMET(
                ArmorConstructionData.HELMET_TYPE,
                ArmorPartData.HELMET_CHAINMAIL,
                ModItems.HELMET_CHAINMAIL::get,
                ArmorPartData.HELMET_PLATE,
                ModItems.HELMET_PLATE::get
        ) {
            @Override
            ItemStack create(ArmorAssemblyParts parts) {
                return ModItems.MODULAR_HELMET.get().create(
                        requiredMaterial(parts.base),
                        optionalMaterial(parts.plate),
                        quality(parts)
                );
            }
        },
        CHESTPLATE(
                ArmorConstructionData.CHESTPLATE_TYPE,
                ArmorPartData.CHESTPLATE_CHAINMAIL,
                ModItems.CHESTPLATE_CHAINMAIL::get,
                ArmorPartData.CHESTPLATE_BODY,
                ModItems.CHESTPLATE_BODY::get
        ) {
            @Override
            ItemStack create(ArmorAssemblyParts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_CHESTPLATE.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_CHESTPLATE.get().createBase(requiredMaterial(parts.base), quality(parts)));
            }
        },
        LEGGINGS(
                ArmorConstructionData.LEGGINGS_TYPE,
                ArmorPartData.LEGGINGS_CHAINMAIL,
                ModItems.LEGGINGS_CHAINMAIL::get,
                ArmorPartData.LEGGINGS_PLATE,
                ModItems.LEGGINGS_PLATE::get
        ) {
            @Override
            ItemStack create(ArmorAssemblyParts parts) {
                return ModItems.MODULAR_LEGGINGS.get().create(
                        requiredMaterial(parts.base),
                        optionalMaterial(parts.plate),
                        quality(parts)
                );
            }
        },
        BOOTS(
                ArmorConstructionData.BOOTS_TYPE,
                ArmorPartData.BOOTS_CHAINMAIL,
                ModItems.BOOTS_CHAINMAIL::get,
                ArmorPartData.BOOTS_PLATE,
                ModItems.BOOTS_PLATE::get
        ) {
            @Override
            ItemStack create(ArmorAssemblyParts parts) {
                return ModItems.MODULAR_BOOTS.get().create(
                        requiredMaterial(parts.base),
                        optionalMaterial(parts.plate),
                        quality(parts)
                );
            }
        };

        private final ResourceLocation armorType;
        private final String basePartType;
        private final Supplier<ModularArmorPartItem> baseItem;
        private final String platePartType;
        private final Supplier<ModularArmorPartItem> plateItem;

        ArmorAssemblyKind(ResourceLocation armorType, String basePartType, Supplier<ModularArmorPartItem> baseItem, String platePartType, Supplier<ModularArmorPartItem> plateItem) {
            this.armorType = armorType;
            this.basePartType = basePartType;
            this.baseItem = baseItem;
            this.platePartType = platePartType;
            this.plateItem = plateItem;
        }

        private boolean accept(ArmorAssemblyParts parts, ItemStack stack, ArmorPartData data) {
            if (matchesBase(stack, data)) {
                return parts.setKind(this) && parts.setBase(stack);
            }
            if (matchesPlate(stack, data)) {
                return parts.setKind(this) && parts.setPlate(stack);
            }
            return false;
        }

        private boolean matchesBase(ItemStack stack, ArmorPartData data) {
            return matches(stack, data, basePartType, baseItem) && MaterialCatalog.IRON.equals(data.materialId());
        }

        private boolean matchesPlate(ItemStack stack, ArmorPartData data) {
            return matches(stack, data, platePartType, plateItem);
        }

        private ItemStack createBasePart(ResourceLocation material, int quality) {
            return baseItem.get().createPart(material, quality);
        }

        private ItemStack createPlatePart(ResourceLocation material, int quality) {
            return plateItem.get().createPart(material, quality);
        }

        private ItemStack createPlatePart(ResourceLocation material, Optional<ResourceLocation> baseMaterial, int quality) {
            ItemStack stack = createPlatePart(material, quality);
            ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
            if (data != null && baseMaterial.isPresent()) {
                stack.set(ModDataComponents.ARMOR_PART.get(), data.withCoatingBaseMaterial(baseMaterial.get()));
            }
            return stack;
        }

        abstract ItemStack create(ArmorAssemblyParts parts);

        private static Optional<ArmorAssemblyKind> fromPart(ItemStack stack, ArmorPartData data) {
            for (ArmorAssemblyKind kind : values()) {
                if (kind.matchesBase(stack, data) || kind.matchesPlate(stack, data)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }

        private static Optional<ArmorAssemblyKind> fromArmorType(ResourceLocation armorType) {
            for (ArmorAssemblyKind kind : values()) {
                if (kind.armorType.equals(armorType)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }

        private static boolean isKnownPart(ItemStack stack, ArmorPartData data) {
            return fromPart(stack, data).isPresent();
        }

        private static boolean matches(ItemStack stack, ArmorPartData data, String partType, Supplier<ModularArmorPartItem> item) {
            return stack.is(item.get()) && partType.equals(data.partType());
        }

        private static Optional<ResourceLocation> optionalMaterial(ItemStack stack) {
            ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
            return data == null ? Optional.empty() : Optional.of(data.materialId());
        }

        private static ResourceLocation requiredMaterial(ItemStack stack) {
            return optionalMaterial(stack).orElse(MaterialCatalog.IRON);
        }

        private static int quality(ArmorAssemblyParts parts) {
            if (!MobsToolForgingConfig.ENABLE_QUALITY.get()) {
                return ArmorPartData.DEFAULT_QUALITY;
            }
            List<ArmorPartData> partData = parts.stacks().stream()
                    .map(stack -> stack.get(ModDataComponents.ARMOR_PART.get()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (partData.isEmpty()) {
                return ArmorPartData.DEFAULT_QUALITY;
            }
            float total = 0.0F;
            for (ArmorPartData data : partData) {
                total += data.quality();
            }
            return ForgingQuality.clampScore(Math.round(total / partData.size()));
        }
    }

    private static final class ArmorAssemblyParts {
        private ArmorAssemblyKind kind;
        private ItemStack base = ItemStack.EMPTY;
        private ItemStack plate = ItemStack.EMPTY;
        private boolean invalid;

        private static ArmorAssemblyParts invalid() {
            ArmorAssemblyParts parts = new ArmorAssemblyParts();
            parts.invalid = true;
            return parts;
        }

        private boolean valid() {
            ArmorPartData baseData = base.get(ModDataComponents.ARMOR_PART.get());
            return !invalid
                    && kind != null
                    && baseData != null
                    && (plate.isEmpty() || !MaterialCatalog.LEATHER.equals(baseData.materialId()));
        }

        private ArmorAssemblyKind kind() {
            return kind;
        }

        private ItemStack base() {
            return base;
        }

        private ItemStack plate() {
            return plate;
        }

        private boolean setKind(ArmorAssemblyKind nextKind) {
            if (kind != null && kind != nextKind) {
                return false;
            }
            kind = nextKind;
            return true;
        }

        private boolean setBase(ItemStack stack) {
            if (!base.isEmpty()) {
                return false;
            }
            base = stack;
            return true;
        }

        private boolean setPlate(ItemStack stack) {
            if (!plate.isEmpty()) {
                return false;
            }
            plate = stack;
            return true;
        }

        private List<ItemStack> stacks() {
            List<ItemStack> stacks = new ArrayList<>();
            addIfPresent(stacks, base);
            addIfPresent(stacks, plate);
            return stacks;
        }

        private List<ItemStack> enchantmentSources() {
            return stacks();
        }

        private static void addIfPresent(List<ItemStack> stacks, ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
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
                    partData.coatingBaseMaterial(),
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

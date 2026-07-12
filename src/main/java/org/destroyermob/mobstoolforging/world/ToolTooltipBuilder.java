package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolTooltipBuilder {
    private static final int NORMAL_TRAIT_LIMIT = 4;

    private ToolTooltipBuilder() {
    }

    public static List<Component> tooltip(ItemStack stack, ToolKind toolKind, TooltipFlag flag) {
        return ToolTypeRegistry.toolType(toolKind)
                .map(definition -> tooltip(stack, definition, flag))
                .orElseGet(List::of);
    }

    public static List<Component> tooltip(ItemStack stack, ToolTypeDefinition definition, TooltipFlag flag) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null) {
            return List.of();
        }
        ToolStatProfile profile = ToolStatBuilder.profileForTooltip(stack, definition, construction);
        List<Component> lines = new ArrayList<>();
        lines.add(qualityLine(construction.qualityLevel()));
        construction.treatment().ifPresent(treatment -> lines.add(treatmentLine(treatment)));

        if (flag.hasShiftDown()) {
            addConstructionTooltip(lines, stack, definition, construction);
            addDetailedTraits(lines, profile);
            addEnchantmentHeading(lines, stack);
        } else {
            addNormalTooltip(lines, profile);
        }

        if (flag.isAdvanced()) {
            addBlankIfNeeded(lines);
            addAdvancedTooltip(lines, construction, profile);
        }

        return lines;
    }

    private static Component qualityLine(ForgingQuality quality) {
        return Component.translatable("tooltip.mobstoolforging.quality")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(quality.displayName());
    }

    private static Component treatmentLine(ResourceLocation treatment) {
        return Component.translatable("tooltip.mobstoolforging.part_treatment", MaterialCatalog.displayName(treatment))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private static void addNormalTooltip(List<Component> lines, ToolStatProfile profile) {
        List<ResourceLocation> displayTraits = displayTraits(profile);
        if (displayTraits.isEmpty()) {
            return;
        }
        MutableComponent traitLine = Component.translatable("tooltip.mobstoolforging.traits")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
        int shown = Math.min(displayTraits.size(), NORMAL_TRAIT_LIMIT);
        for (int index = 0; index < shown; index++) {
            if (index > 0) {
                traitLine.append(Component.literal(" \u2022 ").withStyle(ChatFormatting.DARK_GRAY));
            }
            traitLine.append(traitNameWithLevel(profile, displayTraits.get(index)));
        }
        int hidden = displayTraits.size() - shown;
        if (hidden > 0) {
            traitLine.append(Component.literal(" +" + hidden).withStyle(ChatFormatting.DARK_GRAY));
        }
        lines.add(traitLine);
    }

    private static void addConstructionTooltip(List<Component> lines, ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
        ToolMaterialSummary materials = ToolMaterialSummary.from(stack, definition, construction);
        lines.add(Component.translatable("tooltip.mobstoolforging.construction").withStyle(ChatFormatting.GRAY));
        lines.add(partLine("tooltip.mobstoolforging.part.head", materials.headMaterials()));
        if (!materials.coreMaterials().isEmpty()) {
            lines.add(partLine("tooltip.mobstoolforging.part.core", materials.coreMaterials()));
        }
        lines.add(partLine("tooltip.mobstoolforging.part.handle", Optional.of(construction.handleMaterial())));
        if (!definition.requiredAssemblyParts().isEmpty() && !definition.averageRequiredHeadDurability()) {
            lines.add(partLine("tooltip.mobstoolforging.part.guard", materials.guardMaterials(), construction.guardMaterial()));
        }
    }

    private static void addDetailedTraits(List<Component> lines, ToolStatProfile profile) {
        List<ResourceLocation> displayTraits = displayTraits(profile);
        if (!displayTraits.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.mobstoolforging.traits").withStyle(ChatFormatting.GRAY));
            for (ResourceLocation traitId : displayTraits) {
                lines.add(traitDescriptionLine(profile, traitId));
            }
        }
    }

    private static void addEnchantmentHeading(List<Component> lines, ItemStack stack) {
        if (stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
            return;
        }
        addBlankIfNeeded(lines);
        lines.add(Component.translatable("tooltip.mobstoolforging.enchantments").withStyle(ChatFormatting.GRAY));
    }

    private static void addAdvancedTooltip(List<Component> lines, ToolConstructionData construction, ToolStatProfile profile) {
        lines.add(Component.translatable("tooltip.mobstoolforging.stat_profile").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(rawLine("tooltip.mobstoolforging.stat.max_damage", Integer.toString(profile.maxDamage())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.durability_multiplier", decimal(profile.durabilityMultiplier())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.mining_speed_multiplier", decimal(profile.miningSpeedMultiplier())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.attack_damage_bonus", decimal(profile.attackDamageBonus())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.attack_speed_bonus", decimal(profile.attackSpeedBonus())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.fire_resistant", Boolean.toString(profile.fireResistant())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.enchant_affinities", idList(profile.enchantAffinity())));
        lines.add(rawLine("tooltip.mobstoolforging.stat.traits", traitIdList(profile)));
        lines.add(rawLine("tooltip.mobstoolforging.stat.construction", rawConstruction(construction)));
        List<String> debugLines = nonQualityDebugLines(profile.debugLines());
        if (!debugLines.isEmpty()) {
            lines.add(Component.translatable("tooltip.mobstoolforging.debug").withStyle(ChatFormatting.DARK_GRAY));
            for (String debugLine : debugLines) {
                lines.add(Component.literal(debugLine).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static List<ResourceLocation> displayTraits(ToolStatProfile profile) {
        return ToolTraitDisplay.resolve(nonQualityTraits(profile.traits()));
    }

    private static List<ResourceLocation> nonQualityTraits(List<ResourceLocation> traits) {
        return traits.stream().filter(trait -> !isQualityTrait(trait)).toList();
    }

    private static boolean isQualityTrait(ResourceLocation trait) {
        return ToolTrait.WORKMANSHIP_GOOD.id().equals(trait) || ToolTrait.WORKMANSHIP_ROUGH.id().equals(trait);
    }

    private static List<String> nonQualityDebugLines(List<String> debugLines) {
        return debugLines.stream().filter(line -> !line.startsWith("Quality:")).toList();
    }

    private static Component partLine(String labelKey, Optional<ResourceLocation> material) {
        return partLine(labelKey, material.map(List::of).orElseGet(List::of));
    }

    private static Component partLine(String labelKey, List<ResourceLocation> materials) {
        return partLine(labelKey, materials, Optional.empty());
    }

    private static Component partLine(String labelKey, List<ResourceLocation> materials, Optional<ResourceLocation> fallbackMaterial) {
        MutableComponent line = Component.translatable(labelKey)
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY));
        Component value = materialList(materials, fallbackMaterial);
        return line.append(value);
    }

    private static Component materialList(List<ResourceLocation> materials, Optional<ResourceLocation> fallbackMaterial) {
        List<ResourceLocation> values = materials.isEmpty()
                ? fallbackMaterial.map(List::of).orElseGet(List::of)
                : materials;
        if (values.isEmpty()) {
            return Component.translatable("tooltip.mobstoolforging.none").withStyle(ChatFormatting.GRAY);
        }

        MutableComponent result = Component.empty();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                result.append(Component.literal(" \u00B7 ").withStyle(ChatFormatting.DARK_GRAY));
            }
            result.append(MaterialCatalog.displayName(values.get(index)).copy().withStyle(ChatFormatting.GRAY));
        }
        return result;
    }

    private static Component traitDescriptionLine(ToolStatProfile profile, ResourceLocation traitId) {
        MutableComponent line = Component.empty()
                .append(traitNameWithLevel(profile, traitId))
                .append(Component.literal(" \u2014 ").withStyle(ChatFormatting.DARK_GRAY));
        ToolTraitRegistry.definition(traitId)
                .map(ToolTraitDefinition::description)
                .ifPresentOrElse(line::append, () -> line.append(Component.literal(traitId.toString()).withStyle(ChatFormatting.GRAY)));
        return line;
    }

    private static Component traitNameWithLevel(ToolStatProfile profile, ResourceLocation traitId) {
        MutableComponent line = Component.empty().append(traitName(traitId));
        int level = traitLevel(profile, traitId);
        if (level > 0) {
            line.append(Component.literal(" " + roman(level)).withStyle(ChatFormatting.DARK_GRAY));
        }
        return line;
    }

    private static Component traitName(ResourceLocation traitId) {
        return ToolTraitRegistry.definition(traitId)
                .map(ToolTraitDefinition::displayName)
                .orElseGet(() -> Component.literal(ToolTrait.fallbackName(traitId)).withStyle(ChatFormatting.GRAY));
    }

    private static Component rawLine(String labelKey, String value) {
        return Component.translatable(labelKey)
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": " + value).withStyle(ChatFormatting.GRAY));
    }

    private static String decimal(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String idList(List<ResourceLocation> ids) {
        if (ids.isEmpty()) {
            return "-";
        }
        return String.join(", ", ids.stream().map(ResourceLocation::toString).toList());
    }

    private static String traitIdList(ToolStatProfile profile) {
        List<ResourceLocation> traits = displayTraits(profile);
        if (traits.isEmpty()) {
            return "-";
        }
        return String.join(", ", traits.stream()
                .map(trait -> trait + " " + roman(traitLevel(profile, trait)))
                .toList());
    }

    private static int traitLevel(ToolStatProfile profile, ResourceLocation traitId) {
        int level = 0;
        for (ResourceLocation trait : nonQualityTraits(profile.traits())) {
            if (trait.equals(traitId)) {
                level++;
            }
        }
        return level;
    }

    private static String roman(int value) {
        return switch (Math.max(1, Math.min(10, value))) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> "X";
        };
    }

    private static String rawConstruction(ToolConstructionData construction) {
        return "head=" + construction.headMaterial()
                + ", headBase=" + construction.headBaseMaterial().map(ResourceLocation::toString).orElse("-")
                + ", handle=" + construction.handleMaterial()
                + ", guard=" + construction.guardMaterial().map(ResourceLocation::toString).orElse("-")
                + ", treatment=" + construction.treatment().map(ResourceLocation::toString).orElse("-")
                + ", quality=" + construction.quality();
    }

    private static void addBlankIfNeeded(List<Component> lines) {
        if (!lines.isEmpty()) {
            lines.add(Component.empty());
        }
    }

    private record ToolMaterialSummary(
            List<ResourceLocation> headMaterials,
            List<ResourceLocation> coreMaterials,
            List<ResourceLocation> guardMaterials
    ) {
        private static ToolMaterialSummary from(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
            Set<ResourceLocation> heads = new LinkedHashSet<>();
            Set<ResourceLocation> cores = new LinkedHashSet<>();
            Set<ResourceLocation> guards = new LinkedHashSet<>();

            ToolAssemblyParts assemblyParts = stack.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
            if (assemblyParts != null) {
                for (ItemStack partStack : assemblyParts.stacks()) {
                    ToolPartData part = partStack.get(ModDataComponents.TOOL_PART.get());
                    if (part == null) {
                        continue;
                    }
                    if (!definition.primaryPartType().equals(part.partType()) && !definition.requiredAssemblyParts().contains(part.partType())) {
                        continue;
                    }
                    part.coatingBaseMaterial().ifPresent(cores::add);
                    if (definition.primaryPartType().equals(part.partType()) || definition.averageRequiredHeadDurability()) {
                        heads.add(part.materialId());
                    } else {
                        guards.add(part.materialId());
                    }
                }
            }

            if (heads.isEmpty()) {
                heads.add(construction.headMaterial());
                if (definition.averageRequiredHeadDurability()) {
                    construction.guardMaterial().ifPresent(heads::add);
                }
            }
            if (cores.isEmpty()) {
                construction.headBaseMaterial().ifPresent(cores::add);
            }
            if (guards.isEmpty() && !definition.averageRequiredHeadDurability()) {
                construction.guardMaterial().ifPresent(guards::add);
            }

            return new ToolMaterialSummary(List.copyOf(heads), List.copyOf(cores), List.copyOf(guards));
        }
    }
}

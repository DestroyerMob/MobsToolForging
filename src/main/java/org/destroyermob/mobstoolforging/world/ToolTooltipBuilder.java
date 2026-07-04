package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

        if (flag.hasShiftDown()) {
            addShiftTooltip(lines, definition, construction, profile);
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
            traitLine.append(traitName(displayTraits.get(index)));
        }
        int hidden = displayTraits.size() - shown;
        if (hidden > 0) {
            traitLine.append(Component.literal(" +" + hidden).withStyle(ChatFormatting.DARK_GRAY));
        }
        lines.add(traitLine);
        lines.add(Component.translatable("tooltip.mobstoolforging.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void addShiftTooltip(List<Component> lines, ToolTypeDefinition definition, ToolConstructionData construction, ToolStatProfile profile) {
        lines.add(Component.translatable("tooltip.mobstoolforging.construction").withStyle(ChatFormatting.GRAY));
        lines.add(partLine("tooltip.mobstoolforging.part.head", Optional.of(construction.headMaterial())));
        lines.add(partLine("tooltip.mobstoolforging.part.handle", Optional.of(construction.handleMaterial())));
        if (!definition.requiredAssemblyParts().isEmpty()) {
            lines.add(partLine("tooltip.mobstoolforging.part.guard", construction.guardMaterial()));
        }
        lines.add(partLine("tooltip.mobstoolforging.part.binding", construction.bindingMaterial()));
        lines.add(partLine("tooltip.mobstoolforging.part.wrap", construction.wrapMaterial()));
        lines.add(partLine("tooltip.mobstoolforging.part.focus", construction.focusMaterial()));
        lines.add(partLine("tooltip.mobstoolforging.part.treatment", construction.treatment()));

        List<ResourceLocation> displayTraits = displayTraits(profile);
        if (!displayTraits.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.mobstoolforging.traits").withStyle(ChatFormatting.GRAY));
            for (ResourceLocation traitId : displayTraits) {
                lines.add(traitDescriptionLine(traitId));
            }
        }
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
        lines.add(rawLine("tooltip.mobstoolforging.stat.traits", idList(nonQualityTraits(profile.traits()))));
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
        MutableComponent line = Component.translatable(labelKey)
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY));
        Component value = material
                .map(MaterialCatalog::displayName)
                .orElseGet(() -> Component.translatable("tooltip.mobstoolforging.none"));
        return line.append(value.copy().withStyle(ChatFormatting.GRAY));
    }

    private static Component traitDescriptionLine(ResourceLocation traitId) {
        MutableComponent line = Component.empty()
                .append(traitName(traitId))
                .append(Component.literal(" \u2014 ").withStyle(ChatFormatting.DARK_GRAY));
        ToolTraitRegistry.definition(traitId)
                .map(ToolTraitDefinition::description)
                .ifPresentOrElse(line::append, () -> line.append(Component.literal(traitId.toString()).withStyle(ChatFormatting.GRAY)));
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

    private static String rawConstruction(ToolConstructionData construction) {
        return "head=" + construction.headMaterial()
                + ", handle=" + construction.handleMaterial()
                + ", guard=" + construction.guardMaterial().map(ResourceLocation::toString).orElse("-")
                + ", binding=" + construction.bindingMaterial().map(ResourceLocation::toString).orElse("-")
                + ", wrap=" + construction.wrapMaterial().map(ResourceLocation::toString).orElse("-")
                + ", focus=" + construction.focusMaterial().map(ResourceLocation::toString).orElse("-")
                + ", treatment=" + construction.treatment().map(ResourceLocation::toString).orElse("-")
                + ", quality=" + construction.quality();
    }

    private static void addBlankIfNeeded(List<Component> lines) {
        if (!lines.isEmpty()) {
            lines.add(Component.empty());
        }
    }
}

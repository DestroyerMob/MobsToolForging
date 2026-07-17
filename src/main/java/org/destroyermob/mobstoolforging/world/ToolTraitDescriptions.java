package org.destroyermob.mobstoolforging.world;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Builds level-aware player text from the same values used by trait gameplay. */
public final class ToolTraitDescriptions {
    private ToolTraitDescriptions() {
    }

    public static Component description(ResourceLocation traitId, int level) {
        return ToolTrait.byId(traitId)
                .map(trait -> description(trait, level))
                .orElseGet(() -> ToolTraitRegistry.definition(traitId)
                        .map(definition -> (Component) definition.description())
                        .orElseGet(Component::empty));
    }

    public static Component description(ToolTrait trait, int level) {
        int safeLevel = Math.max(1, level);
        float potency = ToolTraitTuning.potency(safeLevel);
        return switch (trait) {
            case STEADY -> effect(trait,
                    multiplier(ToolTraitTuning.STEADY_MINING_BONUS, safeLevel),
                    decimal(ToolTraitTuning.STEADY_ATTACK_SPEED_BONUS * potency),
                    percent(ToolTraitTuning.STEADY_REPAIR_MULTIPLIER - 1.0F));
            case SWIFT -> effect(trait,
                    multiplier(ToolTraitTuning.SWIFT_MINING_BONUS, safeLevel),
                    decimal(ToolTraitTuning.SWIFT_ATTACK_SPEED_BONUS * potency),
                    decimal(ToolTraitTuning.durabilityWearMultiplier(safeLevel, 0, 0)));
            case KINDLED -> effect(trait, Integer.toString(ToolTraitTuning.KINDLED_BURN_SECONDS));
            case REINFORCED -> effect(trait,
                    percent(ToolTraitTuning.reinforcedWearPrevention(safeLevel)),
                    percent(ToolTraitTuning.REINFORCED_REPAIR_FRACTION));
            case WORK_HARDENED -> effect(trait,
                    percent(ToolTraitTuning.WORK_HARDENED_WORN_BONUS * potency),
                    percent(ToolTraitTuning.WORK_HARDENED_DAMAGED_BONUS * potency),
                    percent(ToolTraitTuning.WORK_HARDENED_CRITICAL_BONUS * potency));
            case RESONANT -> effect(trait, Integer.toString(ToolTraitTuning.discreteEnchantmentBonus(safeLevel)));
            case GILDED -> effect(trait, Integer.toString(safeLevel));
            case ADAMANT -> effect(trait,
                    multiplier(ToolTraitTuning.ADAMANT_MINING_BONUS, safeLevel),
                    percent(ToolTraitTuning.ADAMANT_ARMOR_BYPASS * potency));
            case NETHER_FORGED -> effect(trait,
                    multiplier(ToolTraitTuning.NETHER_FORGED_DURABILITY_BONUS, safeLevel),
                    multiplier(ToolTraitTuning.NETHER_FORGED_PHYSICAL_DAMAGE_BONUS, safeLevel));
            case FORTUNATE -> effect(trait, Integer.toString(ToolTraitTuning.discreteEnchantmentBonus(safeLevel)));
            case KEEN -> effect(trait,
                    multiplier(ToolTraitTuning.KEEN_MINING_BONUS, safeLevel),
                    multiplier(ToolTraitTuning.KEEN_PHYSICAL_DAMAGE_BONUS, safeLevel));
            case FORCEFUL -> effect(trait,
                    multiplier(ToolTraitTuning.FORCEFUL_MINING_BONUS, safeLevel),
                    multiplier(ToolTraitTuning.FORCEFUL_PHYSICAL_DAMAGE_BONUS, safeLevel),
                    decimal(ToolTraitTuning.durabilityWearMultiplier(0, 0, safeLevel)));
            case JAGGED -> effect(trait,
                    multiplier(ToolTraitTuning.JAGGED_MINING_BONUS, safeLevel),
                    multiplier(ToolTraitTuning.JAGGED_PHYSICAL_DAMAGE_BONUS, safeLevel),
                    decimal(ToolTraitTuning.durabilityWearMultiplier(0, safeLevel, 0)));
            case TENSIONED -> effect(trait,
                    Integer.toString(ToolTraitTuning.discreteEnchantmentBonus(safeLevel)),
                    multiplier(ToolTraitTuning.TENSIONED_PROJECTILE_DAMAGE_BONUS, safeLevel));
            case FOCUSED -> effect(trait,
                    multiplier(ToolTraitTuning.FOCUSED_MINING_BONUS, safeLevel),
                    decimal(ToolTraitTuning.FOCUSED_ATTACK_SPEED_BONUS * potency),
                    Integer.toString(ToolTraitTuning.discreteEnchantmentBonus(safeLevel)));
            case WORKMANSHIP_GOOD, WORKMANSHIP_ROUGH -> trait.description();
        };
    }

    private static Component effect(ToolTrait trait, Object... values) {
        return Component.translatable(trait.translationKey() + ".effect", values).withStyle(ChatFormatting.GRAY);
    }

    private static String multiplier(float levelOneBonus, int level) {
        return decimal(ToolTraitTuning.scaledMultiplier(levelOneBonus, level));
    }

    private static String percent(float value) {
        return decimal(value * 100.0F);
    }

    private static String decimal(float value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }
}

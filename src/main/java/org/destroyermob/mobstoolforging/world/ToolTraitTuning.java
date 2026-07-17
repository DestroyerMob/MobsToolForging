package org.destroyermob.mobstoolforging.world;

/** Shared trait values used by gameplay, tooltips, JEI/JEMI, and documentation helpers. */
public final class ToolTraitTuning {
    public static final float STEADY_MINING_BONUS = 0.25F;
    public static final float STEADY_ATTACK_SPEED_BONUS = 0.35F;
    public static final float STEADY_REPAIR_MULTIPLIER = 1.50F;

    public static final float SWIFT_MINING_BONUS = 0.60F;
    public static final float SWIFT_ATTACK_SPEED_BONUS = 0.75F;
    public static final float SWIFT_WEAR_BONUS = 1.00F;

    public static final int KINDLED_BURN_SECONDS = 5;

    public static final float NORMAL_REPAIR_FRACTION = 0.25F;
    public static final float REINFORCED_REPAIR_FRACTION = 0.50F;

    public static final float WORK_HARDENED_WORN_BONUS = 0.25F;
    public static final float WORK_HARDENED_DAMAGED_BONUS = 0.50F;
    public static final float WORK_HARDENED_CRITICAL_BONUS = 1.00F;

    public static final float ADAMANT_MINING_BONUS = 0.75F;
    public static final float ADAMANT_ARMOR_BYPASS = 0.30F;

    public static final float KEEN_MINING_BONUS = 0.15F;
    public static final float KEEN_PHYSICAL_DAMAGE_BONUS = 0.35F;

    public static final float FORCEFUL_MINING_BONUS = 0.30F;
    public static final float FORCEFUL_PHYSICAL_DAMAGE_BONUS = 0.60F;
    public static final float FORCEFUL_WEAR_BONUS = 0.50F;

    public static final float JAGGED_MINING_BONUS = 0.20F;
    public static final float JAGGED_PHYSICAL_DAMAGE_BONUS = 0.35F;
    public static final float JAGGED_WEAR_BONUS = 1.00F;

    public static final float FOCUSED_MINING_BONUS = 0.15F;
    public static final float FOCUSED_ATTACK_SPEED_BONUS = 0.55F;

    public static final float TENSIONED_PROJECTILE_DAMAGE_BONUS = 0.30F;

    public static final float NETHER_FORGED_DURABILITY_BONUS = 1.00F;
    public static final float NETHER_FORGED_PHYSICAL_DAMAGE_BONUS = 0.30F;

    private ToolTraitTuning() {
    }

    /** Full strength at level I, then half of the remaining strength from each repeated component. */
    public static float potency(int level) {
        if (level <= 0) {
            return 0.0F;
        }
        return 2.0F - (float) Math.pow(0.5D, level - 1);
    }

    public static float scaledMultiplier(float levelOneBonus, int level) {
        return 1.0F + levelOneBonus * potency(level);
    }

    public static int discreteEnchantmentBonus(int level) {
        return level <= 0 ? 0 : Math.max(2, Math.round(2.0F * potency(level)));
    }

    public static float reinforcedWearPrevention(int level) {
        float traitPotency = potency(level);
        return traitPotency <= 0.0F ? 0.0F : 1.0F - 1.0F / (1.0F + 3.0F * traitPotency);
    }

    public static float durabilityWearMultiplier(int swiftLevel, int jaggedLevel, int forcefulLevel) {
        return 1.0F
                + SWIFT_WEAR_BONUS * potency(swiftLevel)
                + JAGGED_WEAR_BONUS * potency(jaggedLevel)
                + FORCEFUL_WEAR_BONUS * potency(forcefulLevel);
    }

    public static float workHardenedBonus(int level, float remainingCondition) {
        if (level <= 0) {
            return 0.0F;
        }
        float baseBonus = remainingCondition < 0.25F ? WORK_HARDENED_CRITICAL_BONUS
                : remainingCondition < 0.50F ? WORK_HARDENED_DAMAGED_BONUS
                : remainingCondition < 0.75F ? WORK_HARDENED_WORN_BONUS
                : 0.0F;
        return baseBonus * potency(level);
    }
}

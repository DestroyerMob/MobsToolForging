package org.destroyermob.mobstoolforging;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MobsToolForgingConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue DEBUG_TEMPLATE_SELECTOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CRUDE_FLINT_TOOLS;
    public static final ModConfigSpec.BooleanValue DISABLE_STONE_TOOLS;
    public static final ModConfigSpec.BooleanValue DISABLE_WOODEN_TOOLS;
    public static final ModConfigSpec.BooleanValue FLINT_CAN_MINE_COPPER;
    public static final ModConfigSpec.BooleanValue FLINT_CAN_MINE_IRON;
    public static final ModConfigSpec.DoubleValue FLINT_KNAPPING_SUCCESS_CHANCE;
    public static final ModConfigSpec.DoubleValue FLINT_KNAPPING_BONUS_SHARD_CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DEBUG_TEMPLATE_SELECTOR = builder
                .comment("When true, sneak-right-clicking a workstation can open the old template selector GUI for debugging.")
                .define("debugTemplateSelector", false);
        ENABLE_CRUDE_FLINT_TOOLS = builder
                .comment("When true, crude flint survival tools are craftable.")
                .define("enableCrudeFlintTools", true);
        DISABLE_STONE_TOOLS = builder
                .comment("When true, vanilla stone tool recipes are removed at server start.")
                .define("disableStoneTools", true);
        DISABLE_WOODEN_TOOLS = builder
                .comment("When true, vanilla wooden tool recipes are removed at server start. Default stays false for standalone play.")
                .define("disableWoodenTools", false);
        FLINT_CAN_MINE_COPPER = builder
                .comment("When true, crude flint picks can harvest copper ore.")
                .define("flintCanMineCopper", true);
        FLINT_CAN_MINE_IRON = builder
                .comment("When true, crude flint picks can harvest iron ore. This should normally stay false so copper matters.")
                .define("flintCanMineIron", false);
        FLINT_KNAPPING_SUCCESS_CHANCE = builder
                .comment("Chance for a flint knapping hit against a pickaxe-mineable block to produce shards.")
                .defineInRange("flintKnappingSuccessChance", 0.6D, 0.0D, 1.0D);
        FLINT_KNAPPING_BONUS_SHARD_CHANCE = builder
                .comment("Chance for a successful flint knapping hit to produce one extra shard.")
                .defineInRange("flintKnappingBonusShardChance", 0.4D, 0.0D, 1.0D);
        COMMON_SPEC = builder.build();
    }

    private MobsToolForgingConfig() {
    }
}

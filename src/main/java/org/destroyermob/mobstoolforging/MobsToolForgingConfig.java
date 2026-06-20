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
    public static final ModConfigSpec.BooleanValue ENABLE_FORGE_HEATING;
    public static final ModConfigSpec.BooleanValue REQUIRE_HEATED_METAL;
    public static final ModConfigSpec.DoubleValue MINIMUM_FORGE_TEMPERATURE;
    public static final ModConfigSpec.IntValue HEATED_WORKPIECE_TICKS;
    public static final ModConfigSpec.IntValue COOLING_TICKS;
    public static final ModConfigSpec.BooleanValue ENABLE_BLOOMERY;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_ORE_SMELTING;
    public static final ModConfigSpec.BooleanValue ENABLE_CRUCIBLE;
    public static final ModConfigSpec.BooleanValue ENABLE_CASTING;
    public static final ModConfigSpec.BooleanValue ENABLE_INGOT_CASTING;
    public static final ModConfigSpec.BooleanValue ENABLE_BLOCK_CASTING;
    public static final ModConfigSpec.BooleanValue ENABLE_TOOL_HEAD_CASTING;

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
        builder.push("heating");
        ENABLE_FORGE_HEATING = builder
                .comment("When true, the Heating Forge can heat solid metal workpieces.")
                .define("enableForgeHeating", true);
        REQUIRE_HEATED_METAL = builder
                .comment("When true, metal materials must be heated before they can be placed on the Smithing Anvil.")
                .define("requireHeatedMetal", true);
        MINIMUM_FORGE_TEMPERATURE = builder
                .comment("Default minimum metal temperature required for forging. 0.9 means 90%. Datapack forge templates can override this per template.")
                .defineInRange("minimumForgeTemperature", 0.9D, 0.0D, 1.0D);
        HEATED_WORKPIECE_TICKS = builder
                .comment("Ticks required for the Heating Forge to heat a metal workpiece.")
                .defineInRange("heatedWorkpieceTicks", 1200, 1, 20 * 60 * 60);
        COOLING_TICKS = builder
                .comment("Ticks a heated workpiece remains workable after leaving the Heating Forge.")
                .defineInRange("coolingTicks", 1200, 1, 20 * 60 * 60);
        builder.pop();
        builder.push("bloomery");
        ENABLE_BLOOMERY = builder
                .comment("Reserved for future raw ore to bloom/crude ingot processing.")
                .define("enableBloomery", false);
        REPLACE_VANILLA_ORE_SMELTING = builder
                .comment("Reserved for future bloomery progression. This pass does not replace vanilla ore smelting.")
                .define("replaceVanillaOreSmelting", false);
        builder.pop();
        builder.push("casting");
        ENABLE_CRUCIBLE = builder
                .comment("Reserved for future molten metal handling. This pass does not implement crucibles.")
                .define("enableCrucible", false);
        ENABLE_CASTING = builder
                .comment("Reserved for future casting systems. This pass does not implement casting.")
                .define("enableCasting", false);
        ENABLE_INGOT_CASTING = builder
                .comment("Reserved for future ingot casting.")
                .define("enableIngotCasting", false);
        ENABLE_BLOCK_CASTING = builder
                .comment("Reserved for future block casting.")
                .define("enableBlockCasting", false);
        ENABLE_TOOL_HEAD_CASTING = builder
                .comment("Reserved for future rough cast tool heads that still need finishing.")
                .define("enableToolHeadCasting", false);
        builder.pop();
        COMMON_SPEC = builder.build();
    }

    private MobsToolForgingConfig() {
    }
}

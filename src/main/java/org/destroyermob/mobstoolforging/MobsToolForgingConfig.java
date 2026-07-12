package org.destroyermob.mobstoolforging;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.destroyermob.mobstoolforging.world.ForgingQuality;
import org.destroyermob.mobstoolforging.world.HeatLevel;

public final class MobsToolForgingConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue DEBUG_TEMPLATE_SELECTOR;
    public static final ModConfigSpec.BooleanValue DEBUG_ACTIONBAR_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_TOOL_RECIPES;
    public static final ModConfigSpec.BooleanValue ENABLE_CRUDE_FLINT_TOOLS;
    public static final ModConfigSpec.BooleanValue ENABLE_PLANT_FIBER_DROPS;
    public static final ModConfigSpec.BooleanValue DISABLE_STONE_TOOLS;
    public static final ModConfigSpec.BooleanValue DISABLE_WOODEN_TOOLS;
    public static final ModConfigSpec.BooleanValue COPPER_REQUIRES_WOODEN_TOOL;
    public static final ModConfigSpec.BooleanValue ENABLE_ANVIL_CRAFTING_RECIPES;
    public static final ModConfigSpec.BooleanValue ENABLE_CRUDE_ANVIL;
    public static final ModConfigSpec.BooleanValue BASIC_PATTERNS_REQUIRE_PAPER;
    public static final ModConfigSpec.BooleanValue ENABLE_PATTERN_RACK;
    public static final ModConfigSpec.IntValue WORKSHOP_PATTERN_RANGE;
    public static final ModConfigSpec.BooleanValue ALLOW_DIRECT_PATTERN_STATION_SELECTION;
    @Deprecated(forRemoval = false)
    public static final FinishedToolEnchantingCompatValue ALLOW_FINISHED_TOOL_ENCHANTING = new FinishedToolEnchantingCompatValue();
    public static final ModConfigSpec.BooleanValue REQUIRE_TOOL_PART_ENCHANTING;
    public static final ModConfigSpec.BooleanValue REQUIRE_ARMOR_PART_ENCHANTING;
    public static final ModConfigSpec.BooleanValue CONVERT_VANILLA_LOOT_TO_MODULAR_TOOLS;
    public static final ModConfigSpec.BooleanValue ENABLE_FORGE_HEATING;
    public static final ModConfigSpec.BooleanValue REQUIRE_HEATED_METAL;
    public static final ModConfigSpec.DoubleValue MINIMUM_FORGE_TEMPERATURE;
    public static final ModConfigSpec.DoubleValue LOW_HEAT_MINIMUM_FORGE_TEMPERATURE;
    public static final ModConfigSpec.IntValue HEATED_WORKPIECE_TICKS;
    public static final ModConfigSpec.IntValue COOLING_TICKS;
    public static final ModConfigSpec.BooleanValue ENABLE_CAMPFIRE_LOW_HEAT;
    public static final ModConfigSpec.ConfigValue<String> CAMPFIRE_HEAT_LEVEL;
    public static final ModConfigSpec.BooleanValue REQUIRE_HEAT_AT_JOB_START_ONLY;
    public static final ModConfigSpec.BooleanValue WORKPIECE_COOLS_MID_CRAFT;
    public static final ModConfigSpec.IntValue FORGE_HEAT_BUFFER_TICKS;
    public static final ModConfigSpec.IntValue FLUID_FORGE_HEAT_UNITS_PER_MB;
    public static final ModConfigSpec.BooleanValue ENABLE_QUALITY;
    public static final ModConfigSpec.BooleanValue ENABLE_TIMING_QUALITY;
    public static final ModConfigSpec.BooleanValue QUALITY_AFFECTS_STATS;
    public static final ModConfigSpec.IntValue TIMING_QUALITY_WINDOW_TICKS;
    public static final ModConfigSpec.ConfigValue<String> CRUDE_ANVIL_MAX_QUALITY;
    public static final ModConfigSpec.ConfigValue<String> SMITHING_ANVIL_MAX_QUALITY;
    public static final ModConfigSpec.ConfigValue<String> LAPIDARY_TABLE_MAX_QUALITY;
    public static final ModConfigSpec.ConfigValue<String> LEATHER_STATION_MAX_QUALITY;
    public static final ModConfigSpec.BooleanValue GEMCUTTERS_FILE_REQUIRED;
    public static final ModConfigSpec.BooleanValue DIAMOND_REQUIRES_ABRASIVE;
    public static final ModConfigSpec.ConfigValue<String> DIAMOND_ABRASIVE_ITEM;
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
        DEBUG_ACTIONBAR_FEEDBACK = builder
                .comment("When true, development-only workstation and status actionbar feedback is shown to players.")
                .define("debugActionbarFeedback", false);
        ENABLE_VANILLA_TOOL_RECIPES = builder
                .comment("When true, vanilla sword, shovel, pickaxe, axe, and hoe recipes remain enabled. When false, vanilla material tool recipes are removed so Mobs Tool Forging owns tool progression.")
                .define("enableVanillaToolRecipes", false);
        ENABLE_CRUDE_FLINT_TOOLS = builder
                .comment("When true, starter flint tool progression is enabled: placed flint knapping and data-driven ground assembly.")
                .define("enableCrudeFlintTools", true);
        ENABLE_PLANT_FIBER_DROPS = builder
                .comment("When true, grass and ferns have a 10% chance to drop Plant Fiber when broken with flint or another knapping tool.")
                .define("enablePlantFiberDrops", true);
        DISABLE_STONE_TOOLS = builder
                .comment("When true, vanilla stone tool recipes are removed at server start.")
                .define("disableStoneTools", true);
        DISABLE_WOODEN_TOOLS = builder
                .comment("When true, vanilla wooden tool recipes are removed at server start. Default stays false for standalone play.")
                .define("disableWoodenTools", false);
        COPPER_REQUIRES_WOODEN_TOOL = builder
                .comment("When true, vanilla copper ore, copper blocks, and raw copper blocks can be harvested with wooden-tier pickaxes. Flint uses wooden-tier mining rules.")
                .define("copperRequiresWoodenTool", true);
        builder.push("progression");
        ENABLE_ANVIL_CRAFTING_RECIPES = builder
                .comment("When true, mod anvil crafting recipes are enabled and the sneak-right-click hammer recipes for crude and iron anvils are disabled. Vanilla anvil crafting stays vanilla.")
                .define("enableAnvilCraftingRecipes", false);
        ENABLE_CRUDE_ANVIL = builder
                .comment("When true, the cheap Crude Anvil is available as an early, lower-quality shaping station.")
                .define("enableCrudeAnvil", true);
        BASIC_PATTERNS_REQUIRE_PAPER = builder
                .comment("When false, Pattern Boards can be used for basic Pattern Creation Station output before paper is available.")
                .define("basicPatternsRequirePaper", false);
        ENABLE_PATTERN_RACK = builder
                .comment("When true, Pattern Racks can store visible patterns and assign them to nearby stations.")
                .define("enablePatternRack", true);
        WORKSHOP_PATTERN_RANGE = builder
                .comment("Horizontal block range for assigning a Pattern Rack pattern to a station. Vertical range is always plus/minus 1 block.")
                .defineInRange("workshopPatternRange", 4, 1, 16);
        ALLOW_DIRECT_PATTERN_STATION_SELECTION = builder
                .comment("When true, using a pattern item directly on a station still selects that station pattern.")
                .define("allowDirectPatternStationSelection", true);
        builder.pop();
        REQUIRE_TOOL_PART_ENCHANTING = builder
                .comment("Legacy compatibility toggle. When true, finished modular tools cannot be enchanted directly. The default finished-tool flow stores routed enchantments on assembled parts instead.")
                .define("requireToolPartEnchanting", false);
        REQUIRE_ARMOR_PART_ENCHANTING = builder
                .comment("When true, finished Mobs Tool Forging modular armor cannot be enchanted directly. Enchant armor parts before assembly instead.")
                .define("requireArmorPartEnchanting", true);
        CONVERT_VANILLA_LOOT_TO_MODULAR_TOOLS = builder
                .comment("When true, vanilla tools, weapons, and armor generated by loot tables or equipped by hostile mobs are replaced with Mobs Tool Forging modular equivalents.")
                .define("convertVanillaLootToModularTools", true);
        builder.push("heating");
        ENABLE_FORGE_HEATING = builder
                .comment("When true, the Heating Forge can heat solid metal workpieces.")
                .define("enableForgeHeating", true);
        REQUIRE_HEATED_METAL = builder
                .comment("When true, metal materials must be heated before they can be placed on an anvil station.")
                .define("requireHeatedMetal", true);
        MINIMUM_FORGE_TEMPERATURE = builder
                .comment("Default minimum metal temperature required for forging. 0.9 means 90%. Datapack forge templates can override this per template.")
                .defineInRange("minimumForgeTemperature", 0.9D, 0.0D, 1.0D);
        LOW_HEAT_MINIMUM_FORGE_TEMPERATURE = builder
                .comment("Minimum remaining temperature for low-heat metals such as copper, gold, and early iron. This is lower than the LOW heat target so campfire-heated ingots have a cooling margin.")
                .defineInRange("lowHeatMinimumForgeTemperature", 0.4D, 0.0D, 1.0D);
        HEATED_WORKPIECE_TICKS = builder
                .comment("Ticks required for the Heating Forge to heat a metal workpiece.")
                .defineInRange("heatedWorkpieceTicks", 1200, 1, 20 * 60 * 60);
        COOLING_TICKS = builder
                .comment("Ticks a heated workpiece remains workable after leaving the Heating Forge.")
                .defineInRange("coolingTicks", 2400, 1, 20 * 60 * 60);
        ENABLE_CAMPFIRE_LOW_HEAT = builder
                .comment("When true, campfire-warmed workpieces can start low-heat forging. Nearby lit campfires count as low workshop heat only for already-warmed workpieces; cold ingots still need to be warmed first.")
                .define("enableCampfireLowHeat", true);
        CAMPFIRE_HEAT_LEVEL = builder
                .comment("Heat level supplied by nearby lit campfires. Valid values: NONE, LOW, HOT, HIGH.")
                .define("campfireHeatLevel", "LOW");
        REQUIRE_HEAT_AT_JOB_START_ONLY = builder
                .comment("When true, metal heat is checked when a shaping job starts rather than interrupting hammering mid-craft.")
                .define("requireHeatAtJobStartOnly", true);
        WORKPIECE_COOLS_MID_CRAFT = builder
                .comment("When false, an active anvil workpiece stays workable until it completes or is cleared.")
                .define("workpieceCoolsMidCraft", false);
        FORGE_HEAT_BUFFER_TICKS = builder
                .comment("Ticks a Heating Forge keeps nearby workshop heat after burning fuel.")
                .defineInRange("forgeHeatBufferTicks", 2400, 1, 20 * 60 * 60);
        FLUID_FORGE_HEAT_UNITS_PER_MB = builder
                .comment("Heat progress supplied by each millibucket in the Lava Heating Forge. The default heats roughly 16 full-temperature ingots per lava bucket.")
                .defineInRange("fluidForgeHeatUnitsPerMb", 20, 1, 1000);
        builder.pop();
        builder.push("quality");
        ENABLE_QUALITY = builder
                .comment("When true, shaped parts and finished tools store workmanship quality.")
                .define("enableQuality", true);
        ENABLE_TIMING_QUALITY = builder
                .comment("When true, well-timed in-world work actions can improve workmanship quality.")
                .define("enableTimingQuality", true);
        QUALITY_AFFECTS_STATS = builder
                .comment("When true, finished tool quality modestly adjusts durability and stats.")
                .define("qualityAffectsStats", true);
        TIMING_QUALITY_WINDOW_TICKS = builder
                .comment("Half-window in ticks for a well-timed hammer/cutting action.")
                .defineInRange("timingQualityWindowTicks", 8, 1, 40);
        CRUDE_ANVIL_MAX_QUALITY = builder
                .comment("Maximum quality from the Crude Anvil.")
                .define("crudeAnvilMaxQuality", "WORKED");
        SMITHING_ANVIL_MAX_QUALITY = builder
                .comment("Maximum quality from the Smithing Anvil.")
                .define("smithingAnvilMaxQuality", "FINE");
        LAPIDARY_TABLE_MAX_QUALITY = builder
                .comment("Maximum quality from the Lapidary Table.")
                .define("lapidaryTableMaxQuality", "FINE");
        LEATHER_STATION_MAX_QUALITY = builder
                .comment("Maximum quality from Leather Station timing-quality work.")
                .define("leatherStationMaxQuality", "FINE");
        builder.pop();
        builder.push("lapidary");
        GEMCUTTERS_FILE_REQUIRED = builder
                .comment("When true, the Gem Cutter's Knife is required for every Lapidary Table work action. When false, it is optional quality help.")
                .define("gemcuttersFileRequired", false);
        DIAMOND_REQUIRES_ABRASIVE = builder
                .comment("Deprecated compatibility option. Diamond Powder is optional lapidary quality help and no longer gates diamond work.")
                .define("diamondRequiresAbrasive", false);
        DIAMOND_ABRASIVE_ITEM = builder
                .comment("Item id used as the optional lapidary abrasive. The default intentionally standardises on diamond_powder.")
                .define("diamondAbrasiveItem", "mobstoolforging:diamond_powder");
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

    public static boolean crudeFlintToolsEnabled() {
        return COMMON_SPEC.isLoaded() && ENABLE_CRUDE_FLINT_TOOLS.get();
    }

    public static HeatLevel campfireHeatLevel() {
        return HeatLevel.parse(CAMPFIRE_HEAT_LEVEL.get(), HeatLevel.LOW);
    }

    public static ForgingQuality crudeAnvilMaxQuality() {
        return ForgingQuality.parse(CRUDE_ANVIL_MAX_QUALITY.get(), ForgingQuality.WORKED);
    }

    public static ForgingQuality smithingAnvilMaxQuality() {
        return ForgingQuality.parse(SMITHING_ANVIL_MAX_QUALITY.get(), ForgingQuality.FINE);
    }

    public static ForgingQuality lapidaryTableMaxQuality() {
        return ForgingQuality.parse(LAPIDARY_TABLE_MAX_QUALITY.get(), ForgingQuality.FINE);
    }

    public static ForgingQuality leatherStationMaxQuality() {
        return ForgingQuality.parse(LEATHER_STATION_MAX_QUALITY.get(), ForgingQuality.FINE);
    }

    public static final class FinishedToolEnchantingCompatValue {
        private FinishedToolEnchantingCompatValue() {
        }

        public Boolean get() {
            return !REQUIRE_TOOL_PART_ENCHANTING.get();
        }
    }
}

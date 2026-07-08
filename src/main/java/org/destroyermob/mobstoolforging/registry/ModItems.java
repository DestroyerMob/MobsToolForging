package org.destroyermob.mobstoolforging.registry;

import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.FireStickItem;
import org.destroyermob.mobstoolforging.item.LeatherStationBlockItem;
import org.destroyermob.mobstoolforging.item.ModularArmorPartItem;
import org.destroyermob.mobstoolforging.item.ModularBootsItem;
import org.destroyermob.mobstoolforging.item.ModularChestplateItem;
import org.destroyermob.mobstoolforging.item.ModularHelmetItem;
import org.destroyermob.mobstoolforging.item.ModularLeggingsItem;
import org.destroyermob.mobstoolforging.item.ModularAxeItem;
import org.destroyermob.mobstoolforging.item.ModularHoeItem;
import org.destroyermob.mobstoolforging.item.ModularMattockItem;
import org.destroyermob.mobstoolforging.item.ModularPickaxeItem;
import org.destroyermob.mobstoolforging.item.ModularShovelItem;
import org.destroyermob.mobstoolforging.item.ModularSwordItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.item.StationToolItem;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MobsToolForging.MOD_ID);

    public static final DeferredItem<BlockItem> TOOL_FORGE = ITEMS.register(
            "tool_forge",
            () -> new BlockItem(ModBlocks.TOOL_FORGE.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> CRUDE_ANVIL = ITEMS.register(
            "crude_anvil",
            () -> new BlockItem(ModBlocks.CRUDE_ANVIL.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> LAPIDARY_TABLE = ITEMS.register(
            "lapidary_table",
            () -> new BlockItem(ModBlocks.LAPIDARY_TABLE.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> PATTERN_CREATION_STATION = ITEMS.register(
            "pattern_creation_station",
            () -> new BlockItem(ModBlocks.PATTERN_CREATION_STATION.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> PATTERN_RACK = blockItem("pattern_rack", ModBlocks.PATTERN_RACK);
    public static final DeferredItem<BlockItem> SPRUCE_PATTERN_RACK = blockItem("spruce_pattern_rack", ModBlocks.SPRUCE_PATTERN_RACK);
    public static final DeferredItem<BlockItem> BIRCH_PATTERN_RACK = blockItem("birch_pattern_rack", ModBlocks.BIRCH_PATTERN_RACK);
    public static final DeferredItem<BlockItem> JUNGLE_PATTERN_RACK = blockItem("jungle_pattern_rack", ModBlocks.JUNGLE_PATTERN_RACK);
    public static final DeferredItem<BlockItem> ACACIA_PATTERN_RACK = blockItem("acacia_pattern_rack", ModBlocks.ACACIA_PATTERN_RACK);
    public static final DeferredItem<BlockItem> DARK_OAK_PATTERN_RACK = blockItem("dark_oak_pattern_rack", ModBlocks.DARK_OAK_PATTERN_RACK);
    public static final DeferredItem<BlockItem> MANGROVE_PATTERN_RACK = blockItem("mangrove_pattern_rack", ModBlocks.MANGROVE_PATTERN_RACK);
    public static final DeferredItem<BlockItem> CHERRY_PATTERN_RACK = blockItem("cherry_pattern_rack", ModBlocks.CHERRY_PATTERN_RACK);
    public static final DeferredItem<BlockItem> BAMBOO_PATTERN_RACK = blockItem("bamboo_pattern_rack", ModBlocks.BAMBOO_PATTERN_RACK);
    public static final DeferredItem<BlockItem> CRIMSON_PATTERN_RACK = blockItem("crimson_pattern_rack", ModBlocks.CRIMSON_PATTERN_RACK);
    public static final DeferredItem<BlockItem> WARPED_PATTERN_RACK = blockItem("warped_pattern_rack", ModBlocks.WARPED_PATTERN_RACK);
    public static final List<DeferredItem<BlockItem>> PATTERN_RACK_ITEMS = List.of(
            PATTERN_RACK,
            SPRUCE_PATTERN_RACK,
            BIRCH_PATTERN_RACK,
            JUNGLE_PATTERN_RACK,
            ACACIA_PATTERN_RACK,
            DARK_OAK_PATTERN_RACK,
            MANGROVE_PATTERN_RACK,
            CHERRY_PATTERN_RACK,
            BAMBOO_PATTERN_RACK,
            CRIMSON_PATTERN_RACK,
            WARPED_PATTERN_RACK
    );
    public static final DeferredItem<BlockItem> TOOLMAKERS_BENCH = ITEMS.register(
            "toolmakers_bench",
            () -> new BlockItem(ModBlocks.TOOLMAKERS_BENCH.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> SPRUCE_TOOLMAKERS_BENCH = blockItem("spruce_toolmakers_bench", ModBlocks.SPRUCE_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> BIRCH_TOOLMAKERS_BENCH = blockItem("birch_toolmakers_bench", ModBlocks.BIRCH_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> JUNGLE_TOOLMAKERS_BENCH = blockItem("jungle_toolmakers_bench", ModBlocks.JUNGLE_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> ACACIA_TOOLMAKERS_BENCH = blockItem("acacia_toolmakers_bench", ModBlocks.ACACIA_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> DARK_OAK_TOOLMAKERS_BENCH = blockItem("dark_oak_toolmakers_bench", ModBlocks.DARK_OAK_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> MANGROVE_TOOLMAKERS_BENCH = blockItem("mangrove_toolmakers_bench", ModBlocks.MANGROVE_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> CHERRY_TOOLMAKERS_BENCH = blockItem("cherry_toolmakers_bench", ModBlocks.CHERRY_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> BAMBOO_TOOLMAKERS_BENCH = blockItem("bamboo_toolmakers_bench", ModBlocks.BAMBOO_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> CRIMSON_TOOLMAKERS_BENCH = blockItem("crimson_toolmakers_bench", ModBlocks.CRIMSON_TOOLMAKERS_BENCH);
    public static final DeferredItem<BlockItem> WARPED_TOOLMAKERS_BENCH = blockItem("warped_toolmakers_bench", ModBlocks.WARPED_TOOLMAKERS_BENCH);
    public static final List<DeferredItem<BlockItem>> TOOLMAKER_STATION_ITEMS = List.of(
            TOOLMAKERS_BENCH,
            SPRUCE_TOOLMAKERS_BENCH,
            BIRCH_TOOLMAKERS_BENCH,
            JUNGLE_TOOLMAKERS_BENCH,
            ACACIA_TOOLMAKERS_BENCH,
            DARK_OAK_TOOLMAKERS_BENCH,
            MANGROVE_TOOLMAKERS_BENCH,
            CHERRY_TOOLMAKERS_BENCH,
            BAMBOO_TOOLMAKERS_BENCH,
            CRIMSON_TOOLMAKERS_BENCH,
            WARPED_TOOLMAKERS_BENCH
    );
    public static final DeferredItem<BlockItem> LEATHER_STATION = leatherStationItem("leather_station", ModBlocks.LEATHER_STATION);
    public static final DeferredItem<BlockItem> SPRUCE_LEATHER_STATION = leatherStationItem("spruce_leather_station", ModBlocks.SPRUCE_LEATHER_STATION);
    public static final DeferredItem<BlockItem> BIRCH_LEATHER_STATION = leatherStationItem("birch_leather_station", ModBlocks.BIRCH_LEATHER_STATION);
    public static final DeferredItem<BlockItem> JUNGLE_LEATHER_STATION = leatherStationItem("jungle_leather_station", ModBlocks.JUNGLE_LEATHER_STATION);
    public static final DeferredItem<BlockItem> ACACIA_LEATHER_STATION = leatherStationItem("acacia_leather_station", ModBlocks.ACACIA_LEATHER_STATION);
    public static final DeferredItem<BlockItem> DARK_OAK_LEATHER_STATION = leatherStationItem("dark_oak_leather_station", ModBlocks.DARK_OAK_LEATHER_STATION);
    public static final DeferredItem<BlockItem> MANGROVE_LEATHER_STATION = leatherStationItem("mangrove_leather_station", ModBlocks.MANGROVE_LEATHER_STATION);
    public static final DeferredItem<BlockItem> CHERRY_LEATHER_STATION = leatherStationItem("cherry_leather_station", ModBlocks.CHERRY_LEATHER_STATION);
    public static final DeferredItem<BlockItem> BAMBOO_LEATHER_STATION = leatherStationItem("bamboo_leather_station", ModBlocks.BAMBOO_LEATHER_STATION);
    public static final DeferredItem<BlockItem> CRIMSON_LEATHER_STATION = leatherStationItem("crimson_leather_station", ModBlocks.CRIMSON_LEATHER_STATION);
    public static final DeferredItem<BlockItem> WARPED_LEATHER_STATION = leatherStationItem("warped_leather_station", ModBlocks.WARPED_LEATHER_STATION);
    public static final List<DeferredItem<BlockItem>> LEATHER_STATION_ITEMS = List.of(
            LEATHER_STATION,
            SPRUCE_LEATHER_STATION,
            BIRCH_LEATHER_STATION,
            JUNGLE_LEATHER_STATION,
            ACACIA_LEATHER_STATION,
            DARK_OAK_LEATHER_STATION,
            MANGROVE_LEATHER_STATION,
            CHERRY_LEATHER_STATION,
            BAMBOO_LEATHER_STATION,
            CRIMSON_LEATHER_STATION,
            WARPED_LEATHER_STATION
    );
    public static final DeferredItem<BlockItem> HEATING_FORGE = ITEMS.register(
            "heating_forge",
            () -> new BlockItem(ModBlocks.HEATING_FORGE.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> CRUCIBLE = ITEMS.register(
            "crucible",
            () -> new BlockItem(ModBlocks.CRUCIBLE.get(), new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<BlockItem> FOUNDRY_FORGE = ITEMS.register(
            "foundry_forge",
            () -> new BlockItem(ModBlocks.FOUNDRY_FORGE.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> ASH = blockItem("ash", ModBlocks.ASH);
    public static final DeferredItem<Item> SMITHING_HAMMER = ITEMS.register(
            "smithing_hammer",
            () -> new StationToolItem(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> IRON_SMITHING_HAMMER = ITEMS.register(
            "iron_smithing_hammer",
            () -> new StationToolItem(new Item.Properties().durability(384))
    );
    public static final DeferredItem<Item> SMITHING_HAMMER_HEAD = ITEMS.register(
            "smithing_hammer_head",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> SCREWDRIVER = ITEMS.register(
            "screwdriver",
            () -> new StationToolItem(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> SCREWDRIVER_HEAD = ITEMS.register(
            "screwdriver_head",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> GEM_CUTTERS_KNIFE = ITEMS.register(
            "gem_cutters_knife",
            () -> new StationToolItem(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> GEM_CUTTERS_BLADE = ITEMS.register(
            "gem_cutters_blade",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> DIAMOND_POWDER = ITEMS.register(
            "diamond_powder",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> PATTERN_BOARD = ITEMS.register(
            "pattern_board",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> FLINT_SHARD = ITEMS.register(
            "flint_shard",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.register(
            "plant_fiber",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<FireStickItem> FIRE_STICK = ITEMS.register(
            "fire_stick",
            () -> new FireStickItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<ToolTemplateItem> PICKAXE_HEAD_PATTERN = ITEMS.register(
            "pickaxe_head_pattern",
            () -> new ToolTemplateItem(ForgeTemplate.PICKAXE_HEAD, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> AXE_HEAD_PATTERN = ITEMS.register(
            "axe_head_pattern",
            () -> new ToolTemplateItem(ForgeTemplate.AXE_HEAD, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> SHOVEL_HEAD_PATTERN = ITEMS.register(
            "shovel_head_pattern",
            () -> new ToolTemplateItem(ForgeTemplate.SHOVEL_HEAD, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> HOE_HEAD_PATTERN = ITEMS.register(
            "hoe_head_pattern",
            () -> new ToolTemplateItem(ForgeTemplate.HOE_HEAD, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> SWORD_BLADE_PATTERN = ITEMS.register(
            "sword_blade_pattern",
            () -> new ToolTemplateItem(ForgeTemplate.SWORD_BLADE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> SWORD_GUARD_PATTERN = ITEMS.register(
            "sword_guard_pattern",
            () -> new ToolTemplateItem(ForgeTemplate.SWORD_GUARD, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> SMITHING_HAMMER_HEAD_PATTERN = ITEMS.register(
            "smithing_hammer_head_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.SMITHING_HAMMER_HEAD_TEMPLATE, WorkstationKind.TOOL_FORGE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> SCREWDRIVER_HEAD_PATTERN = ITEMS.register(
            "screwdriver_head_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.SCREWDRIVER_HEAD_TEMPLATE, WorkstationKind.TOOL_FORGE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> GEM_CUTTERS_BLADE_PATTERN = ITEMS.register(
            "gem_cutters_blade_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.GEM_CUTTERS_BLADE_TEMPLATE, WorkstationKind.TOOL_FORGE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> HELMET_CHAINMAIL_PATTERN = ITEMS.register(
            "helmet_chainmail_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.HELMET_CHAINMAIL_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> HELMET_PLATE_PATTERN = ITEMS.register(
            "helmet_plate_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.HELMET_PLATE_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> CHESTPLATE_CHAINMAIL_PATTERN = ITEMS.register(
            "chestplate_chainmail_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.CHESTPLATE_CHAINMAIL_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> CHESTPLATE_BODY_PATTERN = ITEMS.register(
            "chestplate_body_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> LEGGINGS_CHAINMAIL_PATTERN = ITEMS.register(
            "leggings_chainmail_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.LEGGINGS_CHAINMAIL_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> LEGGINGS_PLATE_PATTERN = ITEMS.register(
            "leggings_plate_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.LEGGINGS_PLATE_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> BOOTS_CHAINMAIL_PATTERN = ITEMS.register(
            "boots_chainmail_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.BOOTS_CHAINMAIL_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> BOOTS_PLATE_PATTERN = ITEMS.register(
            "boots_plate_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.BOOTS_PLATE_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> TEMPLATE_PATTERN = ITEMS.register(
            "template_pattern",
            () -> new ToolTemplateItem(new Item.Properties())
    );
    public static final DeferredItem<ModularToolPartItem> SWORD_BLADE = ITEMS.register(
            "sword_blade",
            () -> new ModularToolPartItem(ToolKind.SWORD, new Item.Properties())
    );
    public static final DeferredItem<ModularToolPartItem> SWORD_GUARD = ITEMS.register(
            "sword_guard",
            () -> new ModularToolPartItem(ToolKind.SWORD, ToolPartData.SWORD_GUARD, new Item.Properties())
    );
    public static final DeferredItem<ModularToolPartItem> SHOVEL_HEAD = ITEMS.register(
            "shovel_head",
            () -> new ModularToolPartItem(ToolKind.SHOVEL, new Item.Properties())
    );
    public static final DeferredItem<ModularToolPartItem> PICKAXE_HEAD = ITEMS.register(
            "pickaxe_head",
            () -> new ModularToolPartItem(ToolKind.PICKAXE, new Item.Properties())
    );
    public static final DeferredItem<ModularToolPartItem> AXE_HEAD = ITEMS.register(
            "axe_head",
            () -> new ModularToolPartItem(ToolKind.AXE, new Item.Properties())
    );
    public static final DeferredItem<ModularToolPartItem> HOE_HEAD = ITEMS.register(
            "hoe_head",
            () -> new ModularToolPartItem(ToolKind.HOE, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> HELMET_CHAINMAIL = ITEMS.register(
            ArmorPartData.HELMET_CHAINMAIL,
            () -> new ModularArmorPartItem(ArmorPartData.HELMET_CHAINMAIL, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> HELMET_PLATE = ITEMS.register(
            ArmorPartData.HELMET_PLATE,
            () -> new ModularArmorPartItem(ArmorPartData.HELMET_PLATE, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> CHESTPLATE_CHAINMAIL = ITEMS.register(
            ArmorPartData.CHESTPLATE_CHAINMAIL,
            () -> new ModularArmorPartItem(ArmorPartData.CHESTPLATE_CHAINMAIL, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> CHESTPLATE_BODY = ITEMS.register(
            ArmorPartData.CHESTPLATE_BODY,
            () -> new ModularArmorPartItem(ArmorPartData.CHESTPLATE_BODY, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> LEGGINGS_CHAINMAIL = ITEMS.register(
            ArmorPartData.LEGGINGS_CHAINMAIL,
            () -> new ModularArmorPartItem(ArmorPartData.LEGGINGS_CHAINMAIL, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> LEGGINGS_PLATE = ITEMS.register(
            ArmorPartData.LEGGINGS_PLATE,
            () -> new ModularArmorPartItem(ArmorPartData.LEGGINGS_PLATE, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> BOOTS_CHAINMAIL = ITEMS.register(
            ArmorPartData.BOOTS_CHAINMAIL,
            () -> new ModularArmorPartItem(ArmorPartData.BOOTS_CHAINMAIL, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> BOOTS_PLATE = ITEMS.register(
            ArmorPartData.BOOTS_PLATE,
            () -> new ModularArmorPartItem(ArmorPartData.BOOTS_PLATE, new Item.Properties())
    );
    public static final DeferredItem<ModularSwordItem> SWORD = ITEMS.register(
            "sword",
            () -> new ModularSwordItem(new Item.Properties())
    );
    public static final DeferredItem<ModularShovelItem> SHOVEL = ITEMS.register(
            "shovel",
            () -> new ModularShovelItem(new Item.Properties())
    );
    public static final DeferredItem<ModularPickaxeItem> PICKAXE = ITEMS.register(
            "pickaxe",
            () -> new ModularPickaxeItem(new Item.Properties())
    );
    public static final DeferredItem<ModularAxeItem> AXE = ITEMS.register(
            "axe",
            () -> new ModularAxeItem(new Item.Properties())
    );
    public static final DeferredItem<ModularHoeItem> HOE = ITEMS.register(
            "hoe",
            () -> new ModularHoeItem(new Item.Properties())
    );
    public static final DeferredItem<ModularMattockItem> MATTOCK = ITEMS.register(
            "mattock",
            () -> new ModularMattockItem(new Item.Properties())
    );
    public static final DeferredItem<ModularHelmetItem> MODULAR_HELMET = ITEMS.register(
            "modular_helmet",
            () -> new ModularHelmetItem(ArmorMaterials.LEATHER, new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<ModularChestplateItem> MODULAR_CHESTPLATE = ITEMS.register(
            "modular_chestplate",
            () -> new ModularChestplateItem(ArmorMaterials.LEATHER, new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<ModularLeggingsItem> MODULAR_LEGGINGS = ITEMS.register(
            "modular_leggings",
            () -> new ModularLeggingsItem(ArmorMaterials.LEATHER, new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<ModularBootsItem> MODULAR_BOOTS = ITEMS.register(
            "modular_boots",
            () -> new ModularBootsItem(ArmorMaterials.LEATHER, new Item.Properties().stacksTo(1))
    );

    private ModItems() {
    }

    private static DeferredItem<BlockItem> blockItem(String id, DeferredBlock<? extends Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredItem<BlockItem> leatherStationItem(String id, DeferredBlock<? extends Block> block) {
        return ITEMS.register(id, () -> new LeatherStationBlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

package org.destroyermob.mobstoolforging.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.FireStickItem;
import org.destroyermob.mobstoolforging.item.ModularArmorPartItem;
import org.destroyermob.mobstoolforging.item.ModularBootsItem;
import org.destroyermob.mobstoolforging.item.ModularChestplateItem;
import org.destroyermob.mobstoolforging.item.ModularHelmetItem;
import org.destroyermob.mobstoolforging.item.ModularLeggingsItem;
import org.destroyermob.mobstoolforging.item.ModularAxeItem;
import org.destroyermob.mobstoolforging.item.ModularHoeItem;
import org.destroyermob.mobstoolforging.item.ModularPickaxeItem;
import org.destroyermob.mobstoolforging.item.ModularShovelItem;
import org.destroyermob.mobstoolforging.item.ModularSwordItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
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
    public static final DeferredItem<BlockItem> LAPIDARY_TABLE = ITEMS.register(
            "lapidary_table",
            () -> new BlockItem(ModBlocks.LAPIDARY_TABLE.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> PATTERN_CREATION_STATION = ITEMS.register(
            "pattern_creation_station",
            () -> new BlockItem(ModBlocks.PATTERN_CREATION_STATION.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> TOOLMAKERS_BENCH = ITEMS.register(
            "toolmakers_bench",
            () -> new BlockItem(ModBlocks.TOOLMAKERS_BENCH.get(), new Item.Properties())
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
    public static final DeferredItem<Item> SMITHING_HAMMER = ITEMS.register(
            "smithing_hammer",
            () -> new Item(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> IRON_SMITHING_HAMMER = ITEMS.register(
            "iron_smithing_hammer",
            () -> new Item(new Item.Properties().durability(384))
    );
    public static final DeferredItem<Item> SMITHING_HAMMER_HEAD = ITEMS.register(
            "smithing_hammer_head",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> SCREWDRIVER = ITEMS.register(
            "screwdriver",
            () -> new Item(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> SCREWDRIVER_HEAD = ITEMS.register(
            "screwdriver_head",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> GEM_CUTTERS_KNIFE = ITEMS.register(
            "gem_cutters_knife",
            () -> new Item(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> GEM_CUTTERS_BLADE = ITEMS.register(
            "gem_cutters_blade",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> DIAMOND_POWDER = ITEMS.register(
            "diamond_powder",
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
    public static final DeferredItem<ToolTemplateItem> HELMET_SKULL_PATTERN = ITEMS.register(
            "helmet_skull_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.HELMET_SKULL_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> HELMET_COMB_PATTERN = ITEMS.register(
            "helmet_comb_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.HELMET_COMB_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> HELMET_VISOR_PATTERN = ITEMS.register(
            "helmet_visor_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.HELMET_VISOR_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> CHESTPLATE_BODY_PATTERN = ITEMS.register(
            "chestplate_body_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> LEGGINGS_LEGS_PATTERN = ITEMS.register(
            "leggings_legs_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.LEGGINGS_LEGS_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> LEGGINGS_KNEES_PATTERN = ITEMS.register(
            "leggings_knees_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.LEGGINGS_KNEES_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> LEGGINGS_TASSETS_PATTERN = ITEMS.register(
            "leggings_tassets_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.LEGGINGS_TASSETS_TEMPLATE, new Item.Properties())
    );
    public static final DeferredItem<ToolTemplateItem> BOOTS_FEET_PATTERN = ITEMS.register(
            "boots_feet_pattern",
            () -> new ToolTemplateItem(ToolTypeRegistry.BOOTS_FEET_TEMPLATE, new Item.Properties())
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
    public static final DeferredItem<ModularArmorPartItem> HELMET_SKULL = ITEMS.register(
            ArmorPartData.HELMET_SKULL,
            () -> new ModularArmorPartItem(ArmorPartData.HELMET_SKULL, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> CHESTPLATE_BODY = ITEMS.register(
            ArmorPartData.CHESTPLATE_BODY,
            () -> new ModularArmorPartItem(ArmorPartData.CHESTPLATE_BODY, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> LEGGINGS_LEGS = ITEMS.register(
            ArmorPartData.LEGGINGS_LEGS,
            () -> new ModularArmorPartItem(ArmorPartData.LEGGINGS_LEGS, new Item.Properties())
    );
    public static final DeferredItem<ModularArmorPartItem> BOOTS_FEET = ITEMS.register(
            ArmorPartData.BOOTS_FEET,
            () -> new ModularArmorPartItem(ArmorPartData.BOOTS_FEET, new Item.Properties())
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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

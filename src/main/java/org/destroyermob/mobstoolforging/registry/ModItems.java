package org.destroyermob.mobstoolforging.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.CrudeFlintPickItem;
import org.destroyermob.mobstoolforging.item.ModularAxeItem;
import org.destroyermob.mobstoolforging.item.ModularHoeItem;
import org.destroyermob.mobstoolforging.item.ModularPickaxeItem;
import org.destroyermob.mobstoolforging.item.ModularShovelItem;
import org.destroyermob.mobstoolforging.item.ModularSwordItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.world.CrudeFlintTiers;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;

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
    public static final DeferredItem<Item> SMITHING_HAMMER = ITEMS.register(
            "smithing_hammer",
            () -> new Item(new Item.Properties().durability(128))
    );
    public static final DeferredItem<Item> FLINT_SHARD = ITEMS.register(
            "flint_shard",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<SwordItem> FLINT_KNIFE = ITEMS.register(
            "flint_knife",
            () -> new SwordItem(CrudeFlintTiers.FLINT, new Item.Properties().attributes(SwordItem.createAttributes(CrudeFlintTiers.FLINT, 1.5F, -1.9F)))
    );
    public static final DeferredItem<AxeItem> FLINT_HATCHET = ITEMS.register(
            "flint_hatchet",
            () -> new AxeItem(CrudeFlintTiers.FLINT, new Item.Properties().attributes(AxeItem.createAttributes(CrudeFlintTiers.FLINT, 4.5F, -3.2F)))
    );
    public static final DeferredItem<PickaxeItem> FLINT_PICK = ITEMS.register(
            "flint_pick",
            () -> new CrudeFlintPickItem(CrudeFlintTiers.FLINT, new Item.Properties().attributes(PickaxeItem.createAttributes(CrudeFlintTiers.FLINT, 0.5F, -2.9F)))
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

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

package org.destroyermob.mobstoolforging.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, MobsToolForging.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(ModBlocks.TOOL_FORGE, "Tool Forge");
        addBlock(ModBlocks.LAPIDARY_TABLE, "Lapidary Table");
        addItem(ModItems.SMITHING_HAMMER, "Smithing Hammer");
        addItem(ModItems.SWORD_BLADE, "Sword Blade");
        addItem(ModItems.SHOVEL_HEAD, "Shovel Head");
        addItem(ModItems.PICKAXE_HEAD, "Pickaxe Head");
        addItem(ModItems.AXE_HEAD, "Axe Head");
        addItem(ModItems.HOE_HEAD, "Hoe Head");
        addItem(ModItems.SWORD, "Sword");
        addItem(ModItems.SHOVEL, "Shovel");
        addItem(ModItems.PICKAXE, "Pickaxe");
        addItem(ModItems.AXE, "Axe");
        addItem(ModItems.HOE, "Hoe");
        add("item.mobstoolforging.material_sword_blade", "%s Sword Blade");
        add("item.mobstoolforging.material_shovel_head", "%s Shovel Head");
        add("item.mobstoolforging.material_pickaxe_head", "%s Pickaxe Head");
        add("item.mobstoolforging.material_axe_head", "%s Axe Head");
        add("item.mobstoolforging.material_hoe_head", "%s Hoe Head");
        add("item.mobstoolforging.material_sword", "%s Sword");
        add("item.mobstoolforging.material_shovel", "%s Shovel");
        add("item.mobstoolforging.material_pickaxe", "%s Pickaxe");
        add("item.mobstoolforging.material_axe", "%s Axe");
        add("item.mobstoolforging.material_hoe", "%s Hoe");
        add("material.mobstoolforging.iron", "Iron");
        add("material.mobstoolforging.gold", "Gold");
        add("material.mobstoolforging.copper", "Copper");
        add("material.mobstoolforging.netherite", "Netherite");
        add("material.mobstoolforging.diamond", "Diamond");
        add("material.mobstoolforging.emerald", "Emerald");
        add("material.mobstoolforging.oak", "Oak");
        add("material.mobstoolforging.dark_oak", "Dark Oak");
        add("material.mobstoolforging.blaze", "Blaze");
        add("material.mobstoolforging.breeze", "Breeze");
        add("material.mobstoolforging.leather", "Leather");
        add("material.mobstoolforging.amethyst", "Amethyst");
        add("material.mobstoolforging.nether", "Nether");
        add("material.mobstoolforging.sculk", "Sculk");
        add("screen.mobstoolforging.tool_forge_templates", "Tool Templates");
        add("forge_template.mobstoolforging.sword_blade", "Sword Blade");
        add("forge_template.mobstoolforging.shovel_head", "Shovel Head");
        add("forge_template.mobstoolforging.pickaxe_head", "Pickaxe Head");
        add("forge_template.mobstoolforging.axe_head", "Axe Head");
        add("forge_template.mobstoolforging.hoe_head", "Hoe Head");
        add("message.mobstoolforging.select_template", "Shift right-click to choose a template.");
        add("message.mobstoolforging.template_selected", "Selected %s.");
        add("message.mobstoolforging.forge_busy", "Finish or clear the current work first.");
        add("message.mobstoolforging.materials_full", "The station already has enough material.");
        add("message.mobstoolforging.need_materials", "Place the required matching materials before working.");
        add("message.mobstoolforging.mixed_materials", "Finish this part with the same material.");
        add("message.mobstoolforging.use_lapidary_table", "Gem heads are shaped on a Lapidary Table.");
        add("message.mobstoolforging.use_tool_forge", "Metal heads are forged on a Tool Forge.");
        add("message.mobstoolforging.complete", "The tool part is ready.");
        add("message.mobstoolforging.inventory_full", "Make room before taking the finished part.");
        add("message.mobstoolforging.station_status", "Materials: %s/%s, Progress: %s/%s");
    }
}

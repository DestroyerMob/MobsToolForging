package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class ModTags {
    private ModTags() {
    }

    public static final class Blocks {
        public static final TagKey<Block> PATTERN_RACKS = blockTag("workstations/pattern_racks");
        public static final TagKey<Block> TOOLMAKERS_BENCHES = blockTag("workstations/toolmakers_benches");
        public static final TagKey<Block> SAWMILLS = blockTag("workstations/sawmills");
        public static final TagKey<Block> LEATHER_STATIONS = blockTag("workstations/leather_stations");
        public static final TagKey<Block> DRYING_RACKS = blockTag("workstations/drying_racks");
        public static final TagKey<Block> FOUNDRY_STRUCTURE_BLOCKS = blockTag("foundry_structure_blocks");
        public static final TagKey<Block> CARRY_ON_BLOCK_BLACKLIST = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("carryon", "block_blacklist")
        );
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("neoforge", "relocation_not_supported")
        );

        private Blocks() {
        }

        private static TagKey<Block> blockTag(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path));
        }
    }

    public static final class Items {
        public static final TagKey<Item> MATERIALS = itemTag("materials");
        public static final TagKey<Item> MATERIALS_METALS = itemTag("materials/metals");
        public static final TagKey<Item> MATERIALS_GEMS = itemTag("materials/gems");
        public static final TagKey<Item> GEMS_AMETHYST = commonItemTag("gems/amethyst");
        public static final TagKey<Item> GEMS_RUBY = commonItemTag("gems/ruby");
        public static final TagKey<Item> GEMS_SAPPHIRE = commonItemTag("gems/sapphire");
        public static final TagKey<Item> GEMS_TOPAZ = commonItemTag("gems/topaz");
        public static final TagKey<Item> LEGACY_FORGE_GEMS_AMETHYST = forgeItemTag("gems/amethyst");
        public static final TagKey<Item> LEGACY_FORGE_GEMS_RUBY = forgeItemTag("gems/ruby");
        public static final TagKey<Item> LEGACY_FORGE_GEMS_SAPPHIRE = forgeItemTag("gems/sapphire");
        public static final TagKey<Item> LEGACY_FORGE_GEMS_TOPAZ = forgeItemTag("gems/topaz");
        public static final TagKey<Item> PARTS = itemTag("parts");
        public static final TagKey<Item> PART_HEADS = itemTag("parts/heads");
        public static final TagKey<Item> PART_HANDLES = itemTag("parts/handle");
        public static final TagKey<Item> PART_GUARDS = itemTag("parts/guards");
        public static final TagKey<Item> PART_SWORD_BLADES = itemTag("parts/sword_blade");
        public static final TagKey<Item> PART_SWORD_GUARDS = itemTag("parts/sword_guard");
        public static final TagKey<Item> PART_SHOVEL_HEADS = itemTag("parts/shovel_head");
        public static final TagKey<Item> PART_PICKAXE_HEADS = itemTag("parts/pickaxe_head");
        public static final TagKey<Item> PART_AXE_HEADS = itemTag("parts/axe_head");
        public static final TagKey<Item> PART_HOE_HEADS = itemTag("parts/hoe_head");
        public static final TagKey<Item> PART_COOKING_KNIFE_HEADS = itemTag("parts/cooking_knife_head");
        public static final TagKey<Item> PART_SCREWDRIVER_HEADS = itemTag("parts/screwdriver_head");
        public static final TagKey<Item> PART_GEM_CUTTERS_BLADES = itemTag("parts/gem_cutters_blade");
        public static final TagKey<Item> PART_CROSSBOW_BODIES = itemTag("parts/crossbow_body");
        public static final TagKey<Item> PART_CROSSBOW_LIMBS = itemTag("parts/crossbow_limbs");
        public static final TagKey<Item> ARMOR_PARTS = itemTag("parts/armor");
        public static final TagKey<Item> PART_HELMET_CHAINMAILS = itemTag("parts/helmet_chainmail");
        public static final TagKey<Item> PART_HELMET_PLATES = itemTag("parts/helmet_plate");
        public static final TagKey<Item> PART_CHESTPLATE_CHAINMAILS = itemTag("parts/chestplate_chainmail");
        public static final TagKey<Item> PART_CHESTPLATE_BODIES = itemTag("parts/chestplate_body");
        public static final TagKey<Item> PART_LEGGINGS_CHAINMAILS = itemTag("parts/leggings_chainmail");
        public static final TagKey<Item> PART_LEGGINGS_PLATES = itemTag("parts/leggings_plate");
        public static final TagKey<Item> PART_BOOTS_CHAINMAILS = itemTag("parts/boots_chainmail");
        public static final TagKey<Item> PART_BOOTS_PLATES = itemTag("parts/boots_plate");
        public static final TagKey<Item> TOOL_HANDLES = itemTag("tool_handles");
        public static final TagKey<Item> CROSSBOW_STRINGS = itemTag("crossbow_strings");
        public static final TagKey<Item> LAPIDARY_ABRASIVES = itemTag("lapidary_abrasives");
        public static final TagKey<Item> LAPIDARY_ABRASIVES_DIAMOND = itemTag("lapidary_abrasives/diamond");
        public static final TagKey<Item> KNAPPING_TOOLS = itemTag("knapping_tools");
        public static final TagKey<Item> HAMMER_STONES = itemTag("hammer_stones");
        public static final TagKey<Item> PATTERN_RACKS = itemTag("workstations/pattern_racks");
        public static final TagKey<Item> TOOLMAKERS_BENCHES = itemTag("workstations/toolmakers_benches");
        public static final TagKey<Item> SAWMILLS = itemTag("workstations/sawmills");
        public static final TagKey<Item> LEATHER_STATIONS = itemTag("workstations/leather_stations");
        public static final TagKey<Item> DRYING_RACKS = itemTag("workstations/drying_racks");
        public static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = commonItemTag("hidden_from_recipe_viewers");

        private Items() {
        }

        private static TagKey<Item> itemTag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path));
        }

        private static TagKey<Item> commonItemTag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Item> forgeItemTag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", path));
        }
    }

    public static final class Fluids {
        public static final TagKey<Fluid> HEATING_FLUIDS = TagKey.create(
                Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "heating_fluids")
        );

        private Fluids() {
        }
    }

}

package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class ModTags {
    private ModTags() {
    }

    public static final class Items {
        public static final TagKey<Item> MATERIALS = itemTag("materials");
        public static final TagKey<Item> MATERIALS_METALS = itemTag("materials/metals");
        public static final TagKey<Item> MATERIALS_GEMS = itemTag("materials/gems");
        public static final TagKey<Item> GEMS_RUBY = commonItemTag("gems/ruby");
        public static final TagKey<Item> GEMS_SAPPHIRE = commonItemTag("gems/sapphire");
        public static final TagKey<Item> LEGACY_FORGE_GEMS_RUBY = forgeItemTag("gems/ruby");
        public static final TagKey<Item> LEGACY_FORGE_GEMS_SAPPHIRE = forgeItemTag("gems/sapphire");
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
        public static final TagKey<Item> PART_SCREWDRIVER_HEADS = itemTag("parts/screwdriver_head");
        public static final TagKey<Item> PART_GEM_CUTTERS_BLADES = itemTag("parts/gem_cutters_blade");
        public static final TagKey<Item> ARMOR_PARTS = itemTag("parts/armor");
        public static final TagKey<Item> PART_HELMET_SKULLS = itemTag("parts/helmet_skull");
        public static final TagKey<Item> PART_CHESTPLATE_CHAINMAILS = itemTag("parts/chestplate_chainmail");
        public static final TagKey<Item> PART_CHESTPLATE_BODIES = itemTag("parts/chestplate_body");
        public static final TagKey<Item> PART_LEGGINGS_LEGS = itemTag("parts/leggings_legs");
        public static final TagKey<Item> PART_BOOTS_FEET = itemTag("parts/boots_feet");
        public static final TagKey<Item> TOOL_HANDLES = itemTag("tool_handles");
        public static final TagKey<Item> LAPIDARY_ABRASIVES = itemTag("lapidary_abrasives");
        public static final TagKey<Item> KNAPPING_TOOLS = itemTag("knapping_tools");

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

}

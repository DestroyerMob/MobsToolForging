package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class ModTags {
    private ModTags() {
    }

    public static final class Items {
        public static final TagKey<Item> MATERIALS = itemTag("materials");
        public static final TagKey<Item> MATERIALS_METALS = itemTag("materials/metals");
        public static final TagKey<Item> MATERIALS_GEMS = itemTag("materials/gems");
        public static final TagKey<Item> TOOL_HANDLES = itemTag("tool_handles");
        public static final TagKey<Item> TOOL_BINDINGS = itemTag("tool_bindings");
        public static final TagKey<Item> TOOL_WRAPS = itemTag("tool_wraps");
        public static final TagKey<Item> TOOL_FOCI = itemTag("tool_foci");
        public static final TagKey<Item> TREATMENT_CATALYSTS = itemTag("treatment_catalysts");

        private Items() {
        }

        private static TagKey<Item> itemTag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> INCORRECT_FOR_FLINT_TOOL = blockTag("incorrect_for_flint_tool");

        private Blocks() {
        }

        private static TagKey<Block> blockTag(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path));
        }
    }
}

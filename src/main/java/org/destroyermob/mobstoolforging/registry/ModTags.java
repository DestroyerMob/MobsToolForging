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
        public static final TagKey<Item> TOOL_HANDLES = itemTag("tool_handles");
        public static final TagKey<Item> TOOL_BINDINGS = itemTag("tool_bindings");
        public static final TagKey<Item> TOOL_WRAPS = itemTag("tool_wraps");
        public static final TagKey<Item> TOOL_FOCI = itemTag("tool_foci");
        public static final TagKey<Item> TREATMENT_CATALYSTS = itemTag("treatment_catalysts");
        public static final TagKey<Item> LAPIDARY_ABRASIVES = itemTag("lapidary_abrasives");

        private Items() {
        }

        private static TagKey<Item> itemTag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path));
        }
    }

}

package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;

public final class LapidaryAbrasives {
    private LapidaryAbrasives() {
    }

    public static boolean isAbrasive(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ModTags.Items.LAPIDARY_ABRASIVES) || stack.is(configuredDiamondAbrasive()));
    }

    private static Item configuredDiamondAbrasive() {
        ResourceLocation itemId = ResourceLocation.tryParse(MobsToolForgingConfig.DIAMOND_ABRASIVE_ITEM.get());
        if (itemId == null) {
            return ModItems.DIAMOND_POWDER.get();
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == Items.AIR ? ModItems.DIAMOND_POWDER.get() : item;
    }
}

package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class FoundryCastingOutputs {
    private FoundryCastingOutputs() {
    }

    public static Optional<ItemStack> output(ResourceLocation material, boolean basin) {
        Item item;
        if (MaterialCatalog.IRON.equals(material)) {
            item = basin ? Items.IRON_BLOCK : Items.IRON_INGOT;
        } else if (MaterialCatalog.GOLD.equals(material)) {
            item = basin ? Items.GOLD_BLOCK : Items.GOLD_INGOT;
        } else if (MaterialCatalog.COPPER.equals(material)) {
            item = basin ? Items.COPPER_BLOCK : Items.COPPER_INGOT;
        } else if (MaterialCatalog.NETHERITE.equals(material)) {
            item = basin ? Items.NETHERITE_BLOCK : Items.NETHERITE_INGOT;
        } else if (MaterialCatalog.STEEL.equals(material) && !basin) {
            item = ModItems.STEEL_INGOT.get();
        } else if (MaterialCatalog.BRONZE.equals(material) && !basin) {
            item = ModItems.BRONZE_INGOT.get();
        } else {
            return Optional.empty();
        }
        return Optional.of(new ItemStack(item));
    }
}

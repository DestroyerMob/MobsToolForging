package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record WorldAssemblyJeiRecipe(
        ResourceLocation id,
        Kind kind,
        List<ItemStack> lowerBlocks,
        List<ItemStack> upperBlocks,
        List<ItemStack> activationItems,
        ItemStack output
) {
    public WorldAssemblyJeiRecipe {
        lowerBlocks = List.copyOf(lowerBlocks);
        upperBlocks = List.copyOf(upperBlocks);
        activationItems = List.copyOf(activationItems);
        output = output.copy();
    }

    public enum Kind {
        ANVIL,
        SAWMILL,
        LEATHER_STATION,
        LAPIDARY_TABLE,
        DIAMOND_SAW
    }
}

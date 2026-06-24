package org.destroyermob.mobstoolforging.integration.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record PatternCreationJeiRecipe(
        ResourceLocation id,
        ItemStack station,
        ItemStack paper,
        ItemStack output
) {
}

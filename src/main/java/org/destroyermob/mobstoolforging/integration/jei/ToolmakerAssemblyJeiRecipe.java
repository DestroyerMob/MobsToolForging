package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ToolmakerAssemblyJeiRecipe(
        ResourceLocation id,
        List<ItemStack> parts,
        ItemStack output
) {
    public ToolmakerAssemblyJeiRecipe {
        parts = List.copyOf(parts.stream().map(ItemStack::copy).toList());
        output = output.copy();
    }
}

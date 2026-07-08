package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.DryingRecipe;

public record DryingJeiRecipe(
        ResourceLocation id,
        ItemStack station,
        List<ItemStack> inputs,
        ItemStack output,
        int ticks,
        DryingRecipe sourceRecipe
) {
}

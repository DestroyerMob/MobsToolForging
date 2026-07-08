package org.destroyermob.mobstoolforging.world;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record HeatingDisplayRecipe(
        ResourceLocation id,
        HeatingSource source,
        List<ItemStack> inputs,
        ItemStack output,
        int ticks,
        float targetTemperature,
        boolean workable,
        HeatingRecipe sourceRecipe
) {
    public HeatingDisplayRecipe {
        inputs = List.copyOf(inputs.stream().map(ItemStack::copy).toList());
        output = output.copy();
        ticks = Math.max(1, ticks);
        targetTemperature = Math.max(0.0F, Math.min(1.0F, targetTemperature));
    }
}

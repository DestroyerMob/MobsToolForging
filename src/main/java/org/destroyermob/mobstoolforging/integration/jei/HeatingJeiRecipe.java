package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.HeatingDisplayRecipe;
import org.destroyermob.mobstoolforging.world.HeatingSource;

public record HeatingJeiRecipe(
        ResourceLocation id,
        HeatingSource source,
        List<ItemStack> inputs,
        ItemStack output,
        int ticks,
        float targetTemperature,
        HeatingDisplayRecipe sourceRecipe
) {
}

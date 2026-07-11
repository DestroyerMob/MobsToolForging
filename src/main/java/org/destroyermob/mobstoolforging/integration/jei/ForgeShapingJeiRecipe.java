package org.destroyermob.mobstoolforging.integration.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;

import java.util.List;

public record ForgeShapingJeiRecipe(
        ResourceLocation id,
        ForgeTemplateDefinition template,
        ResourceLocation materialId,
        List<ItemStack> stations,
        ItemStack pattern,
        ItemStack material,
        ItemStack target,
        ItemStack catalyst,
        ItemStack hammer,
        ItemStack output,
        int requiredHits,
        int minimumHammerLevel
) {
    public ForgeShapingJeiRecipe {
        stations = List.copyOf(stations.stream().map(ItemStack::copy).toList());
        pattern = pattern.copy();
        material = material.copy();
        target = target.copy();
        catalyst = catalyst.copy();
        hammer = hammer.copy();
        output = output.copy();
    }
}

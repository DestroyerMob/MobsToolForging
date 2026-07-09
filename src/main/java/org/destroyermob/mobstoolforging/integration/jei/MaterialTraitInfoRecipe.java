package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record MaterialTraitInfoRecipe(
        ResourceLocation id,
        ResourceLocation materialId,
        Component materialName,
        List<ItemStack> inputs,
        List<TraitEntry> traits
) {
    public MaterialTraitInfoRecipe {
        inputs = inputs.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        traits = List.copyOf(traits);
    }

    public record TraitEntry(ResourceLocation traitId, Component source) {
    }
}

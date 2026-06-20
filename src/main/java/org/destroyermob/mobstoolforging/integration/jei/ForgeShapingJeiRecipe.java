package org.destroyermob.mobstoolforging.integration.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public record ForgeShapingJeiRecipe(
        ResourceLocation id,
        WorkstationKind workstationKind,
        ForgeTemplateDefinition template,
        ResourceLocation materialId,
        ItemStack station,
        ItemStack pattern,
        ItemStack material,
        ItemStack catalyst,
        ItemStack hammer,
        ItemStack output,
        int requiredHits,
        int minimumHammerLevel
) {
}

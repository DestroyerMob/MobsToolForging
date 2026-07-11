package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;

public record LapidaryCoatingJeiRecipe(
        ResourceLocation id,
        ForgeTemplateDefinition template,
        ResourceLocation coatingMaterialId,
        ItemStack station,
        List<ItemStack> baseParts,
        ItemStack coatingMaterial,
        ItemStack abrasive,
        ItemStack workTool,
        List<ItemStack> outputs,
        int requiredHits
) {
    public LapidaryCoatingJeiRecipe {
        station = station.copy();
        baseParts = List.copyOf(baseParts.stream().map(ItemStack::copy).toList());
        coatingMaterial = coatingMaterial.copy();
        abrasive = abrasive.copy();
        workTool = workTool.copy();
        outputs = List.copyOf(outputs.stream().map(ItemStack::copy).toList());
    }
}

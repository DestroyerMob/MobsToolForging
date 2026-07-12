package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public record StationWorkJeiRecipe(
        ResourceLocation id,
        WorkstationKind workstationKind,
        ItemStack station,
        List<ItemStack> inputs,
        List<ItemStack> secondaryInputs,
        ItemStack hammer,
        ItemStack output,
        int requiredHits,
        int minimumHammerLevel,
        StationWorkRecipe source
) {
}

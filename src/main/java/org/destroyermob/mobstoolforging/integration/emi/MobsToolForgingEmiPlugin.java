package org.destroyermob.mobstoolforging.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.integration.WorldInteractionDisplayRecipes;

@EmiEntrypoint
public final class MobsToolForgingEmiPlugin implements EmiPlugin {
    private static final ResourceLocation LEGACY_WORLD_ASSEMBLY_CATEGORY =
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID + "_jei", "world_assembly");

    @Override
    public void register(EmiRegistry registry) {
        // JEMI imports the JEI fallback category. Remove those copies and publish
        // the same displays through EMI's native World Interaction category.
        registry.removeRecipes(recipe -> LEGACY_WORLD_ASSEMBLY_CATEGORY.equals(recipe.getCategory().getId()));
        WorldInteractionDisplayRecipes.recipes().stream()
                .map(ShapedWorldInteractionEmiRecipe::new)
                .forEach(registry::addRecipe);
    }
}

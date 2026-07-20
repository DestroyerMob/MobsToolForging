package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class FoundryFuelRegistry {
    public static final float LAVA_TEMPERATURE_C = 2000.0F;
    public static final int DEFAULT_BATCH_MB = 1000;
    public static final int DEFAULT_BURN_TICKS = 2400;
    private static Map<ResourceLocation, FoundryFuelRecipe> recipes = initialRecipes();

    private FoundryFuelRegistry() {
    }

    public static synchronized void replace(Map<ResourceLocation, FoundryFuelRecipe> loaded) {
        recipes = Map.copyOf(loaded);
    }

    public static synchronized Map<ResourceLocation, FoundryFuelRecipe> snapshot() {
        return new LinkedHashMap<>(recipes);
    }

    public static List<FoundryFuelRecipe> recipes() {
        return List.copyOf(recipes.values());
    }

    public static Optional<FoundryFuelRecipe> find(FluidStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        List<FoundryFuelRecipe> ordered = new ArrayList<>(recipes.values());
        ordered.sort(Comparator.comparing((FoundryFuelRecipe recipe) -> !recipe.input().isFluid()));
        return ordered.stream().filter(recipe -> recipe.matches(stack)).findFirst();
    }

    private static Map<ResourceLocation, FoundryFuelRecipe> initialRecipes() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "lava");
        ResourceLocation lavaId = ResourceLocation.withDefaultNamespace("lava");
        return Map.of(id, new FoundryFuelRecipe(
                id,
                FoundryFuelRecipe.Input.fluid(lavaId),
                LAVA_TEMPERATURE_C,
                DEFAULT_BATCH_MB,
                DEFAULT_BURN_TICKS
        ));
    }
}

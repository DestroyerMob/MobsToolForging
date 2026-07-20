package org.destroyermob.mobstoolforging.world;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.resources.ResourceLocation;

public final class FoundryAlloyRegistry {
    private static Map<ResourceLocation, FoundryAlloyRecipe> recipes = Map.of();

    private FoundryAlloyRegistry() {
    }

    public static synchronized void replace(Map<ResourceLocation, FoundryAlloyRecipe> loaded) {
        recipes = Map.copyOf(loaded);
    }

    public static Map<ResourceLocation, FoundryAlloyRecipe> snapshot() {
        return Map.copyOf(recipes);
    }

    public static List<FoundryAlloyRecipe> recipes() {
        return recipes.values().stream().sorted(Comparator.comparing(recipe -> recipe.id().toString())).toList();
    }

    public static Optional<FoundryAlloyRecipe> findCraftable(ToIntFunction<ResourceLocation> availableAmount) {
        return recipes().stream().filter(recipe -> recipe.craftableBatches(availableAmount) > 0).findFirst();
    }

    public static Optional<FoundryAlloyRecipe> findByResult(ResourceLocation result) {
        return recipes().stream().filter(recipe -> recipe.result().equals(result)).findFirst();
    }
}

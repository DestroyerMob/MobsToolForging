package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class FoundryCastRegistry {
    private static Map<ResourceLocation, FoundryCastRecipe> recipes = Map.of();

    private FoundryCastRegistry() {
    }

    public static synchronized void replace(Map<ResourceLocation, FoundryCastRecipe> loaded) {
        recipes = Map.copyOf(loaded);
    }

    public static List<FoundryCastRecipe> recipes() {
        return List.copyOf(recipes.values());
    }

    public static Optional<FoundryCastRecipe> findForInput(ItemStack stack) {
        List<FoundryCastRecipe> ordered = new ArrayList<>(recipes.values());
        ordered.sort(Comparator.comparing(recipe -> !recipe.input().isItem()));
        return ordered.stream().filter(recipe -> recipe.matchesInput(stack)).findFirst();
    }

    public static Optional<FoundryCastRecipe> findForCast(ItemStack stack) {
        return recipes.values().stream().filter(recipe -> recipe.matchesCast(stack)).findFirst();
    }
}

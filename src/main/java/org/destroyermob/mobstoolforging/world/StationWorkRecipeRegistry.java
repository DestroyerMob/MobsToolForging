package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class StationWorkRecipeRegistry {
    private static volatile Map<ResourceLocation, StationWorkRecipe> recipes = Map.of();

    private StationWorkRecipeRegistry() {
    }

    public static void replace(Map<ResourceLocation, StationWorkRecipe> loadedRecipes) {
        recipes = Map.copyOf(new LinkedHashMap<>(loadedRecipes));
    }

    public static Optional<StationWorkRecipe> recipe(ResourceLocation id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public static Optional<StationWorkRecipe> findStartRecipe(WorkstationKind kind, @Nullable ResourceLocation selectedPattern, ItemStack stack) {
        return recipes.values().stream()
                .filter(recipe -> recipe.canStart(kind, selectedPattern, stack))
                .findFirst();
    }

    public static Optional<StationWorkRecipe> findPatternRecipe(WorkstationKind kind, @Nullable ResourceLocation selectedPattern) {
        if (selectedPattern == null) {
            return Optional.empty();
        }
        return recipes.values().stream()
                .filter(recipe -> recipe.workstationKind() == kind)
                .filter(recipe -> recipe.patternId().filter(selectedPattern::equals).isPresent())
                .findFirst();
    }

    public static boolean hasStartRecipe(WorkstationKind kind, ResourceLocation patternId) {
        return recipes.values().stream()
                .anyMatch(recipe -> recipe.workstationKind() == kind
                        && recipe.patternId().filter(patternId::equals).isPresent());
    }

    public static List<StationWorkRecipe> recipes() {
        return List.copyOf(recipes.values());
    }
}

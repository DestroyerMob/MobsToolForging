package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class GroundAssemblyRecipeRegistry {
    private static volatile Map<ResourceLocation, GroundAssemblyRecipe> recipes = Map.of();

    private GroundAssemblyRecipeRegistry() {
    }

    public static void replace(Map<ResourceLocation, GroundAssemblyRecipe> loadedRecipes) {
        recipes = Map.copyOf(new LinkedHashMap<>(loadedRecipes));
    }

    public static Optional<GroundAssemblyRecipe> recipe(ResourceLocation id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public static boolean canStart(ItemStack stack) {
        return recipes.values().stream().anyMatch(recipe -> recipe.canStart(stack));
    }

    public static boolean canAccept(List<ItemStack> existingStacks, ItemStack stack) {
        return recipes.values().stream().anyMatch(recipe -> recipe.canAccept(existingStacks, stack));
    }

    public static ItemStack assemble(List<ItemStack> stacks, HolderLookup.Provider registries) {
        for (GroundAssemblyRecipe recipe : recipes.values()) {
            ItemStack output = recipe.assemble(stacks, registries);
            if (!output.isEmpty()) {
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<GroundAssemblyRecipe> recipes() {
        return List.copyOf(recipes.values());
    }
}

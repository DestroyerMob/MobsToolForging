package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DryingRecipeRegistry {
    private static volatile Map<ResourceLocation, DryingRecipe> recipes = Map.of();

    private DryingRecipeRegistry() {
    }

    public static void replace(Map<ResourceLocation, DryingRecipe> loadedRecipes) {
        recipes = Map.copyOf(loadedRecipes);
    }

    public static Optional<DryingRecipe> recipe(ResourceLocation id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public static Optional<DryingRecipe> find(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        for (DryingRecipe recipe : recipes.values()) {
            if (recipe.matches(stack)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    public static List<DryingRecipe> recipes() {
        return List.copyOf(recipes.values());
    }

    public static List<ItemStack> inputStacks(DryingRecipe.Input input) {
        if (input.itemId().isPresent()) {
            Item item = BuiltInRegistries.ITEM.get(input.itemId().get());
            return item == Items.AIR ? List.of() : List.of(new ItemStack(item, input.count()));
        }
        if (input.tag().isPresent()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(input.tag().get())) {
                Item item = holder.value();
                if (item != Items.AIR) {
                    stacks.add(new ItemStack(item, input.count()));
                }
            }
            return stacks;
        }
        return List.of();
    }
}

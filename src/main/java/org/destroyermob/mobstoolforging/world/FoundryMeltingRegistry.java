package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class FoundryMeltingRegistry {
    private static Map<ResourceLocation, FoundryMeltingRecipe> recipes = Map.of();

    private FoundryMeltingRegistry() {
    }

    public static synchronized void replace(Map<ResourceLocation, FoundryMeltingRecipe> loaded) {
        recipes = Map.copyOf(loaded);
    }

    public static List<FoundryMeltingRecipe> recipes() {
        return List.copyOf(recipes.values());
    }

    public static synchronized Map<ResourceLocation, FoundryMeltingRecipe> snapshot() {
        return new LinkedHashMap<>(recipes);
    }

    public static Optional<FoundryMeltingRecipe> find(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        List<FoundryMeltingRecipe> ordered = new ArrayList<>(recipes.values());
        ordered.sort(Comparator.comparing((FoundryMeltingRecipe recipe) -> !recipe.input().isItem()));
        for (FoundryMeltingRecipe recipe : ordered) {
            if (recipe.matches(stack)) {
                return Optional.of(recipe);
            }
        }
        return fallback(stack);
    }

    private static Optional<FoundryMeltingRecipe> fallback(ItemStack stack) {
        Optional<ResourceLocation> material;
        if (stack.is(Items.NETHERITE_SCRAP)) {
            material = Optional.of(MaterialCatalog.NETHERITE);
        } else {
            material = MaterialCatalog.resolve(stack)
                    .filter(definition -> definition.category() == MaterialCategory.METAL)
                    .map(ToolMaterialDefinition::id);
        }
        if (material.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return Optional.of(new FoundryMeltingRecipe(
                ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "fallback/" + itemId.getNamespace() + "/" + itemId.getPath()),
                FoundryMeltingRecipe.Input.item(itemId),
                material.get(),
                FoundryForgeBlockEntity.INGOT_MB,
                FoundryForgeBlockEntity.DEFAULT_MELT_TICKS
        ));
    }
}

package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class DryingRecipeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final int DEFAULT_DRYING_TICKS = 400;

    public DryingRecipeReloadListener() {
        super(GSON, "mobstoolforging/drying");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, DryingRecipe> loaded = new LinkedHashMap<>();
        Map<ResourceLocation, JsonElement> accepted = new LinkedHashMap<>();
        recipes.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "drying recipe")));
                accepted.put(id, element);
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid drying recipe {}.", id, exception);
            }
        });
        DryingRecipeRegistry.replace(loaded);
        GameplayRegistrySyncStore.capture(GameplayRegistrySyncStore.Section.DRYING, accepted);
        MobsToolForging.LOGGER.info("Loaded {} drying recipe(s).", loaded.size());
    }

    static void applySynchronizedData(Map<ResourceLocation, JsonElement> recipes) {
        Map<ResourceLocation, DryingRecipe> loaded = new LinkedHashMap<>();
        recipes.forEach((id, element) -> loaded.put(id, parse(id,
                GsonHelper.convertToJsonObject(element, "drying recipe"))));
        DryingRecipeRegistry.replace(loaded);
    }

    private static DryingRecipe parse(ResourceLocation id, JsonObject json) {
        DryingRecipe.Input input = parseInput(GsonHelper.getAsJsonObject(json, "input"));
        ItemStack output = parseOutput(GsonHelper.getAsJsonObject(json, "output"));
        int ticks = Math.max(1, GsonHelper.getAsInt(json, "ticks", DEFAULT_DRYING_TICKS));
        return new DryingRecipe(id, input, output, ticks);
    }

    private static DryingRecipe.Input parseInput(JsonObject input) {
        int count = Math.max(1, GsonHelper.getAsInt(input, "count", 1));
        if (input.has("item")) {
            return DryingRecipe.Input.item(ResourceLocation.parse(GsonHelper.getAsString(input, "item")), count);
        }
        if (input.has("tag")) {
            return DryingRecipe.Input.tag(ResourceLocation.parse(GsonHelper.getAsString(input, "tag")), count);
        }
        throw new IllegalArgumentException("Drying input needs an item or tag");
    }

    private static ItemStack parseOutput(JsonObject output) {
        ResourceLocation itemId = ResourceLocation.parse(output.has("id") ? GsonHelper.getAsString(output, "id") : GsonHelper.getAsString(output, "item"));
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Unknown output item " + itemId);
        }
        return new ItemStack(item, Math.max(1, GsonHelper.getAsInt(output, "count", 1)));
    }
}

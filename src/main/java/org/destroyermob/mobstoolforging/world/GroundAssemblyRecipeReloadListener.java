package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class GroundAssemblyRecipeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public GroundAssemblyRecipeReloadListener() {
        super(GSON, "mobstoolforging/ground_assembly");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GroundAssemblyRecipe> loaded = new LinkedHashMap<>();
        Map<ResourceLocation, JsonElement> accepted = new LinkedHashMap<>();
        recipes.forEach((id, element) -> {
            try {
                loaded.put(id, GroundAssemblyRecipe.fromJson(id, GsonHelper.convertToJsonObject(element, "ground assembly recipe")));
                accepted.put(id, element);
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid ground assembly recipe {}.", id, exception);
            }
        });
        GroundAssemblyRecipeRegistry.replace(loaded);
        GameplayRegistrySyncStore.capture(GameplayRegistrySyncStore.Section.GROUND_ASSEMBLY, accepted);
        MobsToolForging.LOGGER.info("Loaded {} ground assembly recipe(s).", loaded.size());
    }

    static void applySynchronizedData(Map<ResourceLocation, JsonElement> recipes) {
        Map<ResourceLocation, GroundAssemblyRecipe> loaded = new LinkedHashMap<>();
        recipes.forEach((id, element) -> loaded.put(id, GroundAssemblyRecipe.fromJson(id,
                GsonHelper.convertToJsonObject(element, "ground assembly recipe"))));
        GroundAssemblyRecipeRegistry.replace(loaded);
    }
}

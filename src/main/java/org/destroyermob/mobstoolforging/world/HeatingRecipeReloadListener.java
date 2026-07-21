package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class HeatingRecipeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public HeatingRecipeReloadListener() {
        super(GSON, "mobstoolforging/heating");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, HeatingRecipe> loaded = new LinkedHashMap<>();
        Map<ResourceLocation, JsonElement> accepted = new LinkedHashMap<>();
        recipes.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "heating recipe")));
                accepted.put(id, element);
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid heating recipe {}.", id, exception);
            }
        });
        HeatingRecipeRegistry.replace(loaded);
        GameplayRegistrySyncStore.capture(GameplayRegistrySyncStore.Section.HEATING, accepted);
        MobsToolForging.LOGGER.info("Loaded {} heating recipe(s).", loaded.size());
    }

    static void applySynchronizedData(Map<ResourceLocation, JsonElement> recipes) {
        Map<ResourceLocation, HeatingRecipe> loaded = new LinkedHashMap<>();
        recipes.forEach((id, element) -> loaded.put(id, parse(id,
                GsonHelper.convertToJsonObject(element, "heating recipe"))));
        HeatingRecipeRegistry.replace(loaded);
    }

    private static HeatingRecipe parse(ResourceLocation id, JsonObject json) {
        HeatingRecipe.Input input = parseInput(GsonHelper.getAsJsonObject(json, "input"));
        EnumSet<HeatingSource> sources = parseSources(json);
        float targetTemperature = parseTargetTemperature(json, sources);
        int ticks = Math.max(1, GsonHelper.getAsInt(json, "ticks", HeatingRecipeRegistry.defaultHeatTicks(primarySource(sources), targetTemperature)));
        boolean workable = GsonHelper.getAsBoolean(json, "workable", true);
        return new HeatingRecipe(id, input, sources, targetTemperature, ticks, workable);
    }

    private static HeatingRecipe.Input parseInput(JsonObject input) {
        int count = Math.max(1, GsonHelper.getAsInt(input, "count", 1));
        if (input.has("item")) {
            return HeatingRecipe.Input.item(ResourceLocation.parse(GsonHelper.getAsString(input, "item")), count);
        }
        if (input.has("tag")) {
            return HeatingRecipe.Input.tag(ResourceLocation.parse(GsonHelper.getAsString(input, "tag")), count);
        }
        throw new IllegalArgumentException("Heating input needs an item or tag");
    }

    private static EnumSet<HeatingSource> parseSources(JsonObject json) {
        EnumSet<HeatingSource> sources = EnumSet.noneOf(HeatingSource.class);
        if (json.has("sources")) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "sources");
            for (JsonElement element : array) {
                addSource(sources, element.getAsString());
            }
        } else if (json.has("source")) {
            addSource(sources, GsonHelper.getAsString(json, "source"));
        } else {
            sources = EnumSet.allOf(HeatingSource.class);
        }
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("Heating recipe needs at least one source");
        }
        return sources;
    }

    private static void addSource(EnumSet<HeatingSource> sources, String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("all") || normalized.equals("both")) {
            sources.addAll(EnumSet.allOf(HeatingSource.class));
            return;
        }
        sources.add(HeatingSource.parse(value));
    }

    private static float parseTargetTemperature(JsonObject json, EnumSet<HeatingSource> sources) {
        if (json.has("target_temperature")) {
            return clamp(GsonHelper.getAsFloat(json, "target_temperature"));
        }
        if (json.has("temperature")) {
            return clamp(GsonHelper.getAsFloat(json, "temperature"));
        }
        if (json.has("target_heat")) {
            return HeatLevel.parse(GsonHelper.getAsString(json, "target_heat"), HeatLevel.HOT).temperature();
        }
        if (json.has("heat")) {
            return HeatLevel.parse(GsonHelper.getAsString(json, "heat"), HeatLevel.HOT).temperature();
        }
        return sources.size() == 1 && sources.contains(HeatingSource.CAMPFIRE)
                ? HeatLevel.LOW.temperature()
                : HeatLevel.HOT.temperature();
    }

    private static HeatingSource primarySource(EnumSet<HeatingSource> sources) {
        return sources.contains(HeatingSource.FORGE) ? HeatingSource.FORGE : HeatingSource.CAMPFIRE;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class FoundryAlloyReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public FoundryAlloyReloadListener() {
        super(GSON, "mobstoolforging/foundry_alloys");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FoundryAlloyRecipe> loaded = new LinkedHashMap<>();
        entries.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "foundry alloy recipe")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid foundry alloy recipe {}.", id, exception);
            }
        });
        FoundryAlloyRegistry.replace(loaded);
        MobsToolForging.LOGGER.info("Loaded {} foundry alloy recipe(s).", loaded.size());
    }

    private static FoundryAlloyRecipe parse(ResourceLocation id, JsonObject json) {
        ResourceLocation result = ResourceLocation.parse(GsonHelper.getAsString(json, "result"));
        JsonObject inputsJson = GsonHelper.getAsJsonObject(json, "inputs");
        Map<ResourceLocation, Integer> inputs = new LinkedHashMap<>();
        inputsJson.entrySet().forEach(entry -> {
            ResourceLocation material = ResourceLocation.parse(entry.getKey());
            int amount = entry.getValue().getAsInt();
            if (inputs.put(material, amount) != null) {
                throw new IllegalArgumentException("Duplicate foundry alloy input " + material);
            }
        });
        int outputAmount = GsonHelper.getAsInt(json, "output_amount", inputs.values().stream().mapToInt(Integer::intValue).sum());
        return new FoundryAlloyRecipe(id, result, inputs, outputAmount);
    }
}

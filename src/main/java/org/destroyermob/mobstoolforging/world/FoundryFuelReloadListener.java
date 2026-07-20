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

public class FoundryFuelReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public FoundryFuelReloadListener() {
        super(GSON, "mobstoolforging/foundry_fuels");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FoundryFuelRecipe> loaded = new LinkedHashMap<>();
        entries.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "foundry fuel recipe")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid foundry fuel recipe {}.", id, exception);
            }
        });
        FoundryFuelRegistry.replace(loaded);
        MobsToolForging.LOGGER.info("Loaded {} foundry fuel recipe(s).", loaded.size());
    }

    private static FoundryFuelRecipe parse(ResourceLocation id, JsonObject json) {
        FoundryFuelRecipe.Input input;
        if (json.has("fluid")) {
            input = FoundryFuelRecipe.Input.fluid(ResourceLocation.parse(GsonHelper.getAsString(json, "fluid")));
        } else if (json.has("fluid_tag")) {
            input = FoundryFuelRecipe.Input.tag(ResourceLocation.parse(GsonHelper.getAsString(json, "fluid_tag")));
        } else {
            throw new IllegalArgumentException("Foundry fuel needs a fluid or fluid_tag");
        }
        return new FoundryFuelRecipe(
                id,
                input,
                GsonHelper.getAsFloat(json, "temperature_c"),
                GsonHelper.getAsInt(json, "amount", FoundryFuelRegistry.DEFAULT_BATCH_MB),
                GsonHelper.getAsInt(json, "burn_ticks", FoundryFuelRegistry.DEFAULT_BURN_TICKS)
        );
    }
}

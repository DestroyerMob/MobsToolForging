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

public class FoundryMeltingReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public FoundryMeltingReloadListener() {
        super(GSON, "mobstoolforging/foundry_melting");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FoundryMeltingRecipe> loaded = new LinkedHashMap<>();
        entries.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "foundry melting recipe")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid foundry melting recipe {}.", id, exception);
            }
        });
        FoundryMeltingRegistry.replace(loaded);
        MobsToolForging.LOGGER.info("Loaded {} foundry melting recipe(s).", loaded.size());
    }

    private static FoundryMeltingRecipe parse(ResourceLocation id, JsonObject json) {
        JsonObject inputJson = GsonHelper.getAsJsonObject(json, "input");
        FoundryMeltingRecipe.Input input;
        if (inputJson.has("item")) {
            input = FoundryMeltingRecipe.Input.item(ResourceLocation.parse(GsonHelper.getAsString(inputJson, "item")));
        } else if (inputJson.has("tag")) {
            input = FoundryMeltingRecipe.Input.tag(ResourceLocation.parse(GsonHelper.getAsString(inputJson, "tag")));
        } else {
            throw new IllegalArgumentException("Foundry melting input needs an item or tag");
        }
        return new FoundryMeltingRecipe(
                id,
                input,
                ResourceLocation.parse(GsonHelper.getAsString(json, "material")),
                GsonHelper.getAsInt(json, "amount", FoundryForgeBlockEntity.INGOT_MB),
                GsonHelper.getAsInt(json, "ticks", FoundryForgeBlockEntity.DEFAULT_MELT_TICKS)
        );
    }
}

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
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class FoundryCastReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public FoundryCastReloadListener() {
        super(GSON, "mobstoolforging/foundry_casts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FoundryCastRecipe> loaded = new LinkedHashMap<>();
        entries.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "foundry cast recipe")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid foundry cast recipe {}.", id, exception);
            }
        });
        FoundryCastRegistry.replace(loaded);
        MobsToolForging.LOGGER.info("Loaded {} foundry cast recipe(s).", loaded.size());
    }

    private static FoundryCastRecipe parse(ResourceLocation id, JsonObject json) {
        JsonObject inputJson = GsonHelper.getAsJsonObject(json, "input");
        FoundryMeltingRecipe.Input input;
        if (inputJson.has("item")) {
            ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(inputJson, "item"));
            if (BuiltInRegistries.ITEM.get(itemId) == Items.AIR) {
                throw new IllegalArgumentException("Unknown cast input item " + itemId);
            }
            input = FoundryMeltingRecipe.Input.item(itemId);
        } else if (inputJson.has("tag")) {
            input = FoundryMeltingRecipe.Input.tag(ResourceLocation.parse(GsonHelper.getAsString(inputJson, "tag")));
        } else {
            throw new IllegalArgumentException("Foundry cast input needs an item or tag");
        }
        ResourceLocation template = ResourceLocation.parse(GsonHelper.getAsString(json, "template"));
        return new FoundryCastRecipe(
                id,
                input,
                template,
                GsonHelper.getAsInt(json, "gold_amount", FoundryForgeBlockEntity.INGOT_MB * 2),
                GsonHelper.getAsInt(json, "amount", FoundryForgeBlockEntity.INGOT_MB)
        );
    }
}

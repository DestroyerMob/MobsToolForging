package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class ToolTraitReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ToolTraitReloadListener() {
        super(GSON, "mobstoolforging/traits");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> traits, ResourceManager resourceManager, ProfilerFiller profiler) {
        ToolTraitRegistry.resetDatapackTraits();
        Map<ResourceLocation, JsonElement> accepted = new LinkedHashMap<>();
        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : traits.entrySet()) {
            try {
                ToolTraitRegistry.registerDatapackTrait(parse(entry.getKey(), GsonHelper.convertToJsonObject(entry.getValue(), "tool trait")));
                accepted.put(entry.getKey(), entry.getValue());
                loaded++;
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid tool trait {}.", entry.getKey(), exception);
            }
        }
        GameplayRegistrySyncStore.capture(GameplayRegistrySyncStore.Section.TRAITS, accepted);
        MobsToolForging.LOGGER.info("Loaded {} datapack tool trait definition(s).", loaded);
    }

    static void applySynchronizedData(Map<ResourceLocation, JsonElement> traits) {
        List<ToolTraitDefinition> parsed = traits.entrySet().stream()
                .map(entry -> parse(entry.getKey(), GsonHelper.convertToJsonObject(entry.getValue(), "tool trait")))
                .toList();
        ToolTraitRegistry.resetDatapackTraits();
        parsed.forEach(ToolTraitRegistry::registerDatapackTrait);
    }

    private static ToolTraitDefinition parse(ResourceLocation id, JsonObject json) {
        String translationKey = GsonHelper.getAsString(json, "translation_key", "tooltip." + id.getNamespace() + ".trait." + id.getPath());
        String descriptionKey = GsonHelper.getAsString(json, "description_translation_key", translationKey + ".desc");
        ChatFormatting color = parseColor(GsonHelper.getAsString(json, "color", "gray"));
        String category = nullableString(json, "category");
        return new ToolTraitDefinition(id, translationKey, descriptionKey, color, category, resourceLocations(json, "suppresses"));
    }

    private static ChatFormatting parseColor(String value) {
        ChatFormatting formatting = ChatFormatting.getByName(value.toLowerCase(Locale.ROOT));
        return formatting == null || !formatting.isColor() ? ChatFormatting.GRAY : formatting;
    }

    @Nullable
    private static String nullableString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? GsonHelper.getAsString(json, key) : null;
    }

    private static List<ResourceLocation> resourceLocations(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, key);
        List<ResourceLocation> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(ResourceLocation.parse(element.getAsString()));
        }
        return List.copyOf(values);
    }
}

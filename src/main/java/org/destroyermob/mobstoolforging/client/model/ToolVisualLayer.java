package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record ToolVisualLayer(
        String slot,
        Optional<String> materialFrom,
        Optional<String> generated,
        Optional<String> template,
        Optional<String> largeTemplate,
        List<ResourceLocation> materials,
        int z,
        boolean optional,
        boolean emissiveAllowed
) {
    public static ToolVisualLayer fromJson(JsonObject json) {
        return new ToolVisualLayer(
                GsonHelper.getAsString(json, "slot"),
                optionalString(json, "material_from"),
                optionalString(json, "generated"),
                optionalString(json, "template"),
                optionalString(json, "large_template"),
                materialIds(json),
                GsonHelper.getAsInt(json, "z", 0),
                GsonHelper.getAsBoolean(json, "optional", false),
                GsonHelper.getAsBoolean(json, "emissive_allowed", false)
        );
    }

    private static Optional<String> optionalString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return Optional.empty();
        }
        return Optional.of(GsonHelper.getAsString(json, key));
    }

    private static List<ResourceLocation> materialIds(JsonObject json) {
        if (!json.has("materials") || !json.get("materials").isJsonArray()) {
            return List.of();
        }
        return GsonHelper.getAsJsonArray(json, "materials").asList().stream()
                .map(element -> ResourceLocation.parse(element.getAsString()))
                .toList();
    }
}

package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.util.GsonHelper;

public record ToolVisualLayer(
        String slot,
        Optional<String> materialFrom,
        Optional<String> generated,
        Optional<String> template,
        Optional<String> largeTemplate,
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
}

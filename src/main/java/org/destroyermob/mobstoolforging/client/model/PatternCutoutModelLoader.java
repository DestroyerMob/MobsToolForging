package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public final class PatternCutoutModelLoader implements IGeometryLoader<PatternCutoutGeometry> {
    private static final ResourceLocation DEFAULT_BOARD_TEXTURE = ResourceLocation.withDefaultNamespace("block/oak_planks");

    @Override
    public PatternCutoutGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new PatternCutoutGeometry(readTextures(jsonObject).getOrDefault("board", DEFAULT_BOARD_TEXTURE));
    }

    private static Map<String, ResourceLocation> readTextures(JsonObject jsonObject) {
        if (!jsonObject.has("textures") || !jsonObject.get("textures").isJsonObject()) {
            return Map.of();
        }

        LinkedHashMap<String, ResourceLocation> textures = new LinkedHashMap<>();
        JsonObject textureObject = GsonHelper.getAsJsonObject(jsonObject, "textures");
        for (Map.Entry<String, JsonElement> entry : textureObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Texture entry '" + entry.getKey() + "' must be a resource location string");
            }
            textures.put(entry.getKey(), ResourceLocation.parse(value.getAsString()));
        }
        return Map.copyOf(textures);
    }
}

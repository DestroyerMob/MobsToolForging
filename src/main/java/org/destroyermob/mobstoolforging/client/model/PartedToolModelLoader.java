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
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ToolKind;

public final class PartedToolModelLoader implements IGeometryLoader<PartedToolGeometry> {
    private final boolean partModel;

    public PartedToolModelLoader(boolean partModel) {
        this.partModel = partModel;
    }

    @Override
    public PartedToolGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        String visual = GsonHelper.getAsString(jsonObject, "visual", null);
        String toolId = GsonHelper.getAsString(jsonObject, "tool", null);
        if (toolId == null && visual == null) {
            throw new JsonParseException("Parted tool model needs a 'tool' or 'visual' property");
        }
        ResourceLocation visualId = visual == null
                ? ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, toolId)
                : ResourceLocation.parse(visual);
        if (toolId == null) {
            toolId = visualId.getPath();
        }
        String resolvedToolId = toolId;
        ToolKind toolKind = ToolKind.byId(resolvedToolId)
                .orElseThrow(() -> new JsonParseException("Unknown tool type for parted tool model: " + resolvedToolId));
        String partType = GsonHelper.getAsString(jsonObject, "part_type", toolKind.partType());
        String partSlot = GsonHelper.getAsString(jsonObject, "part_slot", partType);
        return new PartedToolGeometry(toolKind, visualId, partModel, partType, partSlot, readTextures(jsonObject));
    }

    private static Map<String, ResourceLocation> readTextures(JsonObject jsonObject) {
        if (!jsonObject.has("textures") || !jsonObject.get("textures").isJsonObject()) {
            return Map.of();
        }

        Map<String, ResourceLocation> textures = new LinkedHashMap<>();
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

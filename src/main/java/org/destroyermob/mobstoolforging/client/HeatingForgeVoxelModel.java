package org.destroyermob.mobstoolforging.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.joml.Vector3f;

public record HeatingForgeVoxelModel(List<Element> elements) {
    public static final HeatingForgeVoxelModel EMPTY = new HeatingForgeVoxelModel(List.of());

    public static HeatingForgeVoxelModel load(ResourceManager resourceManager, ResourceLocation modelId) {
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
        return resourceManager.getResource(resourceId)
                .map(resource -> loadResource(resourceId, resource))
                .orElseGet(() -> {
                    MobsToolForging.LOGGER.warn("Missing heating forge insert model {}", modelId);
                    return EMPTY;
                });
    }

    private static HeatingForgeVoxelModel loadResource(ResourceLocation resourceId, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject json = GsonHelper.parse(reader);
            JsonArray elements = GsonHelper.getAsJsonArray(json, "elements", new JsonArray());
            List<Element> parsed = new ArrayList<>();
            for (JsonElement element : elements) {
                parsed.add(parseElement(GsonHelper.convertToJsonObject(element, "element")));
            }
            return new HeatingForgeVoxelModel(List.copyOf(parsed));
        } catch (IOException | RuntimeException exception) {
            MobsToolForging.LOGGER.warn("Failed to load heating forge insert model {}", resourceId, exception);
            return EMPTY;
        }
    }

    private static Element parseElement(JsonObject json) {
        Vector3f from = readVec(GsonHelper.getAsJsonArray(json, "from"));
        Vector3f to = readVec(GsonHelper.getAsJsonArray(json, "to"));
        Map<Direction, Face> faces = new EnumMap<>(Direction.class);
        JsonObject faceJson = GsonHelper.getAsJsonObject(json, "faces", new JsonObject());
        readFace(faceJson, faces, "north", Direction.NORTH);
        readFace(faceJson, faces, "south", Direction.SOUTH);
        readFace(faceJson, faces, "east", Direction.EAST);
        readFace(faceJson, faces, "west", Direction.WEST);
        readFace(faceJson, faces, "up", Direction.UP);
        readFace(faceJson, faces, "down", Direction.DOWN);
        return new Element(from, to, Map.copyOf(faces));
    }

    private static void readFace(JsonObject faceJson, Map<Direction, Face> faces, String key, Direction direction) {
        if (!faceJson.has(key)) {
            return;
        }
        JsonObject json = GsonHelper.getAsJsonObject(faceJson, key);
        JsonArray uv = GsonHelper.getAsJsonArray(json, "uv", null);
        faces.put(direction, new Face(uv == null ? defaultUv() : readUv(uv)));
    }

    private static Vector3f readVec(JsonArray array) {
        return new Vector3f(
                GsonHelper.convertToFloat(array.get(0), "x"),
                GsonHelper.convertToFloat(array.get(1), "y"),
                GsonHelper.convertToFloat(array.get(2), "z")
        );
    }

    private static float[] readUv(JsonArray array) {
        return new float[] {
                GsonHelper.convertToFloat(array.get(0), "u0"),
                GsonHelper.convertToFloat(array.get(1), "v0"),
                GsonHelper.convertToFloat(array.get(2), "u1"),
                GsonHelper.convertToFloat(array.get(3), "v1")
        };
    }

    private static float[] defaultUv() {
        return new float[] {0.0F, 0.0F, 16.0F, 16.0F};
    }

    public record Element(Vector3f from, Vector3f to, Map<Direction, Face> faces) {
    }

    public record Face(float[] uv) {
    }
}

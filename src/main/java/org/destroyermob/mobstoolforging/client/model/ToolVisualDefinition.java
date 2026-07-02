package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record ToolVisualDefinition(
        ResourceLocation id,
        int canvas,
        int largeCanvas,
        boolean largeInHand,
        List<ToolVisualLayer> layers
) {
    public static ToolVisualDefinition fromJson(ResourceLocation id, JsonObject json) {
        JsonArray jsonLayers = GsonHelper.getAsJsonArray(json, "layers");
        List<ToolVisualLayer> layers = jsonLayers.asList().stream()
                .map(element -> ToolVisualLayer.fromJson(GsonHelper.convertToJsonObject(element, "layer")))
                .sorted(Comparator.comparingInt(ToolVisualLayer::z))
                .toList();
        return new ToolVisualDefinition(
                id,
                GsonHelper.getAsInt(json, "canvas", 16),
                GsonHelper.getAsInt(json, "large_canvas", 32),
                GsonHelper.getAsBoolean(json, "large_in_hand", false),
                layers
        );
    }

    public static ToolVisualDefinition fallback(ResourceLocation id, String primaryPartType) {
        String folder = visualFolder(id);
        String handleTemplate = template(id, folder, "handle");
        String headTemplate = template(id, folder, primaryPartType);
        return new ToolVisualDefinition(
                id,
                16,
                32,
                true,
                List.of(
                        new ToolVisualLayer("handle", Optional.of("handleMaterial"), Optional.empty(), Optional.of(handleTemplate), Optional.empty(), Optional.empty(), Optional.of(handleTemplate), Optional.of(template(id, folder, "large_handle")), Optional.empty(), Optional.empty(), HandleRenderStrategy.DEFAULT_HANDLE, List.of(), 1, false, false),
                        new ToolVisualLayer(primaryPartType, Optional.of("headMaterial"), Optional.empty(), Optional.of(headTemplate), Optional.of(headTemplate), Optional.of(template(id, folder, primaryPartType + "_part")), Optional.empty(), Optional.of(template(id, folder, "large_" + primaryPartType)), Optional.empty(), Optional.empty(), HandleRenderStrategy.EXACT_FIRST, List.of(), 2, false, false)
                )
        );
    }

    public ToolVisualLayer partLayer(String primaryPartType) {
        return layers.stream()
                .filter(layer -> layer.materialFrom().filter("headMaterial"::equals).isPresent())
                .findFirst()
                .orElseGet(() -> new ToolVisualLayer(primaryPartType, java.util.Optional.of("headMaterial"), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), HandleRenderStrategy.DEFAULT_HANDLE, List.of(), 0, false, false));
    }

    public ToolVisualLayer layerForSlot(String slot) {
        return layers.stream()
                .filter(layer -> layer.slot().equals(slot))
                .findFirst()
                .orElseGet(() -> new ToolVisualLayer(slot, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), HandleRenderStrategy.DEFAULT_HANDLE, List.of(), 0, false, false));
    }

    private static String visualFolder(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String template(ResourceLocation id, String folder, String name) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "tool_templates/" + folder + "/" + name).toString();
    }
}

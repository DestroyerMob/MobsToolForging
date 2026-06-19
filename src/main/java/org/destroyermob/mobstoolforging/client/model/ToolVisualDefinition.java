package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.destroyermob.mobstoolforging.world.ToolKind;

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

    public static ToolVisualDefinition fallback(ResourceLocation id, ToolKind toolKind) {
        return new ToolVisualDefinition(
                id,
                16,
                32,
                true,
                List.of(
                        new ToolVisualLayer("handle", java.util.Optional.of("handleMaterial"), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), 1, false, false),
                        new ToolVisualLayer(toolKind.partType(), java.util.Optional.of("headMaterial"), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), 2, false, false)
                )
        );
    }

    public ToolVisualLayer partLayer(ToolKind toolKind) {
        return layers.stream()
                .filter(layer -> layer.materialFrom().filter("headMaterial"::equals).isPresent())
                .findFirst()
                .orElseGet(() -> new ToolVisualLayer(toolKind.partType(), java.util.Optional.of("headMaterial"), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), 0, false, false));
    }
}

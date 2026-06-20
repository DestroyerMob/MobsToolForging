package org.destroyermob.mobstoolforging.client.model;

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

public final class ToolMaterialVisualManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final int DEFAULT_TINT = 0xFFFFFFFF;
    public static final ToolMaterialVisualManager INSTANCE = new ToolMaterialVisualManager();

    private volatile Map<ResourceLocation, MaterialVisual> visuals = Map.of();

    private ToolMaterialVisualManager() {
        super(GSON, "tooling/material_visuals");
    }

    public int tintColor(ResourceLocation materialId) {
        return visuals.getOrDefault(materialId, MaterialVisual.DEFAULT).tintColor();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, MaterialVisual> loaded = new LinkedHashMap<>();
        entries.forEach((id, element) -> {
            try {
                loaded.put(id, MaterialVisual.fromJson(GsonHelper.convertToJsonObject(element, "material visual")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid material visual {}.", id, exception);
            }
        });
        visuals = Map.copyOf(loaded);
    }

    private record MaterialVisual(Map<Integer, Integer> palette) {
        private static final MaterialVisual DEFAULT = new MaterialVisual(Map.of(178, DEFAULT_TINT));

        private int tintColor() {
            if (palette.containsKey(178)) {
                return palette.get(178);
            }
            if (palette.containsKey(140)) {
                return palette.get(140);
            }
            return palette.values().stream().findFirst().orElse(DEFAULT_TINT);
        }

        private static MaterialVisual fromJson(JsonObject json) {
            if (!json.has("palette") || !json.get("palette").isJsonObject()) {
                return DEFAULT;
            }
            JsonObject paletteJson = GsonHelper.getAsJsonObject(json, "palette");
            Map<Integer, Integer> palette = new LinkedHashMap<>();
            paletteJson.entrySet().forEach(entry -> palette.put(Integer.parseInt(entry.getKey()), parseColor(entry.getValue())));
            return new MaterialVisual(Map.copyOf(palette));
        }

        private static int parseColor(JsonElement element) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }
            String value = element.getAsString().trim();
            if (value.startsWith("0x") || value.startsWith("0X")) {
                return (int) Long.parseLong(value.substring(2), 16);
            }
            if (value.startsWith("#")) {
                long parsed = Long.parseLong(value.substring(1), 16);
                return value.length() == 7 ? (int) (0xFF000000L | parsed) : (int) parsed;
            }
            return (int) Long.parseLong(value, 16);
        }
    }
}

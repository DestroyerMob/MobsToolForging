package org.destroyermob.mobstoolforging.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Keeps the server's accepted custom gameplay JSON so dedicated clients can
 * rebuild the same client-visible registries without having the server's
 * datapacks installed locally.
 */
public final class GameplayRegistrySyncStore {
    private static final EnumMap<Section, Map<ResourceLocation, String>> SECTIONS = new EnumMap<>(Section.class);

    static {
        for (Section section : Section.values()) {
            SECTIONS.put(section, Map.of());
        }
    }

    private GameplayRegistrySyncStore() {
    }

    public static synchronized void capture(Section section, Map<ResourceLocation, JsonElement> accepted) {
        Map<ResourceLocation, String> encoded = new LinkedHashMap<>();
        accepted.forEach((id, json) -> encoded.put(id, json.toString()));
        SECTIONS.put(section, Map.copyOf(encoded));
    }

    public static synchronized Map<Section, Map<ResourceLocation, String>> snapshot() {
        EnumMap<Section, Map<ResourceLocation, String>> copy = new EnumMap<>(Section.class);
        SECTIONS.forEach((section, values) -> copy.put(section, Map.copyOf(values)));
        return Map.copyOf(copy);
    }

    public static void apply(Map<Section, Map<ResourceLocation, String>> encoded) {
        EnumMap<Section, Map<ResourceLocation, JsonElement>> parsed = new EnumMap<>(Section.class);
        for (Section section : Section.values()) {
            Map<ResourceLocation, String> values = encoded.get(section);
            if (values == null) {
                throw new IllegalArgumentException("Missing synchronized gameplay registry section " + section.id());
            }
            Map<ResourceLocation, JsonElement> decoded = new LinkedHashMap<>();
            values.forEach((id, json) -> {
                JsonElement element = JsonParser.parseString(json);
                if (!element.isJsonObject()) {
                    throw new IllegalArgumentException("Synchronized " + section.id() + " entry " + id + " is not an object");
                }
                decoded.put(id, element);
            });
            parsed.put(section, Map.copyOf(decoded));
        }

        MaterialDefinitionReloadListener.applySynchronizedData(parsed.get(Section.MATERIALS));
        ToolTraitReloadListener.applySynchronizedData(parsed.get(Section.TRAITS));
        ToolTypeReloadListener.applySynchronizedData(parsed.get(Section.TOOL_TYPES));
        ForgeTemplateReloadListener.applySynchronizedData(parsed.get(Section.FORGE_TEMPLATES));
        ToolStatRuleReloadListener.applySynchronizedData(parsed.get(Section.STAT_RULES));
        StationWorkRecipeReloadListener.applySynchronizedData(parsed.get(Section.STATION_WORK));
        HeatingRecipeReloadListener.applySynchronizedData(parsed.get(Section.HEATING));
        DryingRecipeReloadListener.applySynchronizedData(parsed.get(Section.DRYING));
        GroundAssemblyRecipeReloadListener.applySynchronizedData(parsed.get(Section.GROUND_ASSEMBLY));
    }

    public enum Section {
        MATERIALS("materials"),
        TRAITS("traits"),
        TOOL_TYPES("tool_types"),
        FORGE_TEMPLATES("forge_templates"),
        STAT_RULES("stat_rules"),
        STATION_WORK("station_work"),
        HEATING("heating"),
        DRYING("drying"),
        GROUND_ASSEMBLY("ground_assembly");

        private final String id;

        Section(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}

package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class ForgeTemplateReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ForgeTemplateReloadListener() {
        super(GSON, "mobstoolforging/forge_templates");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> templates, ResourceManager resourceManager, ProfilerFiller profiler) {
        ToolTypeRegistry.resetTemplatesToBuiltIns();
        templates.forEach((id, element) -> {
            try {
                ToolTypeRegistry.registerTemplate(parse(id, GsonHelper.convertToJsonObject(element, "forge template")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid forge template {}.", id, exception);
            }
        });
        MobsToolForging.LOGGER.info("Loaded {} datapack forge template override(s).", templates.size());
    }

    private static ForgeTemplateDefinition parse(ResourceLocation id, JsonObject json) {
        ForgeTemplateDefinition base = ToolTypeRegistry.template(id).orElse(null);
        ResourceLocation toolType = json.has("tool_type")
                ? ResourceLocation.parse(GsonHelper.getAsString(json, "tool_type"))
                : base == null ? ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, id.getPath().replace("_head", "").replace("_blade", "")) : base.toolType();
        String partType = GsonHelper.getAsString(json, "part_type", base == null ? id.getPath() : base.partType());
        int requiredMaterials = Math.max(1, GsonHelper.getAsInt(json, "required_materials", base == null ? 1 : base.requiredMaterials()));
        int requiredHits = Math.max(1, GsonHelper.getAsInt(json, "required_hits", base == null ? 5 : base.requiredHits()));
        String translationKey = GsonHelper.getAsString(json, "translation_key", base == null ? "forge_template." + id.getNamespace() + "." + id.getPath() : base.translationKey());
        float minimumTemperature = minimumTemperature(json, base);
        Set<ResourceLocation> whitelist = materialList(json, base, "material_whitelist", "allowed_materials");
        Set<ResourceLocation> blacklist = materialList(json, base, "material_blacklist", "blocked_materials");
        int minimumHammerLevel = minimumHammerLevel(json, base);
        Map<ResourceLocation, Integer> materialHammerLevels = materialHammerLevels(json, base);
        ResourceLocation outputItem = json.has("output_item")
                ? ResourceLocation.parse(GsonHelper.getAsString(json, "output_item"))
                : base == null ? null : base.outputItem();
        return new ForgeTemplateDefinition(id, toolType, partType, requiredMaterials, requiredHits, translationKey, minimumTemperature, whitelist, blacklist, minimumHammerLevel, materialHammerLevels, outputItem);
    }

    private static float minimumTemperature(JsonObject json, ForgeTemplateDefinition base) {
        if (json.has("minimum_temperature_percent")) {
            return clamp(GsonHelper.getAsFloat(json, "minimum_temperature_percent") / 100.0F);
        }
        if (json.has("minimum_temperature")) {
            float value = GsonHelper.getAsFloat(json, "minimum_temperature");
            return clamp(value > 1.0F ? value / 100.0F : value);
        }
        return base == null ? Float.NaN : base.minimumTemperatureOverride();
    }

    private static Set<ResourceLocation> materialList(JsonObject json, ForgeTemplateDefinition base, String key, String alias) {
        if (!json.has(key) && !json.has(alias)) {
            return base == null ? Set.of() : Set.copyOf(key.equals("material_whitelist") ? base.materialWhitelist() : base.materialBlacklist());
        }
        JsonArray values = GsonHelper.getAsJsonArray(json, json.has(key) ? key : alias);
        LinkedHashSet<ResourceLocation> result = new LinkedHashSet<>();
        for (JsonElement element : values) {
            result.add(ResourceLocation.parse(element.getAsString()));
        }
        return Set.copyOf(result);
    }

    private static int minimumHammerLevel(JsonObject json, ForgeTemplateDefinition base) {
        int fallback = base == null ? SmithingHammerLevel.STONE.level() : base.minimumHammerLevel();
        if (!json.has("minimum_hammer_level")) {
            return fallback;
        }
        JsonElement value = json.get("minimum_hammer_level");
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            return Math.max(0, value.getAsInt());
        }
        return SmithingHammerLevel.parseLevel(value.getAsString(), fallback);
    }

    private static Map<ResourceLocation, Integer> materialHammerLevels(JsonObject json, ForgeTemplateDefinition base) {
        if (!json.has("material_hammer_levels")) {
            return base == null ? Map.of() : Map.copyOf(base.materialHammerLevels());
        }
        JsonObject object = GsonHelper.getAsJsonObject(json, "material_hammer_levels");
        LinkedHashMap<ResourceLocation, Integer> result = new LinkedHashMap<>();
        object.entrySet().forEach(entry -> result.put(
                ResourceLocation.parse(entry.getKey()),
                entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()
                        ? Math.max(0, entry.getValue().getAsInt())
                        : SmithingHammerLevel.parseLevel(entry.getValue().getAsString().toLowerCase(Locale.ROOT), SmithingHammerLevel.STONE.level())
        ));
        return Map.copyOf(result);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

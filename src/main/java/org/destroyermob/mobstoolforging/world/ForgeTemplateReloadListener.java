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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class ForgeTemplateReloadListener extends ConditionalJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ForgeTemplateReloadListener(ICondition.IContext conditionContext, HolderLookup.Provider registryLookup) {
        super(GSON, "mobstoolforging/forge_templates", conditionContext, registryLookup);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> templates, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ForgeTemplateDefinition> parsed = new LinkedHashMap<>();
        Map<ResourceLocation, JsonElement> accepted = new LinkedHashMap<>();
        templates.forEach((id, element) -> {
            try {
                if (!conditionsMatch(element)) {
                    return;
                }
                ForgeTemplateDefinition template = parse(id, GsonHelper.convertToJsonObject(element, "forge template"));
                validateReferences(template);
                parsed.put(id, template);
                accepted.put(id, element);
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid forge template {}.", id, exception);
            }
        });
        ToolTypeRegistry.replaceDatapackTemplates(parsed.values());
        GameplayRegistrySyncStore.capture(GameplayRegistrySyncStore.Section.FORGE_TEMPLATES, accepted);
        MobsToolForging.LOGGER.info("Loaded {} datapack forge template override(s).", parsed.size());
    }

    static void applySynchronizedData(Map<ResourceLocation, JsonElement> templates) {
        Map<ResourceLocation, ForgeTemplateDefinition> parsed = new LinkedHashMap<>();
        templates.forEach((id, element) -> {
            ForgeTemplateDefinition template = parse(id, GsonHelper.convertToJsonObject(element, "forge template"));
            validateReferences(template);
            parsed.put(id, template);
        });
        ToolTypeRegistry.replaceDatapackTemplates(parsed.values());
    }

    private static ForgeTemplateDefinition parse(ResourceLocation id, JsonObject json) {
        ForgeTemplateDefinition base = ToolTypeRegistry.baseTemplate(id).orElse(null);
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
        Map<ResourceLocation, ResourceLocation> outputItems = outputItems(json, base);
        Set<ResourceLocation> compatibleToolTypes = compatibleToolTypes(json, base, toolType);
        boolean patternStationEnabled = GsonHelper.getAsBoolean(json, "pattern_station_enabled", base == null || base.patternStationEnabled());
        int patternStationPaperCost = Math.max(1, GsonHelper.getAsInt(json, "pattern_station_paper_cost", base == null ? 1 : base.patternStationPaperCost()));
        return new ForgeTemplateDefinition(id, toolType, partType, requiredMaterials, requiredHits, translationKey, minimumTemperature, whitelist, blacklist, minimumHammerLevel, materialHammerLevels, outputItem, outputItems, compatibleToolTypes, patternStationEnabled, patternStationPaperCost);
    }

    private static void validateReferences(ForgeTemplateDefinition template) {
        if (template.outputItem() != null && BuiltInRegistries.ITEM.get(template.outputItem()) == Items.AIR) {
            throw new IllegalArgumentException("Unknown output_item " + template.outputItem());
        }
        template.outputItems().forEach((materialId, itemId) -> {
            if (BuiltInRegistries.ITEM.get(itemId) == Items.AIR) {
                throw new IllegalArgumentException("Unknown output item " + itemId + " for material " + materialId);
            }
        });

        var allowedMaterials = MaterialCatalog.starterMaterialIds().stream()
                .filter(template::allowsMaterial)
                .toList();
        if (allowedMaterials.isEmpty()) {
            throw new IllegalArgumentException("Template does not allow any registered forging material");
        }
        for (ResourceLocation materialId : allowedMaterials) {
            if (template.outputStack(materialId).isEmpty()) {
                throw new IllegalArgumentException(
                        "Template cannot produce part_type " + template.partType()
                                + " for allowed material " + materialId
                                + " using tool_type " + template.toolType()
                );
            }
        }
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

    private static Map<ResourceLocation, ResourceLocation> outputItems(JsonObject json, ForgeTemplateDefinition base) {
        if (!json.has("output_items")) {
            return base == null ? Map.of() : Map.copyOf(base.outputItems());
        }
        JsonObject object = GsonHelper.getAsJsonObject(json, "output_items");
        LinkedHashMap<ResourceLocation, ResourceLocation> result = new LinkedHashMap<>();
        object.entrySet().forEach(entry -> result.put(ResourceLocation.parse(entry.getKey()), ResourceLocation.parse(entry.getValue().getAsString())));
        return Map.copyOf(result);
    }

    private static Set<ResourceLocation> compatibleToolTypes(JsonObject json, ForgeTemplateDefinition base, ResourceLocation toolType) {
        if (!json.has("compatible_tool_types")) {
            return base == null ? Set.of(toolType) : Set.copyOf(base.compatibleToolTypes());
        }
        JsonArray values = GsonHelper.getAsJsonArray(json, "compatible_tool_types");
        LinkedHashSet<ResourceLocation> result = new LinkedHashSet<>();
        for (JsonElement element : values) {
            result.add(ResourceLocation.parse(element.getAsString()));
        }
        if (result.isEmpty()) {
            result.add(toolType);
        }
        return Set.copyOf(result);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

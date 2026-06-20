package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
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
        return new ForgeTemplateDefinition(id, toolType, partType, requiredMaterials, requiredHits, translationKey, minimumTemperature);
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

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

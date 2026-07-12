package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record ToolVisualLayer(
        String slot,
        Optional<String> materialFrom,
        Optional<String> generated,
        Optional<String> template,
        Optional<String> toolTemplate,
        Optional<String> partTemplate,
        Optional<String> handleTemplate,
        Optional<String> largeTemplate,
        Optional<String> textureNamespace,
        Optional<String> texturePattern,
        HandleRenderStrategy handleStrategy,
        List<ResourceLocation> materials,
        int z,
        boolean optional,
        boolean emissiveAllowed
) {
    public static ToolVisualLayer fromJson(JsonObject json) {
        return new ToolVisualLayer(
                GsonHelper.getAsString(json, "slot"),
                optionalString(json, "material_from"),
                optionalString(json, "generated"),
                optionalString(json, "template"),
                optionalString(json, "tool_template"),
                optionalString(json, "part_template"),
                optionalString(json, "handle_template"),
                optionalString(json, "large_template"),
                optionalString(json, "texture_namespace"),
                optionalString(json, "texture_pattern"),
                handleStrategy(json),
                materialIds(json),
                GsonHelper.getAsInt(json, "z", 0),
                GsonHelper.getAsBoolean(json, "optional", false),
                GsonHelper.getAsBoolean(json, "emissive_allowed", false)
        );
    }

    public Optional<ResourceLocation> templateId() {
        return toolTemplate.or(() -> template).map(ResourceLocation::parse);
    }

    public Optional<ResourceLocation> partTemplateId() {
        return partTemplate.or(() -> template).map(ResourceLocation::parse);
    }

    public Optional<ResourceLocation> handleTemplateId() {
        return handleTemplate.or(this::templateIdString).map(ResourceLocation::parse);
    }

    public Optional<ResourceLocation> largeTemplateId() {
        return largeTemplate.map(ResourceLocation::parse);
    }

    public Optional<ResourceLocation> templateId(boolean partTemplate) {
        if (partTemplate) {
            return partTemplateId();
        }
        if (materialFrom.filter("handleMaterial"::equals).isPresent()) {
            return handleTemplateId();
        }
        return templateId();
    }

    public boolean canUseExactTexture() {
        return !materialFrom.filter("handleMaterial"::equals).isPresent() || handleStrategy.usesExactTextures();
    }

    public boolean canUseTemplateFallback() {
        return !materialFrom.filter("handleMaterial"::equals).isPresent() || handleStrategy.usesTemplateFallback();
    }

    public boolean prefersTemplateFallback() {
        return materialFrom.filter("handleMaterial"::equals).isPresent() && handleStrategy.prefersTemplate();
    }

    public boolean compositesExactAndTemplate() {
        return materialFrom.filter("handleMaterial"::equals).isPresent() && handleStrategy.compositesExactAndTemplate();
    }

    public boolean masksHandleTexture() {
        return materialFrom.filter("handleMaterial"::equals).isPresent() && handleStrategy.usesShapeMask();
    }

    public Optional<ResourceLocation> textureFromPattern(ResourceLocation material, String usage) {
        if (texturePattern.isEmpty()) {
            return Optional.empty();
        }
        String namespace = textureNamespace.orElse(material.getNamespace());
        String path = texturePattern.get()
                .replace("{namespace}", namespace)
                .replace("{material_namespace}", material.getNamespace())
                .replace("{material}", material.getPath())
                .replace("{material_path}", material.getPath())
                .replace("{slot}", slot)
                .replace("{usage}", usage);
        return Optional.of(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private Optional<String> templateIdString() {
        return toolTemplate.or(() -> template);
    }

    private static Optional<String> optionalString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return Optional.empty();
        }
        return Optional.of(GsonHelper.getAsString(json, key));
    }

    private static HandleRenderStrategy handleStrategy(JsonObject json) {
        if (!json.has("handle_strategy") || json.get("handle_strategy").isJsonNull()) {
            return HandleRenderStrategy.DEFAULT_HANDLE;
        }
        return HandleRenderStrategy.parse(GsonHelper.getAsString(json, "handle_strategy"));
    }

    private static List<ResourceLocation> materialIds(JsonObject json) {
        if (!json.has("materials") || !json.get("materials").isJsonArray()) {
            return List.of();
        }
        return GsonHelper.getAsJsonArray(json, "materials").asList().stream()
                .map(element -> ResourceLocation.parse(element.getAsString()))
                .toList();
    }
}

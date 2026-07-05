package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;

public final class PartedToolSpriteSet {
    private static final Set<String> REPORTED_MISSING_REQUIRED_SPRITES = ConcurrentHashMap.newKeySet();
    private static final Set<String> REPORTED_EMPTY_SPRITE_SETS = ConcurrentHashMap.newKeySet();
    private static final Set<String> REPORTED_MISSING_EXPLICIT_TEXTURES = ConcurrentHashMap.newKeySet();

    private final Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites;
    private final Map<ToolPartSpriteKey, ResolvedToolLayerSprite> handleBodySprites;
    private final Map<TemplateKey, TextureAtlasSprite> templateSprites;
    private final Map<TemplateKey, ResourceLocation> templateTextures;
    private final TextureAtlasSprite particle;
    private final TextureAtlasSprite missing;

    private PartedToolSpriteSet(Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites, Map<ToolPartSpriteKey, ResolvedToolLayerSprite> handleBodySprites, Map<TemplateKey, TextureAtlasSprite> templateSprites, Map<TemplateKey, ResourceLocation> templateTextures, TextureAtlasSprite particle, TextureAtlasSprite missing) {
        this.sprites = sprites;
        this.handleBodySprites = handleBodySprites;
        this.templateSprites = templateSprites;
        this.templateTextures = templateTextures;
        this.particle = particle;
        this.missing = missing;
    }

    public static PartedToolSpriteSet from(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ToolVisualDefinition visual, Map<String, ResourceLocation> textureOverrides, boolean partModel) {
        Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites = new LinkedHashMap<>();
        Map<ToolPartSpriteKey, ResolvedToolLayerSprite> handleBodySprites = new LinkedHashMap<>();
        Map<TemplateKey, TextureAtlasSprite> templateSprites = new LinkedHashMap<>();
        Map<TemplateKey, ResourceLocation> templateTextures = new LinkedHashMap<>();
        TextureAtlasSprite missing = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
        for (ToolVisualLayer layer : visual.layers()) {
            Optional<String> materialFrom = layer.materialFrom();
            Set<ResourceLocation> materialIds = materialFrom
                    .map(value -> {
                        Set<ResourceLocation> ids = new LinkedHashSet<>(MaterialCatalog.visualMaterialIds(value));
                        ids.addAll(layer.materials());
                        return ids;
                    })
                    .orElseGet(LinkedHashSet::new);
            if (shouldLoadTemplates(context, textureOverrides, layer, materialIds)) {
                loadTemplate(spriteGetter, templateSprites, templateTextures, layer.slot(), false, layer.templateId(false));
                loadTemplate(spriteGetter, templateSprites, templateTextures, layer.slot(), true, layer.templateId(true));
                loadTemplate(spriteGetter, templateSprites, templateTextures, layer.slot(), false, handleMaskId(visual, layer));
            }
            if (materialFrom.isEmpty()) {
                continue;
            }
            for (ResourceLocation material : materialIds) {
                ToolPartSpriteKey key = new ToolPartSpriteKey(visual.id(), layer.slot(), material);
                readSprite(context, spriteGetter, textureOverrides, key.modelTextureKey())
                        .or(() -> readPatternSprite(spriteGetter, layer, material, spriteUsage(layer, partModel)))
                        .ifPresent(sprite -> sprites.put(key, ResolvedToolLayerSprite.exact(sprite.sprite(), sprite.texture())));
                if (layer.compositesExactAndTemplate()) {
                    readSprite(context, spriteGetter, textureOverrides, ToolPartSpriteKey.handleBodyTextureKey(material))
                            .ifPresent(sprite -> handleBodySprites.put(key, sprite));
                }
            }
            warnMissingRequiredSprites(visual, layer, materialIds, sprites, templateSprites, partModel);
        }

        TextureAtlasSprite particle = readSprite(context, spriteGetter, textureOverrides, "particle")
                .map(ResolvedToolLayerSprite::sprite)
                .orElseGet(() -> firstOrFallback(visual, sprites, missing));
        return new PartedToolSpriteSet(sprites, Map.copyOf(handleBodySprites), Map.copyOf(templateSprites), Map.copyOf(templateTextures), particle, missing);
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material) {
        return resolveLayer(toolType, slot, material, true, false).map(ResolvedToolLayerSprite::sprite);
    }

    public Optional<ResolvedToolLayerSprite> resolveLayer(ResourceLocation toolType, ToolVisualLayer layer, ResourceLocation material) {
        return resolveLayer(toolType, layer, material, false);
    }

    public List<ResolvedToolLayerSprite> resolveLayers(ResourceLocation toolType, ToolVisualLayer layer, ResourceLocation material) {
        if (!layer.compositesExactAndTemplate()) {
            return resolveLayer(toolType, layer, material).map(List::of).orElseGet(List::of);
        }

        ResolvedToolLayerSprite exact = sprites.get(new ToolPartSpriteKey(toolType, layer.slot(), material));
        if (exact != null) {
            return resolveExactHandleBody(toolType, layer, material)
                    .map(resolvedBody -> List.of(resolvedBody, exact))
                    .orElseGet(() -> List.of(exact));
        }
        return resolveTemplate(layer.slot(), material, false).map(List::of).orElseGet(List::of);
    }

    public Optional<ResolvedToolLayerSprite> resolvePartLayer(ResourceLocation toolType, ToolVisualLayer layer, ResourceLocation material) {
        return resolveLayer(toolType, layer, material, true);
    }

    private Optional<ResolvedToolLayerSprite> resolveExactHandleBody(ResourceLocation toolType, ToolVisualLayer layer, ResourceLocation material) {
        ResolvedToolLayerSprite body = handleBodySprites.get(new ToolPartSpriteKey(toolType, layer.slot(), material));
        if (body != null) {
            return Optional.of(body);
        }

        return Optional.empty();
    }

    private Optional<ResolvedToolLayerSprite> resolveLayer(ResourceLocation toolType, ToolVisualLayer layer, ResourceLocation material, boolean partTemplate) {
        if (layer.prefersTemplateFallback()) {
            Optional<ResolvedToolLayerSprite> template = resolveTemplate(layer.slot(), material, partTemplate);
            if (template.isPresent()) {
                return template;
            }
        }
        if (!layer.canUseExactTexture()) {
            return resolveTemplate(layer.slot(), material, partTemplate);
        }
        ResolvedToolLayerSprite exact = sprites.get(new ToolPartSpriteKey(toolType, layer.slot(), material));
        if (exact != null) {
            return Optional.of(exact);
        }
        return layer.canUseTemplateFallback() ? resolveTemplate(layer.slot(), material, partTemplate) : Optional.empty();
    }

    private Optional<ResolvedToolLayerSprite> resolveLayer(ResourceLocation toolType, String slot, ResourceLocation material, boolean allowTemplateFallback, boolean partTemplate) {
        ResolvedToolLayerSprite exact = sprites.get(new ToolPartSpriteKey(toolType, slot, material));
        if (exact != null) {
            return Optional.of(exact);
        }
        if (!allowTemplateFallback) {
            return Optional.empty();
        }
        return resolveTemplate(slot, material, partTemplate);
    }

    private Optional<ResolvedToolLayerSprite> resolveTemplate(String slot, ResourceLocation material, boolean partTemplate) {
        TemplateKey templateKey = new TemplateKey(slot, partTemplate);
        TextureAtlasSprite template = templateSprites.get(templateKey);
        ResourceLocation texture = templateTextures.get(templateKey);
        if (template == null || texture == null) {
            return Optional.empty();
        }
        return Optional.of(ResolvedToolLayerSprite.generated(template, ToolMaterialVisualManager.INSTANCE.tintColor(material), texture));
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material, boolean allowTemplateFallback) {
        return resolveLayer(toolType, slot, material, allowTemplateFallback, false).map(ResolvedToolLayerSprite::sprite);
    }

    private static boolean isMissing(TextureAtlasSprite sprite) {
        return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    public Optional<TextureAtlasSprite> resolveExact(ResourceLocation toolType, String slot, ResourceLocation material) {
        return Optional.ofNullable(sprites.get(new ToolPartSpriteKey(toolType, slot, material))).map(ResolvedToolLayerSprite::sprite);
    }

    public TextureAtlasSprite particle() {
        return particle;
    }

    public TextureAtlasSprite missing() {
        return missing;
    }

    private static Optional<ResolvedToolLayerSprite> readSprite(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, Map<String, ResourceLocation> textureOverrides, String key) {
        ResourceLocation explicitTexture = textureOverrides.get(key);
        if (explicitTexture != null) {
            TextureAtlasSprite sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, explicitTexture));
            warnMissingExplicitTexture(key, explicitTexture, sprite);
            return isMissing(sprite) ? Optional.empty() : Optional.of(ResolvedToolLayerSprite.exact(sprite, explicitTexture));
        }
        if (!context.hasMaterial(key)) {
            return Optional.empty();
        }
        TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial(key));
        return isMissing(sprite) ? Optional.empty() : Optional.of(ResolvedToolLayerSprite.exact(sprite, sprite.contents().name()));
    }

    private static Optional<ResolvedToolLayerSprite> readDirectSprite(Function<Material, TextureAtlasSprite> spriteGetter, ResourceLocation texture) {
        TextureAtlasSprite sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        return isMissing(sprite) ? Optional.empty() : Optional.of(ResolvedToolLayerSprite.exact(sprite, texture));
    }

    private static Optional<ResolvedToolLayerSprite> readPatternSprite(Function<Material, TextureAtlasSprite> spriteGetter, ToolVisualLayer layer, ResourceLocation material, String usage) {
        return layer.textureFromPattern(material, usage)
                .flatMap(texture -> readDirectSprite(spriteGetter, texture));
    }

    private static void loadTemplate(
            Function<Material, TextureAtlasSprite> spriteGetter,
            Map<TemplateKey, TextureAtlasSprite> templateSprites,
            Map<TemplateKey, ResourceLocation> templateTextures,
            String slot,
            boolean partTemplate,
            Optional<ResourceLocation> template
    ) {
        template.ifPresent(texture -> {
            TextureAtlasSprite templateSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
            if (!isMissing(templateSprite)) {
                TemplateKey key = new TemplateKey(slot, partTemplate);
                templateSprites.put(key, templateSprite);
                templateTextures.put(key, texture);
            }
        });
    }

    private static Optional<ResourceLocation> handleMaskId(ToolVisualDefinition visual, ToolVisualLayer layer) {
        if (!layer.compositesExactAndTemplate()) {
            return Optional.empty();
        }
        return Optional.of(ResourceLocation.fromNamespaceAndPath(visual.id().getNamespace(), "source/tool_parts/handle_masks/" + handleShape(visual.id()) + "_handle_mask"));
    }

    private static String spriteUsage(ToolVisualLayer layer, boolean partModel) {
        return partModel && !isTreatmentLayer(layer) ? "part" : "tool";
    }

    private static boolean isTreatmentLayer(ToolVisualLayer layer) {
        return layer.materialFrom().filter("treatment"::equals).isPresent();
    }

    private static String handleShape(ResourceLocation visualId) {
        String path = visualId.getPath();
        if (!MobsToolForging.MOD_ID.equals(visualId.getNamespace())) {
            int slash = path.lastIndexOf('/');
            return slash >= 0 ? path.substring(slash + 1) : path;
        }
        if (path.contains("mattock")) {
            return "mattock";
        }
        if (path.contains("pickaxe")) {
            return "pickaxe";
        }
        if (path.contains("shovel")) {
            return "shovel";
        }
        if (path.contains("hoe")) {
            return "hoe";
        }
        if (path.contains("axe")) {
            return "axe";
        }
        if (path.contains("sword")) {
            return "sword";
        }
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static void warnMissingExplicitTexture(String key, ResourceLocation requestedTexture, TextureAtlasSprite sprite) {
        if (!MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
            return;
        }
        String reportKey = key + "|" + requestedTexture;
        if (REPORTED_MISSING_EXPLICIT_TEXTURES.add(reportKey)) {
            MobsToolForging.LOGGER.warn(
                    "Tool visual texture override resolved to missing texture: textureKey={}, requestedTexture={}",
                    key,
                    requestedTexture
            );
        }
    }

    private static boolean shouldLoadTemplates(IGeometryBakingContext context, Map<String, ResourceLocation> textureOverrides, ToolVisualLayer layer, Set<ResourceLocation> materialIds) {
        if (!layer.optional() || layer.texturePattern().isPresent()) {
            return true;
        }
        for (ResourceLocation material : materialIds) {
            String layerTextureKey = ToolPartSpriteKey.modelTextureKey(layer.slot(), material);
            if (textureOverrides.containsKey(layerTextureKey) || context.hasMaterial(layerTextureKey)) {
                return true;
            }
            if (layer.compositesExactAndTemplate()) {
                String handleBodyTextureKey = ToolPartSpriteKey.handleBodyTextureKey(material);
                if (textureOverrides.containsKey(handleBodyTextureKey) || context.hasMaterial(handleBodyTextureKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static TextureAtlasSprite firstOrFallback(ToolVisualDefinition visual, Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites, TextureAtlasSprite missing) {
        if (!sprites.isEmpty()) {
            return sprites.values().iterator().next().sprite();
        }
        if (REPORTED_EMPTY_SPRITE_SETS.add(visual.id().toString())) {
            MobsToolForging.LOGGER.warn("No sprites were available for tool visual {}; using the missing texture sprite as particle fallback.", visual.id());
        }
        return missing;
    }

    private static void warnMissingRequiredSprites(ToolVisualDefinition visual, ToolVisualLayer layer, Set<ResourceLocation> materialIds, Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites, Map<TemplateKey, TextureAtlasSprite> templateSprites, boolean partModel) {
        if (layer.optional()) {
            return;
        }
        if (layer.canUseTemplateFallback() && templateSprites.containsKey(new TemplateKey(layer.slot(), partModel))) {
            return;
        }
        for (ResourceLocation material : materialIds) {
            ToolPartSpriteKey key = new ToolPartSpriteKey(visual.id(), layer.slot(), material);
            if (sprites.containsKey(key)) {
                continue;
            }
            String reportKey = visual.id() + "|" + layer.slot() + "|" + material + "|" + key.modelTextureKey();
            if (REPORTED_MISSING_REQUIRED_SPRITES.add(reportKey)) {
                MobsToolForging.LOGGER.warn(
                        "Missing required tool visual sprite during bake: visual={}, slot={}, material={}, textureKey={}",
                        visual.id(),
                        layer.slot(),
                        material,
                        key.modelTextureKey()
                );
            }
        }
    }

    private record TemplateKey(String slot, boolean partTemplate) {
    }
}

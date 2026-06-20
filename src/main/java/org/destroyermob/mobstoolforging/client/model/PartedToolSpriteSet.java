package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private final Map<String, TextureAtlasSprite> templateSprites;
    private final Map<String, ResourceLocation> templateTextures;
    private final TextureAtlasSprite particle;
    private final TextureAtlasSprite missing;

    private PartedToolSpriteSet(Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites, Map<String, TextureAtlasSprite> templateSprites, Map<String, ResourceLocation> templateTextures, TextureAtlasSprite particle, TextureAtlasSprite missing) {
        this.sprites = sprites;
        this.templateSprites = templateSprites;
        this.templateTextures = templateTextures;
        this.particle = particle;
        this.missing = missing;
    }

    public static PartedToolSpriteSet from(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ToolVisualDefinition visual, Map<String, ResourceLocation> textureOverrides) {
        Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites = new LinkedHashMap<>();
        Map<String, TextureAtlasSprite> templateSprites = new LinkedHashMap<>();
        Map<String, ResourceLocation> templateTextures = new LinkedHashMap<>();
        TextureAtlasSprite missing = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
        for (ToolVisualLayer layer : visual.layers()) {
            layer.templateId().ifPresent(template -> {
                TextureAtlasSprite templateSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, template));
                if (!isMissing(templateSprite)) {
                    templateSprites.put(layer.slot(), templateSprite);
                    templateTextures.put(layer.slot(), template);
                }
            });
            Optional<String> materialFrom = layer.materialFrom();
            if (materialFrom.isEmpty()) {
                continue;
            }
            Set<ResourceLocation> materialIds = new LinkedHashSet<>(MaterialCatalog.visualMaterialIds(materialFrom.get()));
            materialIds.addAll(layer.materials());
            for (ResourceLocation material : materialIds) {
                ToolPartSpriteKey key = new ToolPartSpriteKey(visual.id(), layer.slot(), material);
                readSprite(context, spriteGetter, textureOverrides, key.modelTextureKey())
                        .ifPresent(sprite -> sprites.put(key, ResolvedToolLayerSprite.exact(sprite.sprite(), sprite.texture())));
            }
            warnMissingRequiredSprites(visual, layer, materialIds, sprites);
        }

        TextureAtlasSprite particle = readSprite(context, spriteGetter, textureOverrides, "particle")
                .map(ResolvedToolLayerSprite::sprite)
                .orElseGet(() -> firstOrFallback(visual, sprites, missing));
        return new PartedToolSpriteSet(sprites, Map.copyOf(templateSprites), Map.copyOf(templateTextures), particle, missing);
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material) {
        return resolveLayer(toolType, slot, material, true).map(ResolvedToolLayerSprite::sprite);
    }

    public Optional<ResolvedToolLayerSprite> resolveLayer(ResourceLocation toolType, ToolVisualLayer layer, ResourceLocation material) {
        return resolveLayer(toolType, layer.slot(), material, layer.canUseTemplateFallback());
    }

    private Optional<ResolvedToolLayerSprite> resolveLayer(ResourceLocation toolType, String slot, ResourceLocation material, boolean allowTemplateFallback) {
        ResolvedToolLayerSprite exact = sprites.get(new ToolPartSpriteKey(toolType, slot, material));
        if (exact != null) {
            return Optional.of(exact);
        }
        if (!allowTemplateFallback) {
            return Optional.empty();
        }
        TextureAtlasSprite template = templateSprites.get(slot);
        ResourceLocation texture = templateTextures.get(slot);
        if (template == null || texture == null) {
            return Optional.empty();
        }
        return Optional.of(ResolvedToolLayerSprite.generated(template, ToolMaterialVisualManager.INSTANCE.tintColor(material), texture));
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material, boolean allowTemplateFallback) {
        return resolveLayer(toolType, slot, material, allowTemplateFallback).map(ResolvedToolLayerSprite::sprite);
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

    private static TextureAtlasSprite firstOrFallback(ToolVisualDefinition visual, Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites, TextureAtlasSprite missing) {
        if (!sprites.isEmpty()) {
            return sprites.values().iterator().next().sprite();
        }
        if (REPORTED_EMPTY_SPRITE_SETS.add(visual.id().toString())) {
            MobsToolForging.LOGGER.warn("No sprites were available for tool visual {}; using the missing texture sprite as particle fallback.", visual.id());
        }
        return missing;
    }

    private static void warnMissingRequiredSprites(ToolVisualDefinition visual, ToolVisualLayer layer, Set<ResourceLocation> materialIds, Map<ToolPartSpriteKey, ResolvedToolLayerSprite> sprites) {
        if (layer.optional()) {
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
}

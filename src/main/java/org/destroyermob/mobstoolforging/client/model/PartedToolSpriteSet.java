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

    private final Map<ToolPartSpriteKey, TextureAtlasSprite> sprites;
    private final TextureAtlasSprite particle;
    private final TextureAtlasSprite missing;

    private PartedToolSpriteSet(Map<ToolPartSpriteKey, TextureAtlasSprite> sprites, TextureAtlasSprite particle, TextureAtlasSprite missing) {
        this.sprites = sprites;
        this.particle = particle;
        this.missing = missing;
    }

    public static PartedToolSpriteSet from(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ToolVisualDefinition visual, Map<String, ResourceLocation> textureOverrides) {
        Map<ToolPartSpriteKey, TextureAtlasSprite> sprites = new LinkedHashMap<>();
        TextureAtlasSprite missing = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
        for (ToolVisualLayer layer : visual.layers()) {
            Optional<String> materialFrom = layer.materialFrom();
            if (materialFrom.isEmpty()) {
                continue;
            }
            Set<ResourceLocation> materialIds = new LinkedHashSet<>(MaterialCatalog.visualMaterialIds(materialFrom.get()));
            materialIds.addAll(layer.materials());
            for (ResourceLocation material : materialIds) {
                ToolPartSpriteKey key = new ToolPartSpriteKey(visual.id(), layer.slot(), material);
                readSprite(context, spriteGetter, textureOverrides, key.modelTextureKey()).ifPresent(sprite -> sprites.put(key, sprite));
            }
            warnMissingRequiredSprites(visual, layer, materialIds, sprites);
        }

        TextureAtlasSprite particle = readSprite(context, spriteGetter, textureOverrides, "particle")
                .orElseGet(() -> firstOrFallback(visual, sprites, missing));
        return new PartedToolSpriteSet(sprites, particle, missing);
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material) {
        return Optional.ofNullable(sprites.get(new ToolPartSpriteKey(toolType, slot, material)));
    }

    public TextureAtlasSprite particle() {
        return particle;
    }

    public TextureAtlasSprite missing() {
        return missing;
    }

    private static Optional<TextureAtlasSprite> readSprite(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, Map<String, ResourceLocation> textureOverrides, String key) {
        ResourceLocation explicitTexture = textureOverrides.get(key);
        if (explicitTexture != null) {
            TextureAtlasSprite sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, explicitTexture));
            warnMissingExplicitTexture(key, explicitTexture, sprite);
            return Optional.of(sprite);
        }
        if (!context.hasMaterial(key)) {
            return Optional.empty();
        }
        return Optional.of(spriteGetter.apply(context.getMaterial(key)));
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

    private static TextureAtlasSprite firstOrFallback(ToolVisualDefinition visual, Map<ToolPartSpriteKey, TextureAtlasSprite> sprites, TextureAtlasSprite missing) {
        if (!sprites.isEmpty()) {
            return sprites.values().iterator().next();
        }
        if (REPORTED_EMPTY_SPRITE_SETS.add(visual.id().toString())) {
            MobsToolForging.LOGGER.warn("No sprites were available for tool visual {}; using the missing texture sprite as particle fallback.", visual.id());
        }
        return missing;
    }

    private static void warnMissingRequiredSprites(ToolVisualDefinition visual, ToolVisualLayer layer, Set<ResourceLocation> materialIds, Map<ToolPartSpriteKey, TextureAtlasSprite> sprites) {
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

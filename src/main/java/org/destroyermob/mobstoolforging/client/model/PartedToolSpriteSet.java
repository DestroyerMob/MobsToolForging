package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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

    private final Map<ToolPartSpriteKey, TextureAtlasSprite> sprites;
    private final TextureAtlasSprite particle;

    private PartedToolSpriteSet(Map<ToolPartSpriteKey, TextureAtlasSprite> sprites, TextureAtlasSprite particle) {
        this.sprites = sprites;
        this.particle = particle;
    }

    public static PartedToolSpriteSet from(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ToolVisualDefinition visual, Map<String, ResourceLocation> textureOverrides) {
        Map<ToolPartSpriteKey, TextureAtlasSprite> sprites = new LinkedHashMap<>();
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
                .orElseGet(() -> firstOrFallback(sprites));
        return new PartedToolSpriteSet(sprites, particle);
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material) {
        return Optional.ofNullable(sprites.get(new ToolPartSpriteKey(toolType, slot, material)));
    }

    public TextureAtlasSprite particle() {
        return particle;
    }

    private static Optional<TextureAtlasSprite> readSprite(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, Map<String, ResourceLocation> textureOverrides, String key) {
        ResourceLocation explicitTexture = textureOverrides.get(key);
        if (explicitTexture != null) {
            return Optional.of(spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, explicitTexture)));
        }
        if (!context.hasMaterial(key)) {
            return Optional.empty();
        }
        return Optional.of(spriteGetter.apply(context.getMaterial(key)));
    }

    private static TextureAtlasSprite firstOrFallback(Map<ToolPartSpriteKey, TextureAtlasSprite> sprites) {
        if (!sprites.isEmpty()) {
            return sprites.values().iterator().next();
        }
        throw new IllegalStateException("Parted tool model needs at least one layer sprite");
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

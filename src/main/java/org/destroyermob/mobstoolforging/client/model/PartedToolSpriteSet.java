package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;

public final class PartedToolSpriteSet {
    private final Map<ToolPartSpriteKey, TextureAtlasSprite> sprites;
    private final TextureAtlasSprite particle;

    private PartedToolSpriteSet(Map<ToolPartSpriteKey, TextureAtlasSprite> sprites, TextureAtlasSprite particle) {
        this.sprites = sprites;
        this.particle = particle;
    }

    public static PartedToolSpriteSet from(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ToolVisualDefinition visual) {
        Map<ToolPartSpriteKey, TextureAtlasSprite> sprites = new LinkedHashMap<>();
        for (ToolVisualLayer layer : visual.layers()) {
            Optional<String> materialFrom = layer.materialFrom();
            if (materialFrom.isEmpty()) {
                continue;
            }
            for (ResourceLocation material : MaterialCatalog.visualMaterialIds(materialFrom.get())) {
                ToolPartSpriteKey key = new ToolPartSpriteKey(visual.id(), layer.slot(), material);
                readSprite(context, spriteGetter, key.modelTextureKey()).ifPresent(sprite -> sprites.put(key, sprite));
            }
        }

        TextureAtlasSprite particle = readSprite(context, spriteGetter, "particle")
                .orElseGet(() -> firstOrFallback(sprites));
        return new PartedToolSpriteSet(sprites, particle);
    }

    public Optional<TextureAtlasSprite> resolve(ResourceLocation toolType, String slot, ResourceLocation material) {
        return Optional.ofNullable(sprites.get(new ToolPartSpriteKey(toolType, slot, material)));
    }

    public TextureAtlasSprite particle() {
        return particle;
    }

    private static java.util.Optional<TextureAtlasSprite> readSprite(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, String key) {
        if (!context.hasMaterial(key)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(spriteGetter.apply(context.getMaterial(key)));
    }

    private static TextureAtlasSprite firstOrFallback(Map<ToolPartSpriteKey, TextureAtlasSprite> sprites) {
        if (!sprites.isEmpty()) {
            return sprites.values().iterator().next();
        }
        throw new IllegalStateException("Parted tool model needs at least one layer sprite");
    }
}

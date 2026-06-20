package org.destroyermob.mobstoolforging.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public record ResolvedToolLayerSprite(TextureAtlasSprite sprite, int color, ResourceLocation texture, boolean generatedFallback) {
    public static ResolvedToolLayerSprite exact(TextureAtlasSprite sprite, ResourceLocation texture) {
        return new ResolvedToolLayerSprite(sprite, 0xFFFFFFFF, texture, false);
    }

    public static ResolvedToolLayerSprite generated(TextureAtlasSprite sprite, int color, ResourceLocation texture) {
        return new ResolvedToolLayerSprite(sprite, color, texture, true);
    }
}

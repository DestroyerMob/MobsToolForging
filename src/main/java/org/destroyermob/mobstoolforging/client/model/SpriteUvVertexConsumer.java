package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

final class SpriteUvVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;
    private final int redTint;
    private final int greenTint;
    private final int blueTint;
    private final int alphaTint;

    SpriteUvVertexConsumer(VertexConsumer delegate, TextureAtlasSprite sprite) {
        this(delegate, sprite, 0xFFFFFFFF);
    }

    SpriteUvVertexConsumer(VertexConsumer delegate, TextureAtlasSprite sprite, int color) {
        this.delegate = delegate;
        this.sprite = sprite;
        this.alphaTint = color >>> 24 & 0xFF;
        this.redTint = color >>> 16 & 0xFF;
        this.greenTint = color >>> 8 & 0xFF;
        this.blueTint = color & 0xFF;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(
                red * redTint / 255,
                green * greenTint / 255,
                blue * blueTint / 255,
                alpha * alphaTint / 255
        );
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(sprite.getU(wrapUv(u)), sprite.getV(wrapUv(v)));
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }

    private static float wrapUv(float value) {
        if (value >= 0.0F && value <= 1.0F) {
            return value;
        }
        float wrapped = value - (float) Math.floor(value);
        if (wrapped == 0.0F && value > 0.0F) {
            return 1.0F;
        }
        return wrapped;
    }
}

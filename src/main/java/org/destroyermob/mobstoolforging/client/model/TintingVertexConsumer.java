package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;

final class TintingVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int redTint;
    private final int greenTint;
    private final int blueTint;
    private final int alphaTint;

    TintingVertexConsumer(VertexConsumer delegate, int color) {
        this.delegate = delegate;
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
        delegate.setUv(u, v);
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
}

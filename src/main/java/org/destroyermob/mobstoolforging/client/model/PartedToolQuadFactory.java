package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Vector3f;

public final class PartedToolQuadFactory {
    private final ModelState modelState;

    public PartedToolQuadFactory(ModelState modelState) {
        this.modelState = modelState;
    }

    public List<BakedQuad> bakeLayer(int layerIndex, TextureAtlasSprite sprite) {
        var elements = UnbakedGeometryHelper.createUnbakedItemElements(layerIndex, sprite);
        return List.copyOf(UnbakedGeometryHelper.bakeElements(elements, ignored -> sprite, modelState));
    }

    public List<BakedQuad> bakeLayer(int layerIndex, TextureAtlasSprite sprite, int color) {
        if (color == 0xFFFFFFFF) {
            return bakeLayer(layerIndex, sprite);
        }
        var elements = UnbakedGeometryHelper.createUnbakedItemElements(layerIndex, sprite, new ExtraFaceData(color, 0, 0, true));
        return List.copyOf(UnbakedGeometryHelper.bakeElements(elements, ignored -> sprite, modelState));
    }

    public List<BakedQuad> bakeMaskedLayer(int layerIndex, TextureAtlasSprite source, TextureAtlasSprite mask, int color) {
        PixelBounds sourceBounds = opaqueBounds(source);
        PixelBounds maskBounds = opaqueBounds(mask);
        List<Pixel> sourcePixels = opaquePixels(source);
        if (sourceBounds == null || maskBounds == null || sourcePixels.isEmpty()) {
            return List.of();
        }

        List<BakedQuad> quads = new ArrayList<>();
        int maskWidth = mask.contents().width();
        int maskHeight = mask.contents().height();
        int sourceWidth = source.contents().width();
        int sourceHeight = source.contents().height();
        for (int y = 0; y < maskHeight; y++) {
            for (int x = 0; x < maskWidth; x++) {
                if (mask.contents().isTransparent(0, x, y)) {
                    continue;
                }
                double sourceX = lerp(sourceBounds.minX(), sourceBounds.maxX(), normalize(x, maskBounds.minX(), maskBounds.maxX()));
                double sourceY = lerp(sourceBounds.minY(), sourceBounds.maxY(), normalize(y, maskBounds.minY(), maskBounds.maxY()));
                Pixel sample = nearest(sourcePixels, sourceX, sourceY);
                float targetX0 = 16.0F * x / maskWidth;
                float targetY0 = 16.0F * y / maskHeight;
                float targetX1 = 16.0F * (x + 1) / maskWidth;
                float targetY1 = 16.0F * (y + 1) / maskHeight;
                float sourceU0 = 16.0F * sample.x() / sourceWidth;
                float sourceV0 = 16.0F * sample.y() / sourceHeight;
                float sourceU1 = 16.0F * (sample.x() + 1) / sourceWidth;
                float sourceV1 = 16.0F * (sample.y() + 1) / sourceHeight;
                quads.addAll(bakePixel(layerIndex, source, color, targetX0, targetY0, targetX1, targetY1, sourceU0, sourceV0, sourceU1, sourceV1));
            }
        }
        return List.copyOf(quads);
    }

    private List<BakedQuad> bakePixel(int layerIndex, TextureAtlasSprite sprite, int color, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        Map<Direction, BlockElementFace> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new BlockElementFace(null, layerIndex, sprite.contents().name().toString(), new BlockFaceUV(new float[]{u0, v0, u1, v1}, 0)));
        }
        BlockElement element = new BlockElement(
                new Vector3f(x0, 16.0F - y1, 7.5F),
                new Vector3f(x1, 16.0F - y0, 8.5F),
                faces,
                null,
                true
        );
        if (color != 0xFFFFFFFF) {
            element.setFaceData(new ExtraFaceData(color, 0, 0, true));
        }
        return List.copyOf(UnbakedGeometryHelper.bakeElements(List.of(element), ignored -> sprite, modelState));
    }

    private static List<Pixel> opaquePixels(TextureAtlasSprite sprite) {
        List<Pixel> pixels = new ArrayList<>();
        for (int y = 0; y < sprite.contents().height(); y++) {
            for (int x = 0; x < sprite.contents().width(); x++) {
                if (!sprite.contents().isTransparent(0, x, y)) {
                    pixels.add(new Pixel(x, y));
                }
            }
        }
        return pixels;
    }

    private static PixelBounds opaqueBounds(TextureAtlasSprite sprite) {
        int minX = sprite.contents().width();
        int minY = sprite.contents().height();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < sprite.contents().height(); y++) {
            for (int x = 0; x < sprite.contents().width(); x++) {
                if (sprite.contents().isTransparent(0, x, y)) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        return maxX < 0 ? null : new PixelBounds(minX, minY, maxX, maxY);
    }

    private static Pixel nearest(List<Pixel> pixels, double x, double y) {
        Pixel nearest = pixels.getFirst();
        double nearestDistance = Double.MAX_VALUE;
        for (Pixel pixel : pixels) {
            double deltaX = pixel.x() - x;
            double deltaY = pixel.y() - y;
            double distance = deltaX * deltaX + deltaY * deltaY;
            if (distance < nearestDistance) {
                nearest = pixel;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static double normalize(int value, int min, int max) {
        return min == max ? 0.5D : (double) (value - min) / (max - min);
    }

    private static double lerp(int min, int max, double value) {
        return min + (max - min) * value;
    }

    private record Pixel(int x, int y) {
    }

    private record PixelBounds(int minX, int minY, int maxX, int maxY) {
    }
}

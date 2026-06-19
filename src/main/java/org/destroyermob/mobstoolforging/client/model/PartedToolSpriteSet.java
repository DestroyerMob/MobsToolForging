package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;

public final class PartedToolSpriteSet {
    private final Map<ResourceLocation, TextureAtlasSprite> heads;
    private final Map<ResourceLocation, TextureAtlasSprite> handles;
    private final TextureAtlasSprite particle;

    private PartedToolSpriteSet(Map<ResourceLocation, TextureAtlasSprite> heads, Map<ResourceLocation, TextureAtlasSprite> handles, TextureAtlasSprite particle) {
        this.heads = heads;
        this.handles = handles;
        this.particle = particle;
    }

    public static PartedToolSpriteSet from(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ToolKind toolKind) {
        Map<ResourceLocation, TextureAtlasSprite> heads = new LinkedHashMap<>();
        for (ResourceLocation material : MaterialCatalog.starterMaterialIds()) {
            readSprite(context, spriteGetter, headKey(material)).ifPresent(sprite -> heads.put(material, sprite));
        }

        Map<ResourceLocation, TextureAtlasSprite> handles = new LinkedHashMap<>();
        for (ResourceLocation material : MaterialCatalog.handleMaterialIds()) {
            readSprite(context, spriteGetter, handleKey(material)).ifPresent(sprite -> handles.put(material, sprite));
        }

        TextureAtlasSprite particle = readSprite(context, spriteGetter, "particle")
                .orElseGet(() -> firstOrFallback(heads, handles));
        return new PartedToolSpriteSet(heads, handles, particle);
    }

    public TextureAtlasSprite head(ResourceLocation material) {
        TextureAtlasSprite sprite = heads.get(material);
        if (sprite != null) {
            return sprite;
        }
        sprite = heads.get(MaterialCatalog.IRON);
        return sprite != null ? sprite : particle;
    }

    public TextureAtlasSprite handle(ResourceLocation material) {
        TextureAtlasSprite sprite = handles.get(material);
        if (sprite != null) {
            return sprite;
        }
        sprite = handles.get(MaterialCatalog.STICK);
        return sprite != null ? sprite : particle;
    }

    public TextureAtlasSprite particle() {
        return particle;
    }

    public static String headKey(ResourceLocation material) {
        return "head_" + materialKey(material);
    }

    public static String handleKey(ResourceLocation material) {
        return "handle_" + materialKey(material);
    }

    public static String materialKey(ResourceLocation material) {
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return material.getPath();
        }
        return material.getNamespace() + "_" + material.getPath().replace('/', '_');
    }

    private static java.util.Optional<TextureAtlasSprite> readSprite(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, String key) {
        if (!context.hasMaterial(key)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(spriteGetter.apply(context.getMaterial(key)));
    }

    private static TextureAtlasSprite firstOrFallback(Map<ResourceLocation, TextureAtlasSprite> heads, Map<ResourceLocation, TextureAtlasSprite> handles) {
        if (!heads.isEmpty()) {
            return heads.values().iterator().next();
        }
        if (!handles.isEmpty()) {
            return handles.values().iterator().next();
        }
        throw new IllegalStateException("Parted tool model needs at least one head or handle sprite");
    }
}

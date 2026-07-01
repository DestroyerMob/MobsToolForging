package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;

public final class ArmorMaterialTextureManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final ResourceLocation FALLBACK_TEXTURE = ResourceLocation.withDefaultNamespace("block/iron_block");
    private static final ResourceLocation VANILLA_CHAINMAIL_TEXTURE = ResourceLocation.withDefaultNamespace("item/chainmail_chestplate");
    private final Set<ResourceLocation> warnedMissingTextures = ConcurrentHashMap.newKeySet();
    private volatile Map<ResourceLocation, ResourceLocation> textures = defaults();

    public static final ArmorMaterialTextureManager INSTANCE = new ArmorMaterialTextureManager();

    private ArmorMaterialTextureManager() {
        super(GSON, "tooling/armor_material_textures");
    }

    public TextureAtlasSprite sprite(ResourceLocation materialId) {
        return sprite(texture(materialId), materialId);
    }

    public TextureAtlasSprite chainmailSprite() {
        return sprite(VANILLA_CHAINMAIL_TEXTURE, MaterialCatalog.IRON);
    }

    private TextureAtlasSprite sprite(ResourceLocation texture, ResourceLocation materialId) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        if (MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name()) && warnedMissingTextures.add(texture)) {
            MobsToolForging.LOGGER.warn("Missing modular armor material texture {} for material {}.", texture, materialId);
        }
        return sprite;
    }

    public ResourceLocation texture(ResourceLocation materialId) {
        return textures.getOrDefault(materialId, FALLBACK_TEXTURE);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ResourceLocation> loaded = new LinkedHashMap<>(defaults());
        entries.forEach((id, element) -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(element, "armor material texture");
                ResourceLocation material = ResourceLocation.parse(GsonHelper.getAsString(json, "material"));
                ResourceLocation texture = ResourceLocation.parse(GsonHelper.getAsString(json, "texture"));
                loaded.put(material, texture);
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid armor material texture {}.", id, exception);
            }
        });
        warnedMissingTextures.clear();
        textures = Map.copyOf(loaded);
    }

    private static Map<ResourceLocation, ResourceLocation> defaults() {
        Map<ResourceLocation, ResourceLocation> defaults = new LinkedHashMap<>();
        defaults.put(MaterialCatalog.IRON, ResourceLocation.withDefaultNamespace("block/iron_block"));
        defaults.put(MaterialCatalog.GOLD, ResourceLocation.withDefaultNamespace("block/gold_block"));
        defaults.put(MaterialCatalog.COPPER, ResourceLocation.withDefaultNamespace("block/copper_block"));
        defaults.put(MaterialCatalog.NETHERITE, ResourceLocation.withDefaultNamespace("block/netherite_block"));
        defaults.put(MaterialCatalog.DIAMOND, ResourceLocation.withDefaultNamespace("block/diamond_block"));
        defaults.put(MaterialCatalog.EMERALD, ResourceLocation.withDefaultNamespace("block/emerald_block"));
        defaults.put(MaterialCatalog.RUBY, ResourceLocation.withDefaultNamespace("block/redstone_block"));
        defaults.put(MaterialCatalog.SAPPHIRE, ResourceLocation.withDefaultNamespace("block/lapis_block"));
        return defaults;
    }
}

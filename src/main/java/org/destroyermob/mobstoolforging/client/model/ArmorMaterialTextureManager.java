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
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;

public final class ArmorMaterialTextureManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final String HELMET_ROLE = "helmet";
    private static final String CHESTPLATE_ROLE = "chestplate";
    private static final String LEGGINGS_ROLE = "leggings";
    private static final String BOOTS_ROLE = "boots";
    private static final ResourceLocation FALLBACK_TEXTURE = ResourceLocation.withDefaultNamespace("block/iron_block");
    private static final ResourceLocation FALLBACK_HELMET_TEXTURE = ResourceLocation.withDefaultNamespace("item/iron_helmet");
    private static final ResourceLocation FALLBACK_CHESTPLATE_TEXTURE = ResourceLocation.withDefaultNamespace("item/iron_chestplate");
    private static final ResourceLocation FALLBACK_LEGGINGS_TEXTURE = ResourceLocation.withDefaultNamespace("item/iron_leggings");
    private static final ResourceLocation FALLBACK_BOOTS_TEXTURE = ResourceLocation.withDefaultNamespace("item/iron_boots");
    private static final ResourceLocation VANILLA_CHAINMAIL_HELMET_TEXTURE = ResourceLocation.withDefaultNamespace("item/chainmail_helmet");
    private static final ResourceLocation VANILLA_CHAINMAIL_CHESTPLATE_TEXTURE = ResourceLocation.withDefaultNamespace("item/chainmail_chestplate");
    private static final ResourceLocation VANILLA_CHAINMAIL_LEGGINGS_TEXTURE = ResourceLocation.withDefaultNamespace("item/chainmail_leggings");
    private static final ResourceLocation VANILLA_CHAINMAIL_BOOTS_TEXTURE = ResourceLocation.withDefaultNamespace("item/chainmail_boots");
    private static final ResourceLocation WORN_CHAINMAIL_LAYER_1_TEXTURE = ResourceLocation.withDefaultNamespace("models/armor/chainmail_layer_1");
    private static final ResourceLocation WORN_CHAINMAIL_LAYER_2_TEXTURE = ResourceLocation.withDefaultNamespace("models/armor/chainmail_layer_2");
    private static final ResourceLocation WORN_MATERIAL_LAYER_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "models/armor/layer_1");
    private static final ResourceLocation WORN_MATERIAL_LAYER_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "models/armor/layer_2");
    private final Set<ResourceLocation> warnedMissingTextures = ConcurrentHashMap.newKeySet();
    private volatile Map<ResourceLocation, ArmorMaterialTextures> textures = defaults();

    public static final ArmorMaterialTextureManager INSTANCE = new ArmorMaterialTextureManager();

    private ArmorMaterialTextureManager() {
        super(GSON, "tooling/armor_material_textures");
    }

    public TextureAtlasSprite sprite(ResourceLocation materialId) {
        return sprite(texture(materialId), materialId);
    }

    public ResolvedArmorTexture itemTexture(ResourceLocation materialId, String partType) {
        if (isChainmailPart(partType)) {
            return resolvedTexture(chainmailTexture(partType), 0xFFFFFFFF, materialId);
        }
        ArmorItemTexture texture = materialTextures(materialId).itemTexture(roleForPart(partType));
        int color = texture.tint() ? ToolMaterialVisualManager.INSTANCE.tintColor(materialId) : 0xFFFFFFFF;
        return resolvedTexture(texture.texture(), color, materialId);
    }

    public TextureAtlasSprite geometrySprite(ResourceLocation materialId, String partType) {
        ResolvedArmorTexture texture = itemTexture(materialId, partType);
        return texture.color() == 0xFFFFFFFF ? texture.sprite() : sprite(materialId);
    }

    public TextureAtlasSprite wornMaterialSprite(ResourceLocation materialId, String partType) {
        return wornMaterialTexture(materialId, partType).sprite();
    }

    public ResolvedArmorTexture wornMaterialTexture(ResourceLocation materialId, String partType) {
        ResourceLocation wornTexture = wornArmorTexture(materialId, partType);
        if (wornTexture != null) {
            return resolvedTexture(wornTexture, 0xFFFFFFFF, materialId);
        }
        ResourceLocation tintedTexture = ArmorPartData.LEGGINGS_PLATE.equals(partType)
                ? WORN_MATERIAL_LAYER_2_TEXTURE
                : WORN_MATERIAL_LAYER_1_TEXTURE;
        return resolvedTexture(tintedTexture, ToolMaterialVisualManager.INSTANCE.tintColor(materialId), materialId);
    }

    public TextureAtlasSprite wornChainmailSprite(String partType) {
        return sprite(wornChainmailTexture(partType), MaterialCatalog.IRON);
    }

    public TextureAtlasSprite chainmailSprite() {
        return sprite(VANILLA_CHAINMAIL_CHESTPLATE_TEXTURE, MaterialCatalog.IRON);
    }

    public TextureAtlasSprite chainmailSprite(String partType) {
        return sprite(chainmailTexture(partType), MaterialCatalog.IRON);
    }

    private TextureAtlasSprite sprite(ResourceLocation texture, ResourceLocation materialId) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        if (MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name()) && warnedMissingTextures.add(texture)) {
            MobsToolForging.LOGGER.warn("Missing modular armor material texture {} for material {}.", texture, materialId);
        }
        return sprite;
    }

    public ResourceLocation texture(ResourceLocation materialId) {
        return materialTextures(materialId).texture();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ArmorMaterialTextures> loaded = new LinkedHashMap<>(defaults());
        entries.forEach((id, element) -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(element, "armor material texture");
                ResourceLocation material = ResourceLocation.parse(GsonHelper.getAsString(json, "material"));
                ResourceLocation texture = ResourceLocation.parse(GsonHelper.getAsString(json, "texture"));
                boolean tintItemTextures = GsonHelper.getAsBoolean(json, "tint_item_textures", false);
                Map<String, ArmorItemTexture> itemTextures = itemTextureDefaults(
                        fallbackItemTexture(HELMET_ROLE),
                        fallbackItemTexture(CHESTPLATE_ROLE),
                        fallbackItemTexture(LEGGINGS_ROLE),
                        fallbackItemTexture(BOOTS_ROLE)
                );
                if (json.has("item_textures")) {
                    JsonObject itemTextureJson = GsonHelper.convertToJsonObject(json.get("item_textures"), "item_textures");
                    itemTextures.put(HELMET_ROLE, readItemTexture(itemTextureJson, HELMET_ROLE, itemTextures.get(HELMET_ROLE), tintItemTextures));
                    itemTextures.put(CHESTPLATE_ROLE, readItemTexture(itemTextureJson, CHESTPLATE_ROLE, itemTextures.get(CHESTPLATE_ROLE), tintItemTextures));
                    itemTextures.put(LEGGINGS_ROLE, readItemTexture(itemTextureJson, LEGGINGS_ROLE, itemTextures.get(LEGGINGS_ROLE), tintItemTextures));
                    itemTextures.put(BOOTS_ROLE, readItemTexture(itemTextureJson, BOOTS_ROLE, itemTextures.get(BOOTS_ROLE), tintItemTextures));
                }
                loaded.put(material, new ArmorMaterialTextures(texture, Map.copyOf(itemTextures)));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid armor material texture {}.", id, exception);
            }
        });
        warnedMissingTextures.clear();
        textures = Map.copyOf(loaded);
    }

    private static ArmorItemTexture readItemTexture(JsonObject json, String role, ArmorItemTexture fallback, boolean defaultTint) {
        if (!json.has(role)) {
            return fallback;
        }
        JsonElement element = json.get(role);
        if (element.isJsonPrimitive()) {
            return new ArmorItemTexture(ResourceLocation.parse(element.getAsString()), defaultTint);
        }
        JsonObject object = GsonHelper.convertToJsonObject(element, role + " item texture");
        return new ArmorItemTexture(
                ResourceLocation.parse(GsonHelper.getAsString(object, "texture")),
                GsonHelper.getAsBoolean(object, "tint", defaultTint)
        );
    }

    private ArmorMaterialTextures materialTextures(ResourceLocation materialId) {
        return textures.getOrDefault(materialId, fallbackMaterialTextures());
    }

    private ResolvedArmorTexture resolvedTexture(ResourceLocation texture, int color, ResourceLocation materialId) {
        return new ResolvedArmorTexture(sprite(texture, materialId), color, texture);
    }

    private static String roleForPart(String partType) {
        return switch (partType) {
            case ArmorPartData.HELMET_CHAINMAIL, ArmorPartData.HELMET_PLATE -> HELMET_ROLE;
            case ArmorPartData.CHESTPLATE_CHAINMAIL, ArmorPartData.CHESTPLATE_BODY -> CHESTPLATE_ROLE;
            case ArmorPartData.LEGGINGS_CHAINMAIL, ArmorPartData.LEGGINGS_PLATE -> LEGGINGS_ROLE;
            case ArmorPartData.BOOTS_CHAINMAIL, ArmorPartData.BOOTS_PLATE -> BOOTS_ROLE;
            default -> CHESTPLATE_ROLE;
        };
    }

    private static boolean isChainmailPart(String partType) {
        return ArmorPartData.HELMET_CHAINMAIL.equals(partType)
                || ArmorPartData.CHESTPLATE_CHAINMAIL.equals(partType)
                || ArmorPartData.LEGGINGS_CHAINMAIL.equals(partType)
                || ArmorPartData.BOOTS_CHAINMAIL.equals(partType);
    }

    private static ResourceLocation chainmailTexture(String partType) {
        return switch (roleForPart(partType)) {
            case HELMET_ROLE -> VANILLA_CHAINMAIL_HELMET_TEXTURE;
            case LEGGINGS_ROLE -> VANILLA_CHAINMAIL_LEGGINGS_TEXTURE;
            case BOOTS_ROLE -> VANILLA_CHAINMAIL_BOOTS_TEXTURE;
            default -> VANILLA_CHAINMAIL_CHESTPLATE_TEXTURE;
        };
    }

    private static ResourceLocation wornChainmailTexture(String partType) {
        return ArmorPartData.LEGGINGS_CHAINMAIL.equals(partType)
                ? WORN_CHAINMAIL_LAYER_2_TEXTURE
                : WORN_CHAINMAIL_LAYER_1_TEXTURE;
    }

    private static ResourceLocation wornArmorTexture(ResourceLocation materialId, String partType) {
        String prefix = vanillaArmorLayerPrefix(materialId);
        if (prefix == null) {
            return null;
        }
        String layer = ArmorPartData.LEGGINGS_PLATE.equals(partType) ? "2" : "1";
        return ResourceLocation.withDefaultNamespace("models/armor/" + prefix + "_layer_" + layer);
    }

    private static String vanillaArmorLayerPrefix(ResourceLocation materialId) {
        if (MaterialCatalog.IRON.equals(materialId)) {
            return "iron";
        }
        if (MaterialCatalog.GOLD.equals(materialId)) {
            return "gold";
        }
        if (MaterialCatalog.DIAMOND.equals(materialId)) {
            return "diamond";
        }
        if (MaterialCatalog.NETHERITE.equals(materialId)) {
            return "netherite";
        }
        return null;
    }

    private static Map<ResourceLocation, ArmorMaterialTextures> defaults() {
        Map<ResourceLocation, ArmorMaterialTextures> defaults = new LinkedHashMap<>();
        defaults.put(MaterialCatalog.IRON, exact(ResourceLocation.withDefaultNamespace("block/iron_block"), "iron"));
        defaults.put(MaterialCatalog.GOLD, exact(ResourceLocation.withDefaultNamespace("block/gold_block"), "golden"));
        defaults.put(MaterialCatalog.COPPER, tintedLeather(ResourceLocation.withDefaultNamespace("block/copper_block")));
        defaults.put(MaterialCatalog.NETHERITE, exact(ResourceLocation.withDefaultNamespace("block/netherite_block"), "netherite"));
        defaults.put(MaterialCatalog.DIAMOND, exact(ResourceLocation.withDefaultNamespace("block/diamond_block"), "diamond"));
        defaults.put(MaterialCatalog.EMERALD, tintedLeather(ResourceLocation.withDefaultNamespace("block/emerald_block")));
        defaults.put(MaterialCatalog.RUBY, tintedLeather(ResourceLocation.withDefaultNamespace("block/redstone_block")));
        defaults.put(MaterialCatalog.SAPPHIRE, tintedLeather(ResourceLocation.withDefaultNamespace("block/lapis_block")));
        return defaults;
    }

    private static ArmorMaterialTextures exact(ResourceLocation texture, String vanillaPrefix) {
        return new ArmorMaterialTextures(texture, itemTextureDefaults(
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/" + vanillaPrefix + "_helmet"), false),
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/" + vanillaPrefix + "_chestplate"), false),
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/" + vanillaPrefix + "_leggings"), false),
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/" + vanillaPrefix + "_boots"), false)
        ));
    }

    private static ArmorMaterialTextures tintedLeather(ResourceLocation texture) {
        return new ArmorMaterialTextures(texture, itemTextureDefaults(
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/leather_helmet"), true),
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/leather_chestplate"), true),
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/leather_leggings"), true),
                new ArmorItemTexture(ResourceLocation.withDefaultNamespace("item/leather_boots"), true)
        ));
    }

    private static ArmorMaterialTextures fallbackMaterialTextures() {
        return new ArmorMaterialTextures(FALLBACK_TEXTURE, itemTextureDefaults(
                fallbackItemTexture(HELMET_ROLE),
                fallbackItemTexture(CHESTPLATE_ROLE),
                fallbackItemTexture(LEGGINGS_ROLE),
                fallbackItemTexture(BOOTS_ROLE)
        ));
    }

    private static Map<String, ArmorItemTexture> itemTextureDefaults(ArmorItemTexture helmet, ArmorItemTexture chestplate, ArmorItemTexture leggings, ArmorItemTexture boots) {
        Map<String, ArmorItemTexture> itemTextures = new LinkedHashMap<>();
        itemTextures.put(HELMET_ROLE, helmet);
        itemTextures.put(CHESTPLATE_ROLE, chestplate);
        itemTextures.put(LEGGINGS_ROLE, leggings);
        itemTextures.put(BOOTS_ROLE, boots);
        return itemTextures;
    }

    private static ArmorItemTexture fallbackItemTexture(String role) {
        return new ArmorItemTexture(switch (role) {
            case HELMET_ROLE -> FALLBACK_HELMET_TEXTURE;
            case LEGGINGS_ROLE -> FALLBACK_LEGGINGS_TEXTURE;
            case BOOTS_ROLE -> FALLBACK_BOOTS_TEXTURE;
            default -> FALLBACK_CHESTPLATE_TEXTURE;
        }, false);
    }

    public record ResolvedArmorTexture(TextureAtlasSprite sprite, int color, ResourceLocation texture) {
    }

    private record ArmorMaterialTextures(ResourceLocation texture, Map<String, ArmorItemTexture> itemTextures) {
        private ArmorItemTexture itemTexture(String role) {
            return itemTextures.getOrDefault(role, fallbackItemTexture(role));
        }
    }

    private record ArmorItemTexture(ResourceLocation texture, boolean tint) {
    }
}

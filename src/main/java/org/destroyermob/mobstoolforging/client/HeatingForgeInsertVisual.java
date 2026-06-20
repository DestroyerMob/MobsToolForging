package org.destroyermob.mobstoolforging.client;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record HeatingForgeInsertVisual(ResourceLocation model, ResourceLocation texture, ResourceLocation hotTexture, ResourceLocation glowTexture) {
    public static final ResourceLocation DEFAULT_FUEL_MODEL = modLoc("block/heating_forge/fuel_chunk");
    public static final ResourceLocation DEFAULT_FUEL_TEXTURE = ResourceLocation.withDefaultNamespace("block/coal_block");
    public static final ResourceLocation DEFAULT_WORKPIECE_MODEL = modLoc("block/heating_forge/billet");
    public static final ResourceLocation DEFAULT_WORKPIECE_TEXTURE = ResourceLocation.withDefaultNamespace("block/iron_block");

    public static HeatingForgeInsertVisual defaultFuel() {
        return new HeatingForgeInsertVisual(DEFAULT_FUEL_MODEL, DEFAULT_FUEL_TEXTURE, null, null);
    }

    public static HeatingForgeInsertVisual defaultWorkpiece() {
        return new HeatingForgeInsertVisual(DEFAULT_WORKPIECE_MODEL, DEFAULT_WORKPIECE_TEXTURE, null, null);
    }

    public static HeatingForgeInsertVisual fuelFromJson(JsonObject json, HeatingForgeInsertVisual fallback) {
        return fromJson(json, fallback, "fuel_texture", "texture");
    }

    public static HeatingForgeInsertVisual workpieceFromJson(JsonObject json, HeatingForgeInsertVisual fallback) {
        return fromJson(json, fallback, "billet_texture", "workpiece_texture", "texture");
    }

    private static HeatingForgeInsertVisual fromJson(JsonObject json, HeatingForgeInsertVisual fallback, String... textureKeys) {
        ResourceLocation model = json.has("model") ? ResourceLocation.parse(GsonHelper.getAsString(json, "model")) : fallback.model();
        ResourceLocation texture = fallback.texture();
        for (String key : textureKeys) {
            if (json.has(key)) {
                texture = ResourceLocation.parse(GsonHelper.getAsString(json, key));
                break;
            }
        }
        ResourceLocation hotTexture = readOptionalTexture(json, fallback.hotTexture(), "hot_texture", "heated_texture", "white_hot_texture");
        ResourceLocation glowTexture = readOptionalTexture(json, fallback.glowTexture(), "glow_texture", "hot_glow_texture");
        return new HeatingForgeInsertVisual(model, texture, hotTexture, glowTexture);
    }

    private static ResourceLocation readOptionalTexture(JsonObject json, ResourceLocation fallback, String... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                return ResourceLocation.parse(GsonHelper.getAsString(json, key));
            }
        }
        return fallback;
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }
}

package org.destroyermob.mobstoolforging.client;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record HeatingForgeInsertVisual(ResourceLocation model, ResourceLocation texture) {
    public static final ResourceLocation DEFAULT_FUEL_MODEL = modLoc("block/heating_forge/fuel_chunk");
    public static final ResourceLocation DEFAULT_FUEL_TEXTURE = ResourceLocation.withDefaultNamespace("item/coal");
    public static final ResourceLocation DEFAULT_WORKPIECE_MODEL = modLoc("block/heating_forge/billet");
    public static final ResourceLocation DEFAULT_WORKPIECE_TEXTURE = ResourceLocation.withDefaultNamespace("item/iron_ingot");

    public static HeatingForgeInsertVisual defaultFuel() {
        return new HeatingForgeInsertVisual(DEFAULT_FUEL_MODEL, DEFAULT_FUEL_TEXTURE);
    }

    public static HeatingForgeInsertVisual defaultWorkpiece() {
        return new HeatingForgeInsertVisual(DEFAULT_WORKPIECE_MODEL, DEFAULT_WORKPIECE_TEXTURE);
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
        return new HeatingForgeInsertVisual(model, texture);
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }
}

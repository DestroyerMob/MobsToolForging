package org.destroyermob.mobstoolforging.client;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record HeatingForgeInsertVisual(ResourceLocation model, ResourceLocation texture) {
    public static final ResourceLocation DEFAULT_FUEL_MODEL = modLoc("block/heating_forge/fuel_chunk");
    public static final ResourceLocation DEFAULT_FUEL_TEXTURE = ResourceLocation.withDefaultNamespace("block/coal_block");
    public static final ResourceLocation DEFAULT_WORKPIECE_MODEL = modLoc("block/heating_forge/billet");
    public static final ResourceLocation DEFAULT_WORKPIECE_TEXTURE = ResourceLocation.withDefaultNamespace("block/iron_block");

    public static HeatingForgeInsertVisual defaultFuel() {
        return new HeatingForgeInsertVisual(DEFAULT_FUEL_MODEL, DEFAULT_FUEL_TEXTURE);
    }

    public static HeatingForgeInsertVisual defaultWorkpiece() {
        return new HeatingForgeInsertVisual(DEFAULT_WORKPIECE_MODEL, DEFAULT_WORKPIECE_TEXTURE);
    }

    public static HeatingForgeInsertVisual fromJson(JsonObject json) {
        ResourceLocation model = ResourceLocation.parse(GsonHelper.getAsString(json, "model"));
        ResourceLocation texture = ResourceLocation.parse(GsonHelper.getAsString(json, "texture"));
        return new HeatingForgeInsertVisual(model, texture);
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }
}

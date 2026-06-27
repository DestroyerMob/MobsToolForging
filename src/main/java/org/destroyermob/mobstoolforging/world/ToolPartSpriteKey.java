package org.destroyermob.mobstoolforging.world;

import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record ToolPartSpriteKey(ResourceLocation toolType, String slot, ResourceLocation material) {
    public String modelTextureKey() {
        return modelTextureKey(slot, material);
    }

    public static String modelTextureKey(String slot, ResourceLocation material) {
        return "layer_" + sanitize(slot) + "_" + materialKey(material);
    }

    public static String handleBodyTextureKey(ResourceLocation material) {
        return "layer_handle_body_" + materialKey(material);
    }

    public static String materialKey(ResourceLocation material) {
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return sanitize(material.getPath());
        }
        return sanitize(material.getNamespace() + "_" + material.getPath());
    }

    private static String sanitize(String value) {
        return value.replace('/', '_').replace('-', '_');
    }
}

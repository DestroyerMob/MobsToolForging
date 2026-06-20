package org.destroyermob.mobstoolforging.world;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;

public record ForgeTemplateDefinition(
        ResourceLocation id,
        ResourceLocation toolType,
        String partType,
        int requiredMaterials,
        int requiredHits,
        String translationKey,
        float minimumTemperature
) {
    public ForgeTemplateDefinition(ResourceLocation id, ResourceLocation toolType, String partType, int requiredMaterials, int requiredHits, String translationKey) {
        this(id, toolType, partType, requiredMaterials, requiredHits, translationKey, Float.NaN);
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    @Override
    public float minimumTemperature() {
        if (Float.isNaN(minimumTemperature)) {
            return MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue();
        }
        return Math.max(0.0F, Math.min(1.0F, minimumTemperature));
    }

    public float minimumTemperatureOverride() {
        return minimumTemperature;
    }

    public ItemStack outputStack(ResourceLocation materialId) {
        return ToolTypeRegistry.createPart(this, materialId);
    }
}

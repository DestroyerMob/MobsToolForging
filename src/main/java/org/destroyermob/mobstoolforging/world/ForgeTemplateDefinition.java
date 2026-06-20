package org.destroyermob.mobstoolforging.world;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ForgeTemplateDefinition(
        ResourceLocation id,
        ResourceLocation toolType,
        String partType,
        int requiredMaterials,
        int requiredHits,
        String translationKey
) {
    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public ItemStack outputStack(ResourceLocation materialId) {
        return ToolTypeRegistry.createPart(this, materialId);
    }
}

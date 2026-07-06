package org.destroyermob.mobstoolforging.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ForgeTemplatePreview {
    private ForgeTemplatePreview() {
    }

    public static ItemStack stack(ForgeTemplateDefinition template) {
        ItemStack preview = stack(template, material(template));
        return preview.isEmpty() ? ItemStack.EMPTY : preview;
    }

    public static ItemStack stack(ForgeTemplateDefinition template, ResourceLocation material) {
        return template.outputStack(material);
    }

    public static ResourceLocation material(ForgeTemplateDefinition template) {
        if (isUsableMaterial(template, MaterialCatalog.IRON)) {
            return MaterialCatalog.IRON;
        }
        for (ResourceLocation material : MaterialCatalog.starterMaterialIds()) {
            if (isUsableMaterial(template, material)) {
                return material;
            }
        }
        return MaterialCatalog.IRON;
    }

    public static boolean isUsableMaterial(ForgeTemplateDefinition template, ResourceLocation material) {
        return template.allowsMaterial(material) && !stack(template, material).isEmpty();
    }
}

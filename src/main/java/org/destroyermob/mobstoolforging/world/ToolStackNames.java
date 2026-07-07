package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ToolStackNames {
    private ToolStackNames() {
    }

    public static void applyPartName(ItemStack stack, String partType, ResourceLocation materialId) {
        if (!stack.isEmpty()) {
            stack.set(DataComponents.ITEM_NAME, partName(partType, materialId));
        }
    }

    public static void applyCoatedPartName(ItemStack stack, String partType, ResourceLocation baseMaterialId, ResourceLocation coatingMaterialId) {
        if (!stack.isEmpty()) {
            stack.set(DataComponents.ITEM_NAME, coatedPartName(partType, baseMaterialId, coatingMaterialId));
        }
    }

    public static void applyToolName(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
        if (!stack.isEmpty()) {
            stack.set(DataComponents.ITEM_NAME, toolName(definition, construction));
        }
    }

    public static Component partName(String partType, ResourceLocation materialId) {
        return materialPrefix(materialId, titleCase(partType));
    }

    public static Component coatedPartName(String partType, ResourceLocation baseMaterialId, ResourceLocation coatingMaterialId) {
        return materialPairPrefix(baseMaterialId, coatingMaterialId, titleCase(partType));
    }

    public static Component toolName(ToolTypeDefinition definition, ToolConstructionData construction) {
        return construction.headBaseMaterial()
                .map(baseMaterial -> materialPairPrefix(baseMaterial, construction.headMaterial(), titleCase(definition.id().getPath())))
                .orElseGet(() -> toolName(definition, construction.headMaterial()));
    }

    public static Component toolName(ToolTypeDefinition definition, ResourceLocation materialId) {
        return materialPrefix(materialId, titleCase(definition.id().getPath()));
    }

    private static Component materialPrefix(ResourceLocation materialId, String noun) {
        return Component.empty()
                .append(MaterialCatalog.displayName(materialId))
                .append(" ")
                .append(Component.literal(noun));
    }

    private static Component materialPairPrefix(ResourceLocation baseMaterialId, ResourceLocation coatingMaterialId, String noun) {
        return Component.empty()
                .append(MaterialCatalog.displayName(baseMaterialId))
                .append("-")
                .append(MaterialCatalog.displayName(coatingMaterialId))
                .append(" ")
                .append(Component.literal(noun));
    }

    private static String titleCase(String value) {
        String[] words = value.replace('-', '_').split("_+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase(java.util.Locale.ROOT));
            }
        }
        return result.isEmpty() ? value : result.toString();
    }
}

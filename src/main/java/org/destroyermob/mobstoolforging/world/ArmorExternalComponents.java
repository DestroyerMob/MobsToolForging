package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ArmorExternalComponents {
    private ArmorExternalComponents() {
    }

    public static void copyArmorPartComponentsToArmor(ItemStack basePart, ItemStack platePart, ItemStack armor) {
        ArmorConstructionData construction = armor.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        ArmorPartData plateData = platePart.get(ModDataComponents.ARMOR_PART.get());
        if (construction != null && plateData != null && plateData.coatingBaseMaterial().isPresent()) {
            armor.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), construction.withOverlayBaseMaterial(plateData.coatingBaseMaterial()));
        }
        ToolExternalComponents.copyCompatibleExternalComponentsWithoutAffixes(basePart, armor);
        ToolExternalComponents.copyCompatibleExternalComponentsWithoutAffixes(platePart, armor);
    }

    public static List<ItemStack> copyArmorComponentsToPrimaryPart(ItemStack armor, List<ItemStack> parts) {
        return copyArmorComponentsToPrimaryPart(armor, parts, true);
    }

    public static List<ItemStack> copyArmorComponentsToPrimaryPartWithoutAffixes(ItemStack armor, List<ItemStack> parts) {
        return copyArmorComponentsToPrimaryPart(armor, parts, false);
    }

    private static List<ItemStack> copyArmorComponentsToPrimaryPart(ItemStack armor, List<ItemStack> parts, boolean includeAffixes) {
        if (armor.isEmpty() || parts.isEmpty()) {
            return List.copyOf(parts);
        }

        int targetIndex = primaryPartIndex(parts);
        if (targetIndex < 0) {
            return List.copyOf(parts);
        }

        List<ItemStack> result = new ArrayList<>(parts.size());
        for (int index = 0; index < parts.size(); index++) {
            ItemStack copy = parts.get(index).copy();
            if (index == targetIndex) {
                if (includeAffixes) {
                    ToolExternalComponents.copyCompatibleExternalComponents(armor, copy);
                } else {
                    ToolExternalComponents.copyCompatibleExternalComponentsWithoutAffixes(armor, copy);
                }
            }
            result.add(copy);
        }
        return List.copyOf(result);
    }

    private static int primaryPartIndex(List<ItemStack> parts) {
        int baseIndex = -1;
        for (int index = 0; index < parts.size(); index++) {
            ArmorPartData data = parts.get(index).get(ModDataComponents.ARMOR_PART.get());
            if (data == null) {
                continue;
            }
            if (!isChainmailPart(data)) {
                return index;
            }
            if (baseIndex < 0) {
                baseIndex = index;
            }
        }
        return baseIndex;
    }

    private static boolean isChainmailPart(ArmorPartData data) {
        return ArmorPartData.HELMET_CHAINMAIL.equals(data.partType())
                || ArmorPartData.CHESTPLATE_CHAINMAIL.equals(data.partType())
                || ArmorPartData.LEGGINGS_CHAINMAIL.equals(data.partType())
                || ArmorPartData.BOOTS_CHAINMAIL.equals(data.partType());
    }
}

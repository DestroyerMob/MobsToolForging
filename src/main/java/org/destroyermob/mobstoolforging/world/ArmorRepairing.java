package org.destroyermob.mobstoolforging.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.item.ModularArmorItem;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ArmorRepairing {
    private static final float MATERIAL_REPAIR_FRACTION = 0.25F;

    private ArmorRepairing() {
    }

    public static boolean isModularArmor(ItemStack stack) {
        return stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null;
    }

    public static boolean isRepairableModularArmor(ItemStack stack) {
        return isModularArmor(stack) && stack.isDamageableItem() && stack.getDamageValue() > 0;
    }

    public static boolean canRepairAt(ItemStack stack, WorkstationKind workstationKind) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !isRepairableModularArmor(stack)) {
            return false;
        }
        return construction.hasLeatherBase()
                ? workstationKind == WorkstationKind.LEATHER_STATION
                : workstationKind.isSmithingAnvilLike();
    }

    public static boolean isRepairMaterial(ItemStack armor, ItemStack candidate) {
        if (armor.isEmpty() || candidate.isEmpty()) {
            return false;
        }
        ArmorConstructionData construction = armor.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
            return false;
        }
        ResourceLocation materialId = repairMaterial(construction);
        return MaterialCatalog.definition(materialId)
                .map(ToolMaterialDefinition::tier)
                .map(tier -> tier.getRepairIngredient().test(candidate))
                .orElse(false);
    }

    public static boolean shouldBlockVanillaAnvilUse(ItemStack left) {
        return isModularArmor(left);
    }

    public static ItemStack repairWithOneMaterial(ItemStack stack) {
        if (!isRepairableModularArmor(stack)) {
            return ItemStack.EMPTY;
        }
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
            return ItemStack.EMPTY;
        }

        ItemStack repaired = stack.copyWithCount(1);
        ArmorStatsCatalog.applyPreservingDamage(repaired, construction);
        int maxDamage = Math.max(1, repaired.getMaxDamage());
        int repairAmount = Math.max(1, Math.round(maxDamage * MATERIAL_REPAIR_FRACTION));
        int damageAfterRepair = Math.max(0, repaired.getDamageValue() - repairAmount);
        if (repaired.isDamageableItem()) {
            repaired.setDamageValue(Math.min(damageAfterRepair, Math.max(0, repaired.getMaxDamage() - 1)));
        }
        if (repaired.getItem() instanceof ModularArmorItem modularArmor) {
            modularArmor.refreshBrokenArmor(repaired);
        }
        return repaired;
    }

    private static ResourceLocation repairMaterial(ArmorConstructionData construction) {
        if (construction.hasLeatherBase()) {
            return MaterialCatalog.LEATHER;
        }
        return construction.overlayMaterial().orElseGet(construction::chainmailMaterial);
    }
}

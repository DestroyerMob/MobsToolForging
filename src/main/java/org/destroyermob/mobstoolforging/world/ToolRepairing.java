package org.destroyermob.mobstoolforging.world;

import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolRepairing {
    private static final float MATERIAL_REPAIR_FRACTION = 0.25F;

    private ToolRepairing() {
    }

    public static boolean isModularTool(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return construction != null && ToolTypeRegistry.toolType(construction.toolType()).isPresent();
    }

    public static boolean isRepairableModularTool(ItemStack stack) {
        return isModularTool(stack) && stack.isDamageableItem() && stack.getDamageValue() > 0;
    }

    public static boolean isRepairMaterial(ItemStack tool, ItemStack candidate) {
        if (tool.isEmpty() || candidate.isEmpty()) {
            return false;
        }
        ToolConstructionData construction = tool.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null) {
            return false;
        }
        return MaterialCatalog.definition(construction.headMaterial())
                .map(ToolMaterialDefinition::tier)
                .map(tier -> tier.getRepairIngredient().test(candidate))
                .orElse(false);
    }

    public static boolean shouldBlockVanillaAnvilRepair(ItemStack left, ItemStack right) {
        if (!isModularTool(left) || right.isEmpty()) {
            return false;
        }
        return isModularTool(right) || isRepairMaterial(left, right);
    }

    public static ItemStack repairWithOneMaterial(ItemStack stack) {
        if (!isRepairableModularTool(stack)) {
            return ItemStack.EMPTY;
        }
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null) {
            return ItemStack.EMPTY;
        }
        ToolTypeDefinition definition = ToolTypeRegistry.toolType(construction.toolType()).orElse(null);
        if (definition == null) {
            return ItemStack.EMPTY;
        }

        ItemStack repaired = stack.copyWithCount(1);
        int maxDamage = Math.max(1, repaired.getMaxDamage());
        int damageAfterRepair = Math.max(0, repaired.getDamageValue() - Math.max(1, Math.round(maxDamage * MATERIAL_REPAIR_FRACTION)));
        boolean wasBroken = Boolean.TRUE.equals(repaired.get(ModDataComponents.TOOL_BROKEN.get()));
        if (wasBroken) {
            repaired.remove(ModDataComponents.TOOL_BROKEN.get());
            ToolStatBuilder.apply(repaired, definition, construction);
        }
        if (repaired.isDamageableItem()) {
            repaired.setDamageValue(Math.min(damageAfterRepair, Math.max(0, repaired.getMaxDamage() - 1)));
        }
        if (repaired.getDamageValue() <= 0) {
            repaired.remove(ModDataComponents.TOOL_BROKEN.get());
        }
        return repaired;
    }
}

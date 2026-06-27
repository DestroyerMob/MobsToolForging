package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolPartWear {
    public static final int SCALE = 10000;

    private ToolPartWear() {
    }

    public static void applyStoredWear(ItemStack tool, ItemStack primaryHead) {
        int wear = storedWear(primaryHead);
        if (wear <= 0 || !tool.isDamageableItem()) {
            return;
        }

        int maxDamage = Math.max(1, tool.getMaxDamage());
        int damage = (int) Math.ceil(wear * (double) maxDamage / SCALE);
        tool.setDamageValue(Math.min(Math.max(0, damage), Math.max(0, maxDamage - 1)));
    }

    public static List<ItemStack> copyWithWearFromTool(ToolTypeDefinition definition, ItemStack tool, List<ItemStack> parts) {
        List<ItemStack> result = new ArrayList<>(parts.size());
        boolean applied = false;
        for (ItemStack part : parts) {
            ItemStack copy = part.copy();
            if (!applied && isPrimaryHead(definition, copy)) {
                storeFromTool(tool, copy);
                applied = true;
            } else {
                copy.remove(ModDataComponents.TOOL_PART_WEAR.get());
            }
            result.add(copy);
        }
        return List.copyOf(result);
    }

    public static int remainingDurabilityPercent(ItemStack part) {
        int wear = storedWear(part);
        if (wear <= 0) {
            return 100;
        }
        return Math.max(0, Math.min(100, Math.round((SCALE - wear) * 100.0F / SCALE)));
    }

    private static void storeFromTool(ItemStack tool, ItemStack primaryHead) {
        if (!tool.isDamageableItem() || tool.getMaxDamage() <= 0 || tool.getDamageValue() <= 0) {
            primaryHead.remove(ModDataComponents.TOOL_PART_WEAR.get());
            return;
        }

        int wear = (int) Math.ceil(tool.getDamageValue() * (double) SCALE / tool.getMaxDamage());
        if (wear <= 0) {
            primaryHead.remove(ModDataComponents.TOOL_PART_WEAR.get());
            return;
        }
        primaryHead.set(ModDataComponents.TOOL_PART_WEAR.get(), clampWear(wear));
    }

    private static boolean isPrimaryHead(ToolTypeDefinition definition, ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        return data != null
                && definition.primaryPartType().equals(data.partType())
                && definition.matchesPartItem(data.partType(), data.materialId(), stack);
    }

    private static int storedWear(ItemStack stack) {
        Integer wear = stack.get(ModDataComponents.TOOL_PART_WEAR.get());
        return wear == null ? 0 : clampWear(wear);
    }

    private static int clampWear(int wear) {
        if (wear < 0) {
            return 0;
        }
        if (wear > SCALE) {
            return SCALE;
        }
        return wear;
    }
}

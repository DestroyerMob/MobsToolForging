package org.destroyermob.mobstoolforging.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;

public final class ModularArmorDyeing {
    private ModularArmorDyeing() {
    }

    public static boolean isDyeable(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        return stack.getItem() instanceof ModularArmorItem
                && construction != null
                && construction.hasLeatherBase();
    }

    public static Optional<Integer> dyedColor(ItemStack stack) {
        if (!isDyeable(stack)) {
            return Optional.empty();
        }
        DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
        return color == null
                ? Optional.empty()
                : Optional.of(FastColor.ARGB32.opaque(color.rgb()));
    }

    public static int color(ItemStack stack, int defaultColor) {
        return dyedColor(stack).orElse(defaultColor);
    }

    public static ItemStack applyDyes(ItemStack stack, List<DyeItem> dyes) {
        if (!isDyeable(stack) || dyes.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stack.copyWithCount(1);
        int red = 0;
        int green = 0;
        int blue = 0;
        int maximum = 0;
        int colorCount = 0;
        DyedItemColor existingColor = result.get(DataComponents.DYED_COLOR);
        if (existingColor != null) {
            int existingRed = FastColor.ARGB32.red(existingColor.rgb());
            int existingGreen = FastColor.ARGB32.green(existingColor.rgb());
            int existingBlue = FastColor.ARGB32.blue(existingColor.rgb());
            maximum += Math.max(existingRed, Math.max(existingGreen, existingBlue));
            red += existingRed;
            green += existingGreen;
            blue += existingBlue;
            colorCount++;
        }

        for (DyeItem dye : dyes) {
            int color = dye.getDyeColor().getTextureDiffuseColor();
            int dyeRed = FastColor.ARGB32.red(color);
            int dyeGreen = FastColor.ARGB32.green(color);
            int dyeBlue = FastColor.ARGB32.blue(color);
            maximum += Math.max(dyeRed, Math.max(dyeGreen, dyeBlue));
            red += dyeRed;
            green += dyeGreen;
            blue += dyeBlue;
            colorCount++;
        }

        int mixedRed = red / colorCount;
        int mixedGreen = green / colorCount;
        int mixedBlue = blue / colorCount;
        float averageMaximum = (float) maximum / (float) colorCount;
        float mixedMaximum = (float) Math.max(mixedRed, Math.max(mixedGreen, mixedBlue));
        mixedRed = (int) ((float) mixedRed * averageMaximum / mixedMaximum);
        mixedGreen = (int) ((float) mixedGreen * averageMaximum / mixedMaximum);
        mixedBlue = (int) ((float) mixedBlue * averageMaximum / mixedMaximum);

        int mixedColor = FastColor.ARGB32.color(0, mixedRed, mixedGreen, mixedBlue);
        boolean showInTooltip = existingColor == null || existingColor.showInTooltip();
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(mixedColor, showInTooltip));
        return result;
    }
}

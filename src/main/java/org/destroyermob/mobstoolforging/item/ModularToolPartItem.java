package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.Metallurgy;
import org.destroyermob.mobstoolforging.world.MetallurgyTooltips;
import org.destroyermob.mobstoolforging.world.ForgingQuality;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolPartWear;
import org.destroyermob.mobstoolforging.world.ToolStackNames;

public class ModularToolPartItem extends Item {
    private final String partType;

    public ModularToolPartItem(ToolKind toolKind, Properties properties) {
        this(toolKind, toolKind.partType(), properties);
    }

    public ModularToolPartItem(ToolKind toolKind, String partType, Properties properties) {
        this(partType, properties);
    }

    public ModularToolPartItem(String partType, Properties properties) {
        super(properties);
        this.partType = partType;
    }

    public ItemStack createPart(ResourceLocation materialId) {
        return createPart(materialId, ToolPartData.DEFAULT_QUALITY);
    }

    public ItemStack createPart(ResourceLocation materialId, int quality) {
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.TOOL_PART.get(), new ToolPartData(partType, materialId, quality));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data != null && partType.equals(data.partType())) {
            if (ToolPartData.CROSSBOW_BODY.equals(partType)) {
                return super.getName(stack);
            }
            if (data.coatingBaseMaterial().isPresent()) {
                return ToolStackNames.coatedPartName(partType, data.coatingBaseMaterial().get(), data.materialId());
            }
            return Component.translatable("item.mobstoolforging.material_" + partType, MaterialCatalog.displayName(data.materialId()));
        }
        return super.getName(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data != null) {
            int effectiveQuality = Metallurgy.adjustedQuality(stack, data.effectiveQuality());
            tooltip.add(Component.translatable("tooltip.mobstoolforging.quality")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(ForgingQuality.fromScore(effectiveQuality).displayName()));
            if (flag.hasShiftDown()) {
                tooltip.add(Component.translatable("tooltip.mobstoolforging.workmanship_score", effectiveQuality, data.quality())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            if (data.finishAffectsQuality()) {
                tooltip.add(Component.translatable(data.isCoated() ? "tooltip.mobstoolforging.core_finish" : "tooltip.mobstoolforging.finish")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(data.finish().displayName()));
            }
            data.coatingBaseMaterial().ifPresent(baseMaterial -> tooltip.add(Component.translatable(
                    "tooltip.mobstoolforging.coating_base",
                    MaterialCatalog.displayName(baseMaterial)
            ).withStyle(ChatFormatting.DARK_GRAY)));
            if (data.treatment().isPresent()) {
                tooltip.add(Component.translatable("tooltip.mobstoolforging.part_treatment", MaterialCatalog.displayName(data.treatment().get())).withStyle(ChatFormatting.DARK_GRAY));
            }
            MetallurgyTooltips.appendPart(tooltip, stack, flag.hasShiftDown());
        }
        int remainingDurability = ToolPartWear.remainingDurabilityPercent(stack);
        if (remainingDurability < 100) {
            tooltip.add(Component.translatable("tooltip.mobstoolforging.part_durability", remainingDurability).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ToolPartWear.remainingDurabilityPercent(stack) < 100;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(ToolPartWear.remainingDurabilityPercent(stack) * 13.0F / 100.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float durability = ToolPartWear.remainingDurabilityPercent(stack) / 100.0F;
        return Mth.hsvToRgb(Math.max(0.0F, durability) / 3.0F, 1.0F, 1.0F);
    }
}

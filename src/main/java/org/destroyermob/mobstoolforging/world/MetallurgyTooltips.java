package org.destroyermob.mobstoolforging.world;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public final class MetallurgyTooltips {
    private MetallurgyTooltips() {
    }

    public static void appendPart(List<Component> tooltip, ItemStack stack, boolean detailed) {
        MetallurgyData data = stack.get(org.destroyermob.mobstoolforging.registry.ModDataComponents.METALLURGY.get());
        if (data == null) {
            return;
        }
        tooltip.add(valueLine("tooltip.mobstoolforging.metallurgy.origin", "metallurgy.mobstoolforging.origin." + data.origin().getSerializedName(), color(data.origin())));
        if (data.castDefect() != MetallurgyData.CastDefect.NONE) {
            tooltip.add(valueLine("tooltip.mobstoolforging.metallurgy.defect", "metallurgy.mobstoolforging.defect." + data.castDefect().getSerializedName(), ChatFormatting.RED));
        }
        tooltip.add(valueLine("tooltip.mobstoolforging.metallurgy.heat_treatment", "metallurgy.mobstoolforging.heat_treatment." + data.heatTreatment().getSerializedName(), heatColor(data.heatTreatment())));
        if (detailed && !data.composition().isEmpty()) {
            tooltip.add(Component.translatable("tooltip.mobstoolforging.metallurgy.composition", composition(data)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void appendToolParts(List<Component> tooltip, ItemStack tool) {
        ToolAssemblyParts parts = tool.get(org.destroyermob.mobstoolforging.registry.ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (parts == null) {
            return;
        }
        boolean heading = false;
        for (ItemStack part : parts.stacks()) {
            MetallurgyData data = part.get(org.destroyermob.mobstoolforging.registry.ModDataComponents.METALLURGY.get());
            if (data == null) {
                continue;
            }
            if (!heading) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("tooltip.mobstoolforging.metallurgy.parts").withStyle(ChatFormatting.GRAY));
                heading = true;
            }
            tooltip.add(Component.translatable(
                    "tooltip.mobstoolforging.metallurgy.part_summary",
                    part.getHoverName(),
                    Component.translatable("metallurgy.mobstoolforging.origin." + data.origin().getSerializedName()),
                    Component.translatable("metallurgy.mobstoolforging.heat_treatment." + data.heatTreatment().getSerializedName())
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static MutableComponent valueLine(String label, String value, ChatFormatting color) {
        return Component.translatable(label).withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(value).withStyle(color));
    }

    private static Component composition(MetallurgyData data) {
        MutableComponent value = Component.empty();
        int total = data.composition().values().stream().mapToInt(Integer::intValue).sum();
        int index = 0;
        for (var entry : data.composition().entrySet()) {
            if (index++ > 0) {
                value.append(Component.literal(" + "));
            }
            int percent = total <= 0 ? 0 : Math.round(entry.getValue() * 100.0F / total);
            value.append(Component.translatable("tooltip.mobstoolforging.metallurgy.composition_entry", MaterialCatalog.displayName(entry.getKey()), percent));
        }
        return value;
    }

    private static ChatFormatting color(MetallurgyData.Origin origin) {
        return origin == MetallurgyData.Origin.CAST ? ChatFormatting.RED
                : origin == MetallurgyData.Origin.REFORGED ? ChatFormatting.GREEN : ChatFormatting.WHITE;
    }

    private static ChatFormatting heatColor(MetallurgyData.HeatTreatment treatment) {
        return switch (treatment) {
            case BRITTLE -> ChatFormatting.RED;
            case HARDENED -> ChatFormatting.AQUA;
            case TEMPERED -> ChatFormatting.GREEN;
            case TEMPERING -> ChatFormatting.GOLD;
            default -> ChatFormatting.GRAY;
        };
    }
}

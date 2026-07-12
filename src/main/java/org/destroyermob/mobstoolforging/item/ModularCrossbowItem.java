package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.CrossbowAssembly;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;

/** A vanilla-behaving crossbow whose construction is retained as MTF components. */
public class ModularCrossbowItem extends CrossbowItem {
    public ModularCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (CrossbowAssembly.isCrossbow(construction)) {
            return Component.translatable("item.mobstoolforging.material_crossbow", MaterialCatalog.displayName(construction.headMaterial()));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (!CrossbowAssembly.isCrossbow(construction)) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.mobstoolforging.crossbow_limbs", MaterialCatalog.displayName(construction.headMaterial())).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.mobstoolforging.crossbow_body").withStyle(ChatFormatting.DARK_GRAY));
        construction.guardMaterial().ifPresent(material -> tooltip.add(Component.translatable("tooltip.mobstoolforging.crossbow_string", MaterialCatalog.displayName(material)).withStyle(ChatFormatting.DARK_GRAY)));
    }
}

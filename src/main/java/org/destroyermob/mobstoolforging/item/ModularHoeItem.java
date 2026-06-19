package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularHoeItem extends HoeItem implements ModularToolItem {
    public ModularHoeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(HoeItem.createAttributes(Tiers.IRON, -2.0F, -1.0F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.HOE;
    }

    @Override
    public Component getName(ItemStack stack) {
        return getModularName(stack, super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        appendModularTooltip(stack, tooltip, flag);
    }
}

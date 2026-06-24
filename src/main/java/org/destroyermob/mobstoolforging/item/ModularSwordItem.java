package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularSwordItem extends SwordItem implements ModularToolItem {
    public ModularSwordItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.SWORD;
    }

    @Override
    public Component getName(ItemStack stack) {
        return getModularName(stack, super.getName(stack));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return allowsFinishedToolEnchanting(stack, super.isEnchantable(stack));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return finishedToolEnchantmentValue(stack, super.getEnchantmentValue(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        appendModularTooltip(stack, tooltip, flag);
    }
}

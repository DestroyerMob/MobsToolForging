package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
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
}

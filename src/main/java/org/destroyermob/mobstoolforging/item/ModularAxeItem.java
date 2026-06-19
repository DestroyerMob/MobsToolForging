package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularAxeItem extends AxeItem implements ModularToolItem {
    public ModularAxeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(AxeItem.createAttributes(Tiers.IRON, 6.0F, -3.1F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.AXE;
    }

    @Override
    public Component getName(ItemStack stack) {
        return getModularName(stack, super.getName(stack));
    }
}

package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularShovelItem extends ShovelItem implements ModularToolItem {
    public ModularShovelItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(ShovelItem.createAttributes(Tiers.IRON, 1.5F, -3.0F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.SHOVEL;
    }

    @Override
    public Component getName(ItemStack stack) {
        return getModularName(stack, super.getName(stack));
    }
}

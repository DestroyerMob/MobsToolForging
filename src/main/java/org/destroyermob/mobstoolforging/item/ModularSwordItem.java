package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
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
}

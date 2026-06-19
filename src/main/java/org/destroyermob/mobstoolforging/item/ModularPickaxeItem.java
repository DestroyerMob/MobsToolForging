package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularPickaxeItem extends PickaxeItem implements ModularToolItem {
    public ModularPickaxeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(PickaxeItem.createAttributes(Tiers.IRON, 1.0F, -2.8F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.PICKAXE;
    }

    @Override
    public Component getName(ItemStack stack) {
        return getModularName(stack, super.getName(stack));
    }
}

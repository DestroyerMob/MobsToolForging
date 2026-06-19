package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularToolPartItem extends Item {
    private final ToolKind toolKind;

    public ModularToolPartItem(ToolKind toolKind, Properties properties) {
        super(properties);
        this.toolKind = toolKind;
    }

    public ItemStack createPart(ResourceLocation materialId) {
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.TOOL_PART.get(), new ToolPartData(toolKind.partType(), materialId));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data != null && toolKind.partType().equals(data.partType())) {
            return toolKind.partName(data.materialId());
        }
        return super.getName(stack);
    }
}

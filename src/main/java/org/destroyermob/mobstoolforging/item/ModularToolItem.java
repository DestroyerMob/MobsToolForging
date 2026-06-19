package org.destroyermob.mobstoolforging.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;

public interface ModularToolItem {
    ToolKind toolKind();

    default ItemStack create(ResourceLocation headMaterialId, ItemStack handle) {
        ResourceLocation handleMaterial = MaterialCatalog.handleMaterial(handle);
        ItemStack stack = new ItemStack((Item) this);
        stack.set(ModDataComponents.TOOL_CONSTRUCTION.get(), ToolConstructionData.basic(toolKind(), headMaterialId, handleMaterial));
        MaterialCatalog.applyToolComponents(stack, headMaterialId, toolKind());
        return stack;
    }

    default Component getModularName(ItemStack stack, Component fallback) {
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return data == null ? fallback : toolKind().toolName(data.headMaterial());
    }
}

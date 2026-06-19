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
        return create(ToolConstructionData.basic(toolKind(), headMaterialId, handleMaterial));
    }

    default ItemStack create(ToolConstructionData construction) {
        ItemStack stack = new ItemStack((Item) this);
        stack.set(ModDataComponents.TOOL_CONSTRUCTION.get(), construction);
        MaterialCatalog.applyToolComponents(stack, construction.headMaterial(), toolKind());
        return stack;
    }

    default Component getModularName(ItemStack stack, Component fallback) {
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return data == null ? fallback : toolKind().toolName(data.headMaterial());
    }
}

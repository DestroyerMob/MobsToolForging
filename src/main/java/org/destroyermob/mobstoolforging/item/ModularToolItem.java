package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolStatBuilder;
import org.destroyermob.mobstoolforging.world.ToolTooltipBuilder;

public interface ModularToolItem {
    ToolKind toolKind();

    default ItemStack create(ResourceLocation headMaterialId, ItemStack handle) {
        ResourceLocation handleMaterial = MaterialCatalog.handleMaterial(handle);
        return create(ToolConstructionData.basic(toolKind(), headMaterialId, handleMaterial));
    }

    default ItemStack create(ToolConstructionData construction) {
        ItemStack stack = new ItemStack((Item) this);
        stack.set(ModDataComponents.TOOL_CONSTRUCTION.get(), construction);
        ToolStatBuilder.apply(stack, toolKind(), construction);
        return stack;
    }

    default Component getModularName(ItemStack stack, Component fallback) {
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return data == null ? fallback : toolKind().toolName(data.headMaterial());
    }

    default void appendModularTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (data == null) {
            return;
        }
        tooltip.addAll(ToolTooltipBuilder.tooltip(stack, toolKind(), flag));
    }
}

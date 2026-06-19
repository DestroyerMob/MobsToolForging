package org.destroyermob.mobstoolforging.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
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

    default void appendModularTooltip(ItemStack stack, List<Component> tooltip) {
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (data == null) {
            return;
        }
        tooltip.add(materialLine("Head", Optional.of(data.headMaterial())));
        tooltip.add(materialLine("Handle", Optional.of(data.handleMaterial())));
        tooltip.add(materialLine(toolKind() == ToolKind.SWORD ? "Guard" : "Binding", data.bindingMaterial()));
        tooltip.add(materialLine("Wrap", data.wrapMaterial()));
        tooltip.add(materialLine("Focus", data.focusMaterial()));
        tooltip.add(materialLine("Treatment", data.treatment()));
    }

    private static Component materialLine(String label, Optional<ResourceLocation> material) {
        Component value = material
                .map(MaterialCatalog::displayName)
                .orElseGet(() -> Component.literal("none"));
        return Component.literal(label + ": ")
                .append(value)
                .withStyle(ChatFormatting.GRAY);
    }
}

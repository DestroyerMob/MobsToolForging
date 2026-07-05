package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class FlintToolStacks {
    private FlintToolStacks() {
    }

    public static ItemStack create(ToolKind toolKind) {
        ToolConstructionData construction = construction(toolKind);
        ItemStack stack = new ItemStack(toolKind.toolItem().get());
        stack.set(ModDataComponents.TOOL_CONSTRUCTION.get(), construction);
        ToolStatBuilder.apply(stack, toolKind, construction);
        return stack;
    }

    public static ToolConstructionData construction(ToolKind toolKind) {
        return new ToolConstructionData(
                ToolConstructionData.toolType(toolKind),
                MaterialCatalog.FLINT,
                MaterialCatalog.OAK,
                toolKind == ToolKind.SWORD || toolKind == ToolKind.MATTOCK ? Optional.of(MaterialCatalog.FLINT) : Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                ToolConstructionData.DEFAULT_QUALITY
        );
    }
}

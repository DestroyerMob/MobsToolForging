package org.destroyermob.mobstoolforging.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class ModularToolRecipe extends CustomRecipe {
    private final ToolKind toolKind;

    public ModularToolRecipe(CraftingBookCategory category, ToolKind toolKind) {
        super(category);
        this.toolKind = toolKind;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findParts(input).isValid();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Parts parts = findParts(input);
        if (!parts.isValid()) {
            return ItemStack.EMPTY;
        }
        ToolPartData partData = parts.part().get(ModDataComponents.TOOL_PART.get());
        return toolKind.createTool(partData.materialId(), parts.handle());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.serializerFor(toolKind).get();
    }

    private Parts findParts(CraftingInput input) {
        ItemStack part = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
            if (stack.is(toolKind.partItem().get()) && partData != null && toolKind.partType().equals(partData.partType())) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty()) {
                    return Parts.invalid();
                }
                if (!part.isEmpty()) {
                    return Parts.invalid();
                }
                part = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_HANDLES)) {
                if (!handle.isEmpty()) {
                    return Parts.invalid();
                }
                handle = stack;
                continue;
            }
            return Parts.invalid();
        }
        return new Parts(part, handle);
    }

    private record Parts(ItemStack part, ItemStack handle) {
        private static Parts invalid() {
            return new Parts(ItemStack.EMPTY, ItemStack.EMPTY);
        }

        private boolean isValid() {
            return !part.isEmpty() && !handle.isEmpty();
        }
    }
}

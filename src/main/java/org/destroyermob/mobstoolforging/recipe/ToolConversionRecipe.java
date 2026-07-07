package org.destroyermob.mobstoolforging.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.VanillaToolConverter;

public class ToolConversionRecipe extends CustomRecipe {
    public ToolConversionRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !converted(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return converted(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TOOL_CONVERSION.get();
    }

    private static ItemStack converted(CraftingInput input) {
        ItemStack original = singleInput(input);
        if (original.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack oneItem = original.copy();
        oneItem.setCount(1);
        return VanillaToolConverter.convertLootOrEquipment(oneItem, MaterialCatalog.OAK);
    }

    private static ItemStack singleInput(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return ItemStack.EMPTY;
            }
            found = stack;
        }
        return found;
    }
}

package org.destroyermob.mobstoolforging.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.item.ModularArmorDyeing;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;

public final class ModularArmorDyeRecipe extends CustomRecipe {
    public ModularArmorDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return ingredients(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        DyeIngredients ingredients = ingredients(input);
        return ingredients == null
                ? ItemStack.EMPTY
                : ModularArmorDyeing.applyDyes(ingredients.armor(), ingredients.dyes());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MODULAR_ARMOR_DYE.get();
    }

    private static DyeIngredients ingredients(CraftingInput input) {
        ItemStack armor = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }
            if (ModularArmorDyeing.isDyeable(stack)) {
                if (!armor.isEmpty()) {
                    return null;
                }
                armor = stack;
            } else if (stack.getItem() instanceof DyeItem dye) {
                dyes.add(dye);
            } else {
                return null;
            }
        }
        return armor.isEmpty() || dyes.isEmpty() ? null : new DyeIngredients(armor, List.copyOf(dyes));
    }

    private record DyeIngredients(ItemStack armor, List<DyeItem> dyes) {
    }
}

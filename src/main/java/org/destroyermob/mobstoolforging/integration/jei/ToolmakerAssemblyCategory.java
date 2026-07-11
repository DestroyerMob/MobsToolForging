package org.destroyermob.mobstoolforging.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ToolmakerAssemblyCategory implements IRecipeCategory<ToolmakerAssemblyJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public ToolmakerAssemblyCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(172, 54);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.toolmakersBenchIcon());
    }

    @Override
    public RecipeType<ToolmakerAssemblyJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.TOOLMAKER_ASSEMBLY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.toolmaker_assembly");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ToolmakerAssemblyJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.CATALYST, 2, 19)
                .setStandardSlotBackground()
                .addItemStack(MobsToolForgingJeiPlugin.toolmakersBenchIcon());
        int partCount = Math.min(5, recipe.parts().size());
        int startX = 28 + Math.max(0, 4 - partCount) * 6;
        for (int index = 0; index < partCount; index++) {
            builder.addInputSlot(startX + index * 22, 19)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.parts().get(index));
        }
        builder.addOutputSlot(144, 19)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ToolmakerAssemblyJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 120, 23);
    }
}

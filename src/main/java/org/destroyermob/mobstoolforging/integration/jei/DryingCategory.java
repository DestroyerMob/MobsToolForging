package org.destroyermob.mobstoolforging.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DryingCategory implements IRecipeCategory<DryingJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public DryingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(112, 42);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.dryingRackIcon());
    }

    @Override
    public RecipeType<DryingJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.DRYING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.drying");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DryingJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 2, 14)
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        builder.addInputSlot(36, 14)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        builder.addOutputSlot(84, 14)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(DryingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawClock(guiGraphics, 44, 1, recipe.ticks());
        JeiRecipeVisuals.drawArrow(guiGraphics, 62, 18);
    }
}

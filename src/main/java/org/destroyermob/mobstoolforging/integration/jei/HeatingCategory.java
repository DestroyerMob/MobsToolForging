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

public class HeatingCategory implements IRecipeCategory<HeatingJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public HeatingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(132, 40);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.heatingForgeIcon());
    }

    @Override
    public RecipeType<HeatingJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.HEATING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.heating");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HeatingJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 2, 17)
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        builder.addInputSlot(38, 17)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        builder.addOutputSlot(104, 17)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(HeatingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawPlus(guiGraphics, 26, 22);
        JeiRecipeVisuals.drawClock(guiGraphics, 57, 2, recipe.ticks());
        JeiRecipeVisuals.drawArrow(guiGraphics, 74, 21);
    }
}

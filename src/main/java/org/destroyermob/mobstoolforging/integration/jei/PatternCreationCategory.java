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

public class PatternCreationCategory implements IRecipeCategory<PatternCreationJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public PatternCreationCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(100, 42);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.patternCreationStationIcon());
    }

    @Override
    public RecipeType<PatternCreationJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.PATTERN_CREATION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.pattern_creation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PatternCreationJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 2, 13)
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        builder.addInputSlot(28, 13)
                .setStandardSlotBackground()
                .addItemStack(recipe.paper());
        builder.addOutputSlot(74, 13)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(PatternCreationJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 52, 17);
    }
}

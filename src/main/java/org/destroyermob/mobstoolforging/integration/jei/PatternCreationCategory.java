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
import net.minecraft.resources.ResourceLocation;

public class PatternCreationCategory implements IRecipeCategory<PatternCreationJeiRecipe> {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 42;
    private final IDrawable icon;
    private final IDrawable arrow;

    public PatternCreationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.patternCreationStationIcon());
        this.arrow = guiHelper.getRecipeArrow();
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
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(PatternCreationJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PatternCreationJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addInputSlot(4, 13), "pattern_material")
                .setStandardSlotBackground()
                .addItemStack(recipe.paper());
        JeiRecipeVisuals.role(builder.addOutputSlot(74, 13), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(PatternCreationJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 40, 13);
    }
}

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
import net.minecraft.resources.ResourceLocation;

public class DryingCategory implements IRecipeCategory<DryingJeiRecipe> {
    private static final int WIDTH = 112;
    private static final int HEIGHT = 42;
    private final IDrawable icon;

    public DryingCategory(IGuiHelper guiHelper) {
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
    public ResourceLocation getRegistryName(DryingJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 2, 14), "workstation")
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        JeiRecipeVisuals.role(builder.addInputSlot(36, 14), "input")
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        JeiRecipeVisuals.role(builder.addOutputSlot(84, 14), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(DryingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawClock(guiGraphics, 44, 1, recipe.ticks());
        JeiRecipeVisuals.drawArrow(guiGraphics, 62, 18);
    }
}

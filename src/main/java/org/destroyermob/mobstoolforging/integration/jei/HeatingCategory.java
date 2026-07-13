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

public class HeatingCategory implements IRecipeCategory<HeatingJeiRecipe> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 52;
    private final IDrawable icon;

    public HeatingCategory(IGuiHelper guiHelper) {
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
    public ResourceLocation getRegistryName(HeatingJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HeatingJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 2, 22), "workstation")
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        JeiRecipeVisuals.role(builder.addInputSlot(40, 22), "workpiece")
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        JeiRecipeVisuals.role(builder.addOutputSlot(122, 22), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(HeatingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawPlus(guiGraphics, 27, 27);
        JeiRecipeVisuals.drawClock(guiGraphics, 64, 3, recipe.ticks());
        JeiRecipeVisuals.drawArrow(guiGraphics, 92, 26);
        JeiRecipeVisuals.drawTargetHeat(guiGraphics, 59, 40, recipe.targetTemperature());
    }
}

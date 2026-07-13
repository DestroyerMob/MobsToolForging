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

public class StationWorkCategory implements IRecipeCategory<StationWorkJeiRecipe> {
    private static final int WIDTH = 134;
    private static final int HEIGHT = 60;
    private final IDrawable icon;

    public StationWorkCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.stationWorkIcon());
    }

    @Override
    public RecipeType<StationWorkJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.STATION_WORK;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.station_work");
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
    public ResourceLocation getRegistryName(StationWorkJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StationWorkJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 2, 21), "workstation")
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        JeiRecipeVisuals.role(builder.addInputSlot(36, 12), "input")
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        if (!recipe.secondaryInputs().isEmpty()) {
            JeiRecipeVisuals.role(builder.addInputSlot(36, 36), "secondary_material")
                    .setStandardSlotBackground()
                    .addItemStacks(recipe.secondaryInputs());
        }
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 60, 36), "tool")
                .setStandardSlotBackground()
                .addItemStack(recipe.hammer());
        JeiRecipeVisuals.role(builder.addOutputSlot(104, 21), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(StationWorkJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 82, 25);
        JeiRecipeVisuals.drawHitCount(guiGraphics, 82, 49, recipe.requiredHits());
    }
}

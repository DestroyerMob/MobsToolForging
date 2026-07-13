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

public class LapidaryCoatingCategory implements IRecipeCategory<LapidaryCoatingJeiRecipe> {
    private static final int WIDTH = 148;
    private static final int HEIGHT = 58;
    private final IDrawable icon;

    public LapidaryCoatingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.lapidaryTableIcon());
    }

    @Override
    public RecipeType<LapidaryCoatingJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.LAPIDARY_COATING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.lapidary_coating");
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
    public ResourceLocation getRegistryName(LapidaryCoatingJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LapidaryCoatingJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 2, 21), "workstation")
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        JeiRecipeVisuals.role(builder.addInputSlot(28, 12), "base_part")
                .setStandardSlotBackground()
                .addItemStacks(recipe.baseParts());
        JeiRecipeVisuals.role(builder.addInputSlot(52, 12), "coating")
                .setStandardSlotBackground()
                .addItemStack(recipe.coatingMaterial());
        if (!recipe.abrasive().isEmpty()) {
            JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 52, 36), "abrasive")
                    .setStandardSlotBackground()
                    .addItemStack(recipe.abrasive());
        }
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 76, 36), "tool")
                .setStandardSlotBackground()
                .addItemStack(recipe.workTool());
        JeiRecipeVisuals.role(builder.addOutputSlot(118, 21), "result")
                .setOutputSlotBackground()
                .addItemStacks(recipe.outputs());
    }

    @Override
    public void draw(LapidaryCoatingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 96, 25);
        JeiRecipeVisuals.drawHitCount(guiGraphics, 96, 43, recipe.requiredHits());
    }
}

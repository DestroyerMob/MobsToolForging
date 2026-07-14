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
    private static final int WIDTH = 132;
    private static final int HEIGHT = 58;
    private final IDrawable icon;
    private final IDrawable arrow;

    public LapidaryCoatingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.lapidaryTableIcon());
        this.arrow = guiHelper.getRecipeArrow();
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
        JeiRecipeVisuals.role(builder.addInputSlot(8, 12), "base_part")
                .setStandardSlotBackground()
                .addItemStacks(recipe.baseParts());
        JeiRecipeVisuals.role(builder.addInputSlot(32, 12), "coating")
                .setStandardSlotBackground()
                .addItemStack(recipe.coatingMaterial());
        if (!recipe.abrasive().isEmpty()) {
            JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 32, 36), "abrasive")
                    .setStandardSlotBackground()
                    .addItemStack(recipe.abrasive());
        }
        JeiRecipeVisuals.role(builder.addOutputSlot(102, 21), "result")
                .setOutputSlotBackground()
                .addItemStacks(recipe.outputs());
    }

    @Override
    public void draw(LapidaryCoatingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 72, 21);
        JeiRecipeVisuals.drawHitCount(guiGraphics, 72, 43, recipe.requiredHits());
    }
}

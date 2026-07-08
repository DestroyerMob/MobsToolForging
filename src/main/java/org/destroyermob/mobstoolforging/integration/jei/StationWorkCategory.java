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

public class StationWorkCategory implements IRecipeCategory<StationWorkJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public StationWorkCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(130, 54);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.smithingAnvilIcon());
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StationWorkJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 2, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        builder.addInputSlot(36, 10)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        if (!recipe.catalyst().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 36, 34)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.catalyst());
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 60, 34)
                .setStandardSlotBackground()
                .addItemStack(recipe.hammer());
        builder.addOutputSlot(100, 19)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(StationWorkJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 78, 23);
        JeiRecipeVisuals.drawHitCount(guiGraphics, 82, 39, recipe.requiredHits());
    }
}

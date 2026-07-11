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

public class LapidaryCoatingCategory implements IRecipeCategory<LapidaryCoatingJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public LapidaryCoatingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(148, 58);
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LapidaryCoatingJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 2, 21)
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        builder.addInputSlot(28, 12)
                .setStandardSlotBackground()
                .addItemStacks(recipe.baseParts());
        builder.addInputSlot(52, 12)
                .setStandardSlotBackground()
                .addItemStack(recipe.coatingMaterial());
        if (!recipe.abrasive().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 52, 36)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.abrasive());
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 76, 36)
                .setStandardSlotBackground()
                .addItemStack(recipe.workTool());
        builder.addOutputSlot(118, 21)
                .setOutputSlotBackground()
                .addItemStacks(recipe.outputs());
    }

    @Override
    public void draw(LapidaryCoatingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 96, 25);
        JeiRecipeVisuals.drawHitCount(guiGraphics, 96, 43, recipe.requiredHits());
    }
}

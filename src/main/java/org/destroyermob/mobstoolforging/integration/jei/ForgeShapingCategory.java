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

public class ForgeShapingCategory implements IRecipeCategory<ForgeShapingJeiRecipe> {
    private static final int WIDTH = 148;
    private static final int HEIGHT = 58;
    private final IDrawable icon;
    private final IDrawable arrow;

    public ForgeShapingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.smithingAnvilIcon());
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ForgeShapingJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.FORGE_SHAPING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.forge_shaping");
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
    public ResourceLocation getRegistryName(ForgeShapingJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgeShapingJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addInputSlot(14, 12), "pattern")
                .setStandardSlotBackground()
                .addItemStack(recipe.pattern());
        JeiRecipeVisuals.role(builder.addInputSlot(38, 12), "material")
                .setStandardSlotBackground()
                .addItemStack(recipe.material());
        if (!recipe.target().isEmpty()) {
            JeiRecipeVisuals.role(builder.addInputSlot(14, 36), "workpiece")
                    .setStandardSlotBackground()
                    .addItemStack(recipe.target());
        }
        if (!recipe.catalyst().isEmpty()) {
            JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 38, 36), "abrasive")
                    .setStandardSlotBackground()
                    .addItemStack(recipe.catalyst());
        }
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 62, 36), "tool")
                .setStandardSlotBackground()
                .addItemStack(recipe.hammer());
        JeiRecipeVisuals.role(builder.addOutputSlot(118, 21), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ForgeShapingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 88, 21);
        JeiRecipeVisuals.drawHitCount(guiGraphics, 91, 43, recipe.requiredHits());
    }
}

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

public class ToolmakerAssemblyCategory implements IRecipeCategory<ToolmakerAssemblyJeiRecipe> {
    private static final int WIDTH = 196;
    private static final int HEIGHT = 54;
    private final IDrawable icon;

    public ToolmakerAssemblyCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.toolmakersBenchIcon());
    }

    @Override
    public RecipeType<ToolmakerAssemblyJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.TOOLMAKER_ASSEMBLY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.toolmaker_assembly");
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
    public ResourceLocation getRegistryName(ToolmakerAssemblyJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ToolmakerAssemblyJeiRecipe recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 2, 19), "workstation")
                .setStandardSlotBackground()
                .addItemStack(MobsToolForgingJeiPlugin.toolmakersBenchIcon());
        int partCount = Math.min(5, recipe.parts().size());
        int startX = 28 + Math.max(0, 5 - partCount) * 11;
        for (int index = 0; index < partCount; index++) {
            JeiRecipeVisuals.role(builder.addInputSlot(startX + index * 22, 19), "component")
                    .setStandardSlotBackground()
                    .addItemStack(recipe.parts().get(index));
        }
        JeiRecipeVisuals.role(builder.addOutputSlot(170, 19), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ToolmakerAssemblyJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawArrow(guiGraphics, 144, 23);
    }
}

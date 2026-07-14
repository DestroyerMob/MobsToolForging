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

public class ToolmakerAssemblyCategory implements IRecipeCategory<ToolmakerAssemblyJeiRecipe> {
    private static final int WIDTH = 170;
    private static final int HEIGHT = 54;
    private final IDrawable icon;
    private final IDrawable arrow;

    public ToolmakerAssemblyCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.toolmakersBenchIcon());
        this.arrow = guiHelper.getRecipeArrow();
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
        int partCount = Math.min(5, recipe.parts().size());
        int startX = 4 + Math.max(0, 5 - partCount) * 11;
        for (int index = 0; index < partCount; index++) {
            JeiRecipeVisuals.role(builder.addInputSlot(startX + index * 22, 19), "component")
                    .setStandardSlotBackground()
                    .addItemStack(recipe.parts().get(index));
        }
        JeiRecipeVisuals.role(builder.addOutputSlot(144, 19), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ToolmakerAssemblyJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int partCount = Math.min(5, recipe.parts().size());
        int startX = 4 + Math.max(0, 5 - partCount) * 11;
        int ingredientsRight = partCount == 0 ? startX : startX + (partCount - 1) * 22 + 18;
        int arrowX = ingredientsRight + (144 - ingredientsRight - arrow.getWidth()) / 2;
        arrow.draw(guiGraphics, arrowX, 19);
    }
}

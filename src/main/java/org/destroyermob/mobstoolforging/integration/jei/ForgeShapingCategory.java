package org.destroyermob.mobstoolforging.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ForgeShapingCategory implements IRecipeCategory<ForgeShapingJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public ForgeShapingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(148, 58);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.smithingAnvilIcon());
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgeShapingJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 2, 21)
                .setStandardSlotBackground()
                .addItemStack(recipe.station());
        builder.addInputSlot(28, 12)
                .setStandardSlotBackground()
                .addItemStack(recipe.pattern());
        builder.addInputSlot(52, 12)
                .setStandardSlotBackground()
                .addItemStack(recipe.material());
        if (!recipe.target().isEmpty()) {
            builder.addInputSlot(28, 36)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.target());
        }
        if (!recipe.catalyst().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 52, 36)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.catalyst());
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 76, 36)
                .setStandardSlotBackground()
                .addItemStack(recipe.hammer());
        builder.addOutputSlot(118, 21)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ForgeShapingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font, Component.literal("->").withStyle(ChatFormatting.DARK_GRAY), 97, 25, 0xFF555555, false);
        Component detail = Component.translatable("jei.mobstoolforging.station_detail", recipe.requiredHits(), recipe.hammer().getHoverName());
        guiGraphics.drawString(Minecraft.getInstance().font, detail, 28, 0, 0xFF606060, false);
    }
}

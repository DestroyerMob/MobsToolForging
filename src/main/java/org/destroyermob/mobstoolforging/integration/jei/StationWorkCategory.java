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
import net.minecraft.world.item.ItemStack;

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
        guiGraphics.drawString(Minecraft.getInstance().font, Component.literal("->").withStyle(ChatFormatting.DARK_GRAY), 80, 23, 0xFF555555, false);
        Component detail = Component.translatable("jei.mobstoolforging.station_detail", recipe.requiredHits(), recipe.hammer().getHoverName());
        guiGraphics.drawString(Minecraft.getInstance().font, detail, 28, 0, 0xFF606060, false);
        if (!recipe.catalyst().isEmpty()) {
            ItemStack catalyst = recipe.catalyst();
            Component catalystText = Component.translatable("jei.mobstoolforging.catalyst", catalyst.getHoverName());
            guiGraphics.drawString(Minecraft.getInstance().font, catalystText, 2, 46, 0xFF606060, false);
        }
    }
}

package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class WorldAssemblyCategory implements IRecipeCategory<WorldAssemblyJeiRecipe> {
    private static final int TEXT = 0xFF606060;
    private final IDrawable background;
    private final IDrawable icon;

    public WorldAssemblyCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 74);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.smithingAnvilIcon());
    }

    @Override
    public RecipeType<WorldAssemblyJeiRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.WORLD_ASSEMBLY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.world_assembly");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WorldAssemblyJeiRecipe recipe, IFocusGroup focuses) {
        if (recipe.kind() == WorldAssemblyJeiRecipe.Kind.LAPIDARY_TABLE) {
            addInput(builder, 5, 7, List.of(recipe.upperBlocks().get(0)));
            addInput(builder, 25, 7, List.of(recipe.upperBlocks().get(1)));
            addInput(builder, 5, 27, List.of(recipe.lowerBlocks().get(0)));
            addInput(builder, 25, 27, List.of(recipe.lowerBlocks().get(1)));
        } else if (recipe.kind() == WorldAssemblyJeiRecipe.Kind.SAWMILL) {
            addInput(builder, 5, 7, List.of(recipe.upperBlocks().get(0)));
            addInput(builder, 25, 7, List.of(recipe.upperBlocks().get(1)));
            addInput(builder, 5, 27, recipe.lowerBlocks());
            addInput(builder, 25, 27, recipe.lowerBlocks());
        } else if (recipe.kind() == WorldAssemblyJeiRecipe.Kind.LEATHER_STATION) {
            addInput(builder, 5, 7, recipe.upperBlocks());
            addInput(builder, 25, 7, recipe.upperBlocks());
            addInput(builder, 5, 27, recipe.lowerBlocks());
            addInput(builder, 25, 27, recipe.lowerBlocks());
        } else {
            addInput(builder, 15, 17, recipe.lowerBlocks());
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 60, 27)
                .setStandardSlotBackground()
                .addItemStacks(recipe.activationItems());
        builder.addOutputSlot(122, 17)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    private static void addInput(IRecipeLayoutBuilder builder, int x, int y, java.util.List<net.minecraft.world.item.ItemStack> stacks) {
        builder.addInputSlot(x, y)
                .setStandardSlotBackground()
                .addItemStacks(stacks);
    }

    @Override
    public void draw(WorldAssemblyJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        Component arrangement = switch (recipe.kind()) {
            case SAWMILL -> Component.translatable("jei.mobstoolforging.world_assembly.front_view");
            case LEATHER_STATION -> Component.translatable("jei.mobstoolforging.world_assembly.side_view");
            case LAPIDARY_TABLE -> Component.translatable("jei.mobstoolforging.world_assembly.front_view");
            case DIAMOND_SAW -> Component.translatable("jei.mobstoolforging.world_assembly.place_block");
            case ANVIL -> Component.translatable("jei.mobstoolforging.world_assembly.place_block");
        };
        guiGraphics.drawString(font, arrangement, 3, 51, TEXT, false);
        Component instruction = recipe.kind() == WorldAssemblyJeiRecipe.Kind.DIAMOND_SAW
                ? Component.translatable("jei.mobstoolforging.world_assembly.sneak_use_abrasive")
                : Component.translatable("jei.mobstoolforging.world_assembly.sneak_use");
        guiGraphics.drawString(font, instruction, 3, 62, TEXT, false);
        JeiRecipeVisuals.drawArrow(guiGraphics, 94, 22);
    }
}

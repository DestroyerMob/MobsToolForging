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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.world.ToolTrait;
import org.destroyermob.mobstoolforging.world.ToolTraitRegistry;

public class MaterialTraitInfoCategory implements IRecipeCategory<MaterialTraitInfoRecipe> {
    private static final int WIDTH = 172;
    private static final int HEIGHT = 106;
    private static final int TEXT = 0xFF404040;
    private static final int MUTED = 0xFF707070;
    private static final int LINE = 0xFFB7B7B7;
    private static final int MAX_ROWS = 3;

    private final IDrawable background;
    private final IDrawable icon;

    public MaterialTraitInfoCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.materialTraitIcon());
    }

    @Override
    public RecipeType<MaterialTraitInfoRecipe> getRecipeType() {
        return MobsToolForgingJeiPlugin.MATERIAL_TRAITS;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.material_traits");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MaterialTraitInfoRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(5, 6)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
    }

    @Override
    public void draw(MaterialTraitInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.fill(0, 0, WIDTH, 1, LINE);
        guiGraphics.fill(0, HEIGHT - 1, WIDTH, HEIGHT, LINE);
        guiGraphics.fill(0, 0, 1, HEIGHT, LINE);
        guiGraphics.fill(WIDTH - 1, 0, WIDTH, HEIGHT, LINE);
        guiGraphics.fill(28, 24, WIDTH - 7, 25, LINE);

        guiGraphics.drawString(font, recipe.materialName(), 29, 7, TEXT, false);
        guiGraphics.drawString(font, Component.translatable("jei.mobstoolforging.material_traits.header"), 29, 17, MUTED, false);

        int rows = Math.min(MAX_ROWS, recipe.traits().size());
        for (int index = 0; index < rows; index++) {
            drawTrait(font, guiGraphics, recipe.traits().get(index), 7, 30 + index * 20);
        }
        int footerY = 30 + rows * 20 + 4;
        if (recipe.traits().size() > MAX_ROWS) {
            guiGraphics.drawString(font, Component.translatable("jei.mobstoolforging.material_traits.more", recipe.traits().size() - MAX_ROWS), 7, footerY, MUTED, false);
        } else {
            guiGraphics.drawString(font, Component.translatable("jei.mobstoolforging.material_traits.level_hint"), 7, footerY, MUTED, false);
        }
    }

    private static void drawTrait(Font font, GuiGraphics guiGraphics, MaterialTraitInfoRecipe.TraitEntry entry, int x, int y) {
        Component name = traitName(entry.traitId());
        String source = trim(font, entry.source(), 74);
        guiGraphics.drawString(font, name, x, y, TEXT, false);
        guiGraphics.drawString(font, source, WIDTH - 8 - font.width(source), y, MUTED, false);
        guiGraphics.drawString(font, trim(font, traitDescription(entry.traitId()), WIDTH - 14), x, y + 10, MUTED, false);
    }

    private static Component traitName(ResourceLocation traitId) {
        return ToolTraitRegistry.definition(traitId)
                .map(definition -> (Component) definition.displayName())
                .orElseGet(() -> Component.literal(ToolTrait.fallbackName(traitId)).withStyle(ChatFormatting.GRAY));
    }

    private static Component traitDescription(ResourceLocation traitId) {
        return ToolTraitRegistry.definition(traitId)
                .map(definition -> (Component) definition.description())
                .orElseGet(() -> Component.empty());
    }

    private static String trim(Font font, Component text, int width) {
        return font.plainSubstrByWidth(text.getString(), width);
    }
}

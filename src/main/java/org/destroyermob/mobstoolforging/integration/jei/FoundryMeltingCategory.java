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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FoundryMeltingCategory implements IRecipeCategory<FoundryJeiRecipes.Melting> {
    private static final int WIDTH = 146;
    private static final int HEIGHT = 60;
    private final IDrawable icon;
    private final IDrawable arrow;

    public FoundryMeltingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.foundryForgeIcon());
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<FoundryJeiRecipes.Melting> getRecipeType() {
        return MobsToolForgingJeiPlugin.FOUNDRY_MELTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.foundry_melting");
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
    public ResourceLocation getRegistryName(FoundryJeiRecipes.Melting recipe) {
        return synthetic(recipe.id());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FoundryJeiRecipes.Melting recipe, IFocusGroup focuses) {
        JeiRecipeVisuals.role(builder.addInputSlot(4, 21), "foundry_input")
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        JeiRecipeVisuals.role(builder.addOutputSlot(118, 21), "molten_result")
                .setOutputSlotBackground()
                .addItemStack(recipe.materialDisplay())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                        "jei.mobstoolforging.foundry.material_amount",
                        recipe.materialName(), recipe.amountMb()
                ).withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public void draw(FoundryJeiRecipes.Melting recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawClock(graphics, 30, 3, recipe.ticks());
        arrow.draw(graphics, 84, 21);
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.temperature_c", Math.round(recipe.temperatureC())), 29, 45);
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.amount_mb", recipe.amountMb()), 91, 45);
    }

    static ResourceLocation synthetic(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath());
    }
}

final class FoundryAlloyingCategory implements IRecipeCategory<FoundryJeiRecipes.Alloying> {
    private static final int WIDTH = 154;
    private static final int HEIGHT = 64;
    private final IDrawable icon;
    private final IDrawable arrow;

    FoundryAlloyingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.foundryForgeIcon());
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<FoundryJeiRecipes.Alloying> getRecipeType() {
        return MobsToolForgingJeiPlugin.FOUNDRY_ALLOYING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.foundry_alloying");
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
    public ResourceLocation getRegistryName(FoundryJeiRecipes.Alloying recipe) {
        return FoundryMeltingCategory.synthetic(recipe.id());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FoundryJeiRecipes.Alloying recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.inputs().size(); index++) {
            FoundryJeiRecipes.MaterialAmount input = recipe.inputs().get(index);
            int x = 4 + index % 3 * 24;
            int y = 6 + index / 3 * 24;
            JeiRecipeVisuals.role(builder.addInputSlot(x, y), "molten_input")
                    .setStandardSlotBackground()
                    .addItemStacks(input.displays())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                            "jei.mobstoolforging.foundry.material_amount", input.name(), input.amountMb()
                    ).withStyle(ChatFormatting.GRAY)));
        }
        JeiRecipeVisuals.role(builder.addOutputSlot(126, 22), "molten_result")
                .setOutputSlotBackground()
                .addItemStack(recipe.resultDisplay())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                        "jei.mobstoolforging.foundry.material_amount", recipe.resultName(), recipe.outputAmountMb()
                ).withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public void draw(FoundryJeiRecipes.Alloying recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 94, 22);
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.amount_mb", recipe.outputAmountMb()), 103, 48);
    }
}

final class FoundryCastingCategory implements IRecipeCategory<FoundryJeiRecipes.Casting> {
    private static final int WIDTH = 146;
    private static final int HEIGHT = 60;
    private final IDrawable icon;
    private final IDrawable arrow;

    FoundryCastingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.castingTableIcon());
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<FoundryJeiRecipes.Casting> getRecipeType() {
        return MobsToolForgingJeiPlugin.FOUNDRY_CASTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.foundry_casting");
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
    public ResourceLocation getRegistryName(FoundryJeiRecipes.Casting recipe) {
        return FoundryMeltingCategory.synthetic(recipe.id());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FoundryJeiRecipes.Casting recipe, IFocusGroup focuses) {
        if (!recipe.forms().isEmpty()) {
            JeiRecipeVisuals.role(builder.addInputSlot(4, 21), recipe.kind() == FoundryJeiRecipes.Casting.Kind.CREATE_CAST
                            ? "cast_pattern" : "casting_mold")
                    .setStandardSlotBackground()
                    .addItemStacks(recipe.forms());
        }
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.INPUT, 34, 21), "molten_input")
                .setStandardSlotBackground()
                .addItemStacks(recipe.materialDisplays())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                        "jei.mobstoolforging.foundry.material_amount", recipe.materialName(), recipe.amountMb()
                ).withStyle(ChatFormatting.GRAY)));
        JeiRecipeVisuals.role(builder.addOutputSlot(118, 21), "result")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(FoundryJeiRecipes.Casting recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 84, 21);
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.cast_kind." + recipe.kind().name().toLowerCase(java.util.Locale.ROOT)), 4, 3);
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.amount_mb", recipe.amountMb()), 91, 45);
    }
}

final class FoundryFuelCategory implements IRecipeCategory<FoundryJeiRecipes.Fuel> {
    private static final int WIDTH = 146;
    private static final int HEIGHT = 60;
    private final IDrawable icon;

    FoundryFuelCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(MobsToolForgingJeiPlugin.fuelTankIcon());
    }

    @Override
    public RecipeType<FoundryJeiRecipes.Fuel> getRecipeType() {
        return MobsToolForgingJeiPlugin.FOUNDRY_FUEL;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobstoolforging.foundry_fuel");
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
    public ResourceLocation getRegistryName(FoundryJeiRecipes.Fuel recipe) {
        return FoundryMeltingCategory.synthetic(recipe.id());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FoundryJeiRecipes.Fuel recipe, IFocusGroup focuses) {
        var fluidSlot = JeiRecipeVisuals.role(builder.addInputSlot(5, 21), "foundry_fuel")
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.amountMb(), true, 16, 16);
        recipe.fluids().forEach(fluid -> fluidSlot.addFluidStack(fluid, recipe.amountMb()));
        JeiRecipeVisuals.role(builder.addSlot(RecipeIngredientRole.CATALYST, 118, 21), "foundry_controller")
                .setStandardSlotBackground()
                .addItemStack(MobsToolForgingJeiPlugin.foundryForgeIcon());
    }

    @Override
    public void draw(FoundryJeiRecipes.Fuel recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        JeiRecipeVisuals.drawClock(graphics, 31, 3, recipe.burnTicks());
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.temperature_c", Math.round(recipe.temperatureC())), 31, 45);
        JeiRecipeVisuals.drawSmallText(graphics, Component.translatable(
                "jei.mobstoolforging.foundry.fuel_use", recipe.amountMb()), 83, 24);
    }
}

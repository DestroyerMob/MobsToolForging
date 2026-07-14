package org.destroyermob.mobstoolforging.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.integration.jei.WorldAssemblyJeiRecipe;

/**
 * Uses EMI's World Interaction category and textures while preserving MTF's
 * meaningful 2x2 in-world block layouts.
 */
final class ShapedWorldInteractionEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 130;
    private static final int HEIGHT = 36;
    private static final int TOOL_X = 61;
    private static final int CENTER_Y = 9;

    private final WorldAssemblyJeiRecipe recipe;
    private final List<EmiIngredient> placedInputs;
    private final EmiIngredient activation;
    private final EmiStack output;
    private final boolean reusableActivation;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;

    ShapedWorldInteractionEmiRecipe(WorldAssemblyJeiRecipe recipe) {
        this.recipe = recipe;
        this.placedInputs = placedInputs(recipe).stream()
                .map(ShapedWorldInteractionEmiRecipe::ingredient)
                .toList();
        this.activation = ingredient(recipe.activationItems());
        this.output = EmiStack.of(recipe.output());
        this.reusableActivation = recipe.kind() != WorldAssemblyJeiRecipe.Kind.DIAMOND_SAW;

        List<EmiIngredient> inputs = new ArrayList<>(placedInputs);
        if (reusableActivation) {
            activation.getEmiStacks().forEach(stack -> {
                if (stack.getRemainder().isEmpty()) {
                    stack.setRemainder(stack.copy());
                }
            });
            this.catalysts = List.of(activation);
        } else {
            inputs.add(activation);
            this.catalysts = List.of();
        }
        this.inputs = List.copyOf(inputs);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.WORLD_INTERACTION;
    }

    @Override
    public ResourceLocation getId() {
        return recipe.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        boolean shaped = placedInputs.size() == 4;
        for (int index = 0; index < placedInputs.size(); index++) {
            int x = shaped ? index % 2 * 18 : 9;
            int y = shaped ? index / 2 * 18 : CENTER_Y;
            SlotWidget slot = new SlotWidget(placedInputs.get(index), x, y);
            if (index == 0) {
                slot.appendTooltip(placementTooltip());
            }
            widgets.add(slot);
        }

        widgets.addTexture(EmiTexture.PLUS, 42, 11);
        widgets.add(new SlotWidget(activation, TOOL_X, CENTER_Y)
                .catalyst(reusableActivation)
                .appendTooltip(activationTooltip()));
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 84, 10);
        widgets.add(new SlotWidget(output, 112, CENTER_Y).recipeContext(this));
    }

    private Component placementTooltip() {
        String key = switch (recipe.kind()) {
            case SAWMILL, LAPIDARY_TABLE -> "jei.mobstoolforging.world_assembly.front_view";
            case LEATHER_STATION -> "jei.mobstoolforging.world_assembly.side_view";
            case ANVIL, DIAMOND_SAW -> "jei.mobstoolforging.world_assembly.place_block";
        };
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }

    private Component activationTooltip() {
        String key = recipe.kind() == WorldAssemblyJeiRecipe.Kind.DIAMOND_SAW
                ? "jei.mobstoolforging.world_assembly.sneak_use_abrasive"
                : "jei.mobstoolforging.world_assembly.sneak_use";
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }

    private static List<List<ItemStack>> placedInputs(WorldAssemblyJeiRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        switch (recipe.kind()) {
            case ANVIL, DIAMOND_SAW -> inputs.add(recipe.lowerBlocks());
            case LAPIDARY_TABLE -> {
                addExactInputs(inputs, recipe.upperBlocks());
                addExactInputs(inputs, recipe.lowerBlocks());
            }
            case SAWMILL -> {
                addExactInputs(inputs, recipe.upperBlocks());
                inputs.add(recipe.lowerBlocks());
                inputs.add(recipe.lowerBlocks());
            }
            case LEATHER_STATION -> {
                inputs.add(recipe.upperBlocks());
                inputs.add(recipe.upperBlocks());
                inputs.add(recipe.lowerBlocks());
                inputs.add(recipe.lowerBlocks());
            }
        }
        return inputs;
    }

    private static void addExactInputs(List<List<ItemStack>> inputs, List<ItemStack> stacks) {
        stacks.forEach(stack -> inputs.add(List.of(stack)));
    }

    private static EmiIngredient ingredient(List<ItemStack> stacks) {
        return EmiIngredient.of(stacks.stream().map(EmiStack::of).toList());
    }
}

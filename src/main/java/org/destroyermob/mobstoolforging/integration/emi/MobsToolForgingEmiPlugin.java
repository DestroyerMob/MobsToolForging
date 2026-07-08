package org.destroyermob.mobstoolforging.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.HeatingDisplayRecipe;
import org.destroyermob.mobstoolforging.world.HeatingRecipeRegistry;
import org.destroyermob.mobstoolforging.world.HeatingSource;

@EmiEntrypoint
public class MobsToolForgingEmiPlugin implements EmiPlugin {
    private static final EmiRecipeCategory HEATING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "heating"),
            EmiStack.of(ModItems.HEATING_FORGE.get())
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(HEATING);
        registry.addWorkstation(HEATING, EmiStack.of(ModItems.HEATING_FORGE.get()));
        registry.addWorkstation(HEATING, EmiStack.of(Items.CAMPFIRE));
        registry.addWorkstation(HEATING, EmiStack.of(Items.SOUL_CAMPFIRE));
        HeatingRecipeRegistry.displayRecipes().forEach(recipe -> registry.addRecipe(new HeatingEmiRecipe(recipe)));
    }

    private record HeatingEmiRecipe(
            HeatingDisplayRecipe recipe,
            EmiIngredient input,
            EmiStack output,
            EmiStack station
    ) implements EmiRecipe {
        private HeatingEmiRecipe(HeatingDisplayRecipe recipe) {
            this(
                    recipe,
                    input(recipe.inputs()),
                    EmiStack.of(recipe.output()),
                    station(recipe.source())
            );
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return HEATING;
        }

        @Override
        public ResourceLocation getId() {
            return recipe.id();
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return List.of(input);
        }

        @Override
        public List<EmiIngredient> getCatalysts() {
            return List.of(station);
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(output);
        }

        @Override
        public int getDisplayWidth() {
            return 132;
        }

        @Override
        public int getDisplayHeight() {
            return 52;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(station, 2, 18).catalyst(true);
            widgets.addSlot(input, 36, 18);
            widgets.addFillingArrow(74, 18, recipe.ticks());
            widgets.addSlot(output, 100, 18).recipeContext(this);
            widgets.addText(Component.translatable("jei.mobstoolforging.heating_detail", recipe.ticks(), Math.round(recipe.targetTemperature() * 100.0F)), 24, 2, 0xFF606060, false);
        }

        private static EmiIngredient input(List<ItemStack> inputs) {
            List<EmiIngredient> ingredients = inputs.stream()
                    .map(EmiStack::of)
                    .map(EmiIngredient.class::cast)
                    .toList();
            return ingredients.size() == 1 ? ingredients.getFirst() : EmiIngredient.of(ingredients);
        }

        private static EmiStack station(HeatingSource source) {
            return source == HeatingSource.CAMPFIRE ? EmiStack.of(Items.CAMPFIRE) : EmiStack.of(ModItems.HEATING_FORGE.get());
        }
    }
}

package org.destroyermob.mobstoolforging.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.recipe.ModularToolRecipe;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ToolKind;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TOOL_FORGE)
                .define('B', Items.IRON_BLOCK)
                .define('I', Items.IRON_INGOT)
                .pattern("BBB")
                .pattern(" I ")
                .pattern("III")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SMITHING_HAMMER)
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .pattern("II")
                .pattern(" S")
                .pattern(" S")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        patternRecipe(output, ModItems.PICKAXE_HEAD_PATTERN.get(), "PPP", " S ", " S ");
        patternRecipe(output, ModItems.AXE_HEAD_PATTERN.get(), "PP ", "PS ", " S ");
        patternRecipe(output, ModItems.SHOVEL_HEAD_PATTERN.get(), " P ", " P ", " S ");
        patternRecipe(output, ModItems.HOE_HEAD_PATTERN.get(), "PP ", " S ", " S ");
        patternRecipe(output, ModItems.SWORD_BLADE_PATTERN.get(), " P ", " P ", " P ");
        patternRecipe(output, ModItems.SWORD_GUARD_PATTERN.get(), "P P", " S ");

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LAPIDARY_TABLE)
                .define('D', Items.DIAMOND)
                .define('C', Items.CRAFTING_TABLE)
                .define('S', Items.SMOOTH_STONE)
                .pattern(" D ")
                .pattern("SCS")
                .pattern("SSS")
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        for (ToolKind toolKind : ToolKind.values()) {
            SpecialRecipeBuilder.special(category -> new ModularToolRecipe(category, toolKind))
                    .save(output, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "modular_" + toolKind.id()));
        }
    }

    private void patternRecipe(RecipeOutput output, ItemLike pattern, String... rows) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pattern)
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER));
        if (usesSymbol(rows, 'S')) {
            builder.define('S', Items.STICK);
        }
        for (String row : rows) {
            builder.pattern(row);
        }
        builder.save(output);
    }

    private static boolean usesSymbol(String[] rows, char symbol) {
        for (String row : rows) {
            if (row.indexOf(symbol) >= 0) {
                return true;
            }
        }
        return false;
    }
}

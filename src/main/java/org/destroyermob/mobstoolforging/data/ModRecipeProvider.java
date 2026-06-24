package org.destroyermob.mobstoolforging.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TOOL_FORGE)
                .define('B', Items.COPPER_BLOCK)
                .define('I', Items.COPPER_INGOT)
                .pattern("BBB")
                .pattern(" I ")
                .pattern("III")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.HEATING_FORGE)
                .define('C', Items.COPPER_INGOT)
                .define('F', Items.FURNACE)
                .define('S', Items.COBBLESTONE)
                .pattern("CSC")
                .pattern("CFC")
                .pattern("CCC")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PATTERN_CREATION_STATION)
                .define('X', Items.PAPER)
                .define('P', ItemTags.PLANKS)
                .pattern("XX")
                .pattern("PP")
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TOOLMAKERS_BENCH)
                .define('S', Items.CHISELED_STONE_BRICKS)
                .define('P', ItemTags.PLANKS)
                .pattern("SSS")
                .pattern("P P")
                .pattern("P P")
                .unlockedBy("has_chiseled_stone_bricks", has(Items.CHISELED_STONE_BRICKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SMITHING_HAMMER)
                .define('S', Items.STICK)
                .define('X', Items.STONE)
                .pattern(" XS")
                .pattern(" SX")
                .pattern("S  ")
                .unlockedBy("has_stone", has(Items.STONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_SMITHING_HAMMER)
                .define('H', ModItems.SMITHING_HAMMER_HEAD.get())
                .define('S', Items.STICK)
                .pattern("H")
                .pattern("S")
                .unlockedBy("has_smithing_hammer_head", has(ModItems.SMITHING_HAMMER_HEAD.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCREWDRIVER)
                .define('H', ModItems.SCREWDRIVER_HEAD.get())
                .define('S', Items.STICK)
                .pattern("H")
                .pattern("S")
                .unlockedBy("has_screwdriver_head", has(ModItems.SCREWDRIVER_HEAD.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GEM_CUTTERS_KNIFE)
                .define('B', ModItems.GEM_CUTTERS_BLADE.get())
                .define('S', Items.STICK)
                .pattern("B")
                .pattern("S")
                .unlockedBy("has_gem_cutters_blade", has(ModItems.GEM_CUTTERS_BLADE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FIRE_STICK)
                .define('S', Items.STICK)
                .pattern("S ")
                .pattern(" S")
                .unlockedBy("has_stick", has(Items.STICK))
                .save(output);

        flintToolRecipe(output, ModItems.FLINT_KNIFE.get(), "F", "S");
        flintToolRecipe(output, ModItems.FLINT_HATCHET.get(), "FF", "FS", " S");
        flintToolRecipe(output, ModItems.FLINT_PICK.get(), "FFF", " S ", " S ");

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LAPIDARY_TABLE)
                .define('D', Items.DIAMOND)
                .define('C', Items.CRAFTING_TABLE)
                .define('S', Items.SMOOTH_STONE)
                .pattern(" D ")
                .pattern("SCS")
                .pattern("SSS")
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);
    }

    private void flintToolRecipe(RecipeOutput output, ItemLike tool, String... rows) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, tool)
                .define('F', ModItems.FLINT_SHARD.get())
                .define('S', Items.STICK)
                .unlockedBy("has_flint_shard", has(ModItems.FLINT_SHARD.get()));
        for (String row : rows) {
            builder.pattern(row);
        }
        builder.save(output);
    }
}

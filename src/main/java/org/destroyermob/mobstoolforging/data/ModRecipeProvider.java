package org.destroyermob.mobstoolforging.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.recipe.ToolConversionRecipe;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        SpecialRecipeBuilder.special(ToolConversionRecipe::new)
                .save(output, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "tool_conversion").toString());
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CRUDE_ANVIL)
                .define('S', Items.COBBLESTONE)
                .pattern("SSS")
                .pattern(" S ")
                .pattern("S S")
                .unlockedBy("has_cobblestone", has(Items.COBBLESTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TOOL_FORGE)
                .define('B', Items.IRON_BLOCK)
                .pattern("B")
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.HEATING_FORGE)
                .define('I', Items.IRON_INGOT)
                .define('F', Items.FURNACE)
                .define('S', Items.COBBLESTONE)
                .pattern("ISI")
                .pattern("IFI")
                .pattern("III")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CRUCIBLE)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.IRON_BLOCK)
                .pattern("I I")
                .pattern("I I")
                .pattern(" B ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_FORGE)
                .define('H', ModBlocks.HEATING_FORGE.get())
                .define('O', Items.OBSIDIAN)
                .define('L', Items.LAVA_BUCKET)
                .pattern("OLO")
                .pattern("OHO")
                .pattern("OOO")
                .unlockedBy("has_lava_bucket", has(Items.LAVA_BUCKET))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PATTERN_BOARD, 4)
                .define('P', ItemTags.PLANKS)
                .pattern("P")
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PATTERN_CREATION_STATION)
                .define('P', ItemTags.PLANKS)
                .define('S', Items.COBBLESTONE)
                .pattern("P P")
                .pattern(" S ")
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output);

        for (ModBlocks.PatternRackVariant variant : ModBlocks.PATTERN_RACK_VARIANTS) {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, variant.block())
                    .define('P', variant.recipePlanks())
                    .define('S', Items.STICK)
                    .pattern("PPP")
                    .pattern("S S")
                    .pattern("PPP")
                    .unlockedBy("has_" + variant.id(), has(variant.recipePlanks()))
                    .save(output);
        }

        for (ModBlocks.ToolmakerStationVariant variant : ModBlocks.TOOLMAKER_STATION_VARIANTS) {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, variant.block(), 4)
                    .define('S', Items.STICK)
                    .define('P', variant.recipePlanks())
                    .pattern("PPP")
                    .pattern("S S")
                    .pattern("S S")
                    .unlockedBy("has_" + variant.id(), has(variant.recipePlanks()))
                    .save(output);
        }

        for (ModBlocks.DryingRackVariant variant : ModBlocks.DRYING_RACK_VARIANTS) {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, variant.block(), 4)
                    .define('S', variant.recipeSlab())
                    .pattern("SSS")
                    .unlockedBy("has_" + variant.id(), has(variant.recipeSlab()))
                    .save(output);
        }

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
}

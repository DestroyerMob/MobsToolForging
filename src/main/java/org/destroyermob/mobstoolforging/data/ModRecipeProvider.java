package org.destroyermob.mobstoolforging.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.recipe.ModularArmorDyeRecipe;
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
        SpecialRecipeBuilder.special(ModularArmorDyeRecipe::new)
                .save(output, ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "modular_armor_dye").toString());
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

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LAVA_HEATING_FORGE)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('H', ModBlocks.HEATING_FORGE.get())
                .define('L', Items.LAVA_BUCKET)
                .pattern("IGI")
                .pattern("IHI")
                .pattern("ILI")
                .unlockedBy("has_heating_forge", has(ModBlocks.HEATING_FORGE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_FORGE)
                .define('F', Items.BLAST_FURNACE)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.POLISHED_BLACKSTONE_BRICKS)
                .pattern("BIB")
                .pattern("BFB")
                .pattern("BBB")
                .unlockedBy("has_blast_furnace", has(Items.BLAST_FURNACE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_FUEL_TANK)
                .define('B', Items.POLISHED_BLACKSTONE_BRICKS)
                .define('G', Items.GLASS)
                .pattern("BGB")
                .pattern("G G")
                .pattern("BGB")
                .unlockedBy("has_foundry_forge", has(ModBlocks.FOUNDRY_FORGE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_GLASS, 4)
                .define('B', Items.POLISHED_BLACKSTONE_BRICKS)
                .define('G', Items.GLASS)
                .pattern("BGB")
                .pattern("GGG")
                .pattern("BGB")
                .unlockedBy("has_foundry_forge", has(ModBlocks.FOUNDRY_FORGE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_DRAIN)
                .define('B', Items.POLISHED_BLACKSTONE_BRICKS)
                .define('I', Items.IRON_INGOT)
                .pattern("BIB")
                .pattern("B B")
                .pattern("BBB")
                .unlockedBy("has_foundry_forge", has(ModBlocks.FOUNDRY_FORGE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_FAUCET)
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .pattern("II ")
                .pattern(" NN")
                .unlockedBy("has_foundry_drain", has(ModBlocks.FOUNDRY_DRAIN.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_CASTING_TABLE)
                .define('B', Items.POLISHED_BLACKSTONE_BRICKS)
                .define('I', Items.IRON_INGOT)
                .pattern("IBI")
                .pattern(" B ")
                .unlockedBy("has_foundry_drain", has(ModBlocks.FOUNDRY_DRAIN.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FOUNDRY_CASTING_BASIN)
                .define('B', Items.POLISHED_BLACKSTONE_BRICKS)
                .pattern("B B")
                .pattern("B B")
                .pattern("BBB")
                .unlockedBy("has_foundry_drain", has(ModBlocks.FOUNDRY_DRAIN.get()))
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
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, variant.block())
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

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FIRE_STICK)
                .define('S', Items.STICK)
                .pattern("S ")
                .pattern(" S")
                .unlockedBy("has_stick", has(Items.STICK))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLAZE_THREAD)
                .requires(Items.STRING)
                .requires(Items.BLAZE_POWDER)
                .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
                .save(output);

    }
}

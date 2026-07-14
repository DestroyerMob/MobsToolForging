package org.destroyermob.mobstoolforging.integration;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.integration.everycomp.CompatWorkstationRegistry;
import org.destroyermob.mobstoolforging.integration.jei.WorldAssemblyJeiRecipe;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;

/**
 * Builds the common display data used by JEI's fallback category and EMI's
 * native world-interaction recipes.
 */
public final class WorldInteractionDisplayRecipes {
    private WorldInteractionDisplayRecipes() {
    }

    public static List<WorldAssemblyJeiRecipe> recipes() {
        List<WorldAssemblyJeiRecipe> recipes = new ArrayList<>();
        List<ItemStack> hammers = List.of(
                new ItemStack(ModItems.SMITHING_HAMMER.get()),
                new ItemStack(ModItems.IRON_SMITHING_HAMMER.get())
        );

        if (!MobsToolForgingConfig.ENABLE_ANVIL_CRAFTING_RECIPES.get()) {
            if (MobsToolForgingConfig.ENABLE_CRUDE_ANVIL.get()) {
                recipes.add(new WorldAssemblyJeiRecipe(
                        recipeId("world_assembly/crude_anvil"),
                        WorldAssemblyJeiRecipe.Kind.ANVIL,
                        List.of(new ItemStack(Items.COBBLESTONE), new ItemStack(Items.STONE)),
                        List.of(),
                        hammers,
                        new ItemStack(ModItems.CRUDE_ANVIL.get())
                ));
            }
            recipes.add(new WorldAssemblyJeiRecipe(
                    recipeId("world_assembly/smithing_anvil"),
                    WorldAssemblyJeiRecipe.Kind.ANVIL,
                    List.of(new ItemStack(Items.IRON_BLOCK)),
                    List.of(),
                    hammers,
                    new ItemStack(ModItems.TOOL_FORGE.get())
            ));
        }

        recipes.add(new WorldAssemblyJeiRecipe(
                recipeId("world_assembly/diamond_saw"),
                WorldAssemblyJeiRecipe.Kind.DIAMOND_SAW,
                List.of(new ItemStack(Items.STONECUTTER)),
                List.of(),
                List.of(new ItemStack(ModItems.DIAMOND_POWDER.get())),
                new ItemStack(ModBlocks.DIAMOND_SAW.get())
        ));

        recipes.add(new WorldAssemblyJeiRecipe(
                recipeId("world_assembly/lapidary_table"),
                WorldAssemblyJeiRecipe.Kind.LAPIDARY_TABLE,
                List.of(new ItemStack(Items.LAPIS_BLOCK), new ItemStack(Items.LAPIS_BLOCK)),
                List.of(new ItemStack(ModBlocks.DIAMOND_SAW.get()), new ItemStack(Items.SMOOTH_STONE)),
                hammers,
                new ItemStack(ModBlocks.LAPIDARY_TABLE.get())
        ));

        ModBlocks.SAWMILL_VARIANTS.forEach(variant -> recipes.add(sawmillAssembly(
                recipeId("world_assembly/sawmill/" + variant.id()),
                variant.recipePlanks(),
                variant.recipeLog(),
                variant.block().get(),
                hammers
        )));
        CompatWorkstationRegistry.sawmills().forEach(variant -> {
            ResourceLocation sawmillId = BuiltInRegistries.BLOCK.getKey(variant.sawmill());
            recipes.add(sawmillAssembly(
                    recipeId("world_assembly/sawmill/" + idPath(sawmillId)),
                    variant.planks(),
                    variant.log(),
                    variant.sawmill(),
                    hammers
            ));
        });

        ModBlocks.LEATHER_STATION_VARIANTS.forEach(variant -> recipes.add(leatherStationAssembly(
                recipeId("world_assembly/leather_station/" + variant.id()),
                variant.recipePlanks(),
                variant.recipeLog(),
                variant.block().get(),
                hammers
        )));
        CompatWorkstationRegistry.leatherStations().forEach(variant -> {
            ResourceLocation stationId = BuiltInRegistries.BLOCK.getKey(variant.station());
            recipes.add(leatherStationAssembly(
                    recipeId("world_assembly/leather_station/" + idPath(stationId)),
                    variant.planks(),
                    variant.log(),
                    variant.station(),
                    hammers
            ));
        });
        return List.copyOf(recipes);
    }

    private static WorldAssemblyJeiRecipe leatherStationAssembly(
            ResourceLocation id,
            Block planks,
            Block log,
            Block output,
            List<ItemStack> hammers
    ) {
        return new WorldAssemblyJeiRecipe(
                id,
                WorldAssemblyJeiRecipe.Kind.LEATHER_STATION,
                List.of(new ItemStack(planks)),
                List.of(new ItemStack(log)),
                hammers,
                new ItemStack(output)
        );
    }

    private static WorldAssemblyJeiRecipe sawmillAssembly(
            ResourceLocation id,
            Block planks,
            Block log,
            Block output,
            List<ItemStack> hammers
    ) {
        return new WorldAssemblyJeiRecipe(
                id,
                WorldAssemblyJeiRecipe.Kind.SAWMILL,
                List.of(new ItemStack(planks)),
                List.of(new ItemStack(Items.STONECUTTER), new ItemStack(log)),
                hammers,
                new ItemStack(output)
        );
    }

    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static String idPath(ResourceLocation id) {
        return id.getNamespace() + "/" + id.getPath();
    }
}

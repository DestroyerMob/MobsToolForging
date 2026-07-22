package org.destroyermob.mobstoolforging.gametest;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.fluids.FluidStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.item.CastingMoldItem;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryAlloyRecipe;
import org.destroyermob.mobstoolforging.world.FoundryAlloyRegistry;
import org.destroyermob.mobstoolforging.world.FoundryCastingBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryCastRecipe;
import org.destroyermob.mobstoolforging.world.FoundryCastRegistry;
import org.destroyermob.mobstoolforging.world.FoundryDrainBlock;
import org.destroyermob.mobstoolforging.world.FoundryFaucetBlock;
import org.destroyermob.mobstoolforging.world.FoundryFaucetBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryFuelTankBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryFuelRecipe;
import org.destroyermob.mobstoolforging.world.FoundryFuelRegistry;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRecipe;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRegistry;
import org.destroyermob.mobstoolforging.world.FoundryMeltingPoint;
import org.destroyermob.mobstoolforging.world.FoundryMeltingPointRegistry;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MetallurgyData;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ForgingQuality;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoundryGameTests {
    private static final BlockPos CONTROLLER_POS = new BlockPos(1, 1, 0);

    private FoundryGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void wallTankFuelsDirectMetalMelting(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        FoundryFuelTankBlockEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 1));
        assertTestStructureBlocks(helper);

        helper.assertTrue(forge.refreshStructure(), "Complete 3x3 blackstone foundry did not form");
        helper.assertTrue(forge.structureWidth() == 1, "Minimal foundry interior width was not one block");
        helper.assertTrue(forge.structureDepth() == 1, "Minimal foundry interior depth was not one block");
        helper.assertTrue(forge.structureHeight() == 1, "Minimal foundry interior height was not one block");
        helper.assertTrue(forge.fluidCapacityMb() == 1000, "One interior block did not provide 1,000 mB of molten capacity");
        helper.assertTrue(tank.capacityMb() == 4000, "Fuel tank capacity incorrectly scaled with the foundry interior");
        helper.assertTrue(tank.acceptLavaBucket(), "Foundry fuel tank rejected a lava bucket");
        helper.assertTrue(tank.lavaVisualFraction() == 0.25F, "Fuel tank did not expose its quarter-full visual level");
        helper.assertTrue(forge.connectedTankCount() == 1, "Foundry did not locate its wall-mounted fuel tank");

        ItemStack iron = new ItemStack(Items.IRON_INGOT, 2);
        helper.assertTrue(forge.acceptSolid(iron) == 2, "Foundry did not accept both solid iron inputs");
        helper.assertTrue(iron.isEmpty(), "Accepted iron remained in the offered stack");
        for (int tick = 0; tick < 800; tick++) {
            FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
        }
        helper.assertTrue(forge.solidItemCount() == 0, "Solid iron remained after its melting time");
        helper.assertTrue(forge.moltenAmount(MaterialCatalog.IRON) == 180, "Two iron ingots did not become 180 mB of molten iron");
        helper.assertTrue(tank.lavaBuckets() == 0, "Foundry did not draw its fuel from the wall tank");
        helper.assertTrue(forge.isLit(), "Formed, fueled foundry was not lit");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void brokenOrForeignFoundryWallPreventsHeating(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        FoundryFuelTankBlockEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 1));
        assertTestStructureBlocks(helper);
        helper.assertTrue(forge.refreshStructure(), "Test foundry did not form before its wall was changed");
        helper.assertTrue(tank.acceptLavaBucket(), "Test fuel tank could not be filled");

        helper.setBlock(new BlockPos(1, 1, 2), Blocks.BASALT);
        helper.assertFalse(forge.refreshStructure(), "Basalt incorrectly counted as a tagged blackstone foundry wall");
        helper.assertFalse(forge.isLit(), "Broken foundry continued heating");
        helper.assertTrue(forge.acceptSolid(new ItemStack(Items.IRON_INGOT)) == 0, "Broken foundry accepted a solid metal input");
        helper.assertTrue(tank.lavaBuckets() == 1, "Broken foundry consumed fuel from its tank");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void foundryGlassFormsAsAVisibleWall(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.setBlock(new BlockPos(1, 1, 2), ModBlocks.FOUNDRY_GLASS.get());
        helper.assertTrue(forge.refreshStructure(), "Foundry Glass did not count as a valid wall block");
        helper.assertTrue(forge.structureWidth() == 1 && forge.structureDepth() == 1 && forge.structureHeight() == 1,
                "Replacing a wall with Foundry Glass changed the interior dimensions");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Glass-wall foundry could not hold molten metal");
        helper.assertTrue(forge.moltenVisualFraction() == 0.09F, "Glass-wall foundry did not expose the correct visual fill fraction");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void shrinkingFoundryPausesWithoutTruncatingContents(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        setSecondWallLayer(helper, true);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Two-high shrink test foundry did not form");
        helper.assertTrue(forge.fluidCapacityMb() == 2000, "Two-high shrink test foundry had the wrong capacity");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 1500) == 1500,
                "Could not fill the two-high shrink test foundry");

        setSecondWallLayer(helper, false);
        helper.assertFalse(forge.refreshStructure(), "Over-capacity foundry remained formed after shrinking");
        helper.assertTrue(forge.moltenAmount(MaterialCatalog.IRON) == 1500,
                "Shrinking the foundry truncated its molten contents");

        setSecondWallLayer(helper, true);
        helper.assertTrue(forge.refreshStructure(), "Foundry did not reform after sufficient capacity was restored");
        helper.assertTrue(forge.moltenAmount(MaterialCatalog.IRON) == 1500,
                "Reforming the foundry changed its preserved contents");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void moltenLayersRetainInsertionAndDrainOrder(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Layer-order test foundry did not form");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Could not add the bottom iron layer");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.GOLD, 90) == 90, "Could not add the middle gold layer");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Could not add the top iron layer");
        helper.assertTrue(forge.moltenLayerCount() == 3, "Separated layers of the same metal were incorrectly merged");
        helper.assertTrue(forge.bottomMoltenMaterial().orElseThrow().equals(MaterialCatalog.IRON), "The first inserted layer was not at the drain");
        helper.assertTrue(forge.visibleMoltenMaterial().orElseThrow().equals(MaterialCatalog.IRON), "The last inserted layer was not visible at the surface");
        helper.assertTrue(forge.drainBottom(MaterialCatalog.IRON, 90) == 90, "The bottom layer did not drain");
        helper.assertTrue(forge.bottomMoltenMaterial().orElseThrow().equals(MaterialCatalog.GOLD), "Draining skipped the next ordered layer");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void moltenMaterialsAlloyAtDatapackRatios(GameTestHelper helper) {
        Map<ResourceLocation, FoundryAlloyRecipe> previousRecipes = FoundryAlloyRegistry.snapshot();
        ResourceLocation alloy = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "test_alloy");
        FoundryAlloyRecipe recipe = new FoundryAlloyRecipe(
                ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_ratio"),
                alloy,
                Map.of(MaterialCatalog.IRON, 180, MaterialCatalog.COPPER, 90)
        );
        FoundryAlloyRegistry.replace(Map.of(recipe.id(), recipe));
        try {
            buildMinimalFoundry(helper);
            FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
            helper.assertTrue(forge.refreshStructure(), "Alloy test foundry did not form");
            helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 360) == 360, "Could not add alloy test iron");
            helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.COPPER, 180) == 180, "Could not add alloy test copper");
            helper.assertTrue(forge.moltenAmount(alloy) == 540, "Two complete 2:1 batches did not become alloy");
            helper.assertTrue(forge.moltenAmount(MaterialCatalog.IRON) == 0, "Consumed iron remained after alloying");
            helper.assertTrue(forge.moltenAmount(MaterialCatalog.COPPER) == 0, "Consumed copper remained after alloying");
            helper.assertTrue(forge.moltenLayerCount() == 1, "Completed alloy did not become one drainable layer");
        } finally {
            FoundryAlloyRegistry.replace(previousRecipes);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void datapackMeltingRecipesControlMetalAmounts(GameTestHelper helper) {
        FoundryMeltingRecipe nugget = FoundryMeltingRegistry.find(new ItemStack(Items.IRON_NUGGET)).orElseThrow();
        FoundryMeltingRecipe block = FoundryMeltingRegistry.find(new ItemStack(Items.IRON_BLOCK)).orElseThrow();
        helper.assertTrue(nugget.amountMb() == 10, "Iron nugget melting did not use its datapack amount");
        helper.assertTrue(block.amountMb() == 810, "Iron block melting did not use its datapack amount");
        helper.assertTrue(block.ticks() > nugget.ticks(), "Recipe-specific melting time was ignored");
        helper.assertTrue(FoundryMeltingPointRegistry.celsius(MaterialCatalog.IRON) == 1538.0F,
                "Iron did not use its centralized Celsius melting point");
        helper.assertTrue(FoundryFuelRegistry.find(new FluidStack(Fluids.LAVA, 1000))
                        .map(FoundryFuelRecipe::temperatureC).orElse(0.0F) == 2000.0F,
                "Lava did not use its configured 2,000°C foundry temperature");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void netheriteAlloyRequiresVanillaUpgradeIngredients(GameTestHelper helper) {
        FoundryMeltingRecipe scrap = FoundryMeltingRegistry.find(new ItemStack(Items.NETHERITE_SCRAP)).orElseThrow();
        FoundryAlloyRecipe netherite = FoundryAlloyRegistry.recipes().stream()
                .filter(recipe -> MaterialCatalog.NETHERITE.equals(recipe.result()))
                .findFirst()
                .orElseThrow();
        helper.assertTrue(MaterialCatalog.NETHERITE_SCRAP.equals(scrap.material()),
                "Netherite scrap still melted directly into castable netherite");
        helper.assertTrue(scrap.amountMb() == 90, "Netherite scrap had an unexpected molten volume");
        helper.assertTrue(netherite.inputs().getOrDefault(MaterialCatalog.NETHERITE_SCRAP, 0) == 360,
                "Netherite alloy did not require four scraps");
        helper.assertTrue(netherite.inputs().getOrDefault(MaterialCatalog.GOLD, 0) == 360,
                "Netherite alloy did not require four gold ingots");
        helper.assertTrue(netherite.outputAmountMb() == 90, "Netherite alloy did not produce exactly one ingot");

        Map<ResourceLocation, FoundryMeltingRecipe> previous = FoundryMeltingRegistry.snapshot();
        try {
            FoundryMeltingRegistry.replace(Map.of());
            helper.assertTrue(FoundryMeltingRegistry.find(new ItemStack(Items.NETHERITE_SCRAP)).isEmpty(),
                    "Java fallback still converted scrap directly into netherite");
        } finally {
            FoundryMeltingRegistry.replace(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void activeMeltingUsesSavedRecipeSnapshotAfterReload(GameTestHelper helper) {
        Map<ResourceLocation, FoundryMeltingRecipe> previousMelting = FoundryMeltingRegistry.snapshot();
        Map<ResourceLocation, FoundryMeltingPoint> previousPoints = FoundryMeltingPointRegistry.snapshot();
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_saved_melt");
        ResourceLocation material = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_saved_material");
        ResourceLocation pointId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_saved_point");
        FoundryMeltingRecipe original = new FoundryMeltingRecipe(
                recipeId,
                FoundryMeltingRecipe.Input.item(ResourceLocation.withDefaultNamespace("iron_ingot")),
                material,
                140,
                3
        );
        FoundryMeltingRecipe replacement = new FoundryMeltingRecipe(
                recipeId,
                FoundryMeltingRecipe.Input.item(ResourceLocation.withDefaultNamespace("iron_ingot")),
                MaterialCatalog.GOLD,
                300,
                100
        );
        try {
            FoundryMeltingRegistry.replace(Map.of(recipeId, original));
            FoundryMeltingPointRegistry.replace(Map.of(pointId, new FoundryMeltingPoint(pointId, material, 1000.0F)));
            buildMinimalFoundry(helper);
            FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
            FoundryFuelTankBlockEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 1));
            helper.assertTrue(forge.refreshStructure(), "Saved-process test foundry did not form");
            helper.assertTrue(tank.acceptLavaBucket(), "Saved-process test tank rejected lava");
            helper.assertTrue(forge.acceptSolid(new ItemStack(Items.IRON_INGOT)) == 1,
                    "Saved-process test foundry rejected its input");
            FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            CompoundTag saved = forge.saveWithoutMetadata(helper.getLevel().registryAccess());

            FoundryMeltingRegistry.replace(Map.of(recipeId, replacement));
            forge.loadWithComponents(saved, helper.getLevel().registryAccess());
            for (int tick = 0; tick < 4; tick++) {
                FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            }
            helper.assertTrue(forge.solidItemCount() == 0, "Saved melting input did not complete");
            helper.assertTrue(forge.moltenAmount(material) == 140,
                    "Active melting job changed material or amount after recipe reload");
            helper.assertTrue(forge.moltenAmount(MaterialCatalog.GOLD) == 0,
                    "Replacement melting recipe affected an already accepted input");
        } finally {
            FoundryMeltingRegistry.replace(previousMelting);
            FoundryMeltingPointRegistry.replace(previousPoints);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void savedMoltenLayersDoNotReactDuringLoad(GameTestHelper helper) {
        Map<ResourceLocation, FoundryAlloyRecipe> previousAlloys = FoundryAlloyRegistry.snapshot();
        ResourceLocation alloy = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_load_alloy");
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_load_reaction");
        try {
            FoundryAlloyRegistry.replace(Map.of());
            buildMinimalFoundry(helper);
            FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
            helper.assertTrue(forge.refreshStructure(), "Load-reaction test foundry did not form");
            helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Could not add saved iron layer");
            helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.COPPER, 90) == 90, "Could not add saved copper layer");
            CompoundTag saved = forge.saveWithoutMetadata(helper.getLevel().registryAccess());
            FoundryAlloyRegistry.replace(Map.of(recipeId, new FoundryAlloyRecipe(
                    recipeId,
                    alloy,
                    Map.of(MaterialCatalog.IRON, 90, MaterialCatalog.COPPER, 90),
                    180
            )));
            forge.loadWithComponents(saved, helper.getLevel().registryAccess());
            helper.assertTrue(forge.moltenAmount(MaterialCatalog.IRON) == 90, "Loading consumed saved molten iron");
            helper.assertTrue(forge.moltenAmount(MaterialCatalog.COPPER) == 90, "Loading consumed saved molten copper");
            helper.assertTrue(forge.moltenAmount(alloy) == 0, "A newly added alloy recipe reacted during chunk load");
        } finally {
            FoundryAlloyRegistry.replace(previousAlloys);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void activeCastingUsesSavedCapacityAndOutputAfterReload(GameTestHelper helper) {
        Map<ResourceLocation, FoundryCastRecipe> previousCasts = FoundryCastRegistry.recipes().stream()
                .collect(java.util.stream.Collectors.toMap(FoundryCastRecipe::id, recipe -> recipe));
        BlockPos tablePos = new BlockPos(0, 0, 0);
        ResourceLocation template = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "pickaxe_head");
        try {
            helper.setBlock(tablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
            FoundryCastingBlockEntity table = helper.getBlockEntity(tablePos);
            ItemStack mold = CastingMoldItem.create(template);
            helper.assertTrue(table.insertForm(mold), "Casting snapshot test rejected its reusable mold");
            helper.assertTrue(table.receive(MaterialCatalog.COPPER, 180) == 180,
                    "Casting snapshot test rejected its recipe-defined amount");
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), tablePos, table.getBlockState(), table);
            CompoundTag saved = table.saveWithoutMetadata(helper.getLevel().registryAccess());

            FoundryCastRegistry.replace(Map.of());
            table.loadWithComponents(saved, helper.getLevel().registryAccess());
            helper.assertTrue(table.amountMb() == 180, "Saved casting liquid was clamped by the reloaded registry");
            helper.assertTrue(table.capacityMb() == 180, "Saved casting capacity was replaced by a live fallback");
            for (int tick = 1; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
                FoundryCastingBlockEntity.serverTick(helper.getLevel(), tablePos, table.getBlockState(), table);
            }
            ToolPartData output = table.output().get(ModDataComponents.TOOL_PART.get());
            helper.assertTrue(output != null && output.partType().equals(ToolPartData.PICKAXE_HEAD),
                    "Saved casting output was lost when its datapack recipe disappeared");
            helper.assertTrue(output != null && output.materialId().equals(MaterialCatalog.COPPER),
                    "Saved casting output changed material after reload");

            CompoundTag invalid = saved.copy();
            invalid.remove("PendingOutput");
            table.loadWithComponents(invalid, helper.getLevel().registryAccess());
            for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS + 5; tick++) {
                FoundryCastingBlockEntity.serverTick(helper.getLevel(), tablePos, table.getBlockState(), table);
            }
            helper.assertTrue(table.amountMb() == 180 && table.output().isEmpty(),
                    "Casting liquid was deleted when its saved output was invalid");
        } finally {
            FoundryCastRegistry.replace(previousCasts);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void fuelTemperatureGatesAboveLavaMeltingPoints(GameTestHelper helper) {
        Map<ResourceLocation, FoundryMeltingRecipe> previousMelting = FoundryMeltingRegistry.snapshot();
        Map<ResourceLocation, FoundryMeltingPoint> previousPoints = FoundryMeltingPointRegistry.snapshot();
        Map<ResourceLocation, FoundryFuelRecipe> previousFuels = FoundryFuelRegistry.snapshot();
        ResourceLocation meltingId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_superhot_melting");
        ResourceLocation pointId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_superhot_netherite");
        ResourceLocation fuelId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_lava");
        ResourceLocation lavaId = ResourceLocation.withDefaultNamespace("lava");
        FoundryMeltingRecipe superhotNetherite = new FoundryMeltingRecipe(
                meltingId,
                FoundryMeltingRecipe.Input.item(ResourceLocation.withDefaultNamespace("netherite_scrap")),
                MaterialCatalog.NETHERITE,
                FoundryForgeBlockEntity.INGOT_MB,
                10
        );
        try {
            FoundryMeltingRegistry.replace(Map.of(meltingId, superhotNetherite));
            FoundryMeltingPointRegistry.replace(Map.of(pointId, new FoundryMeltingPoint(
                    pointId,
                    MaterialCatalog.NETHERITE,
                    2200.0F
            )));
            FoundryFuelRegistry.replace(Map.of(fuelId, new FoundryFuelRecipe(
                    fuelId,
                    FoundryFuelRecipe.Input.fluid(lavaId),
                    2000.0F,
                    1000,
                    40
            )));
            buildMinimalFoundry(helper);
            FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
            FoundryFuelTankBlockEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 1));
            helper.assertTrue(forge.refreshStructure(), "Temperature test foundry did not form");
            helper.assertTrue(FoundryMeltingPointRegistry.celsius(MaterialCatalog.NETHERITE) == 2200.0F,
                    "Material melting point was not loaded in Celsius");
            helper.assertTrue(tank.acceptLavaBucket(), "Temperature test tank rejected its registered fluid");
            helper.assertTrue(forge.acceptSolid(new ItemStack(Items.NETHERITE_SCRAP)) == 1, "Foundry rejected the superhot recipe input");
            for (int tick = 0; tick < 20; tick++) {
                FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            }
            helper.assertTrue(tank.fluidAmountMb() == 1000, "Fuel below the melting point was consumed");
            helper.assertTrue(forge.moltenAmountMb() == 0, "Netherite melted below its configured melting point");

            FoundryFuelRegistry.replace(Map.of(fuelId, new FoundryFuelRecipe(
                    fuelId,
                    FoundryFuelRecipe.Input.fluid(lavaId),
                    2300.0F,
                    1000,
                    40
            )));
            for (int tick = 0; tick < 12; tick++) {
                FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            }
            helper.assertTrue(tank.fluidAmountMb() == 0, "Sufficiently hot fuel was not drawn from the tank");
            helper.assertTrue(forge.moltenAmount(MaterialCatalog.NETHERITE) == 90, "Superhot fuel did not melt the gated metal");
        } finally {
            FoundryMeltingRegistry.replace(previousMelting);
            FoundryMeltingPointRegistry.replace(previousPoints);
            FoundryFuelRegistry.replace(previousFuels);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void castingTableAndBasinCoolStandardOutputs(GameTestHelper helper) {
        BlockPos tablePos = new BlockPos(0, 0, 0);
        BlockPos basinPos = new BlockPos(2, 0, 0);
        helper.setBlock(tablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        helper.setBlock(basinPos, ModBlocks.FOUNDRY_CASTING_BASIN.get());
        FoundryCastingBlockEntity table = helper.getBlockEntity(tablePos);
        FoundryCastingBlockEntity basin = helper.getBlockEntity(basinPos);
        helper.assertTrue(table.receive(MaterialCatalog.IRON, FoundryForgeBlockEntity.INGOT_MB) == 90, "Casting table rejected one ingot of iron");
        helper.assertTrue(basin.receive(MaterialCatalog.GOLD, FoundryForgeBlockEntity.BLOCK_MB) == 810, "Casting basin rejected one block of gold");
        for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), tablePos, table.getBlockState(), table);
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), basinPos, basin.getBlockState(), basin);
        }
        helper.assertTrue(table.output().is(Items.IRON_INGOT), "Casting table did not cool into an iron ingot");
        helper.assertTrue(basin.output().is(Items.GOLD_BLOCK), "Casting basin did not cool into a gold block");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void goldOverPartCreatesReusableDatapackCast(GameTestHelper helper) {
        BlockPos firstTablePos = new BlockPos(0, 0, 0);
        BlockPos secondTablePos = new BlockPos(2, 0, 0);
        helper.setBlock(firstTablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        helper.setBlock(secondTablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity firstTable = helper.getBlockEntity(firstTablePos);
        FoundryCastingBlockEntity secondTable = helper.getBlockEntity(secondTablePos);

        ItemStack sacrificialPart = ModItems.PICKAXE_HEAD.get().createPart(MaterialCatalog.IRON);
        helper.assertTrue(firstTable.insertForm(sacrificialPart), "Casting table rejected a recipe-backed pickaxe head");
        helper.assertTrue(sacrificialPart.isEmpty(), "Inserted sacrificial part remained in the offered stack");
        helper.assertTrue(firstTable.capacityMb() == 180, "Pickaxe cast creation did not require two ingots of gold");
        helper.assertTrue(firstTable.receive(MaterialCatalog.GOLD, 180) == 180, "Casting table rejected molten gold over the part");
        for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), firstTablePos, firstTable.getBlockState(), firstTable);
        }
        ItemStack cast = firstTable.output();
        helper.assertTrue(cast.is(ModItems.CASTING_MOLD.get()), "Gold-over-part recipe did not create a reusable cast");
        helper.assertTrue(cast.get(ModDataComponents.FORGE_TEMPLATE.get()) != null, "Created cast did not retain its template cutout");
        helper.assertTrue(firstTable.form().isEmpty(), "Sacrificial part was not consumed when the cast formed");

        helper.assertTrue(secondTable.insertForm(cast), "Second casting table rejected the completed cast");
        helper.assertTrue(secondTable.capacityMb() == 180, "Pickaxe cast did not use its recipe-defined metal amount");
        helper.assertTrue(secondTable.receive(MaterialCatalog.COPPER, 180) == 180, "Pickaxe cast rejected molten copper");
        for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), secondTablePos, secondTable.getBlockState(), secondTable);
        }
        ToolPartData outputData = secondTable.output().get(ModDataComponents.TOOL_PART.get());
        helper.assertTrue(outputData != null && outputData.partType().equals(ToolPartData.PICKAXE_HEAD), "Reusable cast did not produce a pickaxe head");
        helper.assertTrue(outputData.materialId().equals(MaterialCatalog.COPPER), "Reusable cast did not preserve the poured metal");
        helper.assertTrue(outputData.qualityLevel() == ForgingQuality.CRUDE, "Directly cast tool part did not have crude workmanship");
        helper.assertTrue(outputData.polished().qualityLevel() == ForgingQuality.CRUDE, "Polishing incorrectly improved cast workmanship");
        MetallurgyData metallurgy = secondTable.output().get(ModDataComponents.METALLURGY.get());
        helper.assertTrue(metallurgy != null && metallurgy.origin() == MetallurgyData.Origin.CAST, "Cast part did not record its manufacture method");
        helper.assertTrue(metallurgy != null && metallurgy.castDefect() == MetallurgyData.CastDefect.POROSITY, "Cast part did not record casting porosity");
        helper.assertTrue(secondTable.form().is(ModItems.CASTING_MOLD.get()), "Reusable cast was consumed by a normal pour");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void partRecyclingRewardsConditionAndWorkmanship(GameTestHelper helper) {
        ItemStack crude = ModItems.PICKAXE_HEAD.get().createPart(MaterialCatalog.IRON, ForgingQuality.CRUDE.score());
        ItemStack fine = ModItems.PICKAXE_HEAD.get().createPart(MaterialCatalog.IRON, ForgingQuality.MASTERWORK.score());
        int crudeYield = FoundryForgeBlockEntity.recyclingYieldMb(crude);
        int fineYield = FoundryForgeBlockEntity.recyclingYieldMb(fine);
        fine.set(ModDataComponents.TOOL_PART_WEAR.get(), 5000);
        int wornYield = FoundryForgeBlockEntity.recyclingYieldMb(fine);
        helper.assertTrue(crudeYield > 0, "A valid iron tool part could not be recycled");
        helper.assertTrue(fineYield > crudeYield, "Better workmanship did not improve metal recovery");
        helper.assertTrue(wornYield < fineYield, "Part wear did not reduce metal recovery");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 30)
    public static void droppedMetalIsAbsorbedThroughOpenInterior(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Dropped-item intake test foundry did not form");
        BlockPos interior = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemEntity dropped = new ItemEntity(
                helper.getLevel(),
                interior.getX() + 0.5D,
                interior.getY() + 0.25D,
                interior.getZ() + 0.5D,
                new ItemStack(Items.IRON_INGOT)
        );
        helper.getLevel().addFreshEntity(dropped);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(dropped.isRemoved() || dropped.getItem().isEmpty(), "Dropped iron remained in the open foundry interior");
            helper.assertTrue(forge.solidItemCount() == 1, "Dropped iron was not stored as a visible solid input");
            helper.succeed();
        });
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 120)
    public static void drainAndFaucetPourBottomLayerIntoCastingTable(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        BlockPos drainPos = new BlockPos(2, 1, 1);
        BlockPos faucetPos = new BlockPos(3, 1, 1);
        BlockPos tablePos = faucetPos.below();
        // Drain facing is decorative; physical attachment determines the connection.
        helper.setBlock(drainPos, ModBlocks.FOUNDRY_DRAIN.get().defaultBlockState().setValue(FoundryDrainBlock.FACING, Direction.WEST));
        helper.setBlock(faucetPos, ModBlocks.FOUNDRY_FAUCET.get().defaultBlockState()
                .setValue(FoundryFaucetBlock.FACING, Direction.EAST)
                .setValue(FoundryFaucetBlock.ACTIVE, true));
        helper.setBlock(tablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "A wall drain did not count as part of the foundry shell");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 180) == 180, "Could not fill the faucet test foundry");
        helper.getLevel().scheduleTick(helper.absolutePos(faucetPos), ModBlocks.FOUNDRY_FAUCET.get(), 1);
        helper.runAfterDelay(105, () -> {
            FoundryCastingBlockEntity table = helper.getBlockEntity(tablePos);
            FoundryFaucetBlockEntity faucet = helper.getBlockEntity(faucetPos);
            helper.assertTrue(forge.moltenAmountMb() == 90, "A single faucet activation poured more than one casting-table batch");
            helper.assertTrue(table.output().is(Items.IRON_INGOT), "Faucet-poured metal did not cool into an ingot");
            helper.assertFalse(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.ACTIVE), "Faucet remained active after its casting operation");
            helper.assertTrue(faucet.pouringMaterial().isEmpty(), "Faucet retained its molten stream after pouring stopped");
            helper.succeed();
        });
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void emptyFaucetStopsAndClearsItsStream(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        BlockPos drainPos = new BlockPos(2, 1, 1);
        BlockPos faucetPos = new BlockPos(3, 1, 1);
        helper.setBlock(drainPos, ModBlocks.FOUNDRY_DRAIN.get());
        helper.setBlock(faucetPos, ModBlocks.FOUNDRY_FAUCET.get().defaultBlockState()
                .setValue(FoundryFaucetBlock.FACING, Direction.EAST)
                .setValue(FoundryFaucetBlock.ACTIVE, true));
        helper.setBlock(faucetPos.below(), ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        FoundryFaucetBlockEntity faucet = helper.getBlockEntity(faucetPos);
        helper.assertTrue(forge.refreshStructure(), "Empty-faucet test foundry did not form");
        faucet.setPouringMaterial(MaterialCatalog.IRON);
        helper.getLevel().scheduleTick(helper.absolutePos(faucetPos), ModBlocks.FOUNDRY_FAUCET.get(), 1);
        helper.runAfterDelay(8, () -> {
            helper.assertFalse(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.ACTIVE), "Empty faucet kept retrying after its failed pour");
            helper.assertTrue(faucet.pouringMaterial().isEmpty(), "Empty faucet did not clear its stale molten stream");
            helper.succeed();
        });
    }

    private static void buildMinimalFoundry(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean interior = x == 1 && z == 1;
                boolean wallFace = (x == 1) != (z == 1);
                helper.setBlock(new BlockPos(x, 0, z), interior ? Blocks.BLACKSTONE : Blocks.AIR);
                helper.setBlock(new BlockPos(x, 1, z), wallFace ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.AIR);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
            }
        }
        helper.setBlock(CONTROLLER_POS, ModBlocks.FOUNDRY_FORGE.get());
        helper.setBlock(new BlockPos(0, 1, 1), ModBlocks.FOUNDRY_FUEL_TANK.get());
    }

    private static void setSecondWallLayer(GameTestHelper helper, boolean present) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean wallFace = (x == 1) != (z == 1);
                helper.setBlock(new BlockPos(x, 2, z), present && wallFace
                        ? Blocks.POLISHED_BLACKSTONE_BRICKS
                        : Blocks.AIR);
            }
        }
    }

    private static void assertTestStructureBlocks(GameTestHelper helper) {
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 1)).isAir(), "Test foundry interior was not empty");
        helper.assertTrue(helper.getBlockState(new BlockPos(0, 1, 1)).is(ModTags.Blocks.FOUNDRY_STRUCTURE_BLOCKS), "Test foundry fuel tank was missing the structure tag");
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 0, 1)).is(ModTags.Blocks.FOUNDRY_STRUCTURE_BLOCKS), "Test foundry floor was missing the structure tag");
    }
}

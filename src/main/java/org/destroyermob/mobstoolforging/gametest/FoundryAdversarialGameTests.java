package org.destroyermob.mobstoolforging.gametest;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.CastingMoldItem;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.FoundryCastRecipe;
import org.destroyermob.mobstoolforging.world.FoundryCastRegistry;
import org.destroyermob.mobstoolforging.world.FoundryCastingBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryAlloyRecipe;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryFuelTankBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRecipe;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRegistry;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartData;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoundryAdversarialGameTests {
    private static final BlockPos CONTROLLER_POS = new BlockPos(1, 1, 0);

    private FoundryAdversarialGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void netheriteScrapCannotBypassTheAlloyThroughCasting(GameTestHelper helper) {
        BlockPos moldedTablePos = new BlockPos(0, 0, 0);
        helper.setBlock(moldedTablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity moldedTable = helper.getBlockEntity(moldedTablePos);
        helper.assertTrue(moldedTable.insertForm(CastingMoldItem.create(ForgeTemplate.PICKAXE_HEAD.registryId())),
                "Scrap test could not insert its reusable mold");
        helper.assertTrue(moldedTable.receive(MaterialCatalog.NETHERITE_SCRAP, 90) == 0,
                "Molten netherite scrap bypassed the alloy by casting into a tool part");

        BlockPos emptyTablePos = new BlockPos(2, 0, 0);
        helper.setBlock(emptyTablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity emptyTable = helper.getBlockEntity(emptyTablePos);
        helper.assertTrue(emptyTable.receive(MaterialCatalog.NETHERITE_SCRAP, 90) == 0,
                "Molten netherite scrap cast directly into an ingot");
        helper.assertTrue(emptyTable.receive(MaterialCatalog.NETHERITE, 90) == 90,
                "Completed netherite alloy could not be cast into its intended ingot");
        for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), emptyTablePos, emptyTable.getBlockState(), emptyTable);
        }
        helper.assertTrue(emptyTable.output().is(Items.NETHERITE_INGOT),
                "Completed netherite alloy produced the wrong standard cast output");

        BlockPos basinPos = new BlockPos(4, 0, 0);
        helper.setBlock(basinPos, ModBlocks.FOUNDRY_CASTING_BASIN.get());
        FoundryCastingBlockEntity basin = helper.getBlockEntity(basinPos);
        helper.assertTrue(basin.receive(MaterialCatalog.NETHERITE_SCRAP, 900) == 0,
                "Molten netherite scrap cast directly into a storage block");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void invalidSacrificialFormCannotFallThroughToAnIngot(GameTestHelper helper) {
        BlockPos tablePos = new BlockPos(0, 0, 0);
        helper.setBlock(tablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity table = helper.getBlockEntity(tablePos);
        helper.assertTrue(table.insertForm(new ItemStack(ModItems.PICKAXE_HEAD.get())),
                "Invalid-form test could not insert its sacrificial form");
        helper.assertTrue(table.receive(MaterialCatalog.COPPER, 90) == 0,
                "Wrong-material sacrificial form fell through to a standard ingot");
        helper.assertFalse(table.form().isEmpty(), "Rejected pour consumed the sacrificial form");
        helper.assertTrue(table.amountMb() == 0, "Rejected pour left liquid in the casting table");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void unresolvedLegacyCastRecoversWhenItsRecipeReturns(GameTestHelper helper) {
        Map<ResourceLocation, FoundryCastRecipe> previous = new LinkedHashMap<>();
        FoundryCastRegistry.recipes().forEach(recipe -> previous.put(recipe.id(), recipe));
        BlockPos tablePos = new BlockPos(0, 0, 0);
        helper.setBlock(tablePos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity table = helper.getBlockEntity(tablePos);
        try {
            CompoundTag legacy = new CompoundTag();
            legacy.putString("Material", MaterialCatalog.COPPER.toString());
            legacy.putInt("Amount", 180);
            legacy.put("Form", CastingMoldItem.create(ForgeTemplate.PICKAXE_HEAD.registryId())
                    .saveOptional(helper.getLevel().registryAccess()));

            FoundryCastRegistry.replace(Map.of());
            table.loadWithComponents(legacy, helper.getLevel().registryAccess());
            for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS + 5; tick++) {
                FoundryCastingBlockEntity.serverTick(helper.getLevel(), tablePos, table.getBlockState(), table);
            }
            helper.assertTrue(table.amountMb() == 180 && table.output().isEmpty(),
                    "Unresolved legacy cast lost liquid while its recipe was absent");

            FoundryCastRegistry.replace(previous);
            for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
                FoundryCastingBlockEntity.serverTick(helper.getLevel(), tablePos, table.getBlockState(), table);
            }
            ToolPartData part = table.output().get(ModDataComponents.TOOL_PART.get());
            helper.assertTrue(part != null && ToolPartData.PICKAXE_HEAD.equals(part.partType()),
                    "Legacy cast did not recover after its recipe returned");
            helper.assertTrue(part != null && MaterialCatalog.COPPER.equals(part.materialId()),
                    "Recovered legacy cast changed material");
        } finally {
            FoundryCastRegistry.replace(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void unresolvedLegacyMeltRecoversWhenItsRecipeReturns(GameTestHelper helper) {
        Map<ResourceLocation, FoundryMeltingRecipe> previous = FoundryMeltingRegistry.snapshot();
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_legacy_melt");
        ResourceLocation material = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_legacy_molten");
        try {
            FoundryMeltingRegistry.replace(Map.of());
            buildMinimalFoundry(helper);
            FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
            FoundryFuelTankBlockEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 1));
            helper.assertTrue(tank.acceptLavaBucket(), "Legacy-melt test tank rejected lava");

            CompoundTag legacy = new CompoundTag();
            ListTag inputs = new ListTag();
            CompoundTag entry = new CompoundTag();
            entry.put("Stack", new ItemStack(Items.CLAY_BALL).saveOptional(helper.getLevel().registryAccess()));
            inputs.add(entry);
            legacy.put("SolidInputs", inputs);
            forge.loadWithComponents(legacy, helper.getLevel().registryAccess());
            helper.assertTrue(forge.refreshStructure(), "Legacy-melt test foundry did not form");
            FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            helper.assertTrue(forge.solidItemCount() == 1, "Missing legacy recipe deleted its queued input");

            FoundryMeltingRegistry.replace(Map.of(recipeId, new FoundryMeltingRecipe(
                    recipeId,
                    FoundryMeltingRecipe.Input.item(ResourceLocation.withDefaultNamespace("clay_ball")),
                    material,
                    90,
                    1
            )));
            for (int tick = 0; tick < 25; tick++) {
                FoundryForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            }
            helper.assertTrue(forge.solidItemCount() == 0, "Legacy queued input did not resume when its recipe returned");
            helper.assertTrue(forge.moltenAmount(material) == 90, "Recovered legacy melt produced the wrong material or volume");
        } finally {
            FoundryMeltingRegistry.replace(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void expandingAlloyDefinitionsAreRejected(GameTestHelper helper) {
        boolean rejected = false;
        try {
            new FoundryAlloyRecipe(
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_expanding_alloy"),
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gametest_expanded_result"),
                    Map.of(MaterialCatalog.IRON, 90, MaterialCatalog.COPPER, 90),
                    181
            );
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Volume-expanding alloy definition was accepted");
        helper.succeed();
    }

    private static void buildMinimalFoundry(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean interior = x == 1 && z == 1;
                boolean wallFace = (x == 1) != (z == 1);
                helper.setBlock(new BlockPos(x, 0, z), interior ? net.minecraft.world.level.block.Blocks.BLACKSTONE
                        : net.minecraft.world.level.block.Blocks.AIR);
                helper.setBlock(new BlockPos(x, 1, z), wallFace ? net.minecraft.world.level.block.Blocks.POLISHED_BLACKSTONE_BRICKS
                        : net.minecraft.world.level.block.Blocks.AIR);
                helper.setBlock(new BlockPos(x, 2, z), net.minecraft.world.level.block.Blocks.AIR);
            }
        }
        helper.setBlock(CONTROLLER_POS, ModBlocks.FOUNDRY_FORGE.get());
        helper.setBlock(new BlockPos(0, 1, 1), ModBlocks.FOUNDRY_FUEL_TANK.get());
    }
}

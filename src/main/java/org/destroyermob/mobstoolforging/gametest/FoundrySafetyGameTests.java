package org.destroyermob.mobstoolforging.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.item.CastingMoldItem;
import org.destroyermob.mobstoolforging.world.FoundryCastingBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryAccess;
import org.destroyermob.mobstoolforging.world.FoundryDrainBlock;
import org.destroyermob.mobstoolforging.world.FoundryFaucetBlock;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryFuelTankBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryStructure;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoundrySafetyGameTests {
    private static final BlockPos CONTROLLER_POS = new BlockPos(1, 1, 0);

    private FoundrySafetyGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void filledFoundryBlocksPreserveTheirCompleteDropState(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        FoundryFuelTankBlockEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 1));
        helper.assertTrue(forge.refreshStructure(), "Drop-state test foundry did not form");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Could not add molten metal before copying the controller drop");
        helper.assertTrue(forge.acceptSolid(new ItemStack(Items.GOLD_INGOT)) == 1, "Could not queue a solid before copying the controller drop");

        FluidStack namedFuel = new FluidStack(Fluids.LAVA, FoundryFuelTankBlockEntity.BUCKET_MB);
        namedFuel.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved test fuel"));
        helper.assertTrue(tank.fluidHandler().fill(namedFuel, IFluidHandler.FluidAction.EXECUTE) == namedFuel.getAmount(),
                "Could not fill the component-bearing test fuel");

        BlockPos castingPos = new BlockPos(4, 1, 0);
        helper.setBlock(castingPos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity casting = helper.getBlockEntity(castingPos);
        helper.assertTrue(casting.receive(MaterialCatalog.IRON, 90) == 90, "Could not fill the casting table before copying its drop");

        ItemStack controllerDrop = blockDrop(helper, CONTROLLER_POS);
        ItemStack tankDrop = blockDrop(helper, new BlockPos(0, 1, 1));
        ItemStack castingDrop = blockDrop(helper, castingPos);
        helper.assertTrue(controllerDrop.has(DataComponents.BLOCK_ENTITY_DATA), "Filled controller drop did not contain block-entity data");
        helper.assertTrue(tankDrop.has(DataComponents.BLOCK_ENTITY_DATA), "Filled tank drop did not contain block-entity data");
        helper.assertTrue(castingDrop.has(DataComponents.BLOCK_ENTITY_DATA), "Filled casting-table drop did not contain block-entity data");
        CompoundTag controllerState = controllerDrop.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        for (String derivedKey : List.of(
                "Formed",
                "InteriorMinX", "InteriorMinY", "InteriorMinZ",
                "InteriorMaxX", "InteriorMaxY", "InteriorMaxZ",
                "StructureWidth", "StructureDepth", "StructureHeight"
        )) {
            helper.assertFalse(controllerState.contains(derivedKey), "Portable controller retained derived key " + derivedKey);
        }

        BlockPos restoredControllerPos = new BlockPos(5, 1, 0);
        BlockPos restoredTankPos = new BlockPos(6, 1, 0);
        BlockPos restoredCastingPos = new BlockPos(7, 1, 0);
        helper.setBlock(restoredControllerPos, ModBlocks.FOUNDRY_FORGE.get());
        helper.setBlock(restoredTankPos, ModBlocks.FOUNDRY_FUEL_TANK.get());
        helper.setBlock(restoredCastingPos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        helper.assertTrue(restoreBlockEntityData(helper, restoredControllerPos, controllerDrop), "Controller drop could not restore its data");
        helper.assertTrue(restoreBlockEntityData(helper, restoredTankPos, tankDrop), "Tank drop could not restore its data");
        helper.assertTrue(restoreBlockEntityData(helper, restoredCastingPos, castingDrop), "Casting-table drop could not restore its data");

        FoundryForgeBlockEntity restoredForge = helper.getBlockEntity(restoredControllerPos);
        FoundryFuelTankBlockEntity restoredTank = helper.getBlockEntity(restoredTankPos);
        FoundryCastingBlockEntity restoredCasting = helper.getBlockEntity(restoredCastingPos);
        helper.assertTrue(restoredForge.moltenAmount(MaterialCatalog.IRON) == 90, "Controller drop lost molten metal");
        helper.assertTrue(restoredForge.solidItemCount() == 1, "Controller drop lost its queued solid");
        helper.assertTrue(restoredTank.fluidAmountMb() == namedFuel.getAmount(), "Tank drop lost fuel volume");
        helper.assertTrue(FluidStack.isSameFluidSameComponents(namedFuel, restoredTank.fluidStack()), "Tank drop lost fluid components");
        helper.assertTrue(restoredCasting.amountMb() == 90 && restoredCasting.material().filter(MaterialCatalog.IRON::equals).isPresent(),
                "Casting-table drop lost its partial cast");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void emptyFoundryBlockDropsRemainOrdinaryStackableItems(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Empty-drop test foundry did not form");
        BlockPos tankPos = new BlockPos(0, 1, 1);
        BlockPos castingPos = new BlockPos(4, 1, 0);
        helper.setBlock(castingPos, ModBlocks.FOUNDRY_CASTING_TABLE.get());

        for (BlockPos pos : List.of(CONTROLLER_POS, tankPos, castingPos)) {
            ItemStack drop = blockDrop(helper, pos);
            ItemStack ordinary = new ItemStack(helper.getBlockState(pos).getBlock());
            helper.assertFalse(drop.isEmpty(), "Empty foundry block did not produce its normal self drop");
            helper.assertFalse(drop.has(DataComponents.BLOCK_ENTITY_DATA), "Empty foundry drop retained block-entity data");
            helper.assertTrue(ItemStack.isSameItemSameComponents(drop, ordinary), "Empty foundry drop did not stack with a fresh item");
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void correctToolBreakKeepsStateInOneControllerItem(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Correct-tool drop test foundry did not form");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Could not add molten metal before breaking the controller");
        helper.assertTrue(forge.acceptSolid(new ItemStack(Items.GOLD_INGOT)) == 1, "Could not queue a solid before breaking the controller");
        helper.killAllEntitiesOfClass(ItemEntity.class);

        FakePlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(CONTROLLER_POS)), "Fake player could not break the controller");

        List<ItemEntity> drops = helper.getEntities(EntityType.ITEM, CONTROLLER_POS, 2.0D);
        List<ItemStack> controllerDrops = drops.stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(ModBlocks.FOUNDRY_FORGE.get().asItem()))
                .toList();
        int looseGold = drops.stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(Items.GOLD_INGOT))
                .mapToInt(ItemStack::getCount)
                .sum();
        helper.assertTrue(controllerDrops.size() == 1 && controllerDrops.getFirst().getCount() == 1,
                "Correct-tool break did not produce exactly one controller item");
        helper.assertTrue(controllerDrops.getFirst().has(DataComponents.BLOCK_ENTITY_DATA),
                "Correct-tool controller drop lost its portable contents");
        helper.assertTrue(looseGold == 0, "Correct-tool break duplicated the queued solid as loose loot");

        BlockPos restoredPos = new BlockPos(5, 1, 0);
        helper.setBlock(restoredPos, ModBlocks.FOUNDRY_FORGE.get());
        helper.assertTrue(restoreBlockEntityData(helper, restoredPos, controllerDrops.getFirst()), "Broken controller item could not restore its state");
        FoundryForgeBlockEntity restored = helper.getBlockEntity(restoredPos);
        helper.assertTrue(restored.moltenAmount(MaterialCatalog.IRON) == 90, "Correct-tool drop lost molten metal");
        helper.assertTrue(restored.solidItemCount() == 1, "Correct-tool drop lost its queued solid");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void replacementAfterLootPreviewRecoversTangibleContentsOnce(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Replacement fallback test foundry did not form");
        helper.assertTrue(forge.acceptSolid(new ItemStack(Items.GOLD_INGOT)) == 1, "Could not queue the replacement-test solid");

        BlockPos formPos = new BlockPos(4, 1, 0);
        BlockPos outputPos = new BlockPos(7, 1, 0);
        helper.setBlock(formPos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        helper.setBlock(outputPos, ModBlocks.FOUNDRY_CASTING_TABLE.get());
        FoundryCastingBlockEntity formTable = helper.getBlockEntity(formPos);
        FoundryCastingBlockEntity outputTable = helper.getBlockEntity(outputPos);
        ResourceLocation pickaxeTemplate = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "pickaxe_head");
        helper.assertTrue(formTable.insertForm(CastingMoldItem.create(pickaxeTemplate)), "Could not insert the replacement-test casting mold");
        helper.assertTrue(outputTable.receive(MaterialCatalog.IRON, FoundryForgeBlockEntity.INGOT_MB) == FoundryForgeBlockEntity.INGOT_MB,
                "Could not fill the replacement-test output table");
        for (int tick = 0; tick < FoundryCastingBlockEntity.COOLING_TICKS; tick++) {
            FoundryCastingBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(outputPos), outputTable.getBlockState(), outputTable);
        }
        helper.assertTrue(outputTable.output().is(Items.IRON_INGOT), "Replacement-test cast did not finish");

        blockDrop(helper, CONTROLLER_POS);
        blockDrop(helper, formPos);
        blockDrop(helper, outputPos);
        helper.killAllEntitiesOfClass(ItemEntity.class);
        helper.setBlock(CONTROLLER_POS, Blocks.STONE);
        helper.setBlock(formPos, Blocks.STONE);
        helper.setBlock(outputPos, Blocks.STONE);

        helper.assertItemEntityCountIs(Items.GOLD_INGOT, CONTROLLER_POS, 1.5D, 1);
        helper.assertItemEntityCountIs(ModItems.CASTING_MOLD.get(), formPos, 1.5D, 1);
        helper.assertItemEntityCountIs(Items.IRON_INGOT, outputPos, 1.5D, 1);
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void maximumHeightFoundryRequiresAnOpenTop(GameTestHelper helper) {
        buildTallFoundry(helper);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        BlockPos roofPos = new BlockPos(1, 9, 1);
        helper.setBlock(roofPos, Blocks.POLISHED_BLACKSTONE_BRICKS);
        helper.assertFalse(forge.refreshStructure(), "Eight-high foundry accepted a ninth-layer roof");
        helper.setBlock(roofPos, Blocks.AIR);
        helper.assertTrue(forge.refreshStructure(), "Eight-high foundry did not form after its top was opened");
        helper.assertTrue(forge.structureHeight() == 8, "Maximum-height foundry reported the wrong interior height");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void foundryDiagnosisIdentifiesTheExactMissingWallBlock(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean wallFace = (x == 1) != (z == 1);
                helper.setBlock(new BlockPos(x, 2, z), wallFace ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.AIR);
                helper.setBlock(new BlockPos(x, 3, z), Blocks.AIR);
            }
        }
        BlockPos missing = new BlockPos(1, 2, 2);
        helper.setBlock(missing, Blocks.AIR);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        FoundryStructure.Diagnosis diagnosis = forge.structureDiagnosis();
        helper.assertTrue(diagnosis.failure() == FoundryStructure.Failure.WALL_MISSING,
                "Incomplete upper wall did not report a missing wall block");
        helper.assertTrue(diagnosis.problemPos().filter(helper.absolutePos(missing)::equals).isPresent(),
                "Foundry diagnosis did not identify the exact missing wall coordinate");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void foundryDiagnosisDistinguishesAnObstructedInterior(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        BlockPos obstruction = new BlockPos(1, 1, 1);
        helper.setBlock(obstruction, Blocks.BASALT);
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        FoundryStructure.Diagnosis diagnosis = forge.structureDiagnosis();
        helper.assertTrue(diagnosis.failure() == FoundryStructure.Failure.ENTRANCE_BLOCKED,
                "Blocked first interior cell did not report an entrance obstruction");
        helper.assertTrue(diagnosis.problemPos().filter(helper.absolutePos(obstruction)::equals).isPresent(),
                "Foundry diagnosis did not identify the obstructing interior coordinate");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void indexedControllerOwnershipInvalidatesWithTheStructure(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        BlockPos drainPos = new BlockPos(2, 1, 1);
        helper.setBlock(drainPos, ModBlocks.FOUNDRY_DRAIN.get().defaultBlockState().setValue(FoundryDrainBlock.FACING, Direction.WEST));
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Controller-index test foundry did not form");
        helper.assertTrue(FoundryAccess.findController(helper.getLevel(), helper.absolutePos(drainPos)).orElse(null) == forge,
                "Indexed shell position did not resolve its controller");

        helper.setBlock(new BlockPos(1, 1, 2), Blocks.AIR);
        helper.assertFalse(forge.refreshStructure(), "Broken controller-index test foundry stayed formed");
        helper.assertTrue(FoundryAccess.findController(helper.getLevel(), helper.absolutePos(drainPos)).isEmpty(),
                "Controller index retained ownership after the structure became invalid");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 220)
    public static void poweredFaucetRetriesAndStopsOnFallingSignal(GameTestHelper helper) {
        buildMinimalFoundry(helper);
        BlockPos drainPos = new BlockPos(2, 1, 1);
        BlockPos faucetPos = new BlockPos(3, 1, 1);
        BlockPos powerPos = faucetPos.above();
        helper.setBlock(drainPos, ModBlocks.FOUNDRY_DRAIN.get().defaultBlockState().setValue(FoundryDrainBlock.FACING, Direction.WEST));
        helper.setBlock(faucetPos, ModBlocks.FOUNDRY_FAUCET.get().defaultBlockState().setValue(FoundryFaucetBlock.FACING, Direction.EAST));
        FoundryForgeBlockEntity forge = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(forge.refreshStructure(), "Powered-faucet test foundry did not form");
        helper.assertTrue(forge.addMoltenLayer(MaterialCatalog.IRON, 90) == 90, "Could not fill the powered-faucet test foundry");

        helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);
        helper.assertTrue(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.POWERED), "Faucet did not record its rising redstone signal");
        helper.assertTrue(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.ACTIVE), "Faucet did not activate on rising redstone");
        helper.runAfterDelay(8, () -> {
            helper.assertTrue(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.ACTIVE),
                    "Powered faucet latched off while its receiver was temporarily absent");
            helper.setBlock(faucetPos.below(), ModBlocks.FOUNDRY_CASTING_TABLE.get());
            helper.runAfterDelay(170, () -> {
                FoundryCastingBlockEntity casting = helper.getBlockEntity(faucetPos.below());
                helper.assertTrue(casting.output().is(Items.IRON_INGOT), "Powered faucet did not resume when a receiver appeared");
                helper.assertTrue(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.ACTIVE),
                        "Powered faucet stopped before its redstone signal fell");
                helper.setBlock(powerPos, Blocks.AIR);
                helper.runAfterDelay(1, () -> {
                    helper.assertFalse(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.POWERED),
                            "Faucet retained a stale powered state after the signal fell");
                    helper.assertFalse(helper.getBlockState(faucetPos).getValue(FoundryFaucetBlock.ACTIVE),
                            "Faucet remained active after the signal fell");
                    helper.succeed();
                });
            });
        });
    }

    private static ItemStack blockDrop(GameTestHelper helper, BlockPos pos) {
        BlockPos absolutePos = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(absolutePos);
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), absolutePos, blockEntity);
        return drops.stream().filter(drop -> drop.is(state.getBlock().asItem())).findFirst().orElse(ItemStack.EMPTY);
    }

    private static boolean restoreBlockEntityData(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        return BlockItem.updateCustomBlockEntityTag(helper.getLevel(), null, helper.absolutePos(pos), stack);
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

    private static void buildTallFoundry(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean interior = x == 1 && z == 1;
                boolean wallFace = (x == 1) != (z == 1);
                helper.setBlock(new BlockPos(x, 0, z), interior ? Blocks.BLACKSTONE : Blocks.AIR);
                for (int y = 1; y <= 9; y++) {
                    helper.setBlock(new BlockPos(x, y, z), y <= 8 && wallFace ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.AIR);
                }
            }
        }
        helper.setBlock(CONTROLLER_POS, ModBlocks.FOUNDRY_FORGE.get());
    }
}

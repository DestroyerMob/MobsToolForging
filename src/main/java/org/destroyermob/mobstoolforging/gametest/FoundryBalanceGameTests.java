package org.destroyermob.mobstoolforging.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.FoundryCastRegistry;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryFuelRegistry;
import org.destroyermob.mobstoolforging.world.FoundryMeltingPointRegistry;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRegistry;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoundryBalanceGameTests {
    private FoundryBalanceGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void atLeastOneConfiguredFuelCanReachEveryMeltingPoint(GameTestHelper helper) {
        float hottestFuel = FoundryFuelRegistry.recipes().stream()
                .map(recipe -> recipe.temperatureC())
                .max(Float::compare)
                .orElse(0.0F);
        FoundryMeltingPointRegistry.values().forEach(point -> helper.assertTrue(
                point.celsius() <= hottestFuel,
                "No configured foundry fuel can melt " + point.material() + " at " + point.celsius() + "°C"
        ));
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void alloyIngredientsCannotBeForgedOrCastAsToolMaterials(GameTestHelper helper) {
        helper.assertFalse(MaterialCatalog.isNormalForgingMaterial(MaterialCatalog.CARBON),
                "Carbon alloy ingredient was exposed as a forgeable tool material");
        helper.assertFalse(MaterialCatalog.isNormalForgingMaterial(MaterialCatalog.TIN),
                "Tin alloy ingredient was exposed as a forgeable tool material");
        helper.assertFalse(MaterialCatalog.isNormalForgingMaterial(MaterialCatalog.NETHERITE_SCRAP),
                "Netherite scrap was exposed as a forgeable tool material");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void castVolumesMatchTheirPartMaterialRequirements(GameTestHelper helper) {
        FoundryCastRegistry.recipes().forEach(recipe -> {
            int requiredMaterials = ToolTypeRegistry.template(recipe.template())
                    .orElseThrow(() -> new AssertionError("Cast references missing template " + recipe.template()))
                    .requiredMaterials();
            helper.assertTrue(recipe.amountMb() == requiredMaterials * FoundryForgeBlockEntity.INGOT_MB,
                    "Cast volume for " + recipe.template() + " does not match its forging material count");
            helper.assertTrue(recipe.goldAmountMb() == 2 * FoundryForgeBlockEntity.INGOT_MB,
                    "Reusable cast " + recipe.template() + " does not cost two gold ingots");
        });
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void rawMetalsDoubleWithoutCreatingRemeltingLoops(GameTestHelper helper) {
        assertRawYield(helper, Items.RAW_IRON, Items.RAW_IRON_BLOCK, Items.IRON_INGOT, "iron");
        assertRawYield(helper, Items.RAW_GOLD, Items.RAW_GOLD_BLOCK, Items.GOLD_INGOT, "gold");
        assertRawYield(helper, Items.RAW_COPPER, Items.RAW_COPPER_BLOCK, Items.COPPER_INGOT, "copper");
        helper.succeed();
    }

    private static void assertRawYield(
            GameTestHelper helper,
            net.minecraft.world.item.Item raw,
            net.minecraft.world.item.Item rawBlock,
            net.minecraft.world.item.Item ingot,
            String material
    ) {
        int rawAmount = FoundryMeltingRegistry.find(new ItemStack(raw)).orElseThrow().amountMb();
        int rawBlockAmount = FoundryMeltingRegistry.find(new ItemStack(rawBlock)).orElseThrow().amountMb();
        int ingotAmount = FoundryMeltingRegistry.find(new ItemStack(ingot)).orElseThrow().amountMb();
        helper.assertTrue(rawAmount == 2 * FoundryForgeBlockEntity.INGOT_MB,
                "Raw " + material + " did not yield two ingots");
        helper.assertTrue(rawBlockAmount == 18 * FoundryForgeBlockEntity.INGOT_MB,
                "Raw " + material + " block did not yield eighteen ingots");
        helper.assertTrue(ingotAmount == FoundryForgeBlockEntity.INGOT_MB,
                "Remelting a processed " + material + " ingot duplicated metal");
        helper.assertTrue(rawBlockAmount > 1000,
                "Bulk raw " + material + " did not require an expanded foundry");
    }
}

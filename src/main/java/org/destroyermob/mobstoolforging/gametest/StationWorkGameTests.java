package org.destroyermob.mobstoolforging.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StationWorkGameTests {
    private static final ResourceLocation IRON_LIMBS_RECIPE = ResourceLocation.fromNamespaceAndPath(
            MobsToolForging.MOD_ID,
            "crossbow_limbs_iron"
    );
    private static final BlockPos STATION_POS = new BlockPos(1, 1, 1);

    private StationWorkGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void stationWorkUsesRecipeHitCount(GameTestHelper helper) {
        helper.setBlock(STATION_POS, ModBlocks.TOOL_FORGE.get());
        ToolForgeBlockEntity forge = helper.getBlockEntity(STATION_POS);
        StationWorkRecipe recipe = StationWorkRecipeRegistry.recipe(IRON_LIMBS_RECIPE).orElseThrow();

        helper.assertTrue(
                forge.selectTemplate(ToolTypeRegistry.template(ToolTypeRegistry.CROSSBOW_LIMBS_TEMPLATE).orElseThrow()),
                "Crossbow-limb template could not be selected"
        );
        ItemStack input = new ItemStack(Items.IRON_INGOT, recipe.input().count());
        helper.assertTrue(forge.placeLooseWork(recipe, input), "Iron-limb station work could not be placed");

        for (int hit = 1; hit < recipe.requiredHits(); hit++) {
            helper.assertTrue(forge.hammerLooseWork(recipe), "Station work rejected hit " + hit);
        }

        helper.assertTrue(forge.hitCount() == recipe.requiredHits() - 1, "Station work did not reach 4/5 hits");
        helper.assertFalse(forge.isComplete(), "Station work completed from the 4-hit template instead of the 5-hit recipe");
        helper.assertTrue(forge.outputStack().isEmpty(), "Station work exposed output before the recipe's final hit");

        helper.assertTrue(forge.hammerLooseWork(recipe), "Station work rejected its fifth hit");
        helper.assertTrue(forge.isComplete(), "Station work was not complete after its fifth hit");
        helper.assertTrue(forge.outputStack().is(ModItems.CROSSBOW_LIMBS.get()), "Fifth hit did not produce crossbow limbs");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void modularArmorAcceptsVanillaTrims(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.MODULAR_HELMET.get()).is(ItemTags.TRIMMABLE_ARMOR), "Modular helmet is not trimmable");
        helper.assertTrue(new ItemStack(ModItems.MODULAR_CHESTPLATE.get()).is(ItemTags.TRIMMABLE_ARMOR), "Modular chestplate is not trimmable");
        helper.assertTrue(new ItemStack(ModItems.MODULAR_LEGGINGS.get()).is(ItemTags.TRIMMABLE_ARMOR), "Modular leggings are not trimmable");
        helper.assertTrue(new ItemStack(ModItems.MODULAR_BOOTS.get()).is(ItemTags.TRIMMABLE_ARMOR), "Modular boots are not trimmable");

        ItemStack base = ModItems.MODULAR_CHESTPLATE.get().create(MaterialCatalog.IRON);
        ArmorConstructionData construction = base.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        SmithingTrimRecipe recipe = new SmithingTrimRecipe(
                Ingredient.of(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE),
                Ingredient.of(ItemTags.TRIMMABLE_ARMOR),
                Ingredient.of(Items.AMETHYST_SHARD)
        );
        SmithingRecipeInput input = new SmithingRecipeInput(
                new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE),
                base,
                new ItemStack(Items.AMETHYST_SHARD)
        );

        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Vanilla trim recipe rejected modular body armour");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        helper.assertFalse(result.isEmpty(), "Vanilla trim recipe produced no modular body armour");
        helper.assertTrue(result.get(DataComponents.TRIM) != null, "Trim component was not applied");
        helper.assertTrue(construction != null && construction.equals(result.get(ModDataComponents.ARMOR_CONSTRUCTION.get())), "Trimming lost modular construction data");
        helper.succeed();
    }
}

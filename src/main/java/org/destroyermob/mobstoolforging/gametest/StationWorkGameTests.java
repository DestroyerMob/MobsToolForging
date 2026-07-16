package org.destroyermob.mobstoolforging.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.ModularArmorDyeing;
import org.destroyermob.mobstoolforging.recipe.ModularArmorDyeRecipe;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorRepairing;
import org.destroyermob.mobstoolforging.world.CrossbowAssembly;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolAssemblyParts;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.VanillaToolConverter;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

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

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void modularCrossbowMatchesVanillaEnchantingTags(GameTestHelper helper) {
        ItemStack crossbow = new ItemStack(ModItems.CROSSBOW.get());
        helper.assertTrue(crossbow.is(ItemTags.CROSSBOW_ENCHANTABLE), "Modular crossbow is missing vanilla crossbow enchantments");
        helper.assertTrue(crossbow.is(ItemTags.DURABILITY_ENCHANTABLE), "Modular crossbow is missing durability enchantments");

        ItemStack body = ModItems.CROSSBOW_BODY.get().createPart(MaterialCatalog.WOOD);
        ItemStack limbs = ModItems.CROSSBOW_LIMBS.get().createPart(MaterialCatalog.IRON);
        helper.assertTrue(body.is(ModTags.Items.PART_CROSSBOW_BODIES), "Crossbow body is missing its data-driven part tag");
        helper.assertTrue(limbs.is(ModTags.Items.PART_CROSSBOW_LIMBS), "Crossbow limbs are missing their data-driven part tag");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void leatherStationRepairsLeatherArmorWithLeather(GameTestHelper helper) {
        ItemStack armor = ModItems.MODULAR_CHESTPLATE.get().createBase(MaterialCatalog.LEATHER);
        armor.setDamageValue(Math.max(1, armor.getMaxDamage() / 2));
        int damageBefore = armor.getDamageValue();

        helper.assertTrue(ArmorRepairing.canRepairAt(armor, WorkstationKind.LEATHER_STATION), "Leather armour was not repairable at the Leather Station");
        helper.assertTrue(ArmorRepairing.isRepairMaterial(armor, new ItemStack(Items.LEATHER)), "Leather was not accepted as its repair material");
        helper.assertFalse(ArmorRepairing.isRepairMaterial(armor, new ItemStack(Items.OAK_PLANKS)), "Leather armour incorrectly accepted wooden planks");

        ItemStack repaired = ArmorRepairing.repairWithOneMaterial(armor);
        helper.assertFalse(repaired.isEmpty(), "Leather armour repair produced no output");
        helper.assertTrue(repaired.getDamageValue() < damageBefore, "Leather armour repair did not restore durability");
        helper.assertTrue(repaired.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null, "Leather armour repair lost construction data");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void modularLeatherArmorCanBeDyed(GameTestHelper helper) {
        ItemStack leatherArmor = ModItems.MODULAR_CHESTPLATE.get().createBase(MaterialCatalog.LEATHER);
        ArmorConstructionData construction = leatherArmor.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        ModularArmorDyeRecipe recipe = new ModularArmorDyeRecipe(CraftingBookCategory.MISC);
        CraftingInput input = CraftingInput.of(2, 1, List.of(leatherArmor, new ItemStack(Items.RED_DYE)));

        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Modular leather armour did not accept a vanilla dye");
        ItemStack dyedArmor = recipe.assemble(input, helper.getLevel().registryAccess());
        DyedItemColor dyedColor = dyedArmor.get(DataComponents.DYED_COLOR);
        helper.assertFalse(dyedArmor.isEmpty(), "Dyeing modular leather armour produced no output");
        helper.assertTrue(dyedColor != null, "Dyeing modular leather armour did not set its dyed-color component");
        helper.assertTrue(ModularArmorDyeing.dyedColor(dyedArmor).isPresent(), "The modular armour renderer cannot resolve the dyed color");
        helper.assertTrue(construction != null && construction.equals(dyedArmor.get(ModDataComponents.ARMOR_CONSTRUCTION.get())), "Dyeing lost modular armour construction data");

        ItemStack metalArmor = ModItems.MODULAR_CHESTPLATE.get().createBase(MaterialCatalog.IRON);
        CraftingInput metalInput = CraftingInput.of(2, 1, List.of(metalArmor, new ItemStack(Items.RED_DYE)));
        helper.assertFalse(recipe.matches(metalInput, helper.getLevel()), "Modular metal armour incorrectly accepted dye");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void vanillaCrossbowLootConvertsWithComponents(GameTestHelper helper) {
        ItemStack vanilla = new ItemStack(Items.CROSSBOW);
        vanilla.setDamageValue(80);
        vanilla.set(DataComponents.CUSTOM_NAME, Component.literal("Converted Crossbow"));
        var quickCharge = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.QUICK_CHARGE);
        vanilla.enchant(quickCharge, 2);

        ItemStack converted = VanillaToolConverter.convertLootOrEquipment(vanilla, MaterialCatalog.OAK);
        helper.assertTrue(converted.is(ModItems.CROSSBOW.get()), "Vanilla crossbow did not convert to the MTF crossbow");
        helper.assertTrue(CrossbowAssembly.isCrossbow(converted.get(ModDataComponents.TOOL_CONSTRUCTION.get())), "Converted crossbow lost construction data");
        helper.assertTrue(Component.literal("Converted Crossbow").equals(converted.get(DataComponents.CUSTOM_NAME)), "Converted crossbow lost its custom name");
        helper.assertTrue(converted.getDamageValue() > 0, "Converted crossbow lost its proportional damage");
        helper.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(converted).getLevel(quickCharge) == 2, "Converted crossbow lost Quick Charge");

        ToolAssemblyParts assembly = converted.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        helper.assertTrue(assembly != null && assembly.stacks().size() == 3, "Converted crossbow did not receive body, limb, and string parts");
        ItemStack limbs = assembly == null ? ItemStack.EMPTY : assembly.stacks().stream()
                .filter(part -> {
                    ToolPartData data = part.get(ModDataComponents.TOOL_PART.get());
                    return data != null && ToolPartData.CROSSBOW_LIMBS.equals(data.partType());
                })
                .findFirst()
                .orElse(ItemStack.EMPTY);
        helper.assertFalse(limbs.isEmpty(), "Converted crossbow has no limb part");
        helper.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(limbs).getLevel(quickCharge) == 2, "Quick Charge was not routed onto the crossbow limbs");
        helper.succeed();
    }
}

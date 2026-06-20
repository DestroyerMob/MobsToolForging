package org.destroyermob.mobstoolforging.integration.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCategory;
import org.destroyermob.mobstoolforging.world.SmithingHammerLevel;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolMaterialDefinition;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

@JeiPlugin
public class MobsToolForgingJeiPlugin implements IModPlugin {
    public static final RecipeType<ForgeShapingJeiRecipe> FORGE_SHAPING = RecipeType.create(MobsToolForging.MOD_ID, "forge_shaping", ForgeShapingJeiRecipe.class);
    public static final RecipeType<StationWorkJeiRecipe> STATION_WORK = RecipeType.create(MobsToolForging.MOD_ID, "station_work", StationWorkJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ForgeShapingCategory(guiHelper),
                new StationWorkCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FORGE_SHAPING, forgeShapingRecipes());
        registration.addRecipes(STATION_WORK, stationWorkRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.TOOL_FORGE.get(), FORGE_SHAPING, STATION_WORK);
        registration.addRecipeCatalyst(ModItems.LAPIDARY_TABLE.get(), FORGE_SHAPING, STATION_WORK);
    }

    private static List<ForgeShapingJeiRecipe> forgeShapingRecipes() {
        List<ForgeShapingJeiRecipe> recipes = new ArrayList<>();
        for (ForgeTemplateDefinition template : ToolTypeRegistry.templates()) {
            for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
                Optional<ToolMaterialDefinition> material = MaterialCatalog.definition(materialId);
                if (material.isEmpty() || !template.allowsMaterial(materialId)) {
                    continue;
                }
                WorkstationKind workstation = material.get().category() == MaterialCategory.GEM ? WorkstationKind.LAPIDARY_TABLE : WorkstationKind.TOOL_FORGE;
                ItemStack pattern = patternFor(template);
                if (pattern.isEmpty()) {
                    continue;
                }
                int quality = workstation == WorkstationKind.LAPIDARY_TABLE ? 103 : ToolPartData.DEFAULT_QUALITY;
                ItemStack output = template.outputStack(materialId, quality);
                if (output.isEmpty()) {
                    continue;
                }
                ItemStack materialStack = new ItemStack(material.get().displayItem(), template.requiredMaterials());
                recipes.add(new ForgeShapingJeiRecipe(
                        recipeId("forge_shaping/" + template.id().getPath() + "/" + materialId.getPath()),
                        workstation,
                        template,
                        materialId,
                        stationFor(workstation),
                        pattern,
                        materialStack,
                        workstation == WorkstationKind.LAPIDARY_TABLE ? new ItemStack(ModItems.DIAMOND_POWDER.get()) : ItemStack.EMPTY,
                        hammerFor(template.minimumHammerLevel(materialId)),
                        output,
                        template.requiredHits(),
                        template.minimumHammerLevel(materialId)
                ));
            }
        }
        return recipes;
    }

    private static List<StationWorkJeiRecipe> stationWorkRecipes() {
        List<StationWorkRecipe> recipes = new ArrayList<>(StationWorkRecipeRegistry.recipes());
        if (recipes.isEmpty()) {
            recipes.add(new StationWorkRecipe(
                    recipeId("station_work/diamond_powder"),
                    WorkstationKind.TOOL_FORGE,
                    Optional.empty(),
                    StationWorkRecipe.Input.item(ResourceLocation.withDefaultNamespace("diamond")),
                    new ItemStack(ModItems.DIAMOND_POWDER.get(), 4),
                    4,
                    SmithingHammerLevel.IRON.level()
            ));
        }
        return recipes.stream()
                .map(MobsToolForgingJeiPlugin::stationWorkRecipe)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static Optional<StationWorkJeiRecipe> stationWorkRecipe(StationWorkRecipe recipe) {
        List<ItemStack> inputs = inputStacks(recipe.input());
        if (inputs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new StationWorkJeiRecipe(
                recipe.id(),
                recipe.workstationKind(),
                stationFor(recipe.workstationKind()),
                inputs,
                ItemStack.EMPTY,
                hammerFor(recipe.minimumHammerLevel()),
                recipe.outputCopy(),
                recipe.requiredHits(),
                recipe.minimumHammerLevel(),
                recipe
        ));
    }

    private static List<ItemStack> inputStacks(StationWorkRecipe.Input input) {
        if (input.itemId().isPresent()) {
            Item item = BuiltInRegistries.ITEM.get(input.itemId().get());
            return item == Items.AIR ? List.of() : List.of(new ItemStack(item));
        }
        return List.of();
    }

    private static ItemStack stationFor(WorkstationKind workstation) {
        return workstation == WorkstationKind.LAPIDARY_TABLE ? new ItemStack(ModItems.LAPIDARY_TABLE.get()) : new ItemStack(ModItems.TOOL_FORGE.get());
    }

    private static ItemStack hammerFor(int level) {
        return level >= SmithingHammerLevel.IRON.level() ? new ItemStack(ModItems.IRON_SMITHING_HAMMER.get()) : new ItemStack(ModItems.SMITHING_HAMMER.get());
    }

    private static ItemStack patternFor(ForgeTemplateDefinition template) {
        String partType = template.partType();
        return switch (partType) {
            case ToolPartData.PICKAXE_HEAD -> new ItemStack(ModItems.PICKAXE_HEAD_PATTERN.get());
            case ToolPartData.AXE_HEAD -> new ItemStack(ModItems.AXE_HEAD_PATTERN.get());
            case ToolPartData.SHOVEL_HEAD -> new ItemStack(ModItems.SHOVEL_HEAD_PATTERN.get());
            case ToolPartData.HOE_HEAD -> new ItemStack(ModItems.HOE_HEAD_PATTERN.get());
            case ToolPartData.SWORD_BLADE -> new ItemStack(ModItems.SWORD_BLADE_PATTERN.get());
            case ToolPartData.SWORD_GUARD -> new ItemStack(ModItems.SWORD_GUARD_PATTERN.get());
            case ToolPartData.SMITHING_HAMMER_HEAD -> new ItemStack(ModItems.SMITHING_HAMMER_HEAD_PATTERN.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    static ItemStack smithingAnvilIcon() {
        return new ItemStack(ModItems.TOOL_FORGE.get());
    }
}

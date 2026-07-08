package org.destroyermob.mobstoolforging.integration.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.HeatingDisplayRecipe;
import org.destroyermob.mobstoolforging.world.HeatingRecipeRegistry;
import org.destroyermob.mobstoolforging.world.HeatingSource;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCategory;
import org.destroyermob.mobstoolforging.world.SmithingHammerLevel;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolMaterialDefinition;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

@JeiPlugin
public class MobsToolForgingJeiPlugin implements IModPlugin {
    public static final RecipeType<ForgeShapingJeiRecipe> FORGE_SHAPING = RecipeType.create(MobsToolForging.MOD_ID, "forge_shaping", ForgeShapingJeiRecipe.class);
    public static final RecipeType<StationWorkJeiRecipe> STATION_WORK = RecipeType.create(MobsToolForging.MOD_ID, "station_work", StationWorkJeiRecipe.class);
    public static final RecipeType<PatternCreationJeiRecipe> PATTERN_CREATION = RecipeType.create(MobsToolForging.MOD_ID, "pattern_creation", PatternCreationJeiRecipe.class);
    public static final RecipeType<HeatingJeiRecipe> HEATING = RecipeType.create(MobsToolForging.MOD_ID, "heating", HeatingJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ForgeShapingCategory(guiHelper),
                new StationWorkCategory(guiHelper),
                new PatternCreationCategory(guiHelper),
                new HeatingCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FORGE_SHAPING, forgeShapingRecipes());
        registration.addRecipes(STATION_WORK, stationWorkRecipes());
        registration.addRecipes(PATTERN_CREATION, patternCreationRecipes());
        registration.addRecipes(HEATING, heatingRecipes());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.TEMPLATE_PATTERN.get(), new ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                ResourceLocation template = stack.get(ModDataComponents.FORGE_TEMPLATE.get());
                return template == null ? "" : template;
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                ResourceLocation template = stack.get(ModDataComponents.FORGE_TEMPLATE.get());
                return template == null ? "" : template.toString();
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.CRUDE_ANVIL.get(), FORGE_SHAPING);
        registration.addRecipeCatalyst(ModItems.TOOL_FORGE.get(), FORGE_SHAPING, STATION_WORK);
        registration.addRecipeCatalyst(ModItems.LAPIDARY_TABLE.get(), FORGE_SHAPING, STATION_WORK);
        ModItems.LEATHER_STATION_ITEMS.forEach(item -> registration.addRecipeCatalyst(item.get(), STATION_WORK));
        registration.addRecipeCatalyst(ModItems.PATTERN_CREATION_STATION.get(), PATTERN_CREATION);
        registration.addRecipeCatalyst(ModItems.HEATING_FORGE.get(), HEATING);
        registration.addRecipeCatalyst(Items.CAMPFIRE, HEATING);
        registration.addRecipeCatalyst(Items.SOUL_CAMPFIRE, HEATING);
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
                ItemStack target = ItemStack.EMPTY;
                ItemStack output = template.outputStack(materialId);
                if (output.isEmpty()) {
                    continue;
                }
                ItemStack materialStack = new ItemStack(material.get().displayItem(), template.requiredMaterials());
                recipes.add(new ForgeShapingJeiRecipe(
                        recipeId("forge_shaping/" + idPath(template.id()) + "/" + idPath(materialId)),
                        workstation,
                        template,
                        materialId,
                        stationFor(workstation),
                        pattern,
                        materialStack,
                        target,
                        workstation == WorkstationKind.LAPIDARY_TABLE ? new ItemStack(ModItems.DIAMOND_POWDER.get()) : ItemStack.EMPTY,
                        workToolFor(workstation, template.minimumHammerLevel(materialId)),
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
                    StationWorkRecipe.Input.item(ResourceLocation.withDefaultNamespace("iron_ingot")),
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

    private static List<PatternCreationJeiRecipe> patternCreationRecipes() {
        return ToolTypeRegistry.patternStationTemplates().stream()
                .map(MobsToolForgingJeiPlugin::patternCreationRecipe)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static List<HeatingJeiRecipe> heatingRecipes() {
        return HeatingRecipeRegistry.displayRecipes().stream()
                .map(MobsToolForgingJeiPlugin::heatingRecipe)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static Optional<PatternCreationJeiRecipe> patternCreationRecipe(ForgeTemplateDefinition template) {
        ItemStack pattern = ToolTemplateItem.createPatternStack(template);
        if (pattern.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PatternCreationJeiRecipe(
                recipeId("pattern_creation/" + idPath(template.id())),
                new ItemStack(ModItems.PATTERN_CREATION_STATION.get()),
                patternInput(template.patternStationPaperCost()),
                pattern
        ));
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
                workToolFor(recipe.workstationKind(), recipe.minimumHammerLevel()),
                recipe.outputCopy(),
                recipe.requiredHits(),
                recipe.minimumHammerLevel(),
                recipe
        ));
    }

    private static Optional<HeatingJeiRecipe> heatingRecipe(HeatingDisplayRecipe recipe) {
        if (recipe.inputs().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new HeatingJeiRecipe(
                recipe.id(),
                recipe.source(),
                stationFor(recipe.source()),
                recipe.inputs(),
                recipe.output(),
                recipe.ticks(),
                recipe.targetTemperature(),
                recipe
        ));
    }

    private static List<ItemStack> inputStacks(StationWorkRecipe.Input input) {
        if (input.itemId().isPresent()) {
            Item item = BuiltInRegistries.ITEM.get(input.itemId().get());
            return item == Items.AIR ? List.of() : List.of(new ItemStack(item, input.count()));
        }
        return List.of();
    }

    private static ItemStack stationFor(WorkstationKind workstation) {
        if (workstation == WorkstationKind.LAPIDARY_TABLE) {
            return new ItemStack(ModItems.LAPIDARY_TABLE.get());
        }
        if (workstation == WorkstationKind.CRUDE_ANVIL) {
            return new ItemStack(ModItems.CRUDE_ANVIL.get());
        }
        if (workstation == WorkstationKind.TOOLMAKERS_BENCH) {
            return new ItemStack(ModItems.TOOLMAKERS_BENCH.get());
        }
        if (workstation == WorkstationKind.LEATHER_STATION) {
            return new ItemStack(ModItems.LEATHER_STATION.get());
        }
        return new ItemStack(ModItems.TOOL_FORGE.get());
    }

    private static ItemStack stationFor(HeatingSource source) {
        return source == HeatingSource.CAMPFIRE ? new ItemStack(Items.CAMPFIRE) : new ItemStack(ModItems.HEATING_FORGE.get());
    }

    private static ItemStack patternInput(int count) {
        return MobsToolForgingConfig.BASIC_PATTERNS_REQUIRE_PAPER.get()
                ? new ItemStack(Items.PAPER, count)
                : new ItemStack(ModItems.PATTERN_BOARD.get(), count);
    }

    private static ItemStack workToolFor(WorkstationKind workstation, int level) {
        return workstation == WorkstationKind.LAPIDARY_TABLE ? new ItemStack(ModItems.GEM_CUTTERS_KNIFE.get()) : hammerFor(level);
    }

    private static ItemStack hammerFor(int level) {
        return level >= SmithingHammerLevel.IRON.level() ? new ItemStack(ModItems.IRON_SMITHING_HAMMER.get()) : new ItemStack(ModItems.SMITHING_HAMMER.get());
    }

    private static ItemStack patternFor(ForgeTemplateDefinition template) {
        return ToolTemplateItem.createPatternStack(template);
    }

    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static String idPath(ResourceLocation id) {
        return id.getNamespace() + "/" + id.getPath();
    }

    static ItemStack smithingAnvilIcon() {
        return new ItemStack(ModItems.TOOL_FORGE.get());
    }

    static ItemStack patternCreationStationIcon() {
        return new ItemStack(ModItems.PATTERN_CREATION_STATION.get());
    }

    static ItemStack heatingForgeIcon() {
        return new ItemStack(ModItems.HEATING_FORGE.get());
    }
}

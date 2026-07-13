package org.destroyermob.mobstoolforging.integration.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.integration.everycomp.CompatWorkstationRegistry;
import org.destroyermob.mobstoolforging.world.ArmorForgeAttachment;
import org.destroyermob.mobstoolforging.world.CrossbowAssembly;
import org.destroyermob.mobstoolforging.world.DryingRecipe;
import org.destroyermob.mobstoolforging.world.DryingRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.HeatingDisplayRecipe;
import org.destroyermob.mobstoolforging.world.HeatingRecipeRegistry;
import org.destroyermob.mobstoolforging.world.HeatingSource;
import org.destroyermob.mobstoolforging.world.LapidaryAbrasives;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCategory;
import org.destroyermob.mobstoolforging.world.SmithingHammerLevel;
import org.destroyermob.mobstoolforging.world.SmithingHammerAssembly;
import org.destroyermob.mobstoolforging.world.StationWorkRecipe;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeRegistry;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolMaterialDefinition;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolStatBuilder;
import org.destroyermob.mobstoolforging.world.ToolStatRule;
import org.destroyermob.mobstoolforging.world.ToolTrait;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

@JeiPlugin
public class MobsToolForgingJeiPlugin implements IModPlugin {
    public static final RecipeType<ForgeShapingJeiRecipe> FORGE_SHAPING = RecipeType.create(MobsToolForging.MOD_ID, "forge_shaping", ForgeShapingJeiRecipe.class);
    public static final RecipeType<LapidaryCoatingJeiRecipe> LAPIDARY_COATING = RecipeType.create(MobsToolForging.MOD_ID, "lapidary_coating", LapidaryCoatingJeiRecipe.class);
    public static final RecipeType<StationWorkJeiRecipe> STATION_WORK = RecipeType.create(MobsToolForging.MOD_ID, "station_work", StationWorkJeiRecipe.class);
    public static final RecipeType<PatternCreationJeiRecipe> PATTERN_CREATION = RecipeType.create(MobsToolForging.MOD_ID, "pattern_creation", PatternCreationJeiRecipe.class);
    public static final RecipeType<HeatingJeiRecipe> HEATING = RecipeType.create(MobsToolForging.MOD_ID, "heating", HeatingJeiRecipe.class);
    public static final RecipeType<DryingJeiRecipe> DRYING = RecipeType.create(MobsToolForging.MOD_ID, "drying", DryingJeiRecipe.class);
    public static final RecipeType<MaterialTraitInfoRecipe> MATERIAL_TRAITS = RecipeType.create(MobsToolForging.MOD_ID, "material_traits", MaterialTraitInfoRecipe.class);
    public static final RecipeType<WorldAssemblyJeiRecipe> WORLD_ASSEMBLY = RecipeType.create(MobsToolForging.MOD_ID, "world_assembly", WorldAssemblyJeiRecipe.class);
    public static final RecipeType<ToolmakerAssemblyJeiRecipe> TOOLMAKER_ASSEMBLY = RecipeType.create(MobsToolForging.MOD_ID, "toolmaker_assembly", ToolmakerAssemblyJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ForgeShapingCategory(guiHelper),
                new LapidaryCoatingCategory(guiHelper),
                new StationWorkCategory(guiHelper),
                new PatternCreationCategory(guiHelper),
                new HeatingCategory(guiHelper),
                new DryingCategory(guiHelper),
                new MaterialTraitInfoCategory(guiHelper),
                new ToolmakerAssemblyCategory(guiHelper),
                new WorldAssemblyCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FORGE_SHAPING, forgeShapingRecipes());
        registration.addRecipes(LAPIDARY_COATING, lapidaryCoatingRecipes());
        registration.addRecipes(STATION_WORK, stationWorkRecipes());
        registration.addRecipes(PATTERN_CREATION, patternCreationRecipes());
        registration.addRecipes(HEATING, heatingRecipes());
        registration.addRecipes(DRYING, dryingRecipes());
        registration.addRecipes(MATERIAL_TRAITS, materialTraitRecipes());
        registration.addRecipes(TOOLMAKER_ASSEMBLY, toolmakerAssemblyRecipes());
        registration.addRecipes(WORLD_ASSEMBLY, worldAssemblyRecipes());
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
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(
                new ItemStack(ModItems.SMITHING_HAMMER_HEAD.get()),
                new ItemStack(ModItems.SMITHING_HAMMER_HEAD_PATTERN.get()),
                new ItemStack(ModItems.SCREWDRIVER.get()),
                new ItemStack(ModItems.SCREWDRIVER_HEAD.get()),
                new ItemStack(ModItems.SCREWDRIVER_HEAD_PATTERN.get()),
                new ItemStack(ModItems.GEM_CUTTERS_KNIFE.get()),
                new ItemStack(ModItems.GEM_CUTTERS_BLADE.get()),
                new ItemStack(ModItems.GEM_CUTTERS_BLADE_PATTERN.get())
        ));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.CRUDE_ANVIL.get(), FORGE_SHAPING);
        registration.addRecipeCatalyst(ModItems.TOOL_FORGE.get(), FORGE_SHAPING, STATION_WORK);
        registration.addRecipeCatalyst(ModItems.LAPIDARY_TABLE.get(), FORGE_SHAPING, LAPIDARY_COATING, STATION_WORK);
        ModItems.SAWMILL_ITEMS.forEach(item -> registration.addRecipeCatalyst(item.get(), STATION_WORK));
        CompatWorkstationRegistry.items(CompatWorkstationRegistry.Kind.SAWMILL)
                .forEach(item -> registration.addRecipeCatalyst(item, STATION_WORK));
        ModItems.LEATHER_STATION_ITEMS.forEach(item -> registration.addRecipeCatalyst(item.get(), STATION_WORK));
        CompatWorkstationRegistry.items(CompatWorkstationRegistry.Kind.LEATHER_STATION)
                .forEach(item -> registration.addRecipeCatalyst(item, STATION_WORK));
        ModItems.TOOLMAKER_STATION_ITEMS.forEach(item -> registration.addRecipeCatalyst(item.get(), TOOLMAKER_ASSEMBLY));
        CompatWorkstationRegistry.items(CompatWorkstationRegistry.Kind.TOOLMAKERS_BENCH)
                .forEach(item -> registration.addRecipeCatalyst(item, TOOLMAKER_ASSEMBLY));
        registration.addRecipeCatalyst(ModItems.PATTERN_CREATION_STATION.get(), PATTERN_CREATION);
        registration.addRecipeCatalyst(ModItems.HEATING_FORGE.get(), HEATING);
        registration.addRecipeCatalyst(ModItems.LAVA_HEATING_FORGE.get(), HEATING);
        registration.addRecipeCatalyst(Items.CAMPFIRE, HEATING);
        registration.addRecipeCatalyst(Items.SOUL_CAMPFIRE, HEATING);
        ModItems.DRYING_RACK_ITEMS.forEach(item -> registration.addRecipeCatalyst(item.get(), DRYING));
        CompatWorkstationRegistry.items(CompatWorkstationRegistry.Kind.DRYING_RACK)
                .forEach(item -> registration.addRecipeCatalyst(item, DRYING));
    }

    private static List<ForgeShapingJeiRecipe> forgeShapingRecipes() {
        List<ForgeShapingJeiRecipe> recipes = new ArrayList<>();
        for (ForgeTemplateDefinition template : ToolTypeRegistry.templates()) {
            if (!isActiveTemplate(template)) {
                continue;
            }
            for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
                Optional<ToolMaterialDefinition> material = MaterialCatalog.definition(materialId);
                boolean plate = ArmorForgeAttachment.isPlateTemplate(template);
                if (material.isEmpty()
                        || !template.allowsMaterial(materialId)
                        || (material.get().category() != MaterialCategory.METAL && material.get().category() != MaterialCategory.GEM)
                        || material.get().category() == MaterialCategory.GEM) {
                    continue;
                }
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
                List<ItemStack> stations = plate
                        ? List.of(new ItemStack(ModItems.TOOL_FORGE.get()))
                        : List.of(new ItemStack(ModItems.CRUDE_ANVIL.get()), new ItemStack(ModItems.TOOL_FORGE.get()));
                recipes.add(new ForgeShapingJeiRecipe(
                        recipeId("forge_shaping/" + idPath(template.id()) + "/" + idPath(materialId)),
                        template,
                        materialId,
                        stations,
                        pattern,
                        materialStack,
                        target,
                        material.get().requiredLapidaryAbrasiveTier().map(MobsToolForgingJeiPlugin::abrasiveFor).orElse(ItemStack.EMPTY),
                        hammerFor(template.minimumHammerLevel(materialId)),
                        output,
                        template.requiredHits(),
                        template.minimumHammerLevel(materialId)
                ));
            }
        }
        return recipes;
    }

    private static List<LapidaryCoatingJeiRecipe> lapidaryCoatingRecipes() {
        List<LapidaryCoatingJeiRecipe> recipes = new ArrayList<>();
        for (ForgeTemplateDefinition template : ToolTypeRegistry.templates()) {
            if (!isActiveTemplate(template)) {
                continue;
            }
            for (ResourceLocation coatingId : MaterialCatalog.starterMaterialIds()) {
                Optional<ToolMaterialDefinition> coating = MaterialCatalog.definition(coatingId);
                if (coating.isEmpty() || coating.get().category() != MaterialCategory.GEM || !template.allowsMaterial(coatingId)) {
                    continue;
                }
                List<ItemStack> bases = new ArrayList<>();
                List<ItemStack> outputs = new ArrayList<>();
                for (ResourceLocation baseMaterialId : MaterialCatalog.starterMaterialIds()) {
                    Optional<ToolMaterialDefinition> baseMaterial = MaterialCatalog.definition(baseMaterialId);
                    if (baseMaterial.isEmpty() || baseMaterial.get().category() != MaterialCategory.METAL || !template.allowsMaterial(baseMaterialId)) {
                        continue;
                    }
                    ItemStack base = template.outputStack(baseMaterialId);
                    ItemStack output = ToolForgeBlockEntity.lapidaryCoatingPreview(base, coatingId);
                    if (!base.isEmpty() && !output.isEmpty()) {
                        bases.add(base);
                        outputs.add(output);
                    }
                }
                if (bases.isEmpty()) {
                    continue;
                }
                recipes.add(new LapidaryCoatingJeiRecipe(
                        recipeId("lapidary_coating/" + idPath(template.id()) + "/" + idPath(coatingId)),
                        template,
                        coatingId,
                        new ItemStack(ModItems.LAPIDARY_TABLE.get()),
                        bases,
                        new ItemStack(coating.get().displayItem(), template.requiredMaterials()),
                        coating.get().requiredLapidaryAbrasiveTier().map(MobsToolForgingJeiPlugin::abrasiveFor).orElse(ItemStack.EMPTY),
                        new ItemStack(ModItems.GEM_CUTTERS_KNIFE.get()),
                        outputs,
                        template.requiredHits()
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
                    Optional.empty(),
                    new ItemStack(ModItems.DIAMOND_POWDER.get()),
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
                .filter(MobsToolForgingJeiPlugin::isActiveTemplate)
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

    private static List<DryingJeiRecipe> dryingRecipes() {
        return DryingRecipeRegistry.recipes().stream()
                .map(MobsToolForgingJeiPlugin::dryingRecipe)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static List<MaterialTraitInfoRecipe> materialTraitRecipes() {
        List<MaterialTraitInfoRecipe> recipes = new ArrayList<>();
        for (ResourceLocation materialId : materialTraitMaterials()) {
            List<ItemStack> inputs = MaterialCatalog.ingredientStacks(materialId);
            if (inputs.isEmpty()) {
                continue;
            }
            List<MaterialTraitInfoRecipe.TraitEntry> traits = materialTraitEntries(materialId);
            if (traits.isEmpty()) {
                continue;
            }
            recipes.add(new MaterialTraitInfoRecipe(
                    recipeId("material_traits/" + idPath(materialId)),
                    materialId,
                    MaterialCatalog.displayName(materialId),
                    inputs,
                    traits
            ));
        }
        return recipes;
    }

    private static List<ToolmakerAssemblyJeiRecipe> toolmakerAssemblyRecipes() {
        List<ToolmakerAssemblyJeiRecipe> recipes = new ArrayList<>();
        addHammerAssemblyRecipes(recipes);
        for (var definition : ToolTypeRegistry.toolTypes()) {
            if (CrossbowAssembly.TOOL_TYPE.equals(definition.id())) {
                continue;
            }
            for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
                ItemStack primary = definition.createPart(definition.primaryPartType(), materialId);
                if (primary.isEmpty()) {
                    continue;
                }
                List<ItemStack> parts = new ArrayList<>();
                parts.add(primary);
                boolean valid = true;
                for (String partType : definition.requiredAssemblyParts()) {
                    ItemStack part = definition.createPart(partType, materialId);
                    if (part.isEmpty()) {
                        valid = false;
                        break;
                    }
                    parts.add(part);
                }
                if (!valid) {
                    continue;
                }
                parts.add(new ItemStack(Items.STICK));
                Optional<ResourceLocation> supportMaterial = definition.requiredAssemblyParts().isEmpty()
                        ? Optional.empty()
                        : Optional.of(materialId);
                ToolConstructionData construction = new ToolConstructionData(
                        definition.id(),
                        materialId,
                        MaterialCatalog.OAK,
                        supportMaterial,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        ToolConstructionData.DEFAULT_QUALITY
                );
                ItemStack output = definition.createTool(construction);
                if (!output.isEmpty()) {
                    recipes.add(new ToolmakerAssemblyJeiRecipe(
                            recipeId("toolmaker_assembly/tool/" + idPath(definition.id()) + "/" + idPath(materialId)),
                            parts,
                            output
                    ));
                }
            }
        }
        addCrossbowAssemblyRecipes(recipes);
        addArmorAssemblyRecipes(recipes);
        return recipes;
    }

    private static void addHammerAssemblyRecipes(List<ToolmakerAssemblyJeiRecipe> recipes) {
        ItemStack handle = new ItemStack(Items.STICK);
        ItemStack ironIngot = new ItemStack(Items.IRON_INGOT);
        ItemStack ironHammer = SmithingHammerAssembly.assemble(List.of(ironIngot, handle));
        if (!ironHammer.isEmpty()) {
            recipes.add(new ToolmakerAssemblyJeiRecipe(
                    recipeId("toolmaker_assembly/hammer/iron"),
                    List.of(ironIngot, handle),
                    ironHammer
            ));
        }
        BuiltInRegistries.ITEM.getTagOrEmpty(org.destroyermob.mobstoolforging.registry.ModTags.Items.HAMMER_STONES)
                .forEach(holder -> {
                    ItemStack stone = new ItemStack(holder.value());
                    ItemStack stoneHammer = SmithingHammerAssembly.assemble(List.of(stone, handle));
                    if (!stoneHammer.isEmpty()) {
                        ResourceLocation stoneId = BuiltInRegistries.ITEM.getKey(holder.value());
                        recipes.add(new ToolmakerAssemblyJeiRecipe(
                                recipeId("toolmaker_assembly/hammer/stone/" + idPath(stoneId)),
                                List.of(stone, handle),
                                stoneHammer
                        ));
                    }
                });
    }

    private static void addCrossbowAssemblyRecipes(List<ToolmakerAssemblyJeiRecipe> recipes) {
        ItemStack body = ModItems.CROSSBOW_BODY.get().createPart(MaterialCatalog.WOOD);
        if (body.isEmpty()) {
            return;
        }
        List<ItemStack> strings = CrossbowAssembly.stringIngredientStacks();
        List<ResourceLocation> limbMaterials = List.of(
                MaterialCatalog.OAK,
                MaterialCatalog.COPPER,
                MaterialCatalog.IRON,
                MaterialCatalog.GOLD,
                MaterialCatalog.NETHERITE,
                MaterialCatalog.DIAMOND,
                MaterialCatalog.EMERALD,
                MaterialCatalog.AMETHYST,
                MaterialCatalog.RUBY,
                MaterialCatalog.SAPPHIRE,
                MaterialCatalog.TOPAZ
        );
        for (ResourceLocation limbMaterial : limbMaterials) {
            ItemStack limbs = ModItems.CROSSBOW_LIMBS.get().createPart(limbMaterial);
            if (limbs.isEmpty()) {
                continue;
            }
            for (ItemStack string : strings) {
                ItemStack output = CrossbowAssembly.assemble(List.of(body, limbs, string));
                if (!output.isEmpty()) {
                    recipes.add(new ToolmakerAssemblyJeiRecipe(
                            recipeId("toolmaker_assembly/crossbow/" + idPath(limbMaterial) + "/" + idPath(BuiltInRegistries.ITEM.getKey(string.getItem()))),
                            List.of(body.copy(), limbs.copy(), string.copy()),
                            output
                    ));
                }
            }
        }
    }

    private static void addArmorAssemblyRecipes(List<ToolmakerAssemblyJeiRecipe> recipes) {
        addArmorSlotRecipes(recipes, "helmet", ModItems.HELMET_CHAINMAIL.get(), ModItems.HELMET_PLATE.get());
        addArmorSlotRecipes(recipes, "chestplate", ModItems.CHESTPLATE_CHAINMAIL.get(), ModItems.CHESTPLATE_BODY.get());
        addArmorSlotRecipes(recipes, "leggings", ModItems.LEGGINGS_CHAINMAIL.get(), ModItems.LEGGINGS_PLATE.get());
        addArmorSlotRecipes(recipes, "boots", ModItems.BOOTS_CHAINMAIL.get(), ModItems.BOOTS_PLATE.get());
    }

    private static void addArmorSlotRecipes(
            List<ToolmakerAssemblyJeiRecipe> recipes,
            String slot,
            org.destroyermob.mobstoolforging.item.ModularArmorPartItem baseItem,
            org.destroyermob.mobstoolforging.item.ModularArmorPartItem plateItem
    ) {
        for (ResourceLocation baseMaterial : List.of(MaterialCatalog.LEATHER, MaterialCatalog.IRON)) {
            ItemStack base = baseItem.createPart(baseMaterial);
            ItemStack baseOutput = armorOutput(slot, baseMaterial, Optional.empty());
            if (!base.isEmpty() && !baseOutput.isEmpty()) {
                recipes.add(new ToolmakerAssemblyJeiRecipe(
                        recipeId("toolmaker_assembly/armor/" + slot + "/" + idPath(baseMaterial)),
                        List.of(base),
                        baseOutput
                ));
            }
            if (MaterialCatalog.LEATHER.equals(baseMaterial)) {
                continue;
            }
            for (ResourceLocation plateMaterial : MaterialCatalog.starterMaterialIds()) {
                ItemStack plate = plateItem.createPart(plateMaterial);
                ItemStack platedOutput = armorOutput(slot, baseMaterial, Optional.of(plateMaterial));
                if (!plate.isEmpty() && !platedOutput.isEmpty()) {
                    recipes.add(new ToolmakerAssemblyJeiRecipe(
                            recipeId("toolmaker_assembly/armor/" + slot + "/" + idPath(baseMaterial) + "/" + idPath(plateMaterial)),
                            List.of(base, plate),
                            platedOutput
                    ));
                }
            }
        }
    }

    private static ItemStack armorOutput(String slot, ResourceLocation baseMaterial, Optional<ResourceLocation> plateMaterial) {
        return switch (slot) {
            case "helmet" -> ModItems.MODULAR_HELMET.get().create(baseMaterial, plateMaterial);
            case "chestplate" -> plateMaterial
                    .map(ModItems.MODULAR_CHESTPLATE.get()::create)
                    .orElseGet(() -> ModItems.MODULAR_CHESTPLATE.get().createBase(baseMaterial));
            case "leggings" -> ModItems.MODULAR_LEGGINGS.get().create(baseMaterial, plateMaterial);
            case "boots" -> ModItems.MODULAR_BOOTS.get().create(baseMaterial, plateMaterial);
            default -> ItemStack.EMPTY;
        };
    }

    private static List<WorldAssemblyJeiRecipe> worldAssemblyRecipes() {
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
                List.of(abrasiveFor(LapidaryAbrasives.DIAMOND_TIER)),
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
        return recipes;
    }

    private static WorldAssemblyJeiRecipe leatherStationAssembly(
            ResourceLocation id,
            net.minecraft.world.level.block.Block planks,
            net.minecraft.world.level.block.Block log,
            net.minecraft.world.level.block.Block output,
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
            net.minecraft.world.level.block.Block planks,
            net.minecraft.world.level.block.Block log,
            net.minecraft.world.level.block.Block output,
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

    private static List<ResourceLocation> materialTraitMaterials() {
        LinkedHashSet<ResourceLocation> materials = new LinkedHashSet<>();
        materials.addAll(MaterialCatalog.starterMaterialIds());
        materials.addAll(MaterialCatalog.visualMaterialIds("headMaterial"));
        materials.addAll(MaterialCatalog.visualMaterialIds("guardMaterial"));
        materials.addAll(MaterialCatalog.visualMaterialIds("handleMaterial"));
        materials.addAll(MaterialCatalog.visualMaterialIds("treatment"));
        materials.add(MaterialCatalog.FLINT);
        ToolTypeRegistry.statRules().stream()
                .filter(rule -> !rule.traits().isEmpty())
                .filter(MobsToolForgingJeiPlugin::hasSupportedTraitSource)
                .map(ToolStatRule::material)
                .forEach(materials::add);
        return List.copyOf(materials);
    }

    private static List<MaterialTraitInfoRecipe.TraitEntry> materialTraitEntries(ResourceLocation materialId) {
        Map<String, MaterialTraitInfoRecipe.TraitEntry> entries = new LinkedHashMap<>();
        ToolStatBuilder.primaryTraitForMaterial(materialId)
                .ifPresent(trait -> addTraitEntry(entries, trait.id(), primaryTraitSource(materialId)));
        ToolStatBuilder.supportTraitsForMaterial(materialId)
                .forEach(trait -> addTraitEntry(entries, trait.id(), traitSource("support")));
        ToolTypeRegistry.statRules().stream()
                .filter(rule -> materialId.equals(rule.material()))
                .filter(MobsToolForgingJeiPlugin::hasSupportedTraitSource)
                .forEach(rule -> rule.traits().forEach(trait -> addTraitEntry(entries, trait, statRuleSource(rule))));
        return List.copyOf(entries.values());
    }

    private static boolean hasSupportedTraitSource(ToolStatRule rule) {
        return switch (rule.slot()) {
            case "head", "headMaterial", "head_base", "headBase", "core", "coreMaterial", "handle", "handleMaterial", "guard", "guardMaterial", "treatment", "any", "anyPart" -> true;
            default -> false;
        };
    }

    private static void addTraitEntry(Map<String, MaterialTraitInfoRecipe.TraitEntry> entries, ResourceLocation traitId, net.minecraft.network.chat.Component source) {
        entries.putIfAbsent(traitId + "|" + source.getString(), new MaterialTraitInfoRecipe.TraitEntry(traitId, source));
    }

    private static net.minecraft.network.chat.Component primaryTraitSource(ResourceLocation materialId) {
        if (MaterialCatalog.visualMaterialIds("handleMaterial").contains(materialId)) {
            return traitSource("handle");
        }
        if (MaterialCatalog.visualMaterialIds("treatment").contains(materialId) && !MaterialCatalog.starterMaterialIds().contains(materialId)) {
            return traitSource("treatment");
        }
        return traitSource("any_part");
    }

    private static net.minecraft.network.chat.Component statRuleSource(ToolStatRule rule) {
        net.minecraft.network.chat.Component slot = switch (rule.slot()) {
            case "head", "headMaterial" -> traitSource("head");
            case "head_base", "headBase", "core", "coreMaterial" -> traitSource("core");
            case "handle", "handleMaterial" -> traitSource("handle");
            case "guard", "guardMaterial" -> traitSource("guard");
            case "binding", "bindingMaterial" -> traitSource("binding");
            case "wrap", "wrapMaterial" -> traitSource("wrap");
            case "treatment" -> traitSource("treatment");
            case "any", "anyPart" -> traitSource("any_part");
            default -> net.minecraft.network.chat.Component.literal(rule.slot());
        };
        if (rule.toolType().isPresent()) {
            return net.minecraft.network.chat.Component.translatable(
                    "jei.mobstoolforging.material_traits.source.tool_slot",
                    net.minecraft.network.chat.Component.literal(ToolTrait.fallbackName(rule.toolType().get())),
                    slot
            );
        }
        return slot;
    }

    private static net.minecraft.network.chat.Component traitSource(String key) {
        return net.minecraft.network.chat.Component.translatable("jei.mobstoolforging.material_traits.source." + key);
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
        List<ItemStack> secondaryInputs = recipe.secondaryInput().map(MobsToolForgingJeiPlugin::inputStacks).orElseGet(List::of);
        return Optional.of(new StationWorkJeiRecipe(
                recipe.id(),
                recipe.workstationKind(),
                stationFor(recipe.workstationKind()),
                inputs,
                secondaryInputs,
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

    private static Optional<DryingJeiRecipe> dryingRecipe(DryingRecipe recipe) {
        List<ItemStack> inputs = DryingRecipeRegistry.inputStacks(recipe.input());
        if (inputs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DryingJeiRecipe(
                recipe.id(),
                new ItemStack(ModItems.DRYING_RACK.get()),
                inputs,
                recipe.outputCopy(),
                recipe.ticks(),
                recipe
        ));
    }

    private static List<ItemStack> inputStacks(StationWorkRecipe.Input input) {
        if (input.itemId().isPresent()) {
            Item item = BuiltInRegistries.ITEM.get(input.itemId().get());
            return item == Items.AIR ? List.of() : List.of(new ItemStack(item, input.count()));
        }
        if (input.tag().isPresent()) {
            List<ItemStack> stacks = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(input.tag().get())
                    .forEach(holder -> stacks.add(new ItemStack(holder.value(), input.count())));
            return stacks;
        }
        return List.of();
    }

    private static Optional<ItemStack> armorAttachmentTarget(ForgeTemplateDefinition template) {
        if (ToolTypeRegistry.HELMET_PLATE_TEMPLATE.equals(template.id())) {
            return Optional.of(ModItems.MODULAR_HELMET.get().createChainmail());
        }
        if (ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(template.id())) {
            return Optional.of(ModItems.MODULAR_CHESTPLATE.get().createChainmail());
        }
        if (ToolTypeRegistry.LEGGINGS_PLATE_TEMPLATE.equals(template.id())) {
            return Optional.of(ModItems.MODULAR_LEGGINGS.get().createChainmail());
        }
        if (ToolTypeRegistry.BOOTS_PLATE_TEMPLATE.equals(template.id())) {
            return Optional.of(ModItems.MODULAR_BOOTS.get().createChainmail());
        }
        return Optional.empty();
    }

    private static ItemStack abrasiveFor(ResourceLocation tier) {
        if (LapidaryAbrasives.DIAMOND_TIER.equals(tier)) {
            return new ItemStack(ModItems.DIAMOND_POWDER.get());
        }
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(LapidaryAbrasives.tierTag(tier))) {
            return new ItemStack(holder.value());
        }
        return ItemStack.EMPTY;
    }

    private static boolean isActiveTemplate(ForgeTemplateDefinition template) {
        return !ToolTypeRegistry.SCREWDRIVER_HEAD_TEMPLATE.equals(template.id());
    }

    private static ItemStack stationFor(WorkstationKind workstation) {
        if (workstation == WorkstationKind.LAPIDARY_TABLE) {
            return new ItemStack(ModItems.LAPIDARY_TABLE.get());
        }
        if (workstation == WorkstationKind.SAWMILL) {
            return new ItemStack(ModItems.SAWMILL.get());
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
        return hammerFor(level);
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

    static ItemStack dryingRackIcon() {
        return new ItemStack(ModItems.DRYING_RACK.get());
    }

    static ItemStack lapidaryTableIcon() {
        return new ItemStack(ModItems.LAPIDARY_TABLE.get());
    }

    static ItemStack materialTraitIcon() {
        return new ItemStack(Items.EMERALD);
    }

    static ItemStack toolmakersBenchIcon() {
        return new ItemStack(ModItems.TOOLMAKERS_BENCH.get());
    }
}

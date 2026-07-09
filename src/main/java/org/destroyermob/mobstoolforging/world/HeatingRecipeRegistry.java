package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class HeatingRecipeRegistry {
    private static Map<ResourceLocation, HeatingRecipe> recipes = Map.of();

    private HeatingRecipeRegistry() {
    }

    public static synchronized void replace(Map<ResourceLocation, HeatingRecipe> loaded) {
        recipes = Map.copyOf(loaded);
    }

    public static List<HeatingRecipe> recipes() {
        return List.copyOf(recipes.values());
    }

    public static Optional<HeatingRecipe> find(HeatingSource source, ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        for (HeatingRecipe recipe : recipes.values()) {
            if (recipe.matches(source, stack)) {
                return Optional.of(recipe);
            }
        }
        return defaultRecipe(source, stack);
    }

    public static boolean isHeatable(HeatingSource source, ItemStack stack) {
        return find(source, stack).isPresent();
    }

    public static List<HeatingDisplayRecipe> displayRecipes() {
        List<HeatingDisplayRecipe> displayRecipes = new ArrayList<>();
        for (HeatingRecipe recipe : recipes.values()) {
            for (HeatingSource source : recipe.sources()) {
                displayRecipe(recipe, source).ifPresent(displayRecipes::add);
            }
        }
        displayRecipes.addAll(defaultDisplayRecipes());
        return displayRecipes;
    }

    public static int defaultHeatTicks(HeatingSource source, float targetTemperature) {
        int baseTicks = Math.max(1, MobsToolForgingConfig.HEATED_WORKPIECE_TICKS.get());
        if (source == HeatingSource.CAMPFIRE) {
            return Math.max(1, Math.round(baseTicks * Math.max(0.05F, targetTemperature)));
        }
        return baseTicks;
    }

    public static float defaultTargetTemperature(HeatingSource source) {
        return source == HeatingSource.CAMPFIRE
                ? MobsToolForgingConfig.campfireHeatLevel().temperature()
                : 1.0F;
    }

    private static Optional<HeatingRecipe> defaultRecipe(HeatingSource source, ItemStack stack) {
        if (source == HeatingSource.CAMPFIRE
                && (!MobsToolForgingConfig.ENABLE_CAMPFIRE_LOW_HEAT.get()
                || MobsToolForgingConfig.campfireHeatLevel() == HeatLevel.NONE)) {
            return Optional.empty();
        }
        if (heatableMaterial(stack).isEmpty()) {
            return Optional.empty();
        }
        float targetTemperature = defaultTargetTemperature(source);
        if (targetTemperature <= 0.0F) {
            return Optional.empty();
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return Optional.of(new HeatingRecipe(
                ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "default_heating/" + source.getSerializedName() + "/" + itemId.getNamespace() + "/" + itemId.getPath()),
                HeatingRecipe.Input.item(itemId),
                EnumSet.of(source),
                targetTemperature,
                defaultHeatTicks(source, targetTemperature),
                true
        ));
    }

    private static Optional<ToolMaterialDefinition> heatableMaterial(ItemStack stack) {
        ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
        if (partData != null) {
            Optional<ToolMaterialDefinition> partMaterial = MaterialCatalog.definition(partData.materialId())
                    .filter(HeatingRecipeRegistry::requiresTemperature);
            if (partMaterial.isPresent()) {
                return partMaterial;
            }
            return partData.coatingBaseMaterial()
                    .flatMap(MaterialCatalog::definition)
                    .filter(HeatingRecipeRegistry::requiresTemperature);
        }
        ArmorPartData armorPartData = stack.get(ModDataComponents.ARMOR_PART.get());
        if (armorPartData != null) {
            Optional<ToolMaterialDefinition> partMaterial = MaterialCatalog.definition(armorPartData.materialId())
                    .filter(HeatingRecipeRegistry::requiresTemperature);
            if (partMaterial.isPresent()) {
                return partMaterial;
            }
            return armorPartData.coatingBaseMaterial()
                    .flatMap(MaterialCatalog::definition)
                    .filter(HeatingRecipeRegistry::requiresTemperature);
        }
        return MaterialCatalog.resolve(stack)
                .filter(HeatingRecipeRegistry::requiresTemperature);
    }

    private static boolean requiresTemperature(ToolMaterialDefinition definition) {
        return definition.minimumForgeHeat() != HeatLevel.NONE;
    }

    private static Optional<HeatingDisplayRecipe> displayRecipe(HeatingRecipe recipe, HeatingSource source) {
        List<ItemStack> inputs = inputStacks(recipe.input());
        if (inputs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new HeatingDisplayRecipe(
                sourceId(recipe.id(), source),
                source,
                inputs,
                WorkpieceHeat.displayStack(inputs.getFirst(), recipe.targetTemperature(), recipe.workable()),
                recipe.ticks(),
                recipe.targetTemperature(),
                recipe.workable(),
                recipe
        ));
    }

    private static List<HeatingDisplayRecipe> defaultDisplayRecipes() {
        List<HeatingDisplayRecipe> recipes = new ArrayList<>();
        int index = 0;
        for (ItemStack stack : defaultDisplayStacks()) {
            for (HeatingSource source : HeatingSource.values()) {
                if (explicitRecipe(source, stack).isPresent()) {
                    continue;
                }
                Optional<HeatingRecipe> recipe = defaultRecipe(source, stack);
                if (recipe.isEmpty()) {
                    continue;
                }
                HeatingRecipe heatingRecipe = recipe.get();
                recipes.add(new HeatingDisplayRecipe(
                        ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "default_heating_display/" + source.getSerializedName() + "/" + index++),
                        source,
                        List.of(stack.copyWithCount(1)),
                        WorkpieceHeat.displayStack(stack.copyWithCount(1), heatingRecipe.targetTemperature(), heatingRecipe.workable()),
                        heatingRecipe.ticks(),
                        heatingRecipe.targetTemperature(),
                        heatingRecipe.workable(),
                        heatingRecipe
                ));
            }
        }
        return recipes;
    }

    private static Optional<HeatingRecipe> explicitRecipe(HeatingSource source, ItemStack stack) {
        for (HeatingRecipe recipe : recipes.values()) {
            if (recipe.matches(source, stack)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private static List<ItemStack> defaultDisplayStacks() {
        LinkedHashMap<String, ItemStack> stacks = new LinkedHashMap<>();
        for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
            MaterialCatalog.definition(materialId)
                    .filter(HeatingRecipeRegistry::requiresTemperature)
                    .map(ToolMaterialDefinition::displayItem)
                    .filter(item -> item != Items.AIR)
                    .ifPresent(item -> putStack(stacks, new ItemStack(item)));
        }
        for (ForgeTemplateDefinition template : ToolTypeRegistry.templates()) {
            for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
                if (!template.allowsMaterial(materialId)) {
                    continue;
                }
                MaterialCatalog.definition(materialId)
                        .filter(HeatingRecipeRegistry::requiresTemperature)
                        .ifPresent(material -> putStack(stacks, template.outputStack(materialId)));
            }
        }
        return List.copyOf(stacks.values());
    }

    private static void putStack(Map<String, ItemStack> stacks, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String key = itemId + "|" + stack.getComponentsPatch();
        stacks.putIfAbsent(key, stack.copyWithCount(1));
    }

    private static List<ItemStack> inputStacks(HeatingRecipe.Input input) {
        if (input.itemId().isPresent()) {
            Item item = BuiltInRegistries.ITEM.get(input.itemId().get());
            return item == Items.AIR ? List.of() : List.of(new ItemStack(item, input.count()));
        }
        if (input.tag().isPresent()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(input.tag().get())) {
                Item item = holder.value();
                if (item != Items.AIR) {
                    stacks.add(new ItemStack(item, input.count()));
                }
            }
            return stacks;
        }
        return List.of();
    }

    private static ResourceLocation sourceId(ResourceLocation id, HeatingSource source) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/" + source.getSerializedName());
    }
}

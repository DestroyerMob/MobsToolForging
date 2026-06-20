package org.destroyermob.mobstoolforging.world;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public record ForgeTemplateDefinition(
        ResourceLocation id,
        ResourceLocation toolType,
        String partType,
        int requiredMaterials,
        int requiredHits,
        String translationKey,
        float minimumTemperature,
        Set<ResourceLocation> materialWhitelist,
        Set<ResourceLocation> materialBlacklist,
        int minimumHammerLevel,
        Map<ResourceLocation, Integer> materialHammerLevels,
        ResourceLocation outputItem,
        Map<ResourceLocation, ResourceLocation> outputItems,
        Set<ResourceLocation> compatibleToolTypes
) {
    public ForgeTemplateDefinition {
        materialWhitelist = Set.copyOf(materialWhitelist);
        materialBlacklist = Set.copyOf(materialBlacklist);
        minimumHammerLevel = Math.max(0, minimumHammerLevel);
        materialHammerLevels = Map.copyOf(materialHammerLevels);
        outputItems = Map.copyOf(outputItems);
        compatibleToolTypes = compatibleToolTypes.isEmpty() ? Set.of(toolType) : Set.copyOf(compatibleToolTypes);
    }

    public ForgeTemplateDefinition(ResourceLocation id, ResourceLocation toolType, String partType, int requiredMaterials, int requiredHits, String translationKey) {
        this(id, toolType, partType, requiredMaterials, requiredHits, translationKey, Float.NaN);
    }

    public ForgeTemplateDefinition(ResourceLocation id, ResourceLocation toolType, String partType, int requiredMaterials, int requiredHits, String translationKey, float minimumTemperature) {
        this(id, toolType, partType, requiredMaterials, requiredHits, translationKey, minimumTemperature, Set.of(), Set.of(), SmithingHammerLevel.STONE.level(), Map.of(), null, Map.of(), Set.of(toolType));
    }

    @Override
    public float minimumTemperature() {
        if (Float.isNaN(minimumTemperature)) {
            return MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue();
        }
        return Math.max(0.0F, Math.min(1.0F, minimumTemperature));
    }

    public float minimumTemperatureOverride() {
        return minimumTemperature;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public boolean allowsMaterial(ResourceLocation materialId) {
        return (materialWhitelist.isEmpty() || materialWhitelist.contains(materialId)) && !materialBlacklist.contains(materialId);
    }

    public int minimumHammerLevel(ResourceLocation materialId) {
        return Math.max(minimumHammerLevel, materialHammerLevels.getOrDefault(materialId, SmithingHammerLevel.STONE.level()));
    }

    public ItemStack outputStack(ResourceLocation materialId) {
        return outputStack(materialId, ToolPartData.DEFAULT_QUALITY);
    }

    public ItemStack outputStack(ResourceLocation materialId, int quality) {
        ResourceLocation resolvedOutputItem = outputItems.getOrDefault(materialId, outputItem);
        if (resolvedOutputItem != null) {
            Item item = BuiltInRegistries.ITEM.get(resolvedOutputItem);
            if (item == Items.AIR) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = new ItemStack(item);
            stack.set(ModDataComponents.TOOL_PART.get(), new ToolPartData(partType, materialId, quality));
            if (!(item instanceof ModularToolPartItem)) {
                ToolStackNames.applyPartName(stack, partType, materialId);
            }
            return stack;
        }
        return ToolTypeRegistry.createPart(this, materialId, quality);
    }
}

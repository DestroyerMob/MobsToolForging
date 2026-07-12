package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolExternalComponents {
    private static final ResourceLocation MINECRAFT_CUSTOM_DATA = ResourceLocation.withDefaultNamespace("custom_data");
    private static final Set<ResourceLocation> APOTHEOSIS_AFFIX_COMPONENTS = Set.of(
            ResourceLocation.fromNamespaceAndPath("apotheosis", "affixes"),
            ResourceLocation.fromNamespaceAndPath("apotheosis", "rarity"),
            ResourceLocation.fromNamespaceAndPath("apotheosis", "affix_name")
    );
    private static final List<ResourceLocation> PRIMARY_HEAD_COMPONENTS = List.of(
            ResourceLocation.fromNamespaceAndPath("auric", "imbue")
    );
    private static final Set<ResourceLocation> COMPATIBLE_MINECRAFT_COMPONENTS = Set.of(
            MINECRAFT_CUSTOM_DATA
    );

    private ToolExternalComponents() {
    }

    public static void copyPrimaryHeadComponentsToTool(ItemStack primaryHead, ItemStack tool) {
        copyCompatibleExternalComponentsWithoutAffixes(primaryHead, tool);
    }

    public static void copyCompatibleExternalComponents(ItemStack source, ItemStack target) {
        copyCompatibleExternalComponents(source, target, true);
    }

    public static void copyCompatibleExternalComponentsWithoutAffixes(ItemStack source, ItemStack target) {
        copyCompatibleExternalComponents(source, target, false);
    }

    private static void copyCompatibleExternalComponents(ItemStack source, ItemStack target, boolean includeAffixes) {
        if (source.isEmpty() || target.isEmpty()) {
            return;
        }
        for (DataComponentType<?> component : source.getComponents().keySet()) {
            ResourceLocation componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component);
            if (componentId != null && shouldCopy(componentId) && (includeAffixes || !isApotheosisAffixComponent(componentId))) {
                copyComponent(source, target, component);
            }
        }
    }

    public static List<ItemStack> copyToolComponentsToPrimaryHead(ToolTypeDefinition definition, ItemStack tool, List<ItemStack> parts) {
        return copyToolComponentsToPrimaryHead(definition, tool, parts, true);
    }

    public static List<ItemStack> copyToolComponentsToPrimaryHeadWithoutAffixes(ToolTypeDefinition definition, ItemStack tool, List<ItemStack> parts) {
        return copyToolComponentsToPrimaryHead(definition, tool, parts, false);
    }

    private static List<ItemStack> copyToolComponentsToPrimaryHead(
            ToolTypeDefinition definition,
            ItemStack tool,
            List<ItemStack> parts,
            boolean includeAffixes
    ) {
        if (!hasAnyCompatibleExternalComponent(tool, includeAffixes)) {
            return List.copyOf(parts);
        }

        List<ItemStack> result = new ArrayList<>(parts.size());
        boolean copied = false;
        for (ItemStack part : parts) {
            ItemStack copy = part.copy();
            if (!copied && isPrimaryHead(definition, copy)) {
                copyCompatibleExternalComponents(tool, copy, includeAffixes);
                copied = true;
            }
            result.add(copy);
        }
        return List.copyOf(result);
    }

    private static boolean hasAnyCompatibleExternalComponent(ItemStack stack, boolean includeAffixes) {
        if (stack.isEmpty()) {
            return false;
        }
        for (DataComponentType<?> component : stack.getComponents().keySet()) {
            ResourceLocation componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component);
            if (componentId != null && shouldCopy(componentId) && (includeAffixes || !isApotheosisAffixComponent(componentId))) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldCopy(ResourceLocation componentId) {
        if (PRIMARY_HEAD_COMPONENTS.contains(componentId) || COMPATIBLE_MINECRAFT_COMPONENTS.contains(componentId)) {
            return true;
        }
        return !MobsToolForging.MOD_ID.equals(componentId.getNamespace())
                && !"minecraft".equals(componentId.getNamespace());
    }

    private static boolean isApotheosisAffixComponent(ResourceLocation componentId) {
        return APOTHEOSIS_AFFIX_COMPONENTS.contains(componentId);
    }

    private static <T> void copyComponent(ItemStack source, ItemStack target, DataComponentType<T> component) {
        T value = source.get(component);
        if (value != null) {
            target.set(component, value);
        }
    }

    private static boolean isPrimaryHead(ToolTypeDefinition definition, ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        return data != null
                && definition.primaryPartType().equals(data.partType())
                && definition.matchesPartItem(data.partType(), data.materialId(), stack);
    }
}

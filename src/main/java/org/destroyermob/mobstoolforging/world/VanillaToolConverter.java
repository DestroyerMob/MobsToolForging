package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class VanillaToolConverter {
    private VanillaToolConverter() {
    }

    public static ItemStack convert(ItemStack original, ResourceLocation handleMaterial) {
        if (original.isEmpty() || original.get(ModDataComponents.TOOL_CONSTRUCTION.get()) != null) {
            return ItemStack.EMPTY;
        }

        ToolConversion conversion = conversion(original.getItem(), handleMaterial);
        if (conversion == null) {
            return ItemStack.EMPTY;
        }

        ItemStack converted = conversion.definition().createTool(conversion.construction());
        if (converted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        converted.setCount(original.getCount());
        copyDamage(original, converted);
        copyIfPresent(original, converted, DataComponents.CUSTOM_NAME);
        copyIfPresent(original, converted, DataComponents.LORE);
        copyIfPresent(original, converted, DataComponents.ENCHANTMENTS);
        copyIfPresent(original, converted, DataComponents.REPAIR_COST);
        copyIfPresent(original, converted, DataComponents.CUSTOM_DATA);
        ToolExternalComponents.copyCompatibleExternalComponents(original, converted);
        ToolmakerBenchAssembly.disassemble(converted)
                .ifPresent(parts -> converted.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(parts)));
        return converted;
    }

    private static ToolConversion conversion(Item item, ResourceLocation handleMaterial) {
        ToolKind toolKind = toolKind(item);
        if (toolKind != null) {
            ResourceLocation headMaterial = headMaterial(item);
            ToolTypeDefinition definition = ToolTypeRegistry.toolType(toolKind).orElse(null);
            if (headMaterial != null && definition != null) {
                return new ToolConversion(definition, construction(definition, headMaterial, handleMaterial));
            }
        }
        return externalConversion(item, handleMaterial).orElse(null);
    }

    private static Optional<ToolConversion> externalConversion(Item item, ResourceLocation handleMaterial) {
        for (ToolTypeDefinition definition : ToolTypeRegistry.toolTypes()) {
            if (definition.builtInKind().isPresent()) {
                continue;
            }
            for (ResourceLocation headMaterial : definition.toolItemMaterials()) {
                Optional<Item> candidate = definition.toolItem(headMaterial);
                if (candidate.isPresent() && candidate.get() == item) {
                    return Optional.of(new ToolConversion(definition, construction(definition, headMaterial, handleMaterial)));
                }
            }
            Optional<Item> defaultItem = definition.toolItem();
            if (defaultItem.isPresent() && defaultItem.get() == item) {
                return Optional.of(new ToolConversion(definition, construction(definition, MaterialCatalog.IRON, handleMaterial)));
            }
        }
        return Optional.empty();
    }

    private static ToolKind toolKind(Item item) {
        if (isAny(item,
                Items.WOODEN_SWORD,
                Items.STONE_SWORD,
                Items.IRON_SWORD,
                Items.GOLDEN_SWORD,
                Items.DIAMOND_SWORD,
                Items.NETHERITE_SWORD)) {
            return ToolKind.SWORD;
        }
        if (isAny(item,
                Items.WOODEN_SHOVEL,
                Items.STONE_SHOVEL,
                Items.IRON_SHOVEL,
                Items.GOLDEN_SHOVEL,
                Items.DIAMOND_SHOVEL,
                Items.NETHERITE_SHOVEL)) {
            return ToolKind.SHOVEL;
        }
        if (isAny(item,
                Items.WOODEN_PICKAXE,
                Items.STONE_PICKAXE,
                Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE,
                Items.DIAMOND_PICKAXE,
                Items.NETHERITE_PICKAXE)) {
            return ToolKind.PICKAXE;
        }
        if (isAny(item,
                Items.WOODEN_AXE,
                Items.STONE_AXE,
                Items.IRON_AXE,
                Items.GOLDEN_AXE,
                Items.DIAMOND_AXE,
                Items.NETHERITE_AXE)) {
            return ToolKind.AXE;
        }
        if (isAny(item,
                Items.WOODEN_HOE,
                Items.STONE_HOE,
                Items.IRON_HOE,
                Items.GOLDEN_HOE,
                Items.DIAMOND_HOE,
                Items.NETHERITE_HOE)) {
            return ToolKind.HOE;
        }
        return null;
    }

    private static ResourceLocation headMaterial(Item item) {
        if (isAny(item, Items.WOODEN_SWORD, Items.WOODEN_SHOVEL, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE)
                || isAny(item, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE)) {
            return MaterialCatalog.FLINT;
        }
        if (isAny(item, Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE)) {
            return MaterialCatalog.IRON;
        }
        if (isAny(item, Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE)) {
            return MaterialCatalog.GOLD;
        }
        if (isAny(item, Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE)) {
            return MaterialCatalog.DIAMOND;
        }
        if (isAny(item, Items.NETHERITE_SWORD, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE)) {
            return MaterialCatalog.NETHERITE;
        }
        return null;
    }

    private static ToolConstructionData construction(ToolKind toolKind, ResourceLocation headMaterial, ResourceLocation handleMaterial) {
        return construction(ToolTypeRegistry.toolType(toolKind).orElseThrow(), headMaterial, handleMaterial);
    }

    private static ToolConstructionData construction(ToolTypeDefinition definition, ResourceLocation headMaterial, ResourceLocation handleMaterial) {
        Optional<ResourceLocation> guardMaterial = definition.requiredAssemblyParts().isEmpty()
                ? Optional.empty()
                : Optional.of(headMaterial);
        return new ToolConstructionData(
                definition.id(),
                headMaterial,
                handleMaterial,
                guardMaterial,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                ToolConstructionData.DEFAULT_QUALITY
        );
    }

    private static void copyDamage(ItemStack original, ItemStack converted) {
        if (!original.isDamageableItem() || !converted.isDamageableItem()) {
            return;
        }
        int damage = Math.round((original.getDamageValue() / (float) Math.max(1, original.getMaxDamage())) * converted.getMaxDamage());
        converted.setDamageValue(Math.min(damage, Math.max(0, converted.getMaxDamage() - 1)));
    }

    private static <T> void copyIfPresent(ItemStack original, ItemStack converted, DataComponentType<T> component) {
        T value = original.get(component);
        if (value != null) {
            converted.set(component, value);
        }
    }

    private static boolean isAny(Item item, Item... items) {
        for (Item candidate : items) {
            if (item == candidate) {
                return true;
            }
        }
        return false;
    }

    private record ToolConversion(ToolTypeDefinition definition, ToolConstructionData construction) {
    }
}

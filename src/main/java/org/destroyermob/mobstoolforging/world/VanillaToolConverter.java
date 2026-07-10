package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class VanillaToolConverter {
    private VanillaToolConverter() {
    }

    public static ItemStack convertLootOrEquipment(ItemStack original, ResourceLocation handleMaterial) {
        ItemStack converted = convert(original, handleMaterial);
        return converted.isEmpty() ? convertArmor(original) : converted;
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
        copyConvertedStackData(original, converted);
        establishAssemblyParts(converted);
        return converted;
    }

    public static ItemStack convertArmor(ItemStack original) {
        if (original.isEmpty() || original.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null) {
            return ItemStack.EMPTY;
        }

        ArmorConversion conversion = armorConversion(original.getItem());
        if (conversion == null) {
            return ItemStack.EMPTY;
        }

        ItemStack converted = conversion.create();
        if (converted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        copyConvertedStackData(original, converted);
        establishAssemblyParts(converted);
        return converted;
    }

    private static void establishAssemblyParts(ItemStack converted) {
        ToolmakerBenchAssembly.disassemble(converted).ifPresent(parts -> {
            converted.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(parts));
            ToolExternalComponents.removeApotheosisAffixComponents(converted);
        });
    }

    private static ToolConversion conversion(Item item, ResourceLocation handleMaterial) {
        Optional<ToolConversion> knownExternal = knownExternalConversion(item, handleMaterial);
        if (knownExternal.isPresent()) {
            return knownExternal.get();
        }
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

    private static Optional<ToolConversion> knownExternalConversion(Item item, ResourceLocation handleMaterial) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (!"mobsmoreweapons".equals(itemId.getNamespace())) {
            return Optional.empty();
        }
        return moreWeaponsMatch(itemId.getPath())
                .flatMap(match -> ToolTypeRegistry.toolType(match.toolType())
                        .map(definition -> new ToolConversion(definition, construction(definition, match.headMaterial(), handleMaterial, match.treatment()))));
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
        if (isAny(item, Items.WOODEN_SWORD, Items.WOODEN_SHOVEL, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE)) {
            return MaterialCatalog.FLINT;
        }
        if (isAny(item, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE)) {
            return MaterialCatalog.COPPER;
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
        return construction(definition, normalizedHeadMaterial(headMaterial), handleMaterial, treatmentFor(headMaterial));
    }

    private static ToolConstructionData construction(ToolTypeDefinition definition, ResourceLocation headMaterial, ResourceLocation handleMaterial, Optional<ResourceLocation> treatment) {
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
                treatment,
                ToolConstructionData.DEFAULT_QUALITY
        );
    }

    private static ResourceLocation normalizedHeadMaterial(ResourceLocation headMaterial) {
        return MaterialCatalog.NETHERITE.equals(headMaterial) ? MaterialCatalog.DIAMOND : headMaterial;
    }

    private static Optional<ResourceLocation> treatmentFor(ResourceLocation headMaterial) {
        return MaterialCatalog.NETHERITE.equals(headMaterial) ? Optional.of(MaterialCatalog.NETHERITE) : Optional.empty();
    }

    private static Optional<MoreWeaponsMatch> moreWeaponsMatch(String path) {
        MaterialSelection material = materialSelection(path);
        if (material == null) {
            return Optional.empty();
        }
        String toolType = path.substring(material.prefix().length());
        return switch (toolType) {
            case "great_sword", "katana", "battle_axe", "knife", "machete" -> Optional.of(new MoreWeaponsMatch(
                    ResourceLocation.fromNamespaceAndPath("mobsmoreweapons", toolType),
                    material.headMaterial(),
                    material.treatment()
            ));
            default -> Optional.empty();
        };
    }

    private static MaterialSelection materialSelection(String path) {
        if (path.startsWith("wooden_")) {
            return new MaterialSelection("wooden_", MaterialCatalog.FLINT, Optional.empty());
        }
        if (path.startsWith("stone_")) {
            return new MaterialSelection("stone_", MaterialCatalog.COPPER, Optional.empty());
        }
        if (path.startsWith("iron_")) {
            return new MaterialSelection("iron_", MaterialCatalog.IRON, Optional.empty());
        }
        if (path.startsWith("golden_")) {
            return new MaterialSelection("golden_", MaterialCatalog.GOLD, Optional.empty());
        }
        if (path.startsWith("diamond_")) {
            return new MaterialSelection("diamond_", MaterialCatalog.DIAMOND, Optional.empty());
        }
        if (path.startsWith("netherite_")) {
            return new MaterialSelection("netherite_", MaterialCatalog.DIAMOND, Optional.of(MaterialCatalog.NETHERITE));
        }
        return null;
    }

    private static ArmorConversion armorConversion(Item item) {
        if (isAny(item, Items.LEATHER_HELMET)) {
            return new ArmorConversion(() -> ModItems.MODULAR_HELMET.get().create(MaterialCatalog.LEATHER, Optional.empty()));
        }
        if (isAny(item, Items.LEATHER_CHESTPLATE)) {
            return new ArmorConversion(() -> ModItems.MODULAR_CHESTPLATE.get().createBase(MaterialCatalog.LEATHER));
        }
        if (isAny(item, Items.LEATHER_LEGGINGS)) {
            return new ArmorConversion(() -> ModItems.MODULAR_LEGGINGS.get().create(MaterialCatalog.LEATHER, Optional.empty()));
        }
        if (isAny(item, Items.LEATHER_BOOTS)) {
            return new ArmorConversion(() -> ModItems.MODULAR_BOOTS.get().create(MaterialCatalog.LEATHER, Optional.empty()));
        }
        if (isAny(item, Items.CHAINMAIL_HELMET)) {
            return new ArmorConversion(() -> ModItems.MODULAR_HELMET.get().createChainmail());
        }
        if (isAny(item, Items.CHAINMAIL_CHESTPLATE)) {
            return new ArmorConversion(() -> ModItems.MODULAR_CHESTPLATE.get().createChainmail());
        }
        if (isAny(item, Items.CHAINMAIL_LEGGINGS)) {
            return new ArmorConversion(() -> ModItems.MODULAR_LEGGINGS.get().createChainmail());
        }
        if (isAny(item, Items.CHAINMAIL_BOOTS)) {
            return new ArmorConversion(() -> ModItems.MODULAR_BOOTS.get().createChainmail());
        }

        ResourceLocation material = armorMaterial(item);
        if (material == null) {
            return null;
        }
        if (isAny(item, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET)) {
            return new ArmorConversion(() -> ModItems.MODULAR_HELMET.get().create(material));
        }
        if (isAny(item, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE)) {
            return new ArmorConversion(() -> ModItems.MODULAR_CHESTPLATE.get().create(material));
        }
        if (isAny(item, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS)) {
            return new ArmorConversion(() -> ModItems.MODULAR_LEGGINGS.get().create(material));
        }
        if (isAny(item, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS)) {
            return new ArmorConversion(() -> ModItems.MODULAR_BOOTS.get().create(material));
        }
        return null;
    }

    private static ResourceLocation armorMaterial(Item item) {
        if (isAny(item, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)) {
            return MaterialCatalog.IRON;
        }
        if (isAny(item, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS)) {
            return MaterialCatalog.GOLD;
        }
        if (isAny(item, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)) {
            return MaterialCatalog.DIAMOND;
        }
        if (isAny(item, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)) {
            return MaterialCatalog.NETHERITE;
        }
        return null;
    }

    private static void copyConvertedStackData(ItemStack original, ItemStack converted) {
        converted.setCount(original.getCount());
        copyDamage(original, converted);
        copyIfPresent(original, converted, DataComponents.CUSTOM_NAME);
        copyIfPresent(original, converted, DataComponents.LORE);
        copyIfPresent(original, converted, DataComponents.ENCHANTMENTS);
        copyIfPresent(original, converted, DataComponents.REPAIR_COST);
        copyIfPresent(original, converted, DataComponents.CUSTOM_DATA);
        ToolExternalComponents.copyCompatibleExternalComponents(original, converted);
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

    private record MoreWeaponsMatch(ResourceLocation toolType, ResourceLocation headMaterial, Optional<ResourceLocation> treatment) {
    }

    private record MaterialSelection(String prefix, ResourceLocation headMaterial, Optional<ResourceLocation> treatment) {
    }

    private record ArmorConversion(java.util.function.Supplier<ItemStack> factory) {
        private ItemStack create() {
            return factory.get();
        }
    }
}

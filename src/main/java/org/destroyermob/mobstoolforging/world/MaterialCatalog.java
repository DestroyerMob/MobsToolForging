package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.Tags;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModTags;

public final class MaterialCatalog {
    public static final ResourceLocation IRON = materialId("iron");
    public static final ResourceLocation GOLD = materialId("gold");
    public static final ResourceLocation COPPER = materialId("copper");
    public static final ResourceLocation NETHERITE = materialId("netherite");
    public static final ResourceLocation DIAMOND = materialId("diamond");
    public static final ResourceLocation EMERALD = materialId("emerald");
    public static final ResourceLocation FLINT = materialId("flint");
    public static final ResourceLocation OAK = materialId("oak");
    public static final ResourceLocation DARK_OAK = materialId("dark_oak");
    public static final ResourceLocation BLAZE = materialId("blaze");
    public static final ResourceLocation BREEZE = materialId("breeze");
    public static final ResourceLocation LEATHER = materialId("leather");
    public static final ResourceLocation AMETHYST = materialId("amethyst");
    public static final ResourceLocation NETHER = materialId("nether");
    public static final ResourceLocation SCULK = materialId("sculk");

    private static final Tier COPPER_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            190,
            5.5F,
            1.5F,
            16,
            () -> Ingredient.of(Items.COPPER_INGOT)
    );
    private static final Tier EMERALD_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1250,
            7.5F,
            2.5F,
            18,
            () -> Ingredient.of(Items.EMERALD)
    );

    private static final Map<ResourceLocation, ToolMaterialDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, LinkedHashSet<ResourceLocation>> EXTRA_VISUAL_MATERIALS = new LinkedHashMap<>();
    private static final LinkedHashSet<ResourceLocation> DATAPACK_MATERIALS = new LinkedHashSet<>();
    private static final Map<Item, ResourceLocation> MATERIAL_ITEM_IDS = new LinkedHashMap<>();
    private static final Map<TagKey<Item>, ResourceLocation> MATERIAL_TAG_IDS = new LinkedHashMap<>();
    private static final Map<Item, ResourceLocation> CUSTOM_HANDLE_MATERIALS = new LinkedHashMap<>();
    private static final List<ResourceLocation> STARTER_MATERIALS = List.of(IRON, GOLD, COPPER, NETHERITE, DIAMOND, EMERALD);
    private static final List<ResourceLocation> HANDLE_MATERIALS = List.of(OAK, DARK_OAK, BLAZE, BREEZE);
    private static final List<ResourceLocation> BINDING_MATERIALS = STARTER_MATERIALS;
    private static final List<ResourceLocation> WRAP_MATERIALS = List.of(LEATHER);
    private static final List<ResourceLocation> FOCUS_MATERIALS = List.of(AMETHYST);
    private static final List<ResourceLocation> TREATMENT_MATERIALS = List.of(NETHERITE, NETHER, SCULK);

    static {
        register(IRON, MaterialCategory.METAL, Items.IRON_INGOT, Tiers.IRON);
        register(GOLD, MaterialCategory.METAL, Items.GOLD_INGOT, Tiers.GOLD);
        register(COPPER, MaterialCategory.METAL, Items.COPPER_INGOT, COPPER_TIER);
        register(NETHERITE, MaterialCategory.METAL, Items.NETHERITE_INGOT, Tiers.NETHERITE);
        register(DIAMOND, MaterialCategory.GEM, Items.DIAMOND, Tiers.DIAMOND);
        register(EMERALD, MaterialCategory.GEM, Items.EMERALD, EMERALD_TIER);
        registerVirtual(FLINT, MaterialCategory.GEM, Items.FLINT, CrudeFlintTiers.FLINT);
    }

    private MaterialCatalog() {
    }

    public static Optional<ToolMaterialDefinition> resolve(ItemStack stack) {
        ResourceLocation registeredId = registeredMaterialId(stack).orElse(null);
        Optional<MaterialCategory> category = registeredId == null ? categoryFor(stack) : definition(registeredId).map(ToolMaterialDefinition::category);
        if (category.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation knownId = knownMaterialId(stack, category.get());
        ToolMaterialDefinition known = DEFINITIONS.get(knownId);
        if (known != null) {
            return Optional.of(known);
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Tier fallbackTier = category.get() == MaterialCategory.GEM ? Tiers.DIAMOND : Tiers.IRON;
        return Optional.of(new ToolMaterialDefinition(itemId, category.get(), stack.getItem(), fallbackTier));
    }

    public static boolean isMaterial(ItemStack stack) {
        return registeredMaterialId(stack).isPresent() || stack.is(ModTags.Items.MATERIALS);
    }

    public static Optional<ToolMaterialDefinition> definition(ResourceLocation materialId) {
        ToolMaterialDefinition definition = DEFINITIONS.get(materialId);
        if (definition != null) {
            return Optional.of(definition);
        }
        Item item = BuiltInRegistries.ITEM.get(materialId);
        MaterialCategory category = materialId.getPath().contains("gem") ? MaterialCategory.GEM : MaterialCategory.METAL;
        Tier fallbackTier = category == MaterialCategory.GEM ? Tiers.DIAMOND : Tiers.IRON;
        return Optional.of(new ToolMaterialDefinition(materialId, category, item, fallbackTier));
    }

    public static Component displayName(ResourceLocation materialId) {
        ToolMaterialDefinition definition = DEFINITIONS.get(materialId);
        if (definition != null && definition.translationKey() != null && !definition.translationKey().isBlank()) {
            return Component.translatable(definition.translationKey());
        }
        if (definition != null && materialId.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return Component.translatable("material.mobstoolforging." + materialId.getPath());
        }
        if (materialId.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return Component.translatable("material.mobstoolforging." + materialId.getPath());
        }
        return Component.literal(toTitleCase(materialId.getPath()));
    }

    public static String displayNameText(ResourceLocation materialId) {
        if (DEFINITIONS.containsKey(materialId) || materialId.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return toTitleCase(materialId.getPath());
        }
        return toTitleCase(materialId.getPath());
    }

    public static List<ResourceLocation> starterMaterialIds() {
        LinkedHashSet<ResourceLocation> values = new LinkedHashSet<>(STARTER_MATERIALS);
        DEFINITIONS.values().stream()
                .filter(definition -> definition.category() == MaterialCategory.METAL || definition.category() == MaterialCategory.GEM)
                .map(ToolMaterialDefinition::id)
                .forEach(values::add);
        return List.copyOf(values);
    }

    public static List<ResourceLocation> handleMaterialIds() {
        return HANDLE_MATERIALS;
    }

    public static List<ResourceLocation> visualMaterialIds(String materialFrom) {
        List<ResourceLocation> base = switch (materialFrom) {
            case "headMaterial" -> STARTER_MATERIALS;
            case "handleMaterial" -> HANDLE_MATERIALS;
            case "bindingMaterial" -> BINDING_MATERIALS;
            case "wrapMaterial" -> WRAP_MATERIALS;
            case "focusMaterial" -> FOCUS_MATERIALS;
            case "treatment" -> TREATMENT_MATERIALS;
            default -> List.of();
        };
        LinkedHashSet<ResourceLocation> values = new LinkedHashSet<>(base);
        values.addAll(EXTRA_VISUAL_MATERIALS.getOrDefault(materialFrom, new LinkedHashSet<>()));
        return List.copyOf(values);
    }

    public static synchronized void registerMaterial(ToolMaterialDefinition definition) {
        putDefinition(definition);
        registerVisualMaterial("headMaterial", definition.id());
        registerVisualMaterial("bindingMaterial", definition.id());
    }

    public static synchronized void registerMaterial(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        registerMaterial(new ToolMaterialDefinition(id, category, displayItem, tier));
    }

    public static synchronized void registerDatapackMaterial(ToolMaterialDefinition definition, List<Item> sourceItems, List<TagKey<Item>> sourceTags, List<String> visualSlots, List<Item> handleItems) {
        putDefinition(definition);
        sourceItems.forEach(item -> MATERIAL_ITEM_IDS.put(item, definition.id()));
        sourceTags.forEach(tag -> MATERIAL_TAG_IDS.put(tag, definition.id()));
        DATAPACK_MATERIALS.add(definition.id());
        if (visualSlots.isEmpty()) {
            registerVisualMaterial("headMaterial", definition.id());
            registerVisualMaterial("bindingMaterial", definition.id());
        } else {
            visualSlots.forEach(slot -> registerVisualMaterial(slot, definition.id()));
        }
        handleItems.forEach(item -> registerHandleMaterial(item, definition.id()));
    }

    public static synchronized void resetDatapackMaterials() {
        if (DATAPACK_MATERIALS.isEmpty()) {
            return;
        }
        DATAPACK_MATERIALS.forEach(DEFINITIONS::remove);
        MATERIAL_ITEM_IDS.entrySet().removeIf(entry -> DATAPACK_MATERIALS.contains(entry.getValue()));
        MATERIAL_TAG_IDS.entrySet().removeIf(entry -> DATAPACK_MATERIALS.contains(entry.getValue()));
        CUSTOM_HANDLE_MATERIALS.entrySet().removeIf(entry -> DATAPACK_MATERIALS.contains(entry.getValue()));
        EXTRA_VISUAL_MATERIALS.values().forEach(values -> values.removeIf(DATAPACK_MATERIALS::contains));
        DATAPACK_MATERIALS.clear();
    }

    public static synchronized void registerVisualMaterial(String materialFrom, ResourceLocation materialId) {
        EXTRA_VISUAL_MATERIALS.computeIfAbsent(materialFrom, ignored -> new LinkedHashSet<>()).add(materialId);
    }

    public static synchronized void registerHandleMaterial(Item item, ResourceLocation materialId) {
        CUSTOM_HANDLE_MATERIALS.put(item, materialId);
        registerVisualMaterial("handleMaterial", materialId);
    }

    public static ItemStack displayStack(ResourceLocation materialId) {
        return new ItemStack(definition(materialId).orElseThrow().displayItem());
    }

    public static ResourceLocation handleMaterial(ItemStack handle) {
        if (handle.is(Items.STICK) || handle.is(Tags.Items.RODS_WOODEN)) {
            return OAK;
        }
        if (handle.is(Items.BLAZE_ROD) || handle.is(Tags.Items.RODS_BLAZE)) {
            return BLAZE;
        }
        if (handle.is(Items.BREEZE_ROD) || handle.is(Tags.Items.RODS_BREEZE)) {
            return BREEZE;
        }
        ResourceLocation customHandle = CUSTOM_HANDLE_MATERIALS.get(handle.getItem());
        if (customHandle != null) {
            return customHandle;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(handle.getItem());
        return visualMaterialIds("handleMaterial").contains(itemId) ? itemId : OAK;
    }

    public static ResourceLocation bindingMaterial(ItemStack stack) {
        ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
        if (partData != null) {
            return partData.materialId();
        }
        return resolve(stack)
                .map(ToolMaterialDefinition::id)
                .orElseGet(() -> BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static ResourceLocation wrapMaterial(ItemStack stack) {
        if (stack.is(Items.LEATHER)) {
            return LEATHER;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static ResourceLocation focusMaterial(ItemStack stack) {
        if (stack.is(Items.AMETHYST_SHARD)) {
            return AMETHYST;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static ResourceLocation treatmentMaterial(ItemStack stack) {
        if (stack.is(Items.BLAZE_POWDER) || stack.is(Items.MAGMA_CREAM) || stack.is(Items.NETHERITE_SCRAP)) {
            return NETHER;
        }
        if (stack.is(Items.ECHO_SHARD) || stack.is(Items.SCULK_CATALYST)) {
            return SCULK;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static Optional<MaterialCategory> categoryFor(ItemStack stack) {
        Optional<ResourceLocation> registered = registeredMaterialId(stack);
        if (registered.isPresent()) {
            return definition(registered.get()).map(ToolMaterialDefinition::category);
        }
        if (stack.is(ModTags.Items.MATERIALS_METALS)) {
            return Optional.of(MaterialCategory.METAL);
        }
        if (stack.is(ModTags.Items.MATERIALS_GEMS)) {
            return Optional.of(MaterialCategory.GEM);
        }
        return Optional.empty();
    }

    private static ResourceLocation knownMaterialId(ItemStack stack, MaterialCategory category) {
        Optional<ResourceLocation> registeredMaterial = registeredMaterialId(stack);
        if (registeredMaterial.isPresent()) {
            ResourceLocation registeredId = registeredMaterial.get();
            ToolMaterialDefinition definition = DEFINITIONS.get(registeredId);
            if (definition != null && definition.category() == category) {
                return registeredId;
            }
        }
        ResourceLocation itemMaterial = MATERIAL_ITEM_IDS.get(stack.getItem());
        if (itemMaterial != null) {
            ToolMaterialDefinition definition = DEFINITIONS.get(itemMaterial);
            if (definition != null && definition.category() == category) {
                return itemMaterial;
            }
        }
        if (category == MaterialCategory.METAL) {
            if (stack.is(Tags.Items.INGOTS_IRON)) {
                return IRON;
            }
            if (stack.is(Tags.Items.INGOTS_GOLD)) {
                return GOLD;
            }
            if (stack.is(Tags.Items.INGOTS_COPPER)) {
                return COPPER;
            }
            if (stack.is(Tags.Items.INGOTS_NETHERITE)) {
                return NETHERITE;
            }
        } else {
            if (stack.is(Tags.Items.GEMS_DIAMOND)) {
                return DIAMOND;
            }
            if (stack.is(Tags.Items.GEMS_EMERALD)) {
                return EMERALD;
            }
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static Optional<ResourceLocation> registeredMaterialId(ItemStack stack) {
        ResourceLocation itemMaterial = MATERIAL_ITEM_IDS.get(stack.getItem());
        if (itemMaterial != null) {
            return Optional.of(itemMaterial);
        }
        return MATERIAL_TAG_IDS.entrySet().stream()
                .filter(entry -> stack.is(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private static void register(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        ToolMaterialDefinition definition = new ToolMaterialDefinition(id, category, displayItem, tier);
        putDefinition(definition);
    }

    private static void registerVirtual(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        DEFINITIONS.put(id, new ToolMaterialDefinition(id, category, displayItem, tier));
    }

    private static void putDefinition(ToolMaterialDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        MATERIAL_ITEM_IDS.put(definition.displayItem(), definition.id());
    }

    private static ResourceLocation materialId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static String toTitleCase(String path) {
        String cleaned = path
                .replace("_ingot", "")
                .replace("_gem", "")
                .replace("ingot_", "")
                .replace("gem_", "")
                .replace('_', ' ');
        String[] words = cleaned.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.isEmpty() ? path : builder.toString();
    }
}

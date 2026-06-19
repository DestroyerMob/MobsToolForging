package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Unit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
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
    public static final ResourceLocation OAK = materialId("oak");
    public static final ResourceLocation DARK_OAK = materialId("dark_oak");
    public static final ResourceLocation BLAZE = materialId("blaze");
    public static final ResourceLocation BREEZE = materialId("breeze");
    public static final ResourceLocation LEATHER = materialId("leather");
    public static final ResourceLocation AMETHYST = materialId("amethyst");
    public static final ResourceLocation NETHER = materialId("nether");
    public static final ResourceLocation SCULK = materialId("sculk");

    private static final Tier COPPER_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
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
    private static final List<ResourceLocation> STARTER_MATERIALS = List.of(IRON, GOLD, COPPER, NETHERITE, DIAMOND, EMERALD);
    private static final List<ResourceLocation> HANDLE_MATERIALS = List.of(OAK, DARK_OAK, BLAZE, BREEZE);
    private static final List<ResourceLocation> BINDING_MATERIALS = STARTER_MATERIALS;
    private static final List<ResourceLocation> WRAP_MATERIALS = List.of(LEATHER);
    private static final List<ResourceLocation> FOCUS_MATERIALS = List.of(AMETHYST);
    private static final List<ResourceLocation> TREATMENT_MATERIALS = List.of(NETHER, SCULK);

    static {
        register(IRON, MaterialCategory.METAL, Items.IRON_INGOT, Tiers.IRON);
        register(GOLD, MaterialCategory.METAL, Items.GOLD_INGOT, Tiers.GOLD);
        register(COPPER, MaterialCategory.METAL, Items.COPPER_INGOT, COPPER_TIER);
        register(NETHERITE, MaterialCategory.METAL, Items.NETHERITE_INGOT, Tiers.NETHERITE);
        register(DIAMOND, MaterialCategory.GEM, Items.DIAMOND, Tiers.DIAMOND);
        register(EMERALD, MaterialCategory.GEM, Items.EMERALD, EMERALD_TIER);
    }

    private MaterialCatalog() {
    }

    public static Optional<ToolMaterialDefinition> resolve(ItemStack stack) {
        Optional<MaterialCategory> category = categoryFor(stack);
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
        return stack.is(ModTags.Items.MATERIALS);
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
        if (DEFINITIONS.containsKey(materialId)) {
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
        return STARTER_MATERIALS;
    }

    public static List<ResourceLocation> handleMaterialIds() {
        return HANDLE_MATERIALS;
    }

    public static List<ResourceLocation> visualMaterialIds(String materialFrom) {
        return switch (materialFrom) {
            case "headMaterial" -> STARTER_MATERIALS;
            case "handleMaterial" -> HANDLE_MATERIALS;
            case "bindingMaterial" -> BINDING_MATERIALS;
            case "wrapMaterial" -> WRAP_MATERIALS;
            case "focusMaterial" -> FOCUS_MATERIALS;
            case "treatment" -> TREATMENT_MATERIALS;
            default -> List.of();
        };
    }

    public static Tool toolComponent(ResourceLocation materialId, ToolKind toolKind) {
        Tier tier = definition(materialId).orElseThrow().tier();
        return switch (toolKind) {
            case SWORD -> SwordItem.createToolProperties();
            case SHOVEL -> tier.createToolProperties(BlockTags.MINEABLE_WITH_SHOVEL);
            case PICKAXE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_AXE);
            case HOE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_HOE);
        };
    }

    public static ItemAttributeModifiers toolAttributes(ResourceLocation materialId, ToolKind toolKind) {
        Tier tier = definition(materialId).orElseThrow().tier();
        return switch (toolKind) {
            case SWORD -> SwordItem.createAttributes(tier, 3, -2.4F);
            case SHOVEL -> ShovelItem.createAttributes(tier, 1.5F, -3.0F);
            case PICKAXE -> PickaxeItem.createAttributes(tier, 1.0F, -2.8F);
            case AXE -> AxeItem.createAttributes(tier, axeAttackDamage(materialId), axeAttackSpeed(materialId));
            case HOE -> HoeItem.createAttributes(tier, hoeAttackDamage(materialId), hoeAttackSpeed(materialId));
        };
    }

    public static int durability(ResourceLocation materialId) {
        return definition(materialId).orElseThrow().tier().getUses();
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
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(handle.getItem());
        return HANDLE_MATERIALS.contains(itemId) ? itemId : OAK;
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

    public static void applyToolComponents(ItemStack stack, ResourceLocation materialId, ToolKind toolKind) {
        stack.set(DataComponents.MAX_DAMAGE, durability(materialId));
        stack.set(DataComponents.DAMAGE, 0);
        stack.set(DataComponents.TOOL, toolComponent(materialId, toolKind));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, toolAttributes(materialId, toolKind));
        if (NETHERITE.equals(materialId)) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }
    }

    private static Optional<MaterialCategory> categoryFor(ItemStack stack) {
        if (stack.is(ModTags.Items.MATERIALS_METALS)) {
            return Optional.of(MaterialCategory.METAL);
        }
        if (stack.is(ModTags.Items.MATERIALS_GEMS)) {
            return Optional.of(MaterialCategory.GEM);
        }
        return Optional.empty();
    }

    private static ResourceLocation knownMaterialId(ItemStack stack, MaterialCategory category) {
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

    private static void register(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        DEFINITIONS.put(id, new ToolMaterialDefinition(id, category, displayItem, tier));
    }

    private static float axeAttackDamage(ResourceLocation materialId) {
        if (DIAMOND.equals(materialId) || NETHERITE.equals(materialId) || EMERALD.equals(materialId)) {
            return 5.0F;
        }
        return 6.0F;
    }

    private static float axeAttackSpeed(ResourceLocation materialId) {
        if (IRON.equals(materialId) || COPPER.equals(materialId)) {
            return -3.1F;
        }
        return -3.0F;
    }

    private static float hoeAttackDamage(ResourceLocation materialId) {
        if (GOLD.equals(materialId)) {
            return 0.0F;
        }
        if (IRON.equals(materialId)) {
            return -2.0F;
        }
        if (DIAMOND.equals(materialId) || EMERALD.equals(materialId)) {
            return -3.0F;
        }
        if (NETHERITE.equals(materialId)) {
            return -4.0F;
        }
        return -1.0F;
    }

    private static float hoeAttackSpeed(ResourceLocation materialId) {
        if (GOLD.equals(materialId)) {
            return -3.0F;
        }
        if (IRON.equals(materialId)) {
            return -1.0F;
        }
        if (DIAMOND.equals(materialId) || EMERALD.equals(materialId) || NETHERITE.equals(materialId)) {
            return 0.0F;
        }
        return -2.0F;
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

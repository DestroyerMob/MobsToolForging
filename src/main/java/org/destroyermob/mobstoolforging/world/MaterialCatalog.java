package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import net.neoforged.neoforge.common.Tags;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModTags;

public final class MaterialCatalog {
    public static final ResourceLocation IRON = materialId("iron");
    public static final ResourceLocation GOLD = materialId("gold");
    public static final ResourceLocation DIAMOND = materialId("diamond");
    public static final ResourceLocation STICK = materialId("stick");
    public static final ResourceLocation BLAZE_ROD = materialId("blaze_rod");
    public static final ResourceLocation BREEZE_ROD = materialId("breeze_rod");

    private static final Map<ResourceLocation, ToolMaterialDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final List<ResourceLocation> STARTER_MATERIALS = List.of(IRON, GOLD, DIAMOND);
    private static final List<ResourceLocation> HANDLE_MATERIALS = List.of(STICK, BLAZE_ROD, BREEZE_ROD);

    static {
        register(IRON, MaterialCategory.METAL, Items.IRON_INGOT, Tiers.IRON);
        register(GOLD, MaterialCategory.METAL, Items.GOLD_INGOT, Tiers.GOLD);
        register(DIAMOND, MaterialCategory.GEM, Items.DIAMOND, Tiers.DIAMOND);
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
        return Optional.empty();
    }

    public static boolean isMaterial(ItemStack stack) {
        return stack.is(ModTags.Items.MATERIALS);
    }

    public static Optional<ToolMaterialDefinition> definition(ResourceLocation materialId) {
        ToolMaterialDefinition definition = DEFINITIONS.get(materialId);
        if (definition != null) {
            return Optional.of(definition);
        }
        return Optional.empty();
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

    public static List<ResourceLocation> starterMaterialIds() {
        return STARTER_MATERIALS;
    }

    public static List<ResourceLocation> handleMaterialIds() {
        return HANDLE_MATERIALS;
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
        if (handle.is(Items.STICK)) {
            return STICK;
        }
        if (handle.is(Items.BLAZE_ROD)) {
            return BLAZE_ROD;
        }
        if (handle.is(Items.BREEZE_ROD)) {
            return BREEZE_ROD;
        }
        return BuiltInRegistries.ITEM.getKey(handle.getItem());
    }

    public static void applyToolComponents(ItemStack stack, ResourceLocation materialId, ToolKind toolKind) {
        stack.set(DataComponents.MAX_DAMAGE, durability(materialId));
        stack.set(DataComponents.DAMAGE, 0);
        stack.set(DataComponents.TOOL, toolComponent(materialId, toolKind));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, toolAttributes(materialId, toolKind));
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
        } else {
            if (stack.is(Tags.Items.GEMS_DIAMOND)) {
                return DIAMOND;
            }
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static void register(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        DEFINITIONS.put(id, new ToolMaterialDefinition(id, category, displayItem, tier));
    }

    private static float axeAttackDamage(ResourceLocation materialId) {
        if (DIAMOND.equals(materialId)) {
            return 5.0F;
        }
        return 6.0F;
    }

    private static float axeAttackSpeed(ResourceLocation materialId) {
        if (IRON.equals(materialId)) {
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
        if (DIAMOND.equals(materialId)) {
            return -3.0F;
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
        if (DIAMOND.equals(materialId)) {
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

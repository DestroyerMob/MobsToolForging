package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class ArmorStatsCatalog {
    private static final ResourceLocation HELMET_ARMOR_ID = ResourceLocation.withDefaultNamespace("armor.helmet");
    private static final ResourceLocation CHESTPLATE_ARMOR_ID = ResourceLocation.withDefaultNamespace("armor.chestplate");
    private static final ResourceLocation LEGGINGS_ARMOR_ID = ResourceLocation.withDefaultNamespace("armor.leggings");
    private static final ResourceLocation BOOTS_ARMOR_ID = ResourceLocation.withDefaultNamespace("armor.boots");
    private static final Map<ResourceLocation, ArmorStats> HELMET_STATS = builtInHelmetStats();
    private static final Map<ResourceLocation, ArmorStats> CHESTPLATE_STATS = builtInChestplateStats();
    private static final Map<ResourceLocation, ArmorStats> LEGGINGS_STATS = builtInLeggingsStats();
    private static final Map<ResourceLocation, ArmorStats> BOOTS_STATS = builtInBootsStats();

    private ArmorStatsCatalog() {
    }

    public static void apply(ItemStack stack, ArmorConstructionData construction) {
        apply(stack, construction, true);
    }

    public static void applyPreservingDamage(ItemStack stack, ArmorConstructionData construction) {
        apply(stack, construction, false);
    }

    private static void apply(ItemStack stack, ArmorConstructionData construction, boolean resetDamage) {
        int previousMaxDamage = stack.getMaxDamage();
        int previousDamage = stack.getDamageValue();
        ArmorSlotStats slotStats = slotStats(construction);
        ArmorStats stats = slotStats.stats();
        int maxDamage = slotStats.type().getDurability(stats.durabilityFactor());
        stack.set(DataComponents.MAX_DAMAGE, maxDamage);
        stack.set(DataComponents.DAMAGE, resetDamage ? 0 : scaledDamage(previousDamage, previousMaxDamage, maxDamage));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes(stats, slotStats.modifierId(), slotStats.slotGroup()));
        if (stats.fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.FIRE_RESISTANT);
        }
    }

    private static int scaledDamage(int previousDamage, int previousMaxDamage, int newMaxDamage) {
        if (previousMaxDamage <= 0 || previousDamage <= 0 || newMaxDamage <= 0) {
            return 0;
        }
        int scaled = Math.round(previousDamage * (newMaxDamage / (float) previousMaxDamage));
        return Math.max(0, Math.min(newMaxDamage - 1, scaled));
    }

    public static ArmorStats helmetStats(ResourceLocation materialId) {
        return HELMET_STATS.getOrDefault(materialId, HELMET_STATS.get(MaterialCatalog.IRON));
    }

    public static ArmorStats chainmailHelmetStats(ResourceLocation materialId) {
        if (MaterialCatalog.LEATHER.equals(materialId)) {
            return helmetStats(MaterialCatalog.LEATHER);
        }
        return new ArmorStats(2, 15, 12, 0.0F, 0.0F, false);
    }

    public static ArmorStats chestplateStats(ResourceLocation materialId) {
        return CHESTPLATE_STATS.getOrDefault(materialId, CHESTPLATE_STATS.get(MaterialCatalog.IRON));
    }

    public static ArmorStats chainmailChestplateStats(ResourceLocation materialId) {
        if (MaterialCatalog.LEATHER.equals(materialId)) {
            return chestplateStats(MaterialCatalog.LEATHER);
        }
        return new ArmorStats(5, 15, 12, 0.0F, 0.0F, false);
    }

    public static ArmorStats leggingsStats(ResourceLocation materialId) {
        return LEGGINGS_STATS.getOrDefault(materialId, LEGGINGS_STATS.get(MaterialCatalog.IRON));
    }

    public static ArmorStats chainmailLeggingsStats(ResourceLocation materialId) {
        if (MaterialCatalog.LEATHER.equals(materialId)) {
            return leggingsStats(MaterialCatalog.LEATHER);
        }
        return new ArmorStats(4, 15, 12, 0.0F, 0.0F, false);
    }

    public static ArmorStats bootsStats(ResourceLocation materialId) {
        return BOOTS_STATS.getOrDefault(materialId, BOOTS_STATS.get(MaterialCatalog.IRON));
    }

    public static ArmorStats chainmailBootsStats(ResourceLocation materialId) {
        if (MaterialCatalog.LEATHER.equals(materialId)) {
            return bootsStats(MaterialCatalog.LEATHER);
        }
        return new ArmorStats(1, 15, 12, 0.0F, 0.0F, false);
    }

    public static ArmorStats stats(ArmorConstructionData construction) {
        return slotStats(construction).stats();
    }

    public static boolean isSupportedHelmetMaterial(ResourceLocation materialId) {
        return HELMET_STATS.containsKey(materialId);
    }

    public static boolean isSupportedArmorMaterial(ResourceLocation materialId) {
        return HELMET_STATS.containsKey(materialId)
                || CHESTPLATE_STATS.containsKey(materialId)
                || LEGGINGS_STATS.containsKey(materialId)
                || BOOTS_STATS.containsKey(materialId);
    }

    private static ArmorSlotStats slotStats(ArmorConstructionData construction) {
        if (ArmorConstructionData.BOOTS_TYPE.equals(construction.armorType())) {
            return new ArmorSlotStats(
                    ArmorItem.Type.BOOTS,
                    EquipmentSlotGroup.FEET,
                    BOOTS_ARMOR_ID,
                    construction.bootsPlateMaterial()
                            .map(material -> combinedStats(chainmailBootsStats(construction.bootsChainmailMaterial()), bootsStats(material)))
                            .orElseGet(() -> chainmailBootsStats(construction.bootsChainmailMaterial()))
            );
        }
        if (ArmorConstructionData.LEGGINGS_TYPE.equals(construction.armorType())) {
            return new ArmorSlotStats(
                    ArmorItem.Type.LEGGINGS,
                    EquipmentSlotGroup.LEGS,
                    LEGGINGS_ARMOR_ID,
                    construction.leggingsPlateMaterial()
                            .map(material -> combinedStats(chainmailLeggingsStats(construction.leggingsChainmailMaterial()), leggingsStats(material)))
                            .orElseGet(() -> chainmailLeggingsStats(construction.leggingsChainmailMaterial()))
            );
        }
        if (ArmorConstructionData.CHESTPLATE_TYPE.equals(construction.armorType())) {
            return new ArmorSlotStats(
                    ArmorItem.Type.CHESTPLATE,
                    EquipmentSlotGroup.CHEST,
                    CHESTPLATE_ARMOR_ID,
                    construction.chestplatePlateMaterial()
                            .map(material -> combinedStats(chainmailChestplateStats(construction.chestplateChainmailMaterial()), chestplateStats(material)))
                            .orElseGet(() -> chainmailChestplateStats(construction.chestplateChainmailMaterial()))
            );
        }
        return new ArmorSlotStats(
                ArmorItem.Type.HELMET,
                EquipmentSlotGroup.HEAD,
                HELMET_ARMOR_ID,
                construction.helmetPlateMaterial()
                        .map(material -> combinedStats(chainmailHelmetStats(construction.helmetChainmailMaterial()), helmetStats(material)))
                        .orElseGet(() -> chainmailHelmetStats(construction.helmetChainmailMaterial()))
        );
    }

    private static ArmorStats combinedStats(ArmorStats chainmail, ArmorStats plate) {
        int defenseBonus = Math.max(1, Math.round(chainmail.defense() * 0.25F));
        int durabilityBonus = Math.max(1, Math.round(plate.durabilityFactor() * 0.35F));
        return new ArmorStats(
                plate.defense() + defenseBonus,
                chainmail.durabilityFactor() + durabilityBonus,
                Math.max(chainmail.enchantmentValue(), plate.enchantmentValue()),
                plate.toughness(),
                plate.knockbackResistance(),
                chainmail.fireResistant() || plate.fireResistant()
        );
    }

    private static ItemAttributeModifiers attributes(ArmorStats stats, ResourceLocation modifierId, EquipmentSlotGroup slotGroup) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(modifierId, stats.defense(), AttributeModifier.Operation.ADD_VALUE),
                slotGroup
        );
        if (stats.toughness() > 0.0F) {
            builder.add(
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(modifierId, stats.toughness(), AttributeModifier.Operation.ADD_VALUE),
                    slotGroup
            );
        }
        if (stats.knockbackResistance() > 0.0F) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(modifierId, stats.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE),
                    slotGroup
            );
        }
        return builder.build();
    }

    private static Map<ResourceLocation, ArmorStats> builtInHelmetStats() {
        Map<ResourceLocation, ArmorStats> stats = new LinkedHashMap<>();
        stats.put(MaterialCatalog.LEATHER, new ArmorStats(1, 5, 15, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.IRON, new ArmorStats(2, 15, 9, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.GOLD, new ArmorStats(2, 7, 25, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.COPPER, new ArmorStats(2, 12, 16, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.NETHERITE, new ArmorStats(3, 37, 15, 3.0F, 0.1F, true));
        stats.put(MaterialCatalog.DIAMOND, new ArmorStats(3, 33, 10, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.EMERALD, new ArmorStats(3, 30, 18, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.AMETHYST, new ArmorStats(3, 20, 20, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.RUBY, new ArmorStats(3, 29, 16, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.SAPPHIRE, new ArmorStats(3, 31, 20, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.TOPAZ, new ArmorStats(3, 18, 18, 0.0F, 0.0F, false));
        return Map.copyOf(stats);
    }

    private static Map<ResourceLocation, ArmorStats> builtInChestplateStats() {
        Map<ResourceLocation, ArmorStats> stats = new LinkedHashMap<>();
        stats.put(MaterialCatalog.LEATHER, new ArmorStats(3, 5, 15, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.IRON, new ArmorStats(6, 15, 9, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.GOLD, new ArmorStats(5, 7, 25, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.COPPER, new ArmorStats(5, 12, 16, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.NETHERITE, new ArmorStats(8, 37, 15, 3.0F, 0.1F, true));
        stats.put(MaterialCatalog.DIAMOND, new ArmorStats(8, 33, 10, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.EMERALD, new ArmorStats(7, 30, 18, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.AMETHYST, new ArmorStats(8, 20, 20, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.RUBY, new ArmorStats(7, 29, 16, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.SAPPHIRE, new ArmorStats(7, 31, 20, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.TOPAZ, new ArmorStats(8, 18, 18, 0.0F, 0.0F, false));
        return Map.copyOf(stats);
    }

    private static Map<ResourceLocation, ArmorStats> builtInLeggingsStats() {
        Map<ResourceLocation, ArmorStats> stats = new LinkedHashMap<>();
        stats.put(MaterialCatalog.LEATHER, new ArmorStats(2, 5, 15, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.IRON, new ArmorStats(5, 15, 9, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.GOLD, new ArmorStats(3, 7, 25, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.COPPER, new ArmorStats(4, 12, 16, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.NETHERITE, new ArmorStats(6, 37, 15, 3.0F, 0.1F, true));
        stats.put(MaterialCatalog.DIAMOND, new ArmorStats(6, 33, 10, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.EMERALD, new ArmorStats(6, 30, 18, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.AMETHYST, new ArmorStats(6, 20, 20, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.RUBY, new ArmorStats(6, 29, 16, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.SAPPHIRE, new ArmorStats(6, 31, 20, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.TOPAZ, new ArmorStats(6, 18, 18, 0.0F, 0.0F, false));
        return Map.copyOf(stats);
    }

    private static Map<ResourceLocation, ArmorStats> builtInBootsStats() {
        Map<ResourceLocation, ArmorStats> stats = new LinkedHashMap<>();
        stats.put(MaterialCatalog.LEATHER, new ArmorStats(1, 5, 15, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.IRON, new ArmorStats(2, 15, 9, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.GOLD, new ArmorStats(1, 7, 25, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.COPPER, new ArmorStats(2, 12, 16, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.NETHERITE, new ArmorStats(3, 37, 15, 3.0F, 0.1F, true));
        stats.put(MaterialCatalog.DIAMOND, new ArmorStats(3, 33, 10, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.EMERALD, new ArmorStats(3, 30, 18, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.AMETHYST, new ArmorStats(3, 20, 20, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.RUBY, new ArmorStats(3, 29, 16, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.SAPPHIRE, new ArmorStats(3, 31, 20, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.TOPAZ, new ArmorStats(3, 18, 18, 0.0F, 0.0F, false));
        return Map.copyOf(stats);
    }

    public record ArmorStats(int defense, int durabilityFactor, int enchantmentValue, float toughness, float knockbackResistance, boolean fireResistant) {
    }

    private record ArmorSlotStats(ArmorItem.Type type, EquipmentSlotGroup slotGroup, ResourceLocation modifierId, ArmorStats stats) {
    }
}

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
    private static final Map<ResourceLocation, ArmorStats> HELMET_STATS = builtInHelmetStats();
    private static final Map<ResourceLocation, ArmorStats> CHESTPLATE_STATS = builtInChestplateStats();

    private ArmorStatsCatalog() {
    }

    public static void apply(ItemStack stack, ArmorConstructionData construction) {
        ArmorSlotStats slotStats = slotStats(construction);
        ArmorStats stats = slotStats.stats();
        stack.set(DataComponents.MAX_DAMAGE, slotStats.type().getDurability(stats.durabilityFactor()));
        stack.set(DataComponents.DAMAGE, 0);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes(stats, slotStats.modifierId(), slotStats.slotGroup()));
        if (stats.fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.FIRE_RESISTANT);
        }
    }

    public static ArmorStats helmetStats(ResourceLocation materialId) {
        return HELMET_STATS.getOrDefault(materialId, HELMET_STATS.get(MaterialCatalog.IRON));
    }

    public static ArmorStats chestplateStats(ResourceLocation materialId) {
        return CHESTPLATE_STATS.getOrDefault(materialId, CHESTPLATE_STATS.get(MaterialCatalog.IRON));
    }

    public static ArmorStats stats(ArmorConstructionData construction) {
        return slotStats(construction).stats();
    }

    public static boolean isSupportedHelmetMaterial(ResourceLocation materialId) {
        return HELMET_STATS.containsKey(materialId);
    }

    public static boolean isSupportedArmorMaterial(ResourceLocation materialId) {
        return HELMET_STATS.containsKey(materialId) || CHESTPLATE_STATS.containsKey(materialId);
    }

    private static ArmorSlotStats slotStats(ArmorConstructionData construction) {
        if (ArmorConstructionData.CHESTPLATE_TYPE.equals(construction.armorType())) {
            return new ArmorSlotStats(
                    ArmorItem.Type.CHESTPLATE,
                    EquipmentSlotGroup.CHEST,
                    CHESTPLATE_ARMOR_ID,
                    chestplateStats(construction.skullMaterial())
            );
        }
        return new ArmorSlotStats(
                ArmorItem.Type.HELMET,
                EquipmentSlotGroup.HEAD,
                HELMET_ARMOR_ID,
                helmetStats(construction.skullMaterial())
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
        stats.put(MaterialCatalog.IRON, new ArmorStats(2, 15, 9, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.GOLD, new ArmorStats(2, 7, 25, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.COPPER, new ArmorStats(2, 12, 16, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.NETHERITE, new ArmorStats(3, 37, 15, 3.0F, 0.1F, true));
        stats.put(MaterialCatalog.DIAMOND, new ArmorStats(3, 33, 10, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.EMERALD, new ArmorStats(3, 30, 18, 2.0F, 0.0F, false));
        return Map.copyOf(stats);
    }

    private static Map<ResourceLocation, ArmorStats> builtInChestplateStats() {
        Map<ResourceLocation, ArmorStats> stats = new LinkedHashMap<>();
        stats.put(MaterialCatalog.IRON, new ArmorStats(6, 15, 9, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.GOLD, new ArmorStats(5, 7, 25, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.COPPER, new ArmorStats(5, 12, 16, 0.0F, 0.0F, false));
        stats.put(MaterialCatalog.NETHERITE, new ArmorStats(8, 37, 15, 3.0F, 0.1F, true));
        stats.put(MaterialCatalog.DIAMOND, new ArmorStats(8, 33, 10, 2.0F, 0.0F, false));
        stats.put(MaterialCatalog.EMERALD, new ArmorStats(7, 30, 18, 2.0F, 0.0F, false));
        return Map.copyOf(stats);
    }

    public record ArmorStats(int defense, int durabilityFactor, int enchantmentValue, float toughness, float knockbackResistance, boolean fireResistant) {
    }

    private record ArmorSlotStats(ArmorItem.Type type, EquipmentSlotGroup slotGroup, ResourceLocation modifierId, ArmorStats stats) {
    }
}

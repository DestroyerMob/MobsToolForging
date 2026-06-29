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
    private static final Map<ResourceLocation, ArmorStats> HELMET_STATS = builtInHelmetStats();

    private ArmorStatsCatalog() {
    }

    public static void apply(ItemStack stack, ArmorConstructionData construction) {
        ArmorStats stats = helmetStats(construction.skullMaterial());
        stack.set(DataComponents.MAX_DAMAGE, ArmorItem.Type.HELMET.getDurability(stats.durabilityFactor()));
        stack.set(DataComponents.DAMAGE, 0);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes(stats));
        if (stats.fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.FIRE_RESISTANT);
        }
    }

    public static ArmorStats helmetStats(ResourceLocation materialId) {
        return HELMET_STATS.getOrDefault(materialId, HELMET_STATS.get(MaterialCatalog.IRON));
    }

    public static boolean isSupportedHelmetMaterial(ResourceLocation materialId) {
        return HELMET_STATS.containsKey(materialId);
    }

    private static ItemAttributeModifiers attributes(ArmorStats stats) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(HELMET_ARMOR_ID, stats.defense(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.HEAD
        );
        if (stats.toughness() > 0.0F) {
            builder.add(
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(HELMET_ARMOR_ID, stats.toughness(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HEAD
            );
        }
        if (stats.knockbackResistance() > 0.0F) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(HELMET_ARMOR_ID, stats.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HEAD
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

    public record ArmorStats(int defense, int durabilityFactor, int enchantmentValue, float toughness, float knockbackResistance, boolean fireResistant) {
    }
}

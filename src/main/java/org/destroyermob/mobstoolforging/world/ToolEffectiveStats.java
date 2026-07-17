package org.destroyermob.mobstoolforging.world;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Item-only values suitable for a tooltip, without assuming a wielder, target, or combat state. */
public record ToolEffectiveStats(
        double baseAttackDamage,
        double effectiveAttackDamage,
        double attackSpeed,
        OptionalDouble baseMiningSpeed,
        OptionalDouble effectiveMiningSpeed,
        int remainingDurability,
        int maxDurability,
        OptionalInt crossbowChargeTicks,
        double projectileDamageMultiplier,
        double currentOutputMultiplier
) {
    private static final double PLAYER_ATTACK_DAMAGE_BASE = 1.0D;
    private static final double PLAYER_ATTACK_SPEED_BASE = 4.0D;

    public static ToolEffectiveStats resolve(
            ItemStack stack,
            ToolTypeDefinition definition,
            ToolConstructionData construction,
            ToolStatProfile profile,
            Item.TooltipContext context
    ) {
        ItemEnchantments enchantments = gameplayEnchantments(stack, context);
        double baseAttackDamage = componentAttributeValue(stack, Attributes.ATTACK_DAMAGE, PLAYER_ATTACK_DAMAGE_BASE);
        double attributeAttackDamage = stackAttributeValue(stack, Attributes.ATTACK_DAMAGE, PLAYER_ATTACK_DAMAGE_BASE);
        double effectiveAttackDamage = applyUnconditionalDamageEffects(attributeAttackDamage, enchantments);
        double attackSpeed = stackAttributeValue(stack, Attributes.ATTACK_SPEED, PLAYER_ATTACK_SPEED_BASE);

        OptionalDouble baseMiningSpeed = OptionalDouble.empty();
        OptionalDouble effectiveMiningSpeed = OptionalDouble.empty();
        if (isHarvestTool(definition)) {
            double baseSpeed = MaterialCatalog.definition(construction.headMaterial())
                    .map(material -> material.tier().getSpeed() * profile.miningSpeedMultiplier())
                    .orElse(1.0F);
            double efficiency = stackAttributeValue(stack, Attributes.MINING_EFFICIENCY, 0.0D);
            baseMiningSpeed = OptionalDouble.of(baseSpeed);
            effectiveMiningSpeed = OptionalDouble.of(baseSpeed > 1.0D ? baseSpeed + Math.max(0.0D, efficiency) : baseSpeed);
        }

        OptionalInt chargeTicks = OptionalInt.empty();
        double projectileDamageMultiplier = 1.0D;
        if (CrossbowAssembly.isCrossbow(construction)) {
            chargeTicks = OptionalInt.of(crossbowChargeTicks(enchantments));
            int tensioned = ToolStatBuilder.traitLevel(stack, ToolTrait.TENSIONED);
            projectileDamageMultiplier = profile.physicalDamageMultiplier()
                    * ToolTraitTuning.scaledMultiplier(ToolTraitTuning.TENSIONED_PROJECTILE_DAMAGE_BONUS, tensioned);
        }

        int maxDurability = stack.getMaxDamage();
        int remainingDurability = maxDurability <= 0 ? 0 : Math.max(0, maxDurability - stack.getDamageValue());
        return new ToolEffectiveStats(
                baseAttackDamage,
                effectiveAttackDamage,
                attackSpeed,
                baseMiningSpeed,
                effectiveMiningSpeed,
                remainingDurability,
                maxDurability,
                chargeTicks,
                projectileDamageMultiplier,
                ToolTraitEffects.currentOutputMultiplier(stack)
        );
    }

    private static boolean isHarvestTool(ToolTypeDefinition definition) {
        return definition.miningTag().isPresent()
                || definition.builtInKind().filter(kind -> kind != ToolKind.SWORD).isPresent();
    }

    private static ItemEnchantments gameplayEnchantments(ItemStack stack, Item.TooltipContext context) {
        HolderLookup.Provider provider = context.registries();
        if (provider == null) {
            return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        return provider.lookup(Registries.ENCHANTMENT)
                .map(stack::getAllEnchantments)
                .orElseGet(() -> stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
    }

    private static double componentAttributeValue(ItemStack stack, Holder<Attribute> target, double baseValue) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return attributeValue(target, baseValue, consumer -> modifiers.forEach(EquipmentSlot.MAINHAND, consumer));
    }

    private static double stackAttributeValue(ItemStack stack, Holder<Attribute> target, double baseValue) {
        return attributeValue(target, baseValue, consumer -> stack.forEachModifier(EquipmentSlot.MAINHAND, consumer));
    }

    private static double attributeValue(
            Holder<Attribute> target,
            double baseValue,
            ModifierIteration iteration
    ) {
        double[] addValue = {0.0D};
        double[] addMultipliedBase = {0.0D};
        double[] multipliedTotal = {1.0D};
        iteration.forEach((attribute, modifier) -> {
            if (!target.equals(attribute)) {
                return;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> addValue[0] += modifier.amount();
                case ADD_MULTIPLIED_BASE -> addMultipliedBase[0] += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal[0] *= 1.0D + modifier.amount();
            }
        });
        double withAdds = baseValue + addValue[0];
        double result = (withAdds + withAdds * addMultipliedBase[0]) * multipliedTotal[0];
        return target.value().sanitizeValue(result);
    }

    private static double applyUnconditionalDamageEffects(double damage, ItemEnchantments enchantments) {
        float result = (float) damage;
        RandomSource random = RandomSource.create(0L);
        for (var entry : enchantments.entrySet()) {
            int level = entry.getIntValue();
            for (var conditional : entry.getKey().value().getEffects(EnchantmentEffectComponents.DAMAGE)) {
                if (conditional.requirements().isEmpty()) {
                    result = conditional.effect().process(level, random, result);
                }
            }
        }
        return Math.max(0.0D, result);
    }

    private static int crossbowChargeTicks(ItemEnchantments enchantments) {
        float chargeSeconds = 1.25F;
        RandomSource random = RandomSource.create(0L);
        for (var entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey().value();
            var effect = enchantment.effects().get(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME);
            if (effect != null) {
                chargeSeconds = effect.process(entry.getIntValue(), random, chargeSeconds);
            }
        }
        return Mth.floor(Math.max(0.0F, chargeSeconds) * 20.0F);
    }

    @FunctionalInterface
    private interface ModifierIteration {
        void forEach(java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier> consumer);
    }
}

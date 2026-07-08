package org.destroyermob.mobstoolforging.item;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;

public interface ModularArmorItem extends IItemExtension {
    default boolean allowsFinishedArmorEnchanting(ItemStack stack, boolean fallback) {
        if (isBrokenArmor(stack)) {
            return false;
        }
        if (stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null && MobsToolForgingConfig.REQUIRE_ARMOR_PART_ENCHANTING.get()) {
            return false;
        }
        return fallback;
    }

    default int finishedArmorEnchantmentValue(ItemStack stack, int fallback) {
        if (isBrokenArmor(stack)) {
            return 0;
        }
        return allowsFinishedArmorEnchanting(stack, true) ? fallback : 0;
    }

    @Override
    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return allowsFinishedArmorEnchanting(stack, true) && IItemExtension.super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return allowsFinishedArmorEnchanting(stack, true) && IItemExtension.super.supportsEnchantment(stack, enchantment);
    }

    @Override
    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return allowsFinishedArmorEnchanting(stack, true) && IItemExtension.super.isBookEnchantable(stack, book);
    }

    @Override
    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        if (amount <= 0 || isBrokenArmor(stack) || stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) == null || !stack.isDamageableItem()) {
            return isBrokenArmor(stack) ? 0 : amount;
        }
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0 || stack.getDamageValue() + amount < maxDamage) {
            return amount;
        }
        setBrokenArmor(stack);
        return 0;
    }

    default boolean isBrokenArmor(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ModDataComponents.ARMOR_BROKEN.get()));
    }

    default void setBrokenArmor(ItemStack stack) {
        stack.set(ModDataComponents.ARMOR_BROKEN.get(), true);
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.max(0, stack.getMaxDamage() - 1));
        }
        disableArmorEnchantments(stack);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    default void refreshBrokenArmor(ItemStack stack) {
        if (!isBrokenArmor(stack) || !stack.isDamageableItem() || stack.getDamageValue() >= Math.max(0, stack.getMaxDamage() - 1)) {
            return;
        }
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
            return;
        }
        int repairedDamage = stack.getDamageValue();
        stack.remove(ModDataComponents.ARMOR_BROKEN.get());
        ArmorStatsCatalog.applyPreservingDamage(stack, construction);
        restoreArmorEnchantments(stack);
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.min(repairedDamage, Math.max(0, stack.getMaxDamage() - 1)));
        }
    }

    default void modularArmorInventoryTick(ItemStack stack, Level level) {
        if (!level.isClientSide) {
            refreshBrokenArmor(stack);
        }
    }

    default void disableArmorEnchantments(ItemStack stack) {
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty() && stack.get(ModDataComponents.DISABLED_ARMOR_ENCHANTMENTS.get()) == null) {
            stack.set(ModDataComponents.DISABLED_ARMOR_ENCHANTMENTS.get(), enchantments);
        }
        stack.remove(DataComponents.ENCHANTMENTS);
    }

    default void restoreArmorEnchantments(ItemStack stack) {
        ItemEnchantments enchantments = stack.get(ModDataComponents.DISABLED_ARMOR_ENCHANTMENTS.get());
        if (enchantments != null && !enchantments.isEmpty()) {
            EnchantmentHelper.setEnchantments(stack, enchantments);
        }
        stack.remove(ModDataComponents.DISABLED_ARMOR_ENCHANTMENTS.get());
    }
}

package org.destroyermob.mobstoolforging.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public interface ModularArmorItem extends IItemExtension {
    default boolean allowsFinishedArmorEnchanting(ItemStack stack, boolean fallback) {
        if (stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null && MobsToolForgingConfig.REQUIRE_ARMOR_PART_ENCHANTING.get()) {
            return false;
        }
        return fallback;
    }

    default int finishedArmorEnchantmentValue(ItemStack stack, int fallback) {
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
}

package org.destroyermob.mobstoolforging.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ExternalToolTooltipOrder {
    private ExternalToolTooltipOrder() {
    }

    public static void insertBeforeEnchantments(
            ItemStack stack,
            List<Component> tooltip,
            List<Component> toolDetails
    ) {
        if (toolDetails.isEmpty()) {
            return;
        }

        Set<String> enchantmentKeys = enchantmentTranslationKeys(stack);
        int insertionIndex = tooltip.size();
        for (int index = 1; index < tooltip.size(); index++) {
            if (containsTranslationKey(tooltip.get(index), enchantmentKeys)) {
                insertionIndex = index;
                break;
            }
        }
        tooltip.addAll(insertionIndex, toolDetails);
    }

    static boolean containsTranslationKey(Component component, Set<String> keys) {
        if (component.getContents() instanceof TranslatableContents translatable
                && keys.contains(translatable.getKey())) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (containsTranslationKey(sibling, keys)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> enchantmentTranslationKeys(ItemStack stack) {
        Set<String> keys = new HashSet<>();
        addTranslationKeys(
                stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY),
                keys
        );
        addTranslationKeys(
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY),
                keys
        );
        return keys;
    }

    private static void addTranslationKeys(ItemEnchantments enchantments, Set<String> keys) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            entry.getKey().unwrapKey()
                    .map(key -> key.location().toLanguageKey("enchantment"))
                    .ifPresent(keys::add);
            addComponentTranslationKey(entry.getKey().value().description(), keys);
        }
    }

    private static void addComponentTranslationKey(Component component, Set<String> keys) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            keys.add(translatable.getKey());
        }
        for (Component sibling : component.getSiblings()) {
            addComponentTranslationKey(sibling, keys);
        }
    }
}

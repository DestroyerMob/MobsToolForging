package org.destroyermob.mobstoolforging.world;

import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;

public final class LapidaryAbrasives {
    public static final ResourceLocation DIAMOND_TIER = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "diamond");
    private static final int GENERIC_QUALITY_BONUS = 3;
    private static final int DIAMOND_QUALITY_BONUS = 6;

    private LapidaryAbrasives() {
    }

    public static boolean isAbrasive(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ModTags.Items.LAPIDARY_ABRASIVES) || stack.is(configuredDiamondAbrasive()));
    }

    public static boolean satisfiesTier(ItemStack stack, ResourceLocation tier) {
        if (stack.isEmpty() || tier == null) {
            return false;
        }
        if (DIAMOND_TIER.equals(tier) && stack.is(configuredDiamondAbrasive())) {
            return true;
        }
        return stack.is(tierTag(tier));
    }

    public static int qualityBonus(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (satisfiesTier(stack, DIAMOND_TIER)) {
            return DIAMOND_QUALITY_BONUS;
        }
        return isAbrasive(stack) ? GENERIC_QUALITY_BONUS : 0;
    }

    public static TagKey<Item> tierTag(ResourceLocation tier) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(tier.getNamespace(), "lapidary_abrasives/" + tier.getPath()));
    }

    public static Component displayName(ResourceLocation tier) {
        if (tier.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return Component.translatable("abrasive_tier.mobstoolforging." + tier.getPath());
        }
        return Component.literal(toTitleCase(tier.getPath()));
    }

    private static Item configuredDiamondAbrasive() {
        ResourceLocation itemId = ResourceLocation.tryParse(MobsToolForgingConfig.DIAMOND_ABRASIVE_ITEM.get());
        if (itemId == null) {
            return ModItems.DIAMOND_POWDER.get();
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == Items.AIR ? ModItems.DIAMOND_POWDER.get() : item;
    }

    private static String toTitleCase(String path) {
        String cleaned = path.replace('_', ' ');
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

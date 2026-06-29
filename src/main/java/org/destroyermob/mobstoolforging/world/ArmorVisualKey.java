package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public record ArmorVisualKey(
        ResourceLocation armorType,
        ResourceLocation skullMaterial,
        Optional<ResourceLocation> combMaterial,
        Optional<ResourceLocation> visorMaterial,
        int quality,
        int damageBucket
) {
    private static final int DAMAGE_BUCKETS = 4;

    public static Optional<ArmorVisualKey> from(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
            return Optional.empty();
        }
        return Optional.of(new ArmorVisualKey(
                construction.armorType(),
                construction.skullMaterial(),
                construction.combMaterial(),
                construction.visorMaterial(),
                construction.quality(),
                damageBucket(stack)
        ));
    }

    private static int damageBucket(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 0;
        }
        return Math.min(DAMAGE_BUCKETS, stack.getDamageValue() * DAMAGE_BUCKETS / stack.getMaxDamage());
    }
}

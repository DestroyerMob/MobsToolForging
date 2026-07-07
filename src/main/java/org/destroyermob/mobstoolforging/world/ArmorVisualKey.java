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

    public boolean isChestplate() {
        return ArmorConstructionData.CHESTPLATE_TYPE.equals(armorType);
    }

    public ResourceLocation helmetChainmailMaterial() {
        return chainmailMaterial();
    }

    public Optional<ResourceLocation> helmetPlateMaterial() {
        return overlayMaterial();
    }

    public ResourceLocation chestplateChainmailMaterial() {
        return isChestplate() ? chainmailMaterial() : MaterialCatalog.IRON;
    }

    public Optional<ResourceLocation> chestplatePlateMaterial() {
        return isChestplate() ? overlayMaterial() : Optional.empty();
    }

    public ResourceLocation leggingsChainmailMaterial() {
        return chainmailMaterial();
    }

    public Optional<ResourceLocation> leggingsPlateMaterial() {
        return overlayMaterial();
    }

    public ResourceLocation bootsChainmailMaterial() {
        return chainmailMaterial();
    }

    public Optional<ResourceLocation> bootsPlateMaterial() {
        return overlayMaterial();
    }

    public ResourceLocation chainmailMaterial() {
        if (storesLegacyOverlayAsSkull()) {
            return MaterialCatalog.IRON;
        }
        return skullMaterial;
    }

    public Optional<ResourceLocation> overlayMaterial() {
        if (combMaterial.isPresent()) {
            return combMaterial;
        }
        if (storesLegacyOverlayAsSkull()) {
            return Optional.of(skullMaterial);
        }
        if (!isChestplate() && visorMaterial.isPresent()) {
            return visorMaterial;
        }
        return Optional.empty();
    }

    public boolean hasLeatherBase() {
        return MaterialCatalog.LEATHER.equals(chainmailMaterial());
    }

    private boolean storesLegacyOverlayAsSkull() {
        return combMaterial.isEmpty()
                && visorMaterial.isEmpty()
                && !MaterialCatalog.IRON.equals(skullMaterial)
                && !MaterialCatalog.LEATHER.equals(skullMaterial);
    }

    private static int damageBucket(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 0;
        }
        return Math.min(DAMAGE_BUCKETS, stack.getDamageValue() * DAMAGE_BUCKETS / stack.getMaxDamage());
    }
}

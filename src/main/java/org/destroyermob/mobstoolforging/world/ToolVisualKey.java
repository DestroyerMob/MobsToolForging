package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public record ToolVisualKey(
        ResourceLocation toolType,
        ResourceLocation headMaterial,
        ResourceLocation handleMaterial,
        Optional<ResourceLocation> bindingMaterial,
        Optional<ResourceLocation> wrapMaterial,
        Optional<ResourceLocation> focusMaterial,
        Optional<ResourceLocation> treatment,
        int quality,
        int damageBucket
) {
    private static final int DAMAGE_BUCKETS = 4;

    public static Optional<ToolVisualKey> from(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null) {
            return Optional.empty();
        }
        return Optional.of(new ToolVisualKey(
                construction.toolType(),
                construction.headMaterial(),
                construction.handleMaterial(),
                construction.bindingMaterial(),
                construction.wrapMaterial(),
                construction.focusMaterial(),
                construction.treatment(),
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

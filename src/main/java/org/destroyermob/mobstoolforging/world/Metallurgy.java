package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class Metallurgy {
    private Metallurgy() {
    }

    public static Optional<MetallurgyData> data(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.METALLURGY.get()));
    }

    public static void heat(ItemStack stack, float temperature) {
        data(stack).map(value -> value.heated(temperature)).ifPresent(value -> stack.set(ModDataComponents.METALLURGY.get(), value));
    }

    public static void quench(ItemStack stack, float temperature) {
        data(stack).map(value -> value.quenched(temperature)).ifPresent(value -> stack.set(ModDataComponents.METALLURGY.get(), value));
    }

    public static void finishCooling(ItemStack stack) {
        data(stack).map(MetallurgyData::cooled).ifPresent(value -> stack.set(ModDataComponents.METALLURGY.get(), value));
    }

    public static int adjustedQuality(ItemStack stack, int baseQuality) {
        return ForgingQuality.clampScore(baseQuality + data(stack).map(MetallurgyData::qualityAdjustment).orElse(0));
    }
}

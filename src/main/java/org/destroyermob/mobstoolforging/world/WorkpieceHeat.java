package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class WorkpieceHeat {
    private WorkpieceHeat() {
    }

    public static void heat(ItemStack stack, Level level) {
        setTemperature(stack, level, 1.0F, true);
    }

    public static void setTemperature(ItemStack stack, Level level, float temperature, boolean workable) {
        if (stack.isEmpty()) {
            return;
        }
        float currentTemperature = temperature(stack, level);
        boolean currentWorkable = data(stack).map(HeatedWorkpieceData::workable).orElse(false);
        float appliedTemperature = Math.max(currentTemperature, Math.max(0.0F, Math.min(1.0F, temperature)));
        boolean appliedWorkable = currentWorkable || workable;
        if (appliedTemperature <= 0.0F) {
            stack.remove(ModDataComponents.HEATED_WORKPIECE.get());
            return;
        }
        int coolingTicks = MobsToolForgingConfig.COOLING_TICKS.get();
        stack.set(
                ModDataComponents.HEATED_WORKPIECE.get(),
                new HeatedWorkpieceData(level.getGameTime() + Math.max(1L, Math.round(appliedTemperature * coolingTicks)), appliedTemperature, level.getGameTime(), appliedWorkable)
        );
    }

    public static boolean isHot(ItemStack stack, Level level) {
        return data(stack)
                .filter(data -> data.workable() && data.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) > 0.0F)
                .isPresent();
    }

    public static Optional<HeatedWorkpieceData> data(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.HEATED_WORKPIECE.get()));
    }

    public static long remainingTicks(ItemStack stack, Level level) {
        return data(stack)
                .map(data -> (long) Math.round(temperature(stack, level) * MobsToolForgingConfig.COOLING_TICKS.get()))
                .orElse(0L);
    }

    public static float temperature(ItemStack stack, Level level) {
        return data(stack)
                .map(data -> data.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()))
                .orElse(0.0F);
    }

    public static float storedTemperature(ItemStack stack) {
        return data(stack).map(HeatedWorkpieceData::temperature).orElse(0.0F);
    }

    public static boolean hasHeat(ItemStack stack) {
        return storedTemperature(stack) > 0.0F;
    }

    public static void clearIfCooled(ItemStack stack, Level level) {
        HeatedWorkpieceData data = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        if (data != null && data.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) <= 0.0F) {
            stack.remove(ModDataComponents.HEATED_WORKPIECE.get());
        }
    }
}

package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class WorkpieceHeat {
    public static final float WHITE_HOT_TEMPERATURE = 0.9F;

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
        Metallurgy.heat(stack, appliedTemperature);
        int coolingTicks = MobsToolForgingConfig.COOLING_TICKS.get();
        stack.set(
                ModDataComponents.HEATED_WORKPIECE.get(),
                new HeatedWorkpieceData(level.getGameTime() + Math.max(1L, Math.round(appliedTemperature * coolingTicks)), appliedTemperature, level.getGameTime(), appliedWorkable)
        );
    }

    public static ItemStack displayStack(ItemStack stack, float temperature, boolean workable) {
        ItemStack copy = stack.copy();
        if (copy.isEmpty()) {
            return copy;
        }
        float appliedTemperature = clamp(temperature);
        if (appliedTemperature <= 0.0F) {
            copy.remove(ModDataComponents.HEATED_WORKPIECE.get());
            return copy;
        }
        copy.set(
                ModDataComponents.HEATED_WORKPIECE.get(),
                new HeatedWorkpieceData(Long.MAX_VALUE, appliedTemperature, Long.MAX_VALUE, workable)
        );
        return copy;
    }

    public static boolean isHot(ItemStack stack, Level level) {
        return data(stack)
                .filter(data -> data.workable() && data.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) > 0.0F)
                .isPresent();
    }

    public static boolean isForgeReady(ItemStack stack, Level level, float minimumTemperature) {
        return data(stack)
                .filter(data -> data.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) >= clamp(minimumTemperature))
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

    /** Keeps player-facing heat readouts stable while the underlying value updates every tick. */
    public static int displayPercent(float temperature) {
        return Math.round(clamp(temperature) * 20.0F) * 5;
    }

    public static boolean isWorkable(ItemStack stack) {
        return data(stack).map(HeatedWorkpieceData::workable).orElse(false);
    }

    public static String statusKey(ItemStack stack, Level level, float minimumTemperature) {
        return statusKey(temperature(stack, level), isWorkable(stack), minimumTemperature);
    }

    public static String statusKey(float temperature, boolean workable, float minimumTemperature) {
        float minimum = clamp(minimumTemperature);
        if (temperature >= WHITE_HOT_TEMPERATURE) {
            return "white_hot";
        }
        if (temperature >= minimum) {
            return "workable";
        }
        if (workable && temperature > 0.0F) {
            return "cooling";
        }
        if (temperature >= minimum * 0.75F) {
            return "nearly_workable";
        }
        return "heating";
    }

    public static boolean hasHeat(ItemStack stack) {
        return storedTemperature(stack) > 0.0F;
    }

    public static boolean quench(ItemStack stack) {
        return quench(stack, storedTemperature(stack));
    }

    public static boolean quench(ItemStack stack, float temperature) {
        if (!hasHeat(stack)) {
            return false;
        }
        Metallurgy.quench(stack, temperature);
        stack.remove(ModDataComponents.HEATED_WORKPIECE.get());
        return true;
    }

    public static void clearIfCooled(ItemStack stack, Level level) {
        HeatedWorkpieceData data = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        if (data != null && data.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) <= 0.0F) {
            Metallurgy.finishCooling(stack);
            stack.remove(ModDataComponents.HEATED_WORKPIECE.get());
        }
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

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
        if (stack.isEmpty()) {
            return;
        }
        stack.set(
                ModDataComponents.HEATED_WORKPIECE.get(),
                new HeatedWorkpieceData(level.getGameTime() + MobsToolForgingConfig.COOLING_TICKS.get())
        );
    }

    public static boolean isHot(ItemStack stack, Level level) {
        return data(stack)
                .filter(data -> data.isHot(level.getGameTime()))
                .isPresent();
    }

    public static Optional<HeatedWorkpieceData> data(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.HEATED_WORKPIECE.get()));
    }

    public static long remainingTicks(ItemStack stack, Level level) {
        return data(stack).map(data -> data.remainingTicks(level.getGameTime())).orElse(0L);
    }

    public static void clearIfCooled(ItemStack stack, Level level) {
        HeatedWorkpieceData data = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        if (data != null && !data.isHot(level.getGameTime())) {
            stack.remove(ModDataComponents.HEATED_WORKPIECE.get());
        }
    }
}

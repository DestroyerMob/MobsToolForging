package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public final class HeatRenderUtil {
    private static final ThreadLocal<Float> FORCED_HEAT = new ThreadLocal<>();

    private HeatRenderUtil() {
    }

    public static void renderHeatedItem(ItemRenderer itemRenderer, ItemStack stack, ItemDisplayContext context, int packedLight, int packedOverlay, PoseStack poseStack, MultiBufferSource bufferSource, Level level, float heat) {
        float clamped = HeatVisuals.clamp(heat);
        HeatVisualProfile profile = HeatVisualProfileManager.INSTANCE.resolve(stack);
        int light = HeatVisuals.heatedLight(packedLight, profile, clamped);
        Float previousHeat = FORCED_HEAT.get();
        FORCED_HEAT.set(clamped);
        try {
            itemRenderer.renderStatic(stack, context, light, packedOverlay, poseStack, bufferSource, level, 0);
        } finally {
            if (previousHeat == null) {
                FORCED_HEAT.remove();
            } else {
                FORCED_HEAT.set(previousHeat);
            }
        }
    }

    public static float renderedHeat(ItemStack stack) {
        Float forcedHeat = FORCED_HEAT.get();
        if (forcedHeat != null) {
            return forcedHeat;
        }
        Level level = Minecraft.getInstance().level;
        return HeatVisuals.clamp(level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level));
    }

}

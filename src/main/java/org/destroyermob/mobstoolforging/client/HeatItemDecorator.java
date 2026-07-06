package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.IItemDecorator;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class HeatItemDecorator implements IItemDecorator {
    public static final HeatItemDecorator INSTANCE = new HeatItemDecorator();

    private HeatItemDecorator() {
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        float temperature = HeatVisuals.clamp(temperature(stack));
        if (temperature <= 0.0F) {
            return false;
        }

        int heatColor = HeatVisuals.heatColor(temperature);
        renderHeatLayer(guiGraphics, stack, xOffset, yOffset, 1.0F, heatColor, 0.18F + temperature * 0.20F, 190.0F);
        renderHeatLayer(guiGraphics, stack, xOffset, yOffset, 1.0F, HeatVisuals.lerpColor(heatColor, 0xFFFFFFFF, temperature * 0.55F), 0.10F + temperature * 0.18F, 200.0F);
        renderHeatGauge(guiGraphics, stack, xOffset, yOffset, temperature);
        return true;
    }

    private static void renderHeatGauge(GuiGraphics guiGraphics, ItemStack stack, int xOffset, int yOffset, float temperature) {
        boolean workable = WorkpieceHeat.isWorkable(stack);
        int x = xOffset + 1;
        int y = yOffset + 2;
        int frameColor = workable ? 0xFFFFE8A3 : 0xFF5D1205;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
        guiGraphics.fill(x, y - 1, x + 4, y + 12, 0xF0100300);
        guiGraphics.fill(x, y - 1, x + 4, y, frameColor);
        guiGraphics.fill(x, y + 11, x + 4, y + 12, frameColor);
        guiGraphics.fill(x, y, x + 1, y + 11, frameColor);
        guiGraphics.fill(x + 3, y, x + 4, y + 11, frameColor);
        int litSegments = Math.max(1, Math.round(temperature * 5.0F));
        for (int segment = 0; segment < 5; segment++) {
            int segmentY = y + 9 - segment * 2;
            int color = segment < litSegments ? HeatVisuals.heatColor((segment + 1.0F) / 5.0F) : 0x66340B03;
            guiGraphics.fill(x + 1, segmentY, x + 3, segmentY + 2, color);
        }
        if (temperature >= 0.98F) {
            guiGraphics.fill(x + 1, y - 2, x + 3, y - 1, 0xFFFFFFFF);
        }
        guiGraphics.pose().popPose();
    }

    private static void renderHeatLayer(GuiGraphics guiGraphics, ItemStack stack, int xOffset, int yOffset, float scale, int color, float alpha, float z) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(xOffset + 8.0F, yOffset + 8.0F, z);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-(xOffset + 8.0F), -(yOffset + 8.0F), 0.0F);
        guiGraphics.setColor((color >>> 16 & 0xFF) / 255.0F, (color >>> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
        guiGraphics.renderFakeItem(stack, xOffset, yOffset);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
    }

    private static float temperature(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        return level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
    }
}

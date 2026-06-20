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
        float temperature = temperature(stack);
        if (temperature <= 0.0F) {
            return false;
        }

        renderHeatLayer(guiGraphics, stack, xOffset, yOffset, 1.14F, 1.0F, 0.16F + temperature * 0.34F, 0.02F, 0.32F + temperature * 0.34F, 190.0F);
        renderHeatLayer(guiGraphics, stack, xOffset, yOffset, 0.94F, 1.0F, 0.47F + temperature * 0.52F, 0.14F + temperature * 0.76F, 0.48F + temperature * 0.42F, 200.0F);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 210.0F);
        int fillWidth = Math.max(1, Math.round(13.0F * temperature));
        int fillColor = WorkpieceHeat.data(stack).map(data -> data.workable() ? 0xFFFFF1C6 : 0xFFFF6A1A).orElse(0xFFFF6A1A);
        guiGraphics.fill(xOffset + 2, yOffset + 13, xOffset + 15, yOffset + 15, 0xAA230900);
        guiGraphics.fill(xOffset + 2, yOffset + 13, xOffset + 2 + fillWidth, yOffset + 15, fillColor);
        guiGraphics.pose().popPose();
        return true;
    }

    private static void renderHeatLayer(GuiGraphics guiGraphics, ItemStack stack, int xOffset, int yOffset, float scale, float red, float green, float blue, float alpha, float z) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(xOffset + 8.0F, yOffset + 8.0F, z);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-(xOffset + 8.0F), -(yOffset + 8.0F), 0.0F);
        guiGraphics.setColor(red, green, blue, alpha);
        guiGraphics.renderFakeItem(stack, xOffset, yOffset);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
    }

    private static float temperature(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        return level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
    }
}

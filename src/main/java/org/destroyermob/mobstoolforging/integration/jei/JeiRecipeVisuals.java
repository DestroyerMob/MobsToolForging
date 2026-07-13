package org.destroyermob.mobstoolforging.integration.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

final class JeiRecipeVisuals {
    private static final int DARK = 0xFF555555;
    private static final int LIGHT = 0xFFE9E9E9;
    private static final int HEAT_HOT = 0xFFFFD24A;
    private static final int TEXT = 0xFF606060;

    private JeiRecipeVisuals() {
    }

    static void drawArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y + 4, x + 13, y + 6, DARK);
        guiGraphics.fill(x + 12, y + 2, x + 15, y + 8, DARK);
        guiGraphics.fill(x + 15, y + 3, x + 17, y + 7, DARK);
        guiGraphics.fill(x + 17, y + 4, x + 19, y + 6, DARK);
    }

    static void drawClock(GuiGraphics guiGraphics, int x, int y, int ticks) {
        drawClockFace(guiGraphics, x, y);
        drawSmallText(guiGraphics, secondsText(ticks), x + 14, y + 2);
    }

    static void drawPlus(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y + 3, x + 7, y + 5, DARK);
        guiGraphics.fill(x + 3, y, x + 5, y + 8, DARK);
    }

    static void drawHitCount(GuiGraphics guiGraphics, int x, int y, int hits) {
        drawImpactMark(guiGraphics, x, y);
        drawSmallText(guiGraphics, "x" + Math.max(0, hits), x + 11, y + 1);
    }

    private static void drawClockFace(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 3, y, x + 9, y + 1, DARK);
        guiGraphics.fill(x + 1, y + 1, x + 11, y + 3, DARK);
        guiGraphics.fill(x, y + 3, x + 12, y + 8, DARK);
        guiGraphics.fill(x + 1, y + 8, x + 11, y + 10, DARK);
        guiGraphics.fill(x + 3, y + 10, x + 9, y + 11, DARK);
        guiGraphics.fill(x + 2, y + 2, x + 10, y + 8, LIGHT);
        guiGraphics.fill(x + 3, y + 8, x + 9, y + 9, LIGHT);
        guiGraphics.fill(x + 6, y + 3, x + 7, y + 7, DARK);
        guiGraphics.fill(x + 6, y + 6, x + 9, y + 7, DARK);
    }

    private static void drawImpactMark(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 4, y, x + 6, y + 3, DARK);
        guiGraphics.fill(x + 1, y + 3, x + 4, y + 5, DARK);
        guiGraphics.fill(x + 6, y + 3, x + 9, y + 5, DARK);
        guiGraphics.fill(x + 3, y + 6, x + 7, y + 8, DARK);
        guiGraphics.fill(x + 4, y + 3, x + 6, y + 6, HEAT_HOT);
    }

    private static void drawSmallText(GuiGraphics guiGraphics, String text, int x, int y) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, Component.literal(text), x, y, TEXT, false);
    }

    private static String secondsText(int ticks) {
        if (ticks <= 0) {
            return "0s";
        }
        return Math.max(1, (int) Math.ceil(ticks / 20.0D)) + "s";
    }
}

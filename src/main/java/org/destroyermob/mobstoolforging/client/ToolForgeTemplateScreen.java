package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.destroyermob.mobstoolforging.network.SetForgeTemplatePayload;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;

public class ToolForgeTemplateScreen extends Screen {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int PANEL_DARK = 0xFF000000;
    private static final int SLOT = 0xFF373737;
    private static final int TEXT = 0xFF404040;
    private static final int TEXT_ON_DARK = 0xFFFFFFFF;

    private final BlockPos forgePos;

    public ToolForgeTemplateScreen(BlockPos forgePos) {
        super(Component.translatable("screen.mobstoolforging.tool_forge_templates"));
        this.forgePos = forgePos;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Layout layout = layout();
        drawPanel(graphics, layout.left(), layout.top(), layout.width(), layout.height());
        graphics.drawCenteredString(font, title, width / 2, layout.top() + 8, TEXT);
        for (int i = 0; i < ForgeTemplate.values().length; i++) {
            ForgeTemplate template = ForgeTemplate.values()[i];
            drawButton(graphics, layout.contentLeft(), buttonTop(layout, i), layout.contentWidth(), 22, template.displayName().getString(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Layout layout = layout();
            for (int i = 0; i < ForgeTemplate.values().length; i++) {
                if (contains(mouseX, mouseY, layout.contentLeft(), buttonTop(layout, i), layout.contentWidth(), 22)) {
                    PacketDistributor.sendToServer(new SetForgeTemplatePayload(forgePos, ForgeTemplate.values()[i]));
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Layout layout() {
        int panelWidth = Math.min(width - 20, 220);
        int panelHeight = 32 + ForgeTemplate.values().length * 26;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        return new Layout(left, top, panelWidth, panelHeight, left + 12, left + panelWidth - 12);
    }

    private static int buttonTop(Layout layout, int index) {
        return layout.top() + 28 + index * 26;
    }

    private void drawButton(GuiGraphics graphics, int left, int top, int width, int height, String text, int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY, left, top, width, height);
        graphics.fill(left, top, left + width, top + height, PANEL_DARK);
        graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, hovered ? 0xFF555555 : SLOT);
        graphics.fill(left + 1, top + 1, left + width - 2, top + 2, PANEL_LIGHT);
        graphics.fill(left + 1, top + 1, left + 2, top + height - 2, PANEL_LIGHT);
        graphics.fill(left + width - 2, top + 1, left + width - 1, top + height - 1, PANEL_SHADOW);
        graphics.fill(left + 1, top + height - 2, left + width - 1, top + height - 1, PANEL_SHADOW);
        graphics.drawString(font, text, left + (width - font.width(text)) / 2, top + 7, TEXT_ON_DARK, false);
    }

    private static void drawPanel(GuiGraphics graphics, int left, int top, int width, int height) {
        graphics.fill(left - 2, top - 2, left + width + 2, top + height + 2, PANEL_DARK);
        graphics.fill(left - 1, top - 1, left + width + 1, top + height + 1, PANEL_SHADOW);
        graphics.fill(left, top, left + width, top + height, PANEL);
        graphics.fill(left, top, left + width - 1, top + 1, PANEL_LIGHT);
        graphics.fill(left, top, left + 1, top + height - 1, PANEL_LIGHT);
        graphics.fill(left + width - 1, top, left + width, top + height, PANEL_SHADOW);
        graphics.fill(left, top + height - 1, left + width, top + height, PANEL_SHADOW);
    }

    private static boolean contains(double mouseX, double mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private record Layout(int left, int top, int width, int height, int contentLeft, int contentRight) {
        int contentWidth() {
            return contentRight - contentLeft;
        }
    }
}

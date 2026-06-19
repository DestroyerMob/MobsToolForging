package org.destroyermob.mobstoolforging.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.destroyermob.mobstoolforging.network.SetForgeTemplatePayload;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public class ToolForgeTemplateScreen extends Screen {
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final int COLUMNS = 4;
    private static final int RECIPE_WIDTH = 16;
    private static final int RECIPE_HEIGHT = 18;
    private static final int PREVIEW_BOX_SIZE = 44;
    private static final int PREVIEW_TEXT_WIDTH = 66;
    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int PANEL_DARK = 0xFF000000;
    private static final int INSET = 0xFF8B8B8B;
    private static final int INSET_FILL = 0xFF4F4F4F;
    private static final int TEXT = 0xFF404040;

    private final BlockPos forgePos;
    private final WorkstationKind workstationKind;
    private int previewIndex;

    public ToolForgeTemplateScreen(BlockPos forgePos, WorkstationKind workstationKind) {
        super(Component.translatable("screen.mobstoolforging.tool_forge_templates"));
        this.forgePos = forgePos;
        this.workstationKind = workstationKind;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Layout layout = layout();
        drawPanel(graphics, layout.left(), layout.top(), layout.width(), layout.height());
        graphics.drawString(font, title, layout.left() + 8, layout.top() + 6, TEXT, false);
        drawInset(graphics, layout.recipesLeft() - 4, layout.recipesTop() - 3, COLUMNS * RECIPE_WIDTH + 8, recipeRows() * RECIPE_HEIGHT + 6);

        int hovered = hoveredIndex(layout, mouseX, mouseY);
        int shownIndex = hovered >= 0 ? hovered : previewIndex;
        ItemStack hoveredStack = ItemStack.EMPTY;
        for (int i = 0; i < ForgeTemplate.values().length; i++) {
            ForgeTemplate template = ForgeTemplate.values()[i];
            int left = recipeLeft(layout, i);
            int top = recipeTop(layout, i);
            ItemStack previewStack = previewStack(template);
            drawRecipeButton(graphics, left, top, previewStack, i == shownIndex, i == hovered);
            if (i == hovered) {
                hoveredStack = previewStack;
            }
        }

        ItemStack shownStack = previewStack(ForgeTemplate.values()[shownIndex]);
        drawPreview(graphics, layout.previewLeft(), layout.previewTop(), shownStack);
        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Layout layout = layout();
            for (int i = 0; i < ForgeTemplate.values().length; i++) {
                if (contains(mouseX, mouseY, recipeLeft(layout, i), recipeTop(layout, i), RECIPE_WIDTH, RECIPE_HEIGHT)) {
                    previewIndex = i;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
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
        int panelWidth = Math.min(width - 20, 176);
        int panelHeight = 96;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int recipesLeft = left + 17;
        int recipesTop = top + 26;
        int previewLeft = left + 108;
        int previewTop = top + 24;
        return new Layout(left, top, panelWidth, panelHeight, recipesLeft, recipesTop, previewLeft, previewTop);
    }

    private static int recipeRows() {
        return (ForgeTemplate.values().length + COLUMNS - 1) / COLUMNS;
    }

    private static int recipeLeft(Layout layout, int index) {
        return layout.recipesLeft() + (index % COLUMNS) * RECIPE_WIDTH;
    }

    private static int recipeTop(Layout layout, int index) {
        return layout.recipesTop() + (index / COLUMNS) * RECIPE_HEIGHT;
    }

    private static int hoveredIndex(Layout layout, int mouseX, int mouseY) {
        for (int i = 0; i < ForgeTemplate.values().length; i++) {
            if (contains(mouseX, mouseY, recipeLeft(layout, i), recipeTop(layout, i), RECIPE_WIDTH, RECIPE_HEIGHT)) {
                return i;
            }
        }
        return -1;
    }

    private ItemStack previewStack(ForgeTemplate template) {
        return template.outputStack(previewMaterial());
    }

    private ResourceLocation previewMaterial() {
        return workstationKind == WorkstationKind.LAPIDARY_TABLE ? MaterialCatalog.DIAMOND : MaterialCatalog.IRON;
    }

    private static void drawRecipeButton(GuiGraphics graphics, int left, int top, ItemStack stack, boolean selected, boolean hovered) {
        ResourceLocation sprite = selected ? RECIPE_SELECTED_SPRITE : hovered ? RECIPE_HIGHLIGHTED_SPRITE : RECIPE_SPRITE;
        graphics.blitSprite(sprite, left, top, RECIPE_WIDTH, RECIPE_HEIGHT);
        graphics.renderItem(stack, left, top + 1);
    }

    private void drawPreview(GuiGraphics graphics, int left, int top, ItemStack stack) {
        drawInset(graphics, left, top, PREVIEW_BOX_SIZE, PREVIEW_BOX_SIZE);
        graphics.pose().pushPose();
        graphics.pose().translate(left + 10, top + 10, 0.0F);
        graphics.pose().scale(1.5F, 1.5F, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();

        List<FormattedCharSequence> lines = font.split(stack.getHoverName(), PREVIEW_TEXT_WIDTH);
        int textLeft = left + PREVIEW_BOX_SIZE / 2;
        int textTop = top + PREVIEW_BOX_SIZE + 8;
        int lineCount = Math.min(lines.size(), 2);
        for (int i = 0; i < lineCount; i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.drawString(font, line, textLeft - font.width(line) / 2, textTop + i * 10, TEXT, false);
        }
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

    private static void drawInset(GuiGraphics graphics, int left, int top, int width, int height) {
        graphics.fill(left, top, left + width, top + height, PANEL_SHADOW);
        graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, INSET);
        graphics.fill(left + 2, top + 2, left + width - 2, top + height - 2, INSET_FILL);
        graphics.fill(left + 1, top + 1, left + width - 2, top + 2, PANEL_DARK);
        graphics.fill(left + 1, top + 1, left + 2, top + height - 2, PANEL_DARK);
        graphics.fill(left + width - 2, top + 1, left + width - 1, top + height - 1, PANEL_LIGHT);
        graphics.fill(left + 1, top + height - 2, left + width - 1, top + height - 1, PANEL_LIGHT);
    }

    private static boolean contains(double mouseX, double mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private record Layout(int left, int top, int width, int height, int recipesLeft, int recipesTop, int previewLeft, int previewTop) {
    }
}

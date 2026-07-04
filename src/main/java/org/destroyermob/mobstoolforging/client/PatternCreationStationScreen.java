package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.PatternCreationStationMenu;

public class PatternCreationStationScreen extends AbstractContainerScreen<PatternCreationStationMenu> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/stonecutter.png");
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int RECIPES_COLUMNS = 4;
    private static final int RECIPES_ROWS = 3;
    private static final int RECIPE_WIDTH = 16;
    private static final int RECIPE_HEIGHT = 18;
    private static final int SCROLLER_FULL_HEIGHT = 54;
    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 14;
    private static final int SCROLLER_X = 119;
    private static final int SCROLLER_Y = 15;
    private static final int VISIBLE_RECIPES = RECIPES_COLUMNS * RECIPES_ROWS;

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayPatterns;

    public PatternCreationStationScreen(PatternCreationStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        menu.registerUpdateListener(this::containerChanged);
        this.titleLabelY--;
        containerChanged();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int scrollerOffset = (int)(41.0F * scrollOffs);
        ResourceLocation scrollerSprite = isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        graphics.blitSprite(scrollerSprite, leftPos + SCROLLER_X, topPos + SCROLLER_Y + scrollerOffset, SCROLLER_WIDTH, SCROLLER_HEIGHT);

        if (!displayPatterns) {
            return;
        }

        int recipesLeft = leftPos + RECIPES_X;
        int recipesTop = topPos + RECIPES_Y;
        int lastVisibleIndex = startIndex + VISIBLE_RECIPES;
        renderButtons(graphics, mouseX, mouseY, recipesLeft, recipesTop, lastVisibleIndex);
        renderRecipes(graphics, recipesLeft, recipesTop, lastVisibleIndex);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);
        if (!displayPatterns) {
            return;
        }

        int recipesLeft = leftPos + RECIPES_X;
        int recipesTop = topPos + RECIPES_Y;
        int lastVisibleIndex = startIndex + VISIBLE_RECIPES;
        for (int index = startIndex; index < lastVisibleIndex && index < menu.getNumPatterns(); index++) {
            int relativeIndex = index - startIndex;
            int buttonLeft = recipesLeft + relativeIndex % RECIPES_COLUMNS * RECIPE_WIDTH;
            int buttonTop = recipesTop + relativeIndex / RECIPES_COLUMNS * RECIPE_HEIGHT + 2;
            if (x >= buttonLeft && x < buttonLeft + RECIPE_WIDTH && y >= buttonTop && y < buttonTop + RECIPE_HEIGHT) {
                ItemStack stack = menu.getPatternDisplayStack(index);
                if (!stack.isEmpty()) {
                    graphics.renderTooltip(font, stack, x, y);
                }
            }
        }
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int lastVisibleIndex) {
        for (int index = startIndex; index < lastVisibleIndex && index < menu.getNumPatterns(); index++) {
            int relativeIndex = index - startIndex;
            int buttonLeft = x + relativeIndex % RECIPES_COLUMNS * RECIPE_WIDTH;
            int row = relativeIndex / RECIPES_COLUMNS;
            int buttonTop = y + row * RECIPE_HEIGHT + 2;
            ResourceLocation sprite;
            if (index == menu.getSelectedPatternIndex()) {
                sprite = RECIPE_SELECTED_SPRITE;
            } else if (mouseX >= buttonLeft && mouseY >= buttonTop && mouseX < buttonLeft + RECIPE_WIDTH && mouseY < buttonTop + RECIPE_HEIGHT) {
                sprite = RECIPE_HIGHLIGHTED_SPRITE;
            } else {
                sprite = RECIPE_SPRITE;
            }
            graphics.blitSprite(sprite, buttonLeft, buttonTop - 1, RECIPE_WIDTH, RECIPE_HEIGHT);
        }
    }

    private void renderRecipes(GuiGraphics graphics, int x, int y, int lastVisibleIndex) {
        for (int index = startIndex; index < lastVisibleIndex && index < menu.getNumPatterns(); index++) {
            int relativeIndex = index - startIndex;
            int buttonLeft = x + relativeIndex % RECIPES_COLUMNS * RECIPE_WIDTH;
            int row = relativeIndex / RECIPES_COLUMNS;
            int buttonTop = y + row * RECIPE_HEIGHT + 2;
            graphics.renderItem(menu.getPatternDisplayStack(index), buttonLeft, buttonTop);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        scrolling = false;
        if (displayPatterns) {
            int recipesLeft = leftPos + RECIPES_X;
            int recipesTop = topPos + RECIPES_Y;
            int lastVisibleIndex = startIndex + VISIBLE_RECIPES;
            for (int index = startIndex; index < lastVisibleIndex && index < menu.getNumPatterns(); index++) {
                int relativeIndex = index - startIndex;
                double relativeMouseX = mouseX - (double)(recipesLeft + relativeIndex % RECIPES_COLUMNS * RECIPE_WIDTH);
                double relativeMouseY = mouseY - (double)(recipesTop + relativeIndex / RECIPES_COLUMNS * RECIPE_HEIGHT);
                if (relativeMouseX >= 0.0 && relativeMouseY >= 0.0 && relativeMouseX < RECIPE_WIDTH && relativeMouseY < RECIPE_HEIGHT && menu.clickMenuButton(minecraft.player, index)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                    return true;
                }
            }

            int scrollerLeft = leftPos + SCROLLER_X;
            int scrollerTop = topPos + SCROLLER_Y - 6;
            if (mouseX >= (double)scrollerLeft && mouseX < (double)(scrollerLeft + SCROLLER_WIDTH) && mouseY >= (double)scrollerTop && mouseY < (double)(scrollerTop + SCROLLER_FULL_HEIGHT)) {
                scrolling = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling && isScrollBarActive()) {
            int top = topPos + RECIPES_Y;
            int bottom = top + SCROLLER_FULL_HEIGHT;
            scrollOffs = ((float)mouseY - (float)top - 7.5F) / ((float)(bottom - top) - SCROLLER_HEIGHT);
            scrollOffs = Mth.clamp(scrollOffs, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffs * (float)getOffscreenRows()) + 0.5) * RECIPES_COLUMNS;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isScrollBarActive()) {
            int offscreenRows = getOffscreenRows();
            float scrollAmount = (float)scrollY / (float)offscreenRows;
            scrollOffs = Mth.clamp(scrollOffs - scrollAmount, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffs * (float)offscreenRows) + 0.5) * RECIPES_COLUMNS;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isScrollBarActive() {
        return displayPatterns && menu.getNumPatterns() > VISIBLE_RECIPES;
    }

    private int getOffscreenRows() {
        return (menu.getNumPatterns() + RECIPES_COLUMNS - 1) / RECIPES_COLUMNS - RECIPES_ROWS;
    }

    private void containerChanged() {
        displayPatterns = menu.hasInputItem();
        if (!displayPatterns) {
            scrollOffs = 0.0F;
            startIndex = 0;
        }
    }
}

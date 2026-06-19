package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.destroyermob.mobstoolforging.network.SetForgeTemplatePayload;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public class ToolForgeTemplateScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/stonecutter.png");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 83;
    private static final int COLUMNS = 4;
    private static final int RECIPE_WIDTH = 16;
    private static final int RECIPE_HEIGHT = 18;
    private static final int RECIPE_AREA_LEFT = 52;
    private static final int RECIPE_AREA_TOP = 14;
    private static final int INPUT_LEFT = 20;
    private static final int INPUT_TOP = 33;
    private static final int OUTPUT_LEFT = 143;
    private static final int OUTPUT_TOP = 33;
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
        graphics.blit(BACKGROUND, layout.left(), layout.top(), 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        graphics.drawString(font, title, layout.left() + 8, layout.top() + 6, TEXT, false);
        graphics.renderItem(MaterialCatalog.displayStack(previewMaterial()), layout.left() + INPUT_LEFT, layout.top() + INPUT_TOP);

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
        graphics.renderItem(shownStack, layout.left() + OUTPUT_LEFT, layout.top() + OUTPUT_TOP);

        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredStack, mouseX, mouseY);
        } else if (contains(mouseX, mouseY, layout.left() + INPUT_LEFT, layout.top() + INPUT_TOP, 16, 16)) {
            graphics.renderTooltip(font, MaterialCatalog.displayStack(previewMaterial()), mouseX, mouseY);
        } else if (contains(mouseX, mouseY, layout.left() + OUTPUT_LEFT, layout.top() + OUTPUT_TOP, 16, 16)) {
            graphics.renderTooltip(font, shownStack, mouseX, mouseY);
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
        int left = (width - IMAGE_WIDTH) / 2;
        int top = (height - IMAGE_HEIGHT) / 2;
        return new Layout(left, top);
    }

    private static int recipeLeft(Layout layout, int index) {
        return layout.left() + RECIPE_AREA_LEFT + (index % COLUMNS) * RECIPE_WIDTH;
    }

    private static int recipeTop(Layout layout, int index) {
        return layout.top() + RECIPE_AREA_TOP + (index / COLUMNS) * RECIPE_HEIGHT + 1;
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

    private static boolean contains(double mouseX, double mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private record Layout(int left, int top) {
    }
}

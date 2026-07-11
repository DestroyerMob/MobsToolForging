package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.client.model.ToolVisualDefinition;
import org.destroyermob.mobstoolforging.client.model.ToolVisualManager;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlockEntity;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.joml.Vector3f;

import java.util.Optional;

public class PatternRackRenderer implements BlockEntityRenderer<PatternRackBlockEntity> {
    private static final float ITEM_SCALE = 0.275F;
    private static final float ITEM_SURFACE_Z = 0.328F;
    private static final float[] SLOT_X = {-0.3125F, 0.0F, 0.3125F};
    private static final float[] SLOT_Y = {0.8125F, 0.5F, 0.1875F};

    private final ItemRenderer itemRenderer;

    public PatternRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PatternRackBlockEntity rack, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = rack.getBlockState();
        if (!state.hasProperty(PatternRackBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(PatternRackBlock.FACING);
        Level level = rack.getLevel();
        for (int slot = 0; slot < PatternRackBlockEntity.SLOT_COUNT; slot++) {
            ItemStack pattern = rack.patternStack(slot);
            if (pattern.isEmpty()) {
                continue;
            }
            int row = slot / 3;
            int column = slot % 3;
            renderPattern(pattern, poseStack, bufferSource, packedLight, packedOverlay, level, facing, SLOT_X[column], SLOT_Y[row], ITEM_SCALE);
        }
    }

    private void renderPattern(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Level level, Direction facing, float x, float y, float scale) {
        float modelX = 0.5F + x;
        float modelZ = 0.5F + ITEM_SURFACE_Z;
        float worldX = worldX(facing, modelX, modelZ);
        float worldZ = worldZ(facing, modelX, modelZ);

        poseStack.pushPose();
        poseStack.translate(worldX, y, worldZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(modelRotation(facing)));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        maskSprite(stack).ifPresentOrElse(
                sprite -> renderStencil(poseStack, bufferSource, sprite, packedLight, packedOverlay),
                () -> itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, level, 0)
        );
        poseStack.popPose();
    }

    /**
     * Pattern items have an item model which is deliberately a full board with a cut-out.
     * That model is appropriate in an inventory, but rendering it through a block entity
     * creates an opaque, material-coloured square with some resource packs and shaders.
     * On a rack we only need the part outline, so draw its alpha-masked template directly.
     */
    private Optional<TextureAtlasSprite> maskSprite(ItemStack stack) {
        if (!(stack.getItem() instanceof ToolTemplateItem pattern)) {
            return Optional.empty();
        }
        return pattern.templateId(stack)
                .flatMap(ToolTypeRegistry::template)
                .flatMap(this::maskTexture)
                .map(texture -> Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture))
                .filter(sprite -> !MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name()));
    }

    private Optional<ResourceLocation> maskTexture(ForgeTemplateDefinition template) {
        return ToolTypeRegistry.toolType(template.toolType())
                .flatMap(definition -> maskTexture(definition, template.partType()));
    }

    private Optional<ResourceLocation> maskTexture(ToolTypeDefinition definition, String partType) {
        ToolVisualDefinition visual = ToolVisualManager.resolve(definition.visualId(), definition);
        return visual.layerForSlot(partVisualSlot(partType)).templateId(true)
                .or(() -> visual.layerForSlot(partVisualSlot(partType)).templateId(false));
    }

    private static String partVisualSlot(String partType) {
        return ToolPartData.SWORD_GUARD.equals(partType) ? "guard" : partType;
    }

    private static void renderStencil(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite, int packedLight, int packedOverlay) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        float u1 = sprite.getU(1.0F);
        float v1 = sprite.getV(1.0F);
        Vector3f normal = new Vector3f(0.0F, 0.0F, -1.0F);
        vertex(poseStack, consumer, normal, packedLight, packedOverlay, -0.5F, -0.5F, 0.01F, u0, v1);
        vertex(poseStack, consumer, normal, packedLight, packedOverlay, -0.5F, 0.5F, 0.01F, u0, v0);
        vertex(poseStack, consumer, normal, packedLight, packedOverlay, 0.5F, 0.5F, 0.01F, u1, v0);
        vertex(poseStack, consumer, normal, packedLight, packedOverlay, 0.5F, -0.5F, 0.01F, u1, v1);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int packedLight, int packedOverlay, float x, float y, float z, float u, float v) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }

    private static float worldX(Direction facing, float modelX, float modelZ) {
        return switch (facing) {
            case EAST -> 1.0F - modelZ;
            case SOUTH -> 1.0F - modelX;
            case WEST -> modelZ;
            case NORTH, UP, DOWN -> modelX;
        };
    }

    private static float worldZ(Direction facing, float modelX, float modelZ) {
        return switch (facing) {
            case EAST -> modelX;
            case SOUTH -> 1.0F - modelZ;
            case WEST -> 1.0F - modelX;
            case NORTH, UP, DOWN -> modelZ;
        };
    }

    private static float modelRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            case NORTH, UP, DOWN -> 0.0F;
        };
    }

}

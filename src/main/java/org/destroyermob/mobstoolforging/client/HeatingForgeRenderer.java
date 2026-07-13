package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.LavaHeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;
import org.joml.Vector3f;

public class HeatingForgeRenderer implements BlockEntityRenderer<HeatingForgeBlockEntity> {
    private static final Placement[] FUEL_PLACEMENTS = {
            new Placement(-0.1875F, -0.1875F, 1.0F, 0.75F, 0.0F),
            new Placement(0.1875F, -0.1875F, 1.0F, 0.75F, 0.0F),
            new Placement(0.1875F, 0.1875F, 1.0F, 0.75F, 0.0F),
            new Placement(-0.1875F, 0.1875F, 1.0F, 0.75F, 0.0F)
    };
    private static final Placement[] WORKPIECE_PLACEMENTS = {
            new Placement(-0.21875F, -0.15625F, 1.0F, 0.875F, 90.0F),
            new Placement(-0.15625F, 0.21875F, 1.0F, 0.875F, 0.0F),
            new Placement(0.15625F, -0.21875F, 1.0F, 0.875F, 0.0F),
            new Placement(0.21875F, 0.15625F, 1.0F, 0.875F, 90.0F)
    };
    private static final float FLUID_MIN = 2.01F / 16.0F;
    private static final float FLUID_MAX = 12.98F / 16.0F;
    private static final float FLUID_SIDE_MIN = 2.01F / 16.0F;
    private static final float FLUID_SIDE_MAX = 13.99F / 16.0F;

    public HeatingForgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (forge instanceof LavaHeatingForgeBlockEntity fluidForge) {
            renderFluidTank(fluidForge, poseStack, bufferSource, packedLight, packedOverlay);
        } else {
            renderAsh(forge, poseStack, bufferSource, packedLight, packedOverlay);
            renderFuel(forge, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        for (int slot = 0; slot < forge.workpieceSlots(); slot++) {
            ItemStack workpiece = forge.workpieceStack(slot);
            if (workpiece.isEmpty()) {
                continue;
            }
            Placement placement = WORKPIECE_PLACEMENTS[Math.min(slot, WORKPIECE_PLACEMENTS.length - 1)];
            float heat = heatAmount(forge, slot, workpiece);
            HeatVisualProfile profile = HeatVisualProfileManager.INSTANCE.resolve(workpiece);
            renderInsert(forge, HeatingForgeInsertVisualManager.workpiece(workpiece), poseStack, bufferSource, packedLight, packedOverlay, placement, heat, profile, true);
        }
    }

    private void renderFluidTank(LavaHeatingForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        FluidStack fluid = forge.fluidStack();
        if (fluid.isEmpty()) {
            return;
        }
        float fill = clamp(forge.fluidFillFraction(), 0.0F, 1.0F);
        float maxY = FLUID_MIN + (FLUID_MAX - FLUID_MIN) * fill;
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation texture = extensions.getStillTexture(fluid);
        if (texture == null) {
            return;
        }
        int tint = extensions.getTintColor(fluid);
        int alpha = tint >>> 24 & 0xFF;
        if (alpha == 0) {
            alpha = 0xFF;
        }
        int color = alpha << 24 | tint & 0x00FFFFFF;
        int fluidLight = fluid.getFluidType().getLightLevel(fluid);
        int light = LightTexture.pack(Math.max(LightTexture.block(packedLight), fluidLight), LightTexture.sky(packedLight));
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        renderFluidCuboid(poseStack, consumer, sprite, color, light, packedOverlay, FLUID_SIDE_MIN, FLUID_MIN, FLUID_SIDE_MIN, FLUID_SIDE_MAX, maxY, FLUID_SIDE_MAX);
    }

    private static void renderFluidCuboid(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite, int color, int light, int overlay, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        float widthPixels = (maxX - minX) * 16.0F;
        float depthPixels = (maxZ - minZ) * 16.0F;
        float heightPixels = (maxY - minY) * 16.0F;
        float u0 = sprite.getU(0.0F);
        float v0 = sprite.getV(0.0F);
        // TextureAtlasSprite coordinates are normalized in 1.21.1. Passing the
        // old 0-16 pixel values walks across neighbouring sprites in the atlas.
        float uWidth = sprite.getU(widthPixels / 16.0F);
        float uDepth = sprite.getU(depthPixels / 16.0F);
        float vDepth = sprite.getV(depthPixels / 16.0F);
        float vHeight = sprite.getV(heightPixels / 16.0F);

        quad(poseStack, consumer, Direction.UP.step(), color, light, overlay,
                minX, maxY, minZ, u0, vDepth,
                minX, maxY, maxZ, u0, v0,
                maxX, maxY, maxZ, uWidth, v0,
                maxX, maxY, minZ, uWidth, vDepth);
        quad(poseStack, consumer, Direction.NORTH.step(), color, light, overlay,
                minX, minY, minZ, u0, vHeight,
                maxX, minY, minZ, uWidth, vHeight,
                maxX, maxY, minZ, uWidth, v0,
                minX, maxY, minZ, u0, v0);
        quad(poseStack, consumer, Direction.SOUTH.step(), color, light, overlay,
                maxX, minY, maxZ, u0, vHeight,
                minX, minY, maxZ, uWidth, vHeight,
                minX, maxY, maxZ, uWidth, v0,
                maxX, maxY, maxZ, u0, v0);
        quad(poseStack, consumer, Direction.WEST.step(), color, light, overlay,
                minX, minY, maxZ, u0, vHeight,
                minX, minY, minZ, uDepth, vHeight,
                minX, maxY, minZ, uDepth, v0,
                minX, maxY, maxZ, u0, v0);
        quad(poseStack, consumer, Direction.EAST.step(), color, light, overlay,
                maxX, minY, minZ, u0, vHeight,
                maxX, minY, maxZ, uDepth, vHeight,
                maxX, maxY, maxZ, uDepth, v0,
                maxX, maxY, minZ, u0, v0);
    }

    private static void quad(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int color, int light, int overlay,
                             float x0, float y0, float z0, float u0, float v0,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3) {
        vertex(poseStack, consumer, normal, color, light, overlay, x0, y0, z0, u0, v0);
        vertex(poseStack, consumer, normal, color, light, overlay, x1, y1, z1, u1, v1);
        vertex(poseStack, consumer, normal, color, light, overlay, x2, y2, z2, u2, v2);
        vertex(poseStack, consumer, normal, color, light, overlay, x3, y3, z3, u3, v3);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, Vector3f normal, int color, int light, int overlay, float x, float y, float z, float u, float v) {
        consumer.addVertex(poseStack.last(), x, y, z)
                .setColor(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, color >>> 24 & 0xFF)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
    }

    private void renderAsh(HeatingForgeBlockEntity forge, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        HeatingForgeInsertVisualManager.ResolvedInsert ash = HeatingForgeInsertVisualManager.ash();
        for (int layer = 0; layer < forge.ashLayers(); layer++) {
            renderInsert(forge, ash, poseStack, bufferSource, packedLight, packedOverlay, new Placement(0.0F, 0.0F, 1.0F, (2.0F + layer) / 16.0F, 0.0F), 0.0F, HeatVisualProfile.GENERIC, false);
        }
    }

    private void renderFuel(HeatingForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack fuel = forge.fuelStack();
        int visibleFuel = Math.min(FUEL_PLACEMENTS.length, forge.fuelBedCount());
        if (visibleFuel <= 0) {
            return;
        }
        HeatingForgeInsertVisualManager.ResolvedInsert visual = forge.hasSpentFuelBed()
                ? HeatingForgeInsertVisualManager.spentFuel()
                : HeatingForgeInsertVisualManager.fuel(fuel.isEmpty() ? new ItemStack(Items.COAL) : fuel);
        for (int index = 0; index < visibleFuel; index++) {
            renderInsert(forge, visual, poseStack, bufferSource, packedLight, packedOverlay, FUEL_PLACEMENTS[index], forge.hasSpentFuelBed() ? 0.0F : fuelHeat(forge, partialTick, index), HeatVisualProfile.GENERIC, false);
        }
    }

    private void renderInsert(HeatingForgeBlockEntity forge, HeatingForgeInsertVisualManager.ResolvedInsert visual, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Placement placement, float heat, HeatVisualProfile profile, boolean shimmer) {
        poseStack.pushPose();
        poseStack.translate(0.5F, placement.surfaceY(), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation(forge.getBlockState().getValue(HeatingForgeBlock.FACING))));
        poseStack.translate(placement.localX(), 0.0F, placement.localZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(placement.rotation()));
        poseStack.scale(placement.scale(), placement.scale(), placement.scale());
        HeatingForgeVoxelRenderer.render(visual.model(), visual.visual(), poseStack, bufferSource, packedLight, packedOverlay, heat, profile);
        if (shimmer) {
            HeatingForgeVoxelRenderer.renderShimmer(poseStack, bufferSource, heat, profile);
        }
        poseStack.popPose();
    }

    private static float facingRotation(Direction direction) {
        // The authored forge model opens west before blockstate rotation.
        return switch (direction) {
            case EAST -> 180.0F;
            case SOUTH -> 270.0F;
            case WEST -> 0.0F;
            default -> 90.0F;
        };
    }

    private static float heatAmount(HeatingForgeBlockEntity forge, int slot, ItemStack stack) {
        Level level = forge.getLevel();
        float stackTemperature = level == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, level);
        return Math.max(forge.heatProgressFraction(slot), stackTemperature);
    }

    private static float fuelHeat(HeatingForgeBlockEntity forge, float partialTick, int index) {
        if (!forge.isLit()) {
            return 0.0F;
        }
        Level level = forge.getLevel();
        float time = level == null ? 0.0F : level.getGameTime() + partialTick;
        float pulse = ((float) Math.sin(time * 0.18F + index * 1.7F) + 1.0F) * 0.5F;
        float fuelTemperature = forge.fuelTemperatureFraction();
        float workpieceBonus = forge.hasWorkpiece() ? forge.heatProgressFraction() * 0.08F : 0.0F;
        return clamp(0.24F + fuelTemperature * 0.42F + pulse * 0.12F + workpieceBonus, 0.0F, 0.84F);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Placement(float localX, float localZ, float scale, float surfaceY, float rotation) {
    }
}

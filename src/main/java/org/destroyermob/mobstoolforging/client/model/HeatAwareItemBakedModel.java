package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.destroyermob.mobstoolforging.client.HeatRenderUtil;
import org.destroyermob.mobstoolforging.client.HeatRenderTypes;
import org.destroyermob.mobstoolforging.client.HeatVisualProfile;
import org.destroyermob.mobstoolforging.client.HeatVisualProfileManager;
import org.destroyermob.mobstoolforging.client.HeatVisuals;

/**
 * Resolves heat as model render passes, so vanilla, NeoForge, Fabric Renderer API,
 * Sodium, GUI, hand, and entity item renderers all consume the same heated quads.
 */
public final class HeatAwareItemBakedModel extends ForwardingBakedModel {
    private final ItemOverrides overrides;

    public HeatAwareItemBakedModel(BakedModel delegate) {
        super(delegate);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                BakedModel resolved = delegate.getOverrides().resolve(delegate, stack, level, entity, seed);
                if (resolved == null) {
                    resolved = delegate;
                }
                float heat = HeatRenderUtil.renderedHeat(stack);
                HeatVisualProfile profile = HeatVisualProfileManager.INSTANCE.resolve(stack);
                if (heat < profile.visibleThreshold() || resolved.isCustomRenderer()) {
                    return resolved;
                }
                return new HeatedRootModel(resolved, heat, profile);
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private static final class HeatedRootModel extends ForwardingBakedModel {
        private final float heat;
        private final HeatVisualProfile profile;

        private HeatedRootModel(BakedModel delegate, float heat, HeatVisualProfile profile) {
            super(delegate);
            this.heat = HeatVisuals.clamp(heat);
            this.profile = profile;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            List<BakedModel> sourcePasses = delegate.getRenderPasses(stack, fabulous);
            List<BakedModel> heatedPasses = new ArrayList<>(sourcePasses.size() * 3);
            if (HeatVisuals.itemHaloAlpha(profile, heat) > 0.01F) {
                for (BakedModel pass : sourcePasses) {
                    heatedPasses.add(new HeatedPassModel(pass, heat, profile, HeatPass.HALO));
                }
            }
            for (BakedModel pass : sourcePasses) {
                heatedPasses.add(new HeatedPassModel(pass, heat, profile, HeatPass.BASE));
            }
            if (HeatVisuals.overlayAlpha(profile, heat) > 0.01F) {
                for (BakedModel pass : sourcePasses) {
                    heatedPasses.add(new HeatedPassModel(pass, heat, profile, HeatPass.MASK));
                }
            }
            return heatedPasses;
        }
    }

    private enum HeatPass {
        HALO,
        BASE,
        MASK
    }

    private static final class HeatedPassModel extends ForwardingBakedModel {
        private final int tint;
        private final float alpha;
        private final float scale;
        private final HeatPass pass;

        private HeatedPassModel(BakedModel delegate, float heat, HeatVisualProfile profile, HeatPass pass) {
            super(delegate);
            this.pass = pass;
            this.tint = switch (pass) {
                case BASE -> HeatVisuals.surfaceTint(profile, heat);
                case HALO -> HeatVisuals.heatColor(profile, Math.min(0.78F, Math.max(0.62F, heat)));
                case MASK -> HeatVisuals.heatColor(profile, heat);
            };
            this.alpha = switch (pass) {
                case HALO -> HeatVisuals.itemHaloAlpha(profile, heat);
                case BASE -> 1.0F;
                case MASK -> HeatVisuals.overlayAlpha(profile, heat);
            };
            this.scale = pass == HeatPass.HALO ? HeatVisuals.itemHaloScale(profile, heat) : 1.0F;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return tint(delegate.getQuads(state, direction, random));
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random, ModelData data, @Nullable RenderType renderType) {
            return tint(delegate.getQuads(state, direction, random, data, renderType));
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
            return switch (pass) {
                case HALO -> List.of(RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS, false));
                case BASE -> delegate.getRenderTypes(stack, fabulous);
                case MASK -> List.of(HeatRenderTypes.heatMask());
            };
        }

        private List<BakedQuad> tint(List<BakedQuad> quads) {
            if (quads.isEmpty()) {
                return quads;
            }
            List<BakedQuad> tinted = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                tinted.add(tint(quad));
            }
            return tinted;
        }

        private BakedQuad tint(BakedQuad quad) {
            int[] vertices = quad.getVertices().clone();
            int red = tint >>> 16 & 0xFF;
            int green = tint >>> 8 & 0xFF;
            int blue = tint & 0xFF;
            int vertexStride = vertices.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int positionIndex = vertex * vertexStride;
                if (pass == HeatPass.HALO && positionIndex + 2 < vertices.length) {
                    for (int axis = 0; axis < 3; axis++) {
                        float position = Float.intBitsToFloat(vertices[positionIndex + axis]);
                        vertices[positionIndex + axis] = Float.floatToRawIntBits(0.5F + (position - 0.5F) * scale);
                    }
                }
                int colorIndex = vertex * vertexStride + 3;
                if (colorIndex >= vertices.length) {
                    break;
                }
                int color = vertices[colorIndex];
                vertices[colorIndex] = FastColor.ABGR32.color(
                        Math.round(FastColor.ABGR32.alpha(color) * alpha),
                        FastColor.ABGR32.blue(color) * blue / 0xFF,
                        FastColor.ABGR32.green(color) * green / 0xFF,
                        FastColor.ABGR32.red(color) * red / 0xFF
                );
            }
            return new BakedQuad(
                    vertices,
                    pass == HeatPass.BASE ? quad.getTintIndex() : -1,
                    quad.getDirection(),
                    quad.getSprite(),
                    pass == HeatPass.BASE && quad.isShade(),
                    pass == HeatPass.BASE && quad.hasAmbientOcclusion()
            );
        }
    }

}

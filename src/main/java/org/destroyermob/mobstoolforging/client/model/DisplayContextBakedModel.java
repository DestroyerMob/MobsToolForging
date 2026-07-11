package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

/** Selects the large model only while an item is actually held. */
public final class DisplayContextBakedModel implements BakedModel {
    private final BakedModel compact;
    private final BakedModel inHand;

    public DisplayContextBakedModel(BakedModel compact, BakedModel inHand) {
        this.compact = compact;
        this.inHand = inHand;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
        BakedModel selected = isHandContext(context) ? inHand : compact;
        return selected.applyTransform(context, poseStack, leftHand);
    }

    private static boolean isHandContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return compact.getQuads(state, direction, random);
    }

    @Override public boolean useAmbientOcclusion() { return compact.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return compact.isGui3d(); }
    @Override public boolean usesBlockLight() { return compact.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return compact.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return compact.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return compact.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}

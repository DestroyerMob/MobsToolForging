package org.destroyermob.mobstoolforging.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
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

public final class ResolvedPartedItemModel implements BakedModel {
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ItemTransforms transforms;
    @Nullable
    private final BakedModel transformSource;

    public ResolvedPartedItemModel(List<BakedQuad> quads, TextureAtlasSprite particle, ItemTransforms transforms) {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
        this.transformSource = null;
    }

    public ResolvedPartedItemModel(List<BakedQuad> quads, TextureAtlasSprite particle, BakedModel transformSource) {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transformSource.getTransforms();
        this.transformSource = transformSource;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return direction == null ? quads : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
        if (transformSource != null) {
            transformSource.applyTransform(context, poseStack, leftHand);
        } else {
            transforms.getTransform(context).apply(leftHand, poseStack);
        }
        return this;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public List<net.minecraft.client.renderer.RenderType> getRenderTypes(net.minecraft.world.item.ItemStack itemStack, boolean fabulous) {
        return List.of(net.minecraft.client.renderer.RenderType.entityCutout(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
    }

    public static ResolvedPartedItemModel compose(Map<Integer, List<BakedQuad>> layers, TextureAtlasSprite particle, ItemTransforms transforms) {
        List<BakedQuad> quads = layers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(entry -> entry.getValue().stream())
                .toList();
        return new ResolvedPartedItemModel(quads, particle, transforms);
    }

    public static ResolvedPartedItemModel compose(Map<Integer, List<BakedQuad>> layers, TextureAtlasSprite particle, BakedModel transformSource) {
        List<BakedQuad> quads = layers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(entry -> entry.getValue().stream())
                .toList();
        return new ResolvedPartedItemModel(quads, particle, transformSource);
    }
}

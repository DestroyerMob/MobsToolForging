package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolVisualKey;

public final class PartedToolBakedModel implements BakedModel {
    private final ToolKind toolKind;
    private final PartedToolSpriteSet sprites;
    private final PartedToolQuadFactory quadFactory;
    private final ItemTransforms transforms;
    private final ResolvedPartedItemModel fallback;
    private final ItemOverrides overrides;
    private final Map<ToolVisualKey, ResolvedPartedItemModel> cache = new ConcurrentHashMap<>();

    public PartedToolBakedModel(ToolKind toolKind, PartedToolSpriteSet sprites, PartedToolQuadFactory quadFactory, ItemTransforms transforms) {
        this.toolKind = toolKind;
        this.sprites = sprites;
        this.quadFactory = quadFactory;
        this.transforms = transforms;
        this.fallback = compose(MaterialCatalog.IRON, MaterialCatalog.STICK);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                return ToolVisualKey.from(stack)
                        .filter(key -> key.toolType().equals(ToolConstructionData.toolType(PartedToolBakedModel.this.toolKind)))
                        .map(key -> cache.computeIfAbsent(key, PartedToolBakedModel.this::compose))
                        .orElse(fallback);
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return fallback.getQuads(state, direction, random);
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
        return fallback.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private ResolvedPartedItemModel compose(ToolVisualKey key) {
        return compose(key.headMaterial(), key.handleMaterial());
    }

    private ResolvedPartedItemModel compose(ResourceLocation headMaterial, ResourceLocation handleMaterial) {
        Map<Integer, List<BakedQuad>> layers = new LinkedHashMap<>();
        layers.put(0, quadFactory.bakeLayer(0, sprites.handle(handleMaterial)));
        layers.put(1, quadFactory.bakeLayer(1, sprites.head(headMaterial)));
        return ResolvedPartedItemModel.compose(layers, sprites.head(headMaterial), transforms);
    }
}

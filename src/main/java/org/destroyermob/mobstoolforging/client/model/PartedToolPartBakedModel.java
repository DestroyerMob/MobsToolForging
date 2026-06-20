package org.destroyermob.mobstoolforging.client.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;

public final class PartedToolPartBakedModel implements BakedModel {
    private static final Set<String> REPORTED_MISSING_PART_SPRITES = ConcurrentHashMap.newKeySet();

    private final ToolVisualDefinition visual;
    private final String partType;
    private final ToolVisualLayer partLayer;
    private final PartedToolSpriteSet sprites;
    private final PartedToolQuadFactory quadFactory;
    private final ItemTransforms transforms;
    private final ResolvedPartedItemModel fallback;
    private final ItemOverrides overrides;
    private final Map<ResourceLocation, ResolvedPartedItemModel> cache = new ConcurrentHashMap<>();

    public PartedToolPartBakedModel(ToolVisualDefinition visual, String partType, String partSlot, PartedToolSpriteSet sprites, PartedToolQuadFactory quadFactory, ItemTransforms transforms) {
        this.visual = visual;
        this.partType = partType;
        this.partLayer = visual.layerForSlot(partSlot);
        this.sprites = sprites;
        this.quadFactory = quadFactory;
        this.transforms = transforms;
        this.fallback = compose(MaterialCatalog.IRON);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
                if (data == null || !PartedToolPartBakedModel.this.partType.equals(data.partType())) {
                    return fallback;
                }
                return cache.computeIfAbsent(data.materialId(), PartedToolPartBakedModel.this::compose);
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

    private ResolvedPartedItemModel compose(ResourceLocation material) {
        ResolvedToolLayerSprite resolved = sprites.resolvePartLayer(visual.id(), partLayer, material)
                .orElseGet(() -> {
                    warnMissingPartSprite(material);
                    return ResolvedToolLayerSprite.exact(sprites.missing(), net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
                });
        List<BakedQuad> quads = quadFactory.bakeLayer(0, resolved.sprite(), resolved.color());
        return new ResolvedPartedItemModel(quads, resolved.sprite(), transforms);
    }

    private void warnMissingPartSprite(ResourceLocation material) {
        String textureKey = ToolPartSpriteKey.modelTextureKey(partLayer.slot(), material);
        String reportKey = visual.id() + "|" + partType + "|" + partLayer.slot() + "|" + material + "|" + textureKey;
        if (REPORTED_MISSING_PART_SPRITES.add(reportKey)) {
            MobsToolForging.LOGGER.warn(
                    "Missing tool part visual sprite: visual={}, partType={}, slot={}, material={}, textureKey={}",
                    visual.id(),
                    partType,
                    partLayer.slot(),
                    material,
                    textureKey
            );
        }
    }
}

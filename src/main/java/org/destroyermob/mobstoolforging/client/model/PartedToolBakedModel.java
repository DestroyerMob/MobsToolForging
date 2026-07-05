package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolVisualKey;

public final class PartedToolBakedModel implements BakedModel {
    private static final Set<String> REPORTED_MISSING_LAYERS = ConcurrentHashMap.newKeySet();

    private final ToolTypeDefinition definition;
    private final ToolVisualDefinition visual;
    private final PartedToolSpriteSet sprites;
    private final PartedToolQuadFactory quadFactory;
    private final ItemTransforms transforms;
    private final ResolvedPartedItemModel fallback;
    private final ItemOverrides overrides;
    private final Map<ToolVisualKey, ResolvedPartedItemModel> cache = new ConcurrentHashMap<>();

    public PartedToolBakedModel(ToolTypeDefinition definition, ToolVisualDefinition visual, PartedToolSpriteSet sprites, PartedToolQuadFactory quadFactory, ItemTransforms transforms) {
        this.definition = definition;
        this.visual = visual;
        this.sprites = sprites;
        this.quadFactory = quadFactory;
        this.transforms = transforms;
        this.fallback = composeLayers(new ToolVisualKey(
                definition.id(),
                MaterialCatalog.IRON,
                MaterialCatalog.OAK,
                definition.requiredAssemblyParts().isEmpty() ? Optional.empty() : Optional.of(MaterialCatalog.IRON),
                Optional.empty(),
                ToolConstructionData.DEFAULT_QUALITY,
                0
        )).orElseGet(this::particleFallback);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                return ToolVisualKey.from(stack)
                        .filter(key -> key.toolType().equals(PartedToolBakedModel.this.definition.id()))
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
        return composeLayers(key).orElse(fallback);
    }

    private Optional<ResolvedPartedItemModel> composeLayers(ToolVisualKey key) {
        Map<Integer, List<BakedQuad>> layers = new LinkedHashMap<>();
        TextureAtlasSprite particle = null;
        for (ToolVisualLayer layer : visual.layers()) {
            Optional<ResourceLocation> material = materialFor(key, layer);
            if (material.isEmpty()) {
                if (layer.optional()) {
                    continue;
                }
                warnMissingLayer("required layer missing material", layer, Optional.empty());
                return Optional.empty();
            }
            List<ResolvedToolLayerSprite> resolvedLayers = sprites.resolveLayers(visual.id(), layer, material.get());
            if (resolvedLayers.isEmpty()) {
                warnMissingLayer(layer.optional() ? "optional layer has material but missing sprite" : "required layer missing sprite", layer, material);
                if (layer.optional()) {
                    continue;
                }
                resolvedLayers = List.of(ResolvedToolLayerSprite.exact(sprites.missing(), net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()));
            }
            boolean hasVisibleLayer = resolvedLayers.stream().anyMatch(resolvedLayer -> !isMissing(resolvedLayer.sprite()));
            if (!hasVisibleLayer) {
                warnMissingLayer(layer.optional() ? "optional layer has material but missing sprite" : "required layer missing sprite", layer, material);
                if (layer.optional()) {
                    continue;
                }
            }
            if (particle == null && layer.materialFrom().filter("headMaterial"::equals).isPresent()) {
                particle = resolvedLayers.stream()
                        .filter(resolvedLayer -> !isMissing(resolvedLayer.sprite()))
                        .findFirst()
                        .map(ResolvedToolLayerSprite::sprite)
                        .orElse(resolvedLayers.getFirst().sprite());
            }
            for (ResolvedToolLayerSprite resolvedLayer : resolvedLayers) {
                if (hasVisibleLayer && isMissing(resolvedLayer.sprite())) {
                    continue;
                }
                addLayer(layers, layer.z(), quadFactory.bakeLayer(layer.z(), resolvedLayer.sprite(), resolvedLayer.color()));
            }
        }
        if (layers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ResolvedPartedItemModel.compose(
                layers,
                particle == null ? sprites.particle() : particle,
                transforms
        ));
    }

    private Optional<ResourceLocation> materialFor(ToolVisualKey key, ToolVisualLayer layer) {
        return layer.materialFrom().flatMap(materialFrom -> switch (materialFrom) {
            case "headMaterial" -> Optional.of(key.headMaterial());
            case "handleMaterial" -> Optional.of(key.handleMaterial());
            case "guardMaterial" -> key.guardMaterial();
            case "treatment" -> key.treatment();
            default -> Optional.empty();
        });
    }

    private boolean isMissing(TextureAtlasSprite sprite) {
        return net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    private void addLayer(Map<Integer, List<BakedQuad>> layers, int z, List<BakedQuad> quads) {
        layers.merge(z, quads, (existing, additions) -> {
            List<BakedQuad> combined = new ArrayList<>(existing.size() + additions.size());
            combined.addAll(existing);
            combined.addAll(additions);
            return List.copyOf(combined);
        });
    }

    private void warnMissingLayer(String reason, ToolVisualLayer layer, Optional<ResourceLocation> material) {
        String materialText = material.map(ResourceLocation::toString).orElse("<none>");
        String textureKey = material
                .map(value -> ToolPartSpriteKey.modelTextureKey(layer.slot(), value))
                .orElse("<none>");
        String reportKey = reason + "|" + visual.id() + "|" + layer.slot() + "|" + materialText + "|" + textureKey;
        if (REPORTED_MISSING_LAYERS.add(reportKey)) {
            MobsToolForging.LOGGER.warn(
                    "Missing tool visual layer: reason={}, visual={}, slot={}, material={}, textureKey={}",
                    reason,
                    visual.id(),
                    layer.slot(),
                    materialText,
                    textureKey
            );
        }
    }

    private ResolvedPartedItemModel particleFallback() {
        return new ResolvedPartedItemModel(
                quadFactory.bakeLayer(0, sprites.particle()),
                sprites.particle(),
                transforms
        );
    }
}

package org.destroyermob.mobstoolforging.client.model;

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
                Optional.empty(),
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
            Optional<TextureAtlasSprite> sprite = sprites.resolve(visual.id(), layer.slot(), material.get());
            if (sprite.isEmpty()) {
                warnMissingLayer(layer.optional() ? "optional layer has material but missing sprite" : "required layer missing sprite", layer, material);
                if (layer.optional()) {
                    continue;
                }
                sprite = Optional.of(sprites.missing());
            }
            if (particle == null && layer.materialFrom().filter("headMaterial"::equals).isPresent()) {
                particle = sprite.get();
            }
            layers.put(layer.z(), quadFactory.bakeLayer(layer.z(), sprite.get()));
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
            case "bindingMaterial" -> key.bindingMaterial();
            case "wrapMaterial" -> key.wrapMaterial();
            case "focusMaterial" -> key.focusMaterial();
            case "treatment" -> key.treatment();
            default -> Optional.empty();
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

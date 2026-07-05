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
    private final Map<PartKey, ResolvedPartedItemModel> cache = new ConcurrentHashMap<>();

    public PartedToolPartBakedModel(ToolVisualDefinition visual, String partType, String partSlot, PartedToolSpriteSet sprites, PartedToolQuadFactory quadFactory, ItemTransforms transforms) {
        this.visual = visual;
        this.partType = partType;
        this.partLayer = visual.layerForSlot(partSlot);
        this.sprites = sprites;
        this.quadFactory = quadFactory;
        this.transforms = transforms;
        this.fallback = compose(new PartKey(MaterialCatalog.IRON, Optional.empty()));
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
                if (data == null || !PartedToolPartBakedModel.this.partType.equals(data.partType())) {
                    return fallback;
                }
                return cache.computeIfAbsent(new PartKey(data.materialId(), data.treatment()), PartedToolPartBakedModel.this::compose);
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

    private ResolvedPartedItemModel compose(PartKey key) {
        ResolvedToolLayerSprite resolved = sprites.resolvePartLayer(visual.id(), partLayer, key.material())
                .orElseGet(() -> {
                    warnMissingPartSprite(key.material());
                    return ResolvedToolLayerSprite.exact(sprites.missing(), net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
                });
        Map<Integer, List<BakedQuad>> layers = new LinkedHashMap<>();
        layers.put(0, quadFactory.bakeLayer(0, resolved.sprite(), resolved.color()));
        key.treatment().ifPresent(treatment -> treatmentLayer().ifPresent(treatmentLayer ->
                sprites.resolveLayer(visual.id(), treatmentLayer, treatment)
                        .ifPresent(resolvedTreatment -> layers.put(
                                treatmentLayer.z(),
                                quadFactory.bakeLayer(treatmentLayer.z(), resolvedTreatment.sprite(), resolvedTreatment.color())
                        ))
        ));
        return ResolvedPartedItemModel.compose(layers, resolved.sprite(), transforms);
    }

    private Optional<ToolVisualLayer> treatmentLayer() {
        return visual.layers().stream()
                .filter(layer -> layer.materialFrom().filter("treatment"::equals).isPresent())
                .findFirst();
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

    private record PartKey(ResourceLocation material, Optional<ResourceLocation> treatment) {
    }
}

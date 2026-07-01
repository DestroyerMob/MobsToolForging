package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorVisualKey;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolVisualKey;

public final class ComponentDrivenToolBakedModel implements BakedModel {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private final BakedModel fallback;
    private final PartedToolQuadFactory quadFactory = new PartedToolQuadFactory(BlockModelRotation.X0_Y0);
    private final Map<ToolVisualKey, BakedModel> toolCache = new ConcurrentHashMap<>();
    private final Map<PartKey, BakedModel> partCache = new ConcurrentHashMap<>();
    private final Map<ArmorVisualKey, BakedModel> armorCache = new ConcurrentHashMap<>();
    private final ItemOverrides overrides;

    public ComponentDrivenToolBakedModel(BakedModel fallback) {
        this.fallback = fallback;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                Optional<ToolVisualKey> toolKey = ToolVisualKey.from(stack);
                if (toolKey.isPresent()) {
                    ToolTypeDefinition definition = ToolTypeRegistry.toolType(toolKey.get().toolType()).orElse(null);
                    if (definition != null) {
                        return toolCache.computeIfAbsent(toolKey.get(), key -> composeTool(definition, key));
                    }
                    warnOnce("missing_tool_type|" + toolKey.get().toolType(), "Cannot render MTF tool stack because tool type {} is not loaded on the client.", toolKey.get().toolType());
                }

                ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
                if (partData != null) {
                    ToolTypeDefinition definition = findPartDefinition(stack, partData).orElse(null);
                    if (definition != null) {
                        return partCache.computeIfAbsent(new PartKey(definition.id(), partData.partType(), partData.materialId()), key -> composePart(definition, partData));
                    }
                    warnOnce("missing_part_type|" + partData.partType() + "|" + BuiltInRegistries.ITEM.getKey(stack.getItem()), "Cannot render MTF part stack because no tool type owns part {} for item {}.", partData.partType(), BuiltInRegistries.ITEM.getKey(stack.getItem()));
                }

                Optional<ArmorVisualKey> armorKey = ArmorVisualKey.from(stack);
                if (armorKey.isPresent()) {
                    return armorCache.computeIfAbsent(armorKey.get(), ComponentDrivenToolBakedModel.this::composeArmor);
                }

                return resolveFallback(stack, level, entity, seed);
            }
        };
    }

    public static boolean shouldWrap(BakedModel model) {
        return !(model instanceof ComponentDrivenToolBakedModel)
                && !(model instanceof PartedToolBakedModel)
                && !(model instanceof PartedToolPartBakedModel);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return fallback.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return fallback.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return fallback.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return fallback.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return fallback.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return fallback.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return fallback.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private BakedModel resolveFallback(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        BakedModel resolved = fallback.getOverrides().resolve(fallback, stack, level, entity, seed);
        return resolved == null ? fallback : resolved;
    }

    private BakedModel composeTool(ToolTypeDefinition definition, ToolVisualKey key) {
        ToolVisualDefinition visual = ToolVisualManager.resolve(definition.visualId(), definition);
        Map<Integer, List<BakedQuad>> layers = new LinkedHashMap<>();
        TextureAtlasSprite particle = null;

        for (ToolVisualLayer layer : visual.layers()) {
            Optional<ResourceLocation> material = materialFor(key, layer);
            if (material.isEmpty()) {
                if (layer.optional()) {
                    continue;
                }
                warnMissingLayer("required layer missing material", visual, layer, Optional.empty(), null);
                return fallback;
            }

            List<ResolvedToolLayerSprite> resolvedLayers = resolveToolLayers(definition, layer, material.get());
            if (resolvedLayers.isEmpty()) {
                warnMissingLayer(layer.optional() ? "optional layer has material but missing sprite" : "required layer missing sprite", visual, layer, material, null);
                if (layer.optional()) {
                    continue;
                }
                resolvedLayers = List.of(missingLayer());
            }
            boolean hasVisibleLayer = resolvedLayers.stream().anyMatch(resolvedLayer -> !isMissing(resolvedLayer.sprite()));
            if (!hasVisibleLayer) {
                warnMissingLayer(layer.optional() ? "optional layer has material but missing sprite" : "required layer missing sprite", visual, layer, material, resolvedLayers.getFirst().texture());
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
            return fallback;
        }
        return ResolvedPartedItemModel.compose(layers, particle == null ? fallback.getParticleIcon() : particle, fallback.getTransforms());
    }

    private BakedModel composePart(ToolTypeDefinition definition, ToolPartData partData) {
        ToolVisualDefinition visual = ToolVisualManager.resolve(definition.visualId(), definition);
        ToolVisualLayer layer = visual.layerForSlot(partVisualSlot(partData.partType()));
        ResolvedToolLayerSprite resolvedLayer = resolvePartLayer(definition, layer, partData.partType(), partData.materialId());
        if (isMissing(resolvedLayer.sprite())) {
            warnMissingLayer("part layer missing sprite", visual, layer, Optional.of(partData.materialId()), resolvedLayer.texture());
        }
        return new ResolvedPartedItemModel(quadFactory.bakeLayer(0, resolvedLayer.sprite(), resolvedLayer.color()), resolvedLayer.sprite(), fallback.getTransforms());
    }

    private static String partVisualSlot(String partType) {
        return ToolPartData.SWORD_GUARD.equals(partType) ? "guard" : partType;
    }

    private BakedModel composeArmor(ArmorVisualKey key) {
        if (ArmorConstructionData.HELMET_TYPE.equals(key.armorType())) {
            return ModularHelmetItemModel.compose(key, fallback.getTransforms());
        }
        if (ArmorConstructionData.CHESTPLATE_TYPE.equals(key.armorType())) {
            return ModularBodyArmourItemModel.compose(key, fallback);
        }
        if (ArmorConstructionData.LEGGINGS_TYPE.equals(key.armorType())) {
            return ModularLowerArmourItemModel.composeLeggings(key, fallback.getTransforms());
        }
        if (ArmorConstructionData.BOOTS_TYPE.equals(key.armorType())) {
            return ModularLowerArmourItemModel.composeBoots(key, fallback.getTransforms());
        }
        warnOnce("missing_armor_type|" + key.armorType(), "Cannot render MTF armor stack because armor type {} is not loaded on the client.", key.armorType());
        return fallback;
    }

    private Optional<ToolTypeDefinition> findPartDefinition(ItemStack stack, ToolPartData partData) {
        return ToolTypeRegistry.toolTypes().stream()
                .filter(definition -> definition.matchesPartItem(partData.partType(), partData.materialId(), stack))
                .findFirst();
    }

    private Optional<ResourceLocation> materialFor(ToolVisualKey key, ToolVisualLayer layer) {
        return layer.materialFrom().flatMap(materialFrom -> switch (materialFrom) {
            case "headMaterial" -> Optional.of(key.headMaterial());
            case "handleMaterial" -> Optional.of(key.handleMaterial());
            case "guardMaterial" -> key.guardMaterial();
            case "bindingMaterial" -> key.bindingMaterial();
            case "wrapMaterial" -> key.wrapMaterial();
            case "focusMaterial" -> key.focusMaterial();
            case "treatment" -> key.treatment();
            default -> Optional.empty();
        });
    }

    private List<ResolvedToolLayerSprite> resolveToolLayers(ToolTypeDefinition definition, ToolVisualLayer layer, ResourceLocation material) {
        if (!layer.compositesExactAndTemplate()) {
            return List.of(resolveToolLayer(definition, layer, material));
        }

        Optional<ResolvedToolLayerSprite> template = resolveHandleBody(definition, layer, material);
        ResourceLocation exactTexture = textureForToolLayer(definition, layer, material);
        TextureAtlasSprite exactSprite = sprite(exactTexture);
        if (!isMissing(exactSprite)) {
            ResolvedToolLayerSprite exact = ResolvedToolLayerSprite.exact(exactSprite, exactTexture);
            return template.map(resolvedTemplate -> List.of(resolvedTemplate, exact)).orElseGet(() -> List.of(exact));
        }
        return template.map(List::of).orElseGet(() -> List.of(ResolvedToolLayerSprite.exact(exactSprite, exactTexture)));
    }

    private ResolvedToolLayerSprite resolveToolLayer(ToolTypeDefinition definition, ToolVisualLayer layer, ResourceLocation material) {
        if (layer.prefersTemplateFallback()) {
            Optional<ResolvedToolLayerSprite> template = resolveTemplateFallback(layer, material, false);
            if (template.isPresent()) {
                return template.get();
            }
        }
        if (!layer.canUseExactTexture()) {
            return resolveTemplateFallback(layer, material, false).orElseGet(this::missingLayer);
        }
        ResourceLocation exactTexture = textureForToolLayer(definition, layer, material);
        TextureAtlasSprite exactSprite = sprite(exactTexture);
        if (!isMissing(exactSprite)) {
            return ResolvedToolLayerSprite.exact(exactSprite, exactTexture);
        }
        return resolveTemplateFallback(layer, material, false).orElseGet(() -> ResolvedToolLayerSprite.exact(exactSprite, exactTexture));
    }

    private ResolvedToolLayerSprite resolvePartLayer(ToolTypeDefinition definition, ToolVisualLayer layer, String partType, ResourceLocation material) {
        if (layer.prefersTemplateFallback()) {
            Optional<ResolvedToolLayerSprite> template = resolveTemplateFallback(layer, material, true);
            if (template.isPresent()) {
                return template.get();
            }
        }
        if (!layer.canUseExactTexture()) {
            return resolveTemplateFallback(layer, material, true).orElseGet(this::missingLayer);
        }
        ResourceLocation exactTexture = textureForPart(definition, layer, partType, material);
        TextureAtlasSprite exactSprite = sprite(exactTexture);
        if (!isMissing(exactSprite)) {
            return ResolvedToolLayerSprite.exact(exactSprite, exactTexture);
        }
        return resolveTemplateFallback(layer, material, true).orElseGet(() -> ResolvedToolLayerSprite.exact(exactSprite, exactTexture));
    }

    private Optional<ResolvedToolLayerSprite> resolveHandleBody(ToolTypeDefinition definition, ToolVisualLayer layer, ResourceLocation material) {
        ResourceLocation bodyTexture = handleBodyTexture(definition, material);
        TextureAtlasSprite bodySprite = sprite(bodyTexture);
        if (!isMissing(bodySprite)) {
            return Optional.of(ResolvedToolLayerSprite.exact(bodySprite, bodyTexture));
        }

        return resolveHandleTemplate(definition, layer, material);
    }

    private Optional<ResolvedToolLayerSprite> resolveHandleTemplate(ToolTypeDefinition definition, ToolVisualLayer layer, ResourceLocation material) {
        ResourceLocation maskTexture = handleMaskTexture(definition);
        TextureAtlasSprite maskSprite = sprite(maskTexture);
        if (!isMissing(maskSprite)) {
            return Optional.of(ResolvedToolLayerSprite.generated(maskSprite, ToolMaterialVisualManager.INSTANCE.tintColor(material), maskTexture));
        }
        return resolveTemplateFallback(layer, material, false);
    }

    private Optional<ResolvedToolLayerSprite> resolveTemplateFallback(ToolVisualLayer layer, ResourceLocation material, boolean partTemplate) {
        if (!layer.canUseTemplateFallback()) {
            return Optional.empty();
        }
        return layer.templateId(partTemplate)
                .map(template -> {
                    TextureAtlasSprite templateSprite = sprite(template);
                    if (isMissing(templateSprite)) {
                        return null;
                    }
                    return ResolvedToolLayerSprite.generated(templateSprite, ToolMaterialVisualManager.INSTANCE.tintColor(material), template);
                });
    }

    private ResourceLocation textureForToolLayer(ToolTypeDefinition definition, ToolVisualLayer layer, ResourceLocation material) {
        Optional<ResourceLocation> patternTexture = layer.textureFromPattern(material, "tool");
        if (patternTexture.isPresent()) {
            return patternTexture.get();
        }
        if (layer.materialFrom().filter("handleMaterial"::equals).isPresent()) {
            return handleTexture(definition, material);
        }
        return definition.partItem(layer.slot(), material)
                .map(item -> itemTexture(item, "tool"))
                .orElseGet(() -> conventionalToolTexture(layer.slot(), material, "tool"));
    }

    private ResourceLocation textureForPart(ToolTypeDefinition definition, ToolVisualLayer layer, String partType, ResourceLocation material) {
        Optional<ResourceLocation> patternTexture = layer.textureFromPattern(material, "part");
        if (patternTexture.isPresent()) {
            return patternTexture.get();
        }
        return definition.partItem(partType, material)
                .map(item -> itemTexture(item, "part"))
                .orElseGet(() -> conventionalToolTexture(partType, material, "part"));
    }

    private ResourceLocation itemTexture(Item item, String suffix) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath() + "_" + suffix);
    }

    private ResourceLocation conventionalToolTexture(String slot, ResourceLocation material, String suffix) {
        String materialPath = material.getPath();
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "source/tool_parts/" + materialPath + "/" + materialPath + "_" + slot + "_" + suffix);
        }
        return ResourceLocation.fromNamespaceAndPath(material.getNamespace(), "source/tool_parts/" + materialPath + "/" + materialPath + "_" + slot + "_" + suffix);
    }

    private ResourceLocation handleTexture(ToolTypeDefinition definition, ResourceLocation material) {
        String directory;
        String prefix;
        if (MaterialCatalog.BLAZE.equals(material)) {
            directory = "blaze_rod";
            prefix = "blaze_rod";
        } else if (MaterialCatalog.BREEZE.equals(material)) {
            directory = "breeze_rod";
            prefix = "breeze_rod";
        } else {
            directory = "stick";
            prefix = "stick";
        }
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "source/tool_parts/" + directory + "/" + prefix + "_" + handleShape(definition) + "_handle_tool");
    }

    private ResourceLocation handleBodyTexture(ToolTypeDefinition definition, ResourceLocation material) {
        String directory;
        String prefix;
        if (MaterialCatalog.BLAZE.equals(material)) {
            directory = "blaze_rod";
            prefix = "blaze_rod";
        } else if (MaterialCatalog.BREEZE.equals(material)) {
            directory = "breeze_rod";
            prefix = "breeze_rod";
        } else {
            directory = "stick";
            prefix = "stick";
        }
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "source/tool_parts/" + directory + "/" + prefix + "_" + handleShape(definition) + "_handle_body_tool");
    }

    private ResourceLocation handleMaskTexture(ToolTypeDefinition definition) {
        return ResourceLocation.fromNamespaceAndPath(definition.visualId().getNamespace(), "source/tool_parts/handle_masks/" + handleShape(definition) + "_handle_mask");
    }

    private String handleShape(ToolTypeDefinition definition) {
        String path = definition.visualId().getPath();
        if (path.contains("pickaxe")) {
            return "pickaxe";
        }
        if (path.contains("shovel")) {
            return "shovel";
        }
        if (path.contains("hoe")) {
            return "hoe";
        }
        if (path.contains("axe")) {
            return "axe";
        }
        return definition.swordLike() ? "sword" : "handle";
    }

    private TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }

    private boolean isMissing(TextureAtlasSprite sprite) {
        return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    private ResolvedToolLayerSprite missingLayer() {
        return ResolvedToolLayerSprite.exact(sprite(MissingTextureAtlasSprite.getLocation()), MissingTextureAtlasSprite.getLocation());
    }

    private void addLayer(Map<Integer, List<BakedQuad>> layers, int z, List<BakedQuad> quads) {
        layers.merge(z, quads, (existing, additions) -> {
            List<BakedQuad> combined = new ArrayList<>(existing.size() + additions.size());
            combined.addAll(existing);
            combined.addAll(additions);
            return List.copyOf(combined);
        });
    }

    private void warnMissingLayer(String reason, ToolVisualDefinition visual, ToolVisualLayer layer, Optional<ResourceLocation> material, @Nullable ResourceLocation texture) {
        String materialText = material.map(ResourceLocation::toString).orElse("<none>");
        String textureText = texture == null ? "<none>" : texture.toString();
        String textureKey = material.map(value -> ToolPartSpriteKey.modelTextureKey(layer.slot(), value)).orElse("<none>");
        warnOnce(reason + "|" + visual.id() + "|" + layer.slot() + "|" + materialText + "|" + textureText, "Missing component-driven tool visual layer: reason={}, visual={}, slot={}, material={}, textureKey={}, texture={}", reason, visual.id(), layer.slot(), materialText, textureKey, textureText);
    }

    private static void warnOnce(String key, String message, Object... arguments) {
        if (WARNED.add(key)) {
            MobsToolForging.LOGGER.warn(message, arguments);
        }
    }

    private record PartKey(ResourceLocation toolType, String partType, ResourceLocation material) {
    }
}

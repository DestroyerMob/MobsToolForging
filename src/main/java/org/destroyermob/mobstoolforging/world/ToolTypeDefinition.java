package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.destroyermob.mobstoolforging.item.ModularToolItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolTypeDefinition {
    private final ResourceLocation id;
    private final String primaryPartType;
    private final ResourceLocation visualId;
    private final Optional<ToolKind> builtInKind;
    private final Supplier<? extends Item> toolItem;
    private final Map<ResourceLocation, Supplier<? extends Item>> materialToolItems;
    private final Map<String, Supplier<? extends Item>> partItems;
    private final Map<String, Map<ResourceLocation, Supplier<? extends Item>>> materialPartItems;
    private final List<String> requiredAssemblyParts;
    private final boolean averageRequiredPartQuality;
    private final boolean averageRequiredHeadDurability;
    private final float baseAttackDamageBonus;
    private final float baseAttackSpeedBonus;
    private final Map<ResourceLocation, Float> materialBaseAttackDamageBonuses;
    private final Map<ResourceLocation, Float> materialBaseAttackSpeedBonuses;
    private final double entityInteractionRangeBonus;
    private final double blockInteractionRangeBonus;
    private final boolean swordLike;
    private final Optional<TagKey<Block>> miningTag;
    private final BiFunction<ToolTypeDefinition, ToolConstructionData, ItemStack> toolFactory;
    private final boolean customToolFactory;
    private final PartFactory partFactory;

    private ToolTypeDefinition(Builder builder) {
        this.id = builder.id;
        this.primaryPartType = builder.primaryPartType;
        this.visualId = builder.visualId == null ? builder.id : builder.visualId;
        this.builtInKind = Optional.ofNullable(builder.builtInKind);
        this.toolItem = builder.toolItem;
        this.materialToolItems = Map.copyOf(builder.materialToolItems);
        this.partItems = Map.copyOf(builder.partItems);
        Map<String, Map<ResourceLocation, Supplier<? extends Item>>> materialParts = new LinkedHashMap<>();
        builder.materialPartItems.forEach((partType, items) -> materialParts.put(partType, Map.copyOf(items)));
        this.materialPartItems = Map.copyOf(materialParts);
        this.requiredAssemblyParts = List.copyOf(builder.requiredAssemblyParts);
        this.averageRequiredPartQuality = builder.averageRequiredPartQuality;
        this.averageRequiredHeadDurability = builder.averageRequiredHeadDurability;
        this.baseAttackDamageBonus = builder.baseAttackDamageBonus;
        this.baseAttackSpeedBonus = builder.baseAttackSpeedBonus;
        this.materialBaseAttackDamageBonuses = Map.copyOf(builder.materialBaseAttackDamageBonuses);
        this.materialBaseAttackSpeedBonuses = Map.copyOf(builder.materialBaseAttackSpeedBonuses);
        this.entityInteractionRangeBonus = builder.entityInteractionRangeBonus;
        this.blockInteractionRangeBonus = builder.blockInteractionRangeBonus;
        this.swordLike = builder.swordLike;
        this.miningTag = Optional.ofNullable(builder.miningTag);
        this.toolFactory = builder.toolFactory == null ? ToolTypeDefinition::defaultToolStack : builder.toolFactory;
        this.customToolFactory = builder.toolFactory != null;
        this.partFactory = builder.partFactory == null ? ToolTypeDefinition::defaultPartStack : builder.partFactory;
    }

    public ResourceLocation id() {
        return id;
    }

    public String primaryPartType() {
        return primaryPartType;
    }

    public ResourceLocation visualId() {
        return visualId;
    }

    public Optional<ToolKind> builtInKind() {
        return builtInKind;
    }

    public Optional<Item> toolItem() {
        return toolItem == null ? Optional.empty() : Optional.of(toolItem.get());
    }

    public Optional<Item> toolItem(ResourceLocation materialId) {
        Supplier<? extends Item> supplier = materialToolItems.get(materialId);
        return supplier == null ? toolItem() : Optional.of(supplier.get());
    }

    public Set<ResourceLocation> toolItemMaterials() {
        return materialToolItems.keySet();
    }

    public Optional<Item> partItem(String partType) {
        Supplier<? extends Item> supplier = partItems.get(partType);
        return supplier == null ? Optional.empty() : Optional.of(supplier.get());
    }

    public Optional<Item> partItem(String partType, ResourceLocation materialId) {
        Supplier<? extends Item> supplier = materialPartItems.getOrDefault(partType, Map.of()).get(materialId);
        return supplier == null ? partItem(partType) : Optional.of(supplier.get());
    }

    public Optional<Item> materialPartItem(String partType, ResourceLocation materialId) {
        Supplier<? extends Item> supplier = materialPartItems.getOrDefault(partType, Map.of()).get(materialId);
        return supplier == null ? Optional.empty() : Optional.of(supplier.get());
    }

    public boolean matchesPartItem(String partType, ResourceLocation materialId, ItemStack stack) {
        Optional<Item> item = partItem(partType, materialId);
        if (item.isPresent()) {
            return stack.is(item.get());
        }
        return (primaryPartType.equals(partType) || requiredAssemblyParts.contains(partType))
                && !partItems.containsKey(partType)
                && !materialPartItems.containsKey(partType);
    }

    public Set<String> partTypes() {
        java.util.LinkedHashSet<String> values = new java.util.LinkedHashSet<>(partItems.keySet());
        values.addAll(materialPartItems.keySet());
        return Set.copyOf(values);
    }

    public List<String> requiredAssemblyParts() {
        return requiredAssemblyParts;
    }

    public boolean averageRequiredPartQuality() {
        return averageRequiredPartQuality;
    }

    public boolean averageRequiredHeadDurability() {
        return averageRequiredHeadDurability;
    }

    public int assembledQuality(ToolPartData primary, Iterable<ToolPartData> requiredParts) {
        int primaryScore = primary == null ? ToolConstructionData.DEFAULT_QUALITY : primary.effectiveQuality();
        int requiredTotal = 0;
        int requiredCount = 0;
        for (ToolPartData requiredPart : requiredParts) {
            if (requiredPart == null) {
                continue;
            }
            requiredTotal += requiredPart.effectiveQuality();
            requiredCount++;
        }
        if (averageRequiredPartQuality && requiredCount > 0) {
            return ForgingQuality.clampScore(Math.round((primaryScore + requiredTotal) / (float) (requiredCount + 1)));
        }

        int supportScore = requiredCount == 0 ? primaryScore : Math.round(requiredTotal / (float) requiredCount);
        return ForgingQuality.clampScore(Math.round(
                primaryScore * 0.7F
                        + supportScore * 0.2F
                        + ToolConstructionData.DEFAULT_QUALITY * 0.1F
        ));
    }

    public float baseAttackDamageBonus(ResourceLocation materialId) {
        Float materialValue = materialBaseAttackDamageBonuses.get(materialId);
        return materialValue != null ? materialValue : builtInKind.map(kind -> ToolStatBuilder.builtInBaseAttackDamageBonus(kind, materialId)).orElse(baseAttackDamageBonus);
    }

    public float baseAttackSpeedBonus(ResourceLocation materialId) {
        Float materialValue = materialBaseAttackSpeedBonuses.get(materialId);
        return materialValue != null ? materialValue : builtInKind.map(kind -> ToolStatBuilder.builtInBaseAttackSpeedBonus(kind, materialId)).orElse(baseAttackSpeedBonus);
    }

    public double entityInteractionRangeBonus() {
        return entityInteractionRangeBonus;
    }

    public double blockInteractionRangeBonus() {
        return blockInteractionRangeBonus;
    }

    public boolean swordLike() {
        return swordLike;
    }

    public Optional<TagKey<Block>> miningTag() {
        return miningTag;
    }

    public ItemStack createTool(ToolConstructionData construction) {
        return toolFactory.apply(this, construction);
    }

    public ItemStack createPart(String partType, ResourceLocation materialId) {
        return createPart(partType, materialId, ToolPartData.DEFAULT_QUALITY);
    }

    public ItemStack createPart(String partType, ResourceLocation materialId, int quality) {
        return partFactory.create(this, partType, materialId, quality);
    }

    public boolean requiresAssemblyPart(String partType) {
        return requiredAssemblyParts.contains(partType);
    }

    public boolean canAssemble(ToolConstructionData construction, Map<String, ToolPartData> parts) {
        if (!id.equals(construction.toolType())) {
            return false;
        }
        if (!MaterialCatalog.isNormalForgingMaterial(construction.headMaterial())
                || construction.headBaseMaterial().filter(material -> MaterialCatalog.definition(material)
                .filter(definition -> definition.category() == MaterialCategory.METAL)
                .isEmpty()).isPresent()
                || construction.guardMaterial().filter(material -> !MaterialCatalog.isNormalForgingMaterial(material)).isPresent()) {
            return false;
        }
        if (!customToolFactory && toolItem(construction.headMaterial()).isEmpty()) {
            return false;
        }
        ToolPartData primary = parts.get(primaryPartType);
        if (primary == null || !primaryPartType.equals(primary.partType()) || !construction.headMaterial().equals(primary.materialId())) {
            return false;
        }
        if (!parts.keySet().containsAll(requiredAssemblyParts)) {
            return false;
        }
        return parts.values().stream()
                .allMatch(part -> MaterialCatalog.isNormalForgingMaterial(part.materialId()) && MaterialCatalog.definition(part.materialId()).isPresent());
    }

    public static Builder builder(ResourceLocation id, String primaryPartType) {
        return new Builder(id, primaryPartType);
    }

    private static ItemStack defaultToolStack(ToolTypeDefinition definition, ToolConstructionData construction) {
        Optional<Item> item = definition.toolItem(construction.headMaterial());
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack;
        if (item.get() instanceof ModularToolItem modularTool) {
            stack = modularTool.create(construction);
        } else {
            stack = new ItemStack(item.get());
            stack.set(ModDataComponents.TOOL_CONSTRUCTION.get(), construction);
            ToolStatBuilder.apply(stack, definition, construction);
            ToolStackNames.applyToolName(stack, definition, construction);
        }
        return stack;
    }

    private static ItemStack defaultPartStack(ToolTypeDefinition definition, String partType, ResourceLocation materialId, int quality) {
        return definition.partItem(partType, materialId)
                .map(item -> {
                    if (item instanceof ModularToolPartItem partItem) {
                        return partItem.createPart(materialId, quality);
                    }
                    ItemStack stack = new ItemStack(item);
                    stack.set(ModDataComponents.TOOL_PART.get(), new ToolPartData(partType, materialId, quality));
                    ToolStackNames.applyPartName(stack, partType, materialId);
                    return stack;
                })
                .orElse(ItemStack.EMPTY);
    }

    @FunctionalInterface
    public interface PartFactory {
        ItemStack create(ToolTypeDefinition definition, String partType, ResourceLocation materialId, int quality);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final String primaryPartType;
        private ResourceLocation visualId;
        private ToolKind builtInKind;
        private Supplier<? extends Item> toolItem;
        private final Map<ResourceLocation, Supplier<? extends Item>> materialToolItems = new LinkedHashMap<>();
        private final Map<String, Supplier<? extends Item>> partItems = new LinkedHashMap<>();
        private final Map<String, Map<ResourceLocation, Supplier<? extends Item>>> materialPartItems = new LinkedHashMap<>();
        private final java.util.ArrayList<String> requiredAssemblyParts = new java.util.ArrayList<>();
        private boolean averageRequiredPartQuality;
        private boolean averageRequiredHeadDurability;
        private float baseAttackDamageBonus = 1.0F;
        private float baseAttackSpeedBonus = -2.8F;
        private final Map<ResourceLocation, Float> materialBaseAttackDamageBonuses = new LinkedHashMap<>();
        private final Map<ResourceLocation, Float> materialBaseAttackSpeedBonuses = new LinkedHashMap<>();
        private double entityInteractionRangeBonus;
        private double blockInteractionRangeBonus;
        private boolean swordLike;
        private TagKey<Block> miningTag = BlockTags.MINEABLE_WITH_PICKAXE;
        private BiFunction<ToolTypeDefinition, ToolConstructionData, ItemStack> toolFactory;
        private PartFactory partFactory;

        private Builder(ResourceLocation id, String primaryPartType) {
            this.id = id;
            this.primaryPartType = primaryPartType;
        }

        public Builder visual(ResourceLocation visualId) {
            this.visualId = visualId;
            return this;
        }

        public Builder builtInKind(ToolKind builtInKind) {
            this.builtInKind = builtInKind;
            this.swordLike = builtInKind == ToolKind.SWORD;
            this.miningTag = switch (builtInKind) {
                case SWORD -> null;
                case SHOVEL -> BlockTags.MINEABLE_WITH_SHOVEL;
                case PICKAXE -> BlockTags.MINEABLE_WITH_PICKAXE;
                case AXE -> BlockTags.MINEABLE_WITH_AXE;
                case HOE -> BlockTags.MINEABLE_WITH_HOE;
                case MATTOCK -> BlockTags.MINEABLE_WITH_AXE;
            };
            return this;
        }

        public Builder toolItem(Supplier<? extends Item> toolItem) {
            this.toolItem = toolItem;
            return this;
        }

        public Builder toolItem(ResourceLocation materialId, Supplier<? extends Item> toolItem) {
            this.materialToolItems.put(materialId, toolItem);
            return this;
        }

        public Builder partItem(String partType, Supplier<? extends Item> partItem) {
            this.partItems.put(partType, partItem);
            return this;
        }

        public Builder partItem(String partType, ResourceLocation materialId, Supplier<? extends Item> partItem) {
            this.materialPartItems.computeIfAbsent(partType, ignored -> new LinkedHashMap<>()).put(materialId, partItem);
            return this;
        }

        public Builder requiredAssemblyPart(String partType, Supplier<? extends Item> partItem) {
            this.requiredAssemblyParts.add(partType);
            return partItem(partType, partItem);
        }

        public Builder requiredAssemblyPart(String partType) {
            this.requiredAssemblyParts.add(partType);
            return this;
        }

        public Builder averageRequiredPartQuality(boolean averageRequiredPartQuality) {
            this.averageRequiredPartQuality = averageRequiredPartQuality;
            return this;
        }

        public Builder averageRequiredHeadDurability(boolean averageRequiredHeadDurability) {
            this.averageRequiredHeadDurability = averageRequiredHeadDurability;
            return this;
        }

        public Builder baseStats(float attackDamageBonus, float attackSpeedBonus) {
            this.baseAttackDamageBonus = attackDamageBonus;
            this.baseAttackSpeedBonus = attackSpeedBonus;
            return this;
        }

        public Builder materialBaseStats(ResourceLocation materialId, Float attackDamageBonus, Float attackSpeedBonus) {
            if (attackDamageBonus != null) {
                this.materialBaseAttackDamageBonuses.put(materialId, attackDamageBonus);
            }
            if (attackSpeedBonus != null) {
                this.materialBaseAttackSpeedBonuses.put(materialId, attackSpeedBonus);
            }
            return this;
        }

        public Builder interactionRangeBonuses(double entityInteractionRangeBonus, double blockInteractionRangeBonus) {
            this.entityInteractionRangeBonus = entityInteractionRangeBonus;
            this.blockInteractionRangeBonus = blockInteractionRangeBonus;
            return this;
        }

        public Builder swordLike(boolean swordLike) {
            this.swordLike = swordLike;
            if (swordLike) {
                this.miningTag = null;
            }
            return this;
        }

        public Builder miningTag(TagKey<Block> miningTag) {
            this.miningTag = miningTag;
            this.swordLike = false;
            return this;
        }

        public Builder noMiningTag() {
            this.miningTag = null;
            return this;
        }

        public Builder toolFactory(BiFunction<ToolTypeDefinition, ToolConstructionData, ItemStack> toolFactory) {
            this.toolFactory = toolFactory;
            return this;
        }

        public Builder partFactory(PartFactory partFactory) {
            this.partFactory = partFactory;
            return this;
        }

        public ToolTypeDefinition build() {
            return new ToolTypeDefinition(this);
        }
    }
}

package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final Map<String, Supplier<? extends Item>> partItems;
    private final List<String> requiredAssemblyParts;
    private final float baseAttackDamageBonus;
    private final float baseAttackSpeedBonus;
    private final boolean swordLike;
    private final Optional<TagKey<Block>> miningTag;
    private final BiFunction<ToolTypeDefinition, ToolConstructionData, ItemStack> toolFactory;
    private final PartFactory partFactory;

    private ToolTypeDefinition(Builder builder) {
        this.id = builder.id;
        this.primaryPartType = builder.primaryPartType;
        this.visualId = builder.visualId == null ? builder.id : builder.visualId;
        this.builtInKind = Optional.ofNullable(builder.builtInKind);
        this.toolItem = builder.toolItem;
        this.partItems = Map.copyOf(builder.partItems);
        this.requiredAssemblyParts = List.copyOf(builder.requiredAssemblyParts);
        this.baseAttackDamageBonus = builder.baseAttackDamageBonus;
        this.baseAttackSpeedBonus = builder.baseAttackSpeedBonus;
        this.swordLike = builder.swordLike;
        this.miningTag = Optional.ofNullable(builder.miningTag);
        this.toolFactory = builder.toolFactory == null ? ToolTypeDefinition::defaultToolStack : builder.toolFactory;
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

    public Optional<Item> partItem(String partType) {
        Supplier<? extends Item> supplier = partItems.get(partType);
        return supplier == null ? Optional.empty() : Optional.of(supplier.get());
    }

    public List<String> requiredAssemblyParts() {
        return requiredAssemblyParts;
    }

    public float baseAttackDamageBonus(ResourceLocation materialId) {
        return builtInKind.map(kind -> ToolStatBuilder.builtInBaseAttackDamageBonus(kind, materialId)).orElse(baseAttackDamageBonus);
    }

    public float baseAttackSpeedBonus(ResourceLocation materialId) {
        return builtInKind.map(kind -> ToolStatBuilder.builtInBaseAttackSpeedBonus(kind, materialId)).orElse(baseAttackSpeedBonus);
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
        return partFactory.create(this, partType, materialId);
    }

    public boolean requiresAssemblyPart(String partType) {
        return requiredAssemblyParts.contains(partType);
    }

    public static Builder builder(ResourceLocation id, String primaryPartType) {
        return new Builder(id, primaryPartType);
    }

    private static ItemStack defaultToolStack(ToolTypeDefinition definition, ToolConstructionData construction) {
        Optional<Item> item = definition.toolItem();
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
        }
        return stack;
    }

    private static ItemStack defaultPartStack(ToolTypeDefinition definition, String partType, ResourceLocation materialId) {
        return definition.partItem(partType)
                .map(item -> {
                    if (item instanceof ModularToolPartItem partItem) {
                        return partItem.createPart(materialId);
                    }
                    ItemStack stack = new ItemStack(item);
                    stack.set(ModDataComponents.TOOL_PART.get(), new ToolPartData(partType, materialId));
                    return stack;
                })
                .orElse(ItemStack.EMPTY);
    }

    @FunctionalInterface
    public interface PartFactory {
        ItemStack create(ToolTypeDefinition definition, String partType, ResourceLocation materialId);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final String primaryPartType;
        private ResourceLocation visualId;
        private ToolKind builtInKind;
        private Supplier<? extends Item> toolItem;
        private final Map<String, Supplier<? extends Item>> partItems = new LinkedHashMap<>();
        private final java.util.ArrayList<String> requiredAssemblyParts = new java.util.ArrayList<>();
        private float baseAttackDamageBonus = 1.0F;
        private float baseAttackSpeedBonus = -2.8F;
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
            };
            return this;
        }

        public Builder toolItem(Supplier<? extends Item> toolItem) {
            this.toolItem = toolItem;
            return this;
        }

        public Builder partItem(String partType, Supplier<? extends Item> partItem) {
            this.partItems.put(partType, partItem);
            return this;
        }

        public Builder requiredAssemblyPart(String partType, Supplier<? extends Item> partItem) {
            this.requiredAssemblyParts.add(partType);
            return partItem(partType, partItem);
        }

        public Builder baseStats(float attackDamageBonus, float attackSpeedBonus) {
            this.baseAttackDamageBonus = attackDamageBonus;
            this.baseAttackSpeedBonus = attackSpeedBonus;
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

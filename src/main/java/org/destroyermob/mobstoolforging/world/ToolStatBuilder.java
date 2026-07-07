package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolStatBuilder {
    public static final ResourceLocation AFFINITY_FIRE = affinity("fire");
    public static final ResourceLocation AFFINITY_NETHER = affinity("nether");
    public static final ResourceLocation AFFINITY_RESONANCE = affinity("resonance");
    public static final ResourceLocation AFFINITY_ENCHANTING = affinity("enchanting");
    public static final ResourceLocation AFFINITY_LUCK = affinity("luck");
    public static final ResourceLocation AFFINITY_TRADE = affinity("trade");
    public static final ResourceLocation AFFINITY_FORTUNE = affinity("fortune");
    public static final ResourceLocation AFFINITY_ENCHANT_CONTROL = affinity("enchant_control");
    public static final ResourceLocation AFFINITY_SCULK = affinity("sculk");
    public static final ResourceLocation AFFINITY_SILENCE = affinity("silence");
    public static final ResourceLocation AFFINITY_ECHO = affinity("echo");
    private static final ResourceLocation LEGACY_NETHER_TOUCHED_TRAIT = affinity("nether_touched");

    private ToolStatBuilder() {
    }

    public static ToolStatProfile build(ToolKind toolKind, ToolConstructionData construction) {
        return build(ToolTypeRegistry.toolType(toolKind).orElseThrow(), construction);
    }

    public static ToolStatProfile build(ToolTypeDefinition definition, ToolConstructionData construction) {
        Tier headTier = MaterialCatalog.definition(construction.headMaterial()).orElseThrow().tier();
        MutableStats stats = new MutableStats(
                baseMaxDamage(definition, construction, headTier),
                definition.baseAttackDamageBonus(construction.headMaterial()),
                definition.baseAttackSpeedBonus(construction.headMaterial())
        );

        applyHeadMaterial(stats, construction.headMaterial(), "Head");
        construction.headBaseMaterial().ifPresent(material -> applyHeadMaterial(stats, material, "Head Core"));
        if (definition.averageRequiredHeadDurability()) {
            construction.guardMaterial().ifPresent(material -> applyHeadMaterial(stats, material, "Second Head"));
        } else {
            applyGuard(stats, construction.guardMaterial());
        }
        applyHandle(stats, construction.handleMaterial());
        applyTreatment(stats, construction);
        ToolTypeRegistry.applyStatModifiers(definition, construction, stats);
        applyQuality(stats, construction);

        int maxDamage = Math.max(1, Math.round(stats.baseMaxDamage * stats.durabilityMultiplier));
        return new ToolStatProfile(
                maxDamage,
                stats.attackDamageBonus,
                stats.attackSpeedBonus,
                stats.miningSpeedMultiplier,
                stats.durabilityMultiplier,
                List.copyOf(stats.affinities),
                stats.fireResistant,
                List.copyOf(stats.traits),
                List.copyOf(stats.debugLines)
        );
    }

    public static void apply(ItemStack stack, ToolKind toolKind, ToolConstructionData construction) {
        apply(stack, ToolTypeRegistry.toolType(toolKind).orElseThrow(), construction);
    }

    public static void apply(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
        ToolStatProfile profile = build(definition, construction);
        Tier headTier = MaterialCatalog.definition(construction.headMaterial()).orElseThrow().tier();
        Tier adjustedTier = adjustedTier(headTier, profile, construction);

        stack.set(DataComponents.MAX_DAMAGE, profile.maxDamage());
        stack.set(DataComponents.DAMAGE, 0);
        Tool tool = toolComponent(definition, adjustedTier);
        if (tool != null) {
            stack.set(DataComponents.TOOL, tool);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, toolAttributes(definition, adjustedTier, profile));
        stack.set(ModDataComponents.TOOL_STAT_PROFILE.get(), profile);
        if (profile.fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }
    }

    public static Optional<ToolStatProfile> profile(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.TOOL_STAT_PROFILE.get()));
    }

    public static ToolStatProfile profileForTooltip(ItemStack stack, ToolKind toolKind, ToolConstructionData construction) {
        return profileForTooltip(stack, ToolTypeRegistry.toolType(toolKind).orElseThrow(), construction);
    }

    public static ToolStatProfile profileForTooltip(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
        return profile(stack)
                .filter(profile -> !profile.traits().isEmpty()
                        && !profile.traits().contains(LEGACY_NETHER_TOUCHED_TRAIT)
                        && (!MaterialCatalog.GOLD.equals(construction.headMaterial()) || profile.traits().contains(ToolTrait.GILDED.id())))
                .orElseGet(() -> build(definition, construction));
    }

    public static List<ResourceLocation> enchantAffinities(ItemStack stack) {
        return profile(stack).map(ToolStatProfile::enchantAffinity).orElseGet(List::of);
    }

    public static void validateStarterMaterialAttributes() {
        for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
            MaterialCatalog.definition(materialId).ifPresentOrElse(
                    ignored -> validateStarterMaterialAttributes(materialId),
                    () -> MobsToolForging.LOGGER.warn("Starter material {} has no material definition; modular tools made from it will not build attributes.", materialId)
            );
        }
    }

    public static boolean hasEnchantAffinity(ItemStack stack, ResourceLocation affinity) {
        return profile(stack).map(profile -> profile.hasAffinity(affinity)).orElse(false);
    }

    public static boolean shouldBeFireResistant(ItemStack stack, ToolKind toolKind) {
        return shouldBeFireResistant(stack, ToolTypeRegistry.toolType(toolKind).orElseThrow());
    }

    public static boolean shouldBeFireResistant(ItemStack stack, ToolTypeDefinition definition) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction != null) {
            return build(definition, construction).fireResistant();
        }
        return profile(stack).map(ToolStatProfile::fireResistant).orElseGet(() -> stack.has(DataComponents.FIRE_RESISTANT));
    }

    public static void ensureFireResistanceComponent(ItemStack stack, ToolKind toolKind) {
        ensureFireResistanceComponent(stack, ToolTypeRegistry.toolType(toolKind).orElseThrow());
    }

    public static void ensureFireResistanceComponent(ItemStack stack, ToolTypeDefinition definition) {
        if (shouldBeFireResistant(stack, definition) && !stack.has(DataComponents.FIRE_RESISTANT)) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }
    }

    private static int baseMaxDamage(ToolTypeDefinition definition, ToolConstructionData construction, Tier headTier) {
        int primaryUses = headTier.getUses();
        if (!definition.averageRequiredHeadDurability() || construction.guardMaterial().isEmpty()) {
            return primaryUses;
        }
        return construction.guardMaterial()
                .flatMap(MaterialCatalog::definition)
                .map(material -> Math.max(1, Math.round((primaryUses + material.tier().getUses()) / 2.0F)))
                .orElse(primaryUses);
    }

    private static void applyHeadMaterial(MutableStats stats, ResourceLocation material, String label) {
        stats.addDebug(line(label, material, ""));
        if (MaterialCatalog.NETHERITE.equals(material)) {
            stats.fireResistant = true;
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
        }
        if (MaterialCatalog.GOLD.equals(material)) {
            stats.addTrait(ToolTrait.GILDED);
            stats.addDebug(label + ": Gilded experience");
        }
    }

    private static void applyHandle(MutableStats stats, ResourceLocation handle) {
        if (MaterialCatalog.DARK_OAK.equals(handle)) {
            stats.durabilityMultiplier *= 1.08F;
            stats.attackSpeedBonus -= 0.05F;
            stats.miningSpeedMultiplier *= 0.98F;
            stats.addTrait(ToolTrait.STURDY);
            stats.addDebug(line("Handle", handle, "+Durability, -Speed"));
        } else if (MaterialCatalog.BLAZE.equals(handle)) {
            stats.durabilityMultiplier *= 0.95F;
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
            stats.fireResistant = true;
            stats.addTrait(ToolTrait.KINDLED);
            stats.addDebug(line("Handle", handle, "+Fireproof, -Durability"));
        } else if (MaterialCatalog.BREEZE.equals(handle)) {
            stats.attackSpeedBonus += 0.15F;
            stats.durabilityMultiplier *= 0.90F;
            stats.miningSpeedMultiplier *= 1.03F;
            stats.addTrait(ToolTrait.SWIFT);
            stats.addDebug(line("Handle", handle, "+Speed, -Durability"));
        } else {
            stats.addDebug(line("Handle", handle, ""));
        }
    }

    private static void applyGuard(MutableStats stats, Optional<ResourceLocation> material) {
        if (material.isEmpty()) {
            return;
        }
        ResourceLocation value = material.get();
        String note = "";
        if (MaterialCatalog.IRON.equals(value)) {
            stats.durabilityMultiplier *= 1.10F;
            note = "+Durability";
            stats.addTrait(ToolTrait.REINFORCED);
        } else if (MaterialCatalog.COPPER.equals(value)) {
            stats.durabilityMultiplier *= 1.05F;
            stats.addAffinities(AFFINITY_RESONANCE, AFFINITY_ENCHANTING);
            note = "+Resonance";
            stats.addTrait(ToolTrait.RESONANT);
        } else if (MaterialCatalog.GOLD.equals(value)) {
            stats.durabilityMultiplier *= 0.90F;
            stats.addAffinities(AFFINITY_ENCHANTING);
            note = "+Enchanting, -Durability";
            stats.addTrait(ToolTrait.CONDUCTIVE);
        } else if (MaterialCatalog.DIAMOND.equals(value)) {
            stats.durabilityMultiplier *= 1.20F;
            note = "+Stability";
            stats.addTrait(ToolTrait.STABILIZED);
        } else if (MaterialCatalog.RUBY.equals(value)) {
            stats.attackDamageBonus += 0.10F;
            stats.addAffinities(AFFINITY_LUCK, AFFINITY_TRADE);
            note = "+Value, +Edge";
            stats.addTrait(ToolTrait.FORTUNATE);
        } else if (MaterialCatalog.SAPPHIRE.equals(value)) {
            stats.durabilityMultiplier *= 1.03F;
            stats.addAffinities(AFFINITY_RESONANCE, AFFINITY_ENCHANTING);
            note = "+Enchanting";
            stats.addTrait(ToolTrait.RESONANT);
        } else if (MaterialCatalog.NETHERITE.equals(value)) {
            stats.durabilityMultiplier *= 1.25F;
            stats.attackSpeedBonus -= 0.05F;
            stats.fireResistant = true;
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
            note = "+Durability, +Fireproof, -Speed";
            stats.addTrait(ToolTrait.NETHER_FORGED);
        } else if (MaterialCatalog.EMERALD.equals(value)) {
            stats.addAffinities(AFFINITY_LUCK, AFFINITY_TRADE, AFFINITY_FORTUNE);
            note = "+Fortune";
            stats.addTrait(ToolTrait.FORTUNATE);
        }
        stats.addDebug(line("Guard", value, note));
    }

    private static void applyTreatment(MutableStats stats, ToolConstructionData construction) {
        if (construction.treatment().isEmpty()) {
            return;
        }
        ResourceLocation treatment = construction.treatment().get();
        String note = "";
        if (MaterialCatalog.NETHER.equals(treatment)) {
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
            if (MaterialCatalog.BLAZE.equals(construction.handleMaterial())
                    || MaterialCatalog.NETHERITE.equals(construction.headMaterial())
                    || construction.guardMaterial().filter(MaterialCatalog.NETHERITE::equals).isPresent()) {
                stats.fireResistant = true;
                note = "+Nether, +Fireproof";
            } else {
                note = "+Nether";
            }
            stats.addTrait(ToolTrait.NETHER_TREATED);
        } else if (MaterialCatalog.NETHERITE.equals(treatment)) {
            stats.durabilityMultiplier *= 1.15F;
            stats.attackDamageBonus += 1.0F;
            note = "+Durability, +Damage, +Mining Level";
        } else if (MaterialCatalog.SCULK.equals(treatment)) {
            stats.addAffinities(AFFINITY_SCULK, AFFINITY_SILENCE, AFFINITY_ECHO);
            note = "+Echo";
            stats.addTrait(ToolTrait.ECHOING);
        }
        stats.addDebug(line("Treatment", treatment, note));
    }

    private static void applyQuality(MutableStats stats, ToolConstructionData construction) {
        boolean configLoaded = MobsToolForgingConfig.COMMON_SPEC.isLoaded();
        boolean qualityEnabled = !configLoaded || MobsToolForgingConfig.ENABLE_QUALITY.get();
        if (!qualityEnabled) {
            return;
        }
        ForgingQuality quality = construction.qualityLevel();
        stats.addDebug("Quality: " + quality.getSerializedName());
        boolean qualityAffectsStats = !configLoaded || MobsToolForgingConfig.QUALITY_AFFECTS_STATS.get();
        if (!qualityAffectsStats) {
            return;
        }
        stats.durabilityMultiplier *= quality.durabilityMultiplier();
        stats.miningSpeedMultiplier *= quality.miningSpeedMultiplier();
        stats.attackDamageBonus += quality.attackDamageBonus();
        if (quality == ForgingQuality.CRUDE) {
            stats.addTrait(ToolTrait.WORKMANSHIP_ROUGH);
        } else if (quality == ForgingQuality.FINE || quality == ForgingQuality.MASTERWORK) {
            stats.addTrait(ToolTrait.WORKMANSHIP_GOOD);
        }
    }

    private static Tier adjustedTier(Tier headTier, ToolStatProfile profile, ToolConstructionData construction) {
        Tier harvestTier = harvestTier(headTier, construction);
        return new SimpleTier(
                harvestTier.getIncorrectBlocksForDrops(),
                profile.maxDamage(),
                headTier.getSpeed() * profile.miningSpeedMultiplier(),
                headTier.getAttackDamageBonus(),
                headTier.getEnchantmentValue(),
                () -> {
                    Ingredient ingredient = headTier.getRepairIngredient();
                    return ingredient;
            }
        );
    }

    private static Tier harvestTier(Tier headTier, ToolConstructionData construction) {
        if (construction.treatment().filter(MaterialCatalog.NETHERITE::equals).isEmpty()) {
            return headTier;
        }
        return MaterialCatalog.NETHERITE.equals(construction.headMaterial()) ? headTier : Tiers.DIAMOND;
    }

    private static Tool toolComponent(ToolKind toolKind, Tier tier) {
        return switch (toolKind) {
            case SWORD -> SwordItem.createToolProperties();
            case SHOVEL -> tier.createToolProperties(BlockTags.MINEABLE_WITH_SHOVEL);
            case PICKAXE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_AXE);
            case HOE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_HOE);
            case MATTOCK -> mattockToolProperties(tier);
        };
    }

    private static Tool mattockToolProperties(Tier tier) {
        return new Tool(
                List.of(
                        Tool.Rule.deniesDrops(tier.getIncorrectBlocksForDrops()),
                        Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, tier.getSpeed()),
                        Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tier.getSpeed()),
                        Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, tier.getSpeed())
                ),
                1.0F,
                1
        );
    }

    private static Tool toolComponent(ToolTypeDefinition definition, Tier tier) {
        if (definition.builtInKind().isPresent()) {
            return toolComponent(definition.builtInKind().get(), tier);
        }
        if (definition.swordLike()) {
            return SwordItem.createToolProperties();
        }
        return definition.miningTag()
                .map(tier::createToolProperties)
                .orElse(null);
    }

    private static ItemAttributeModifiers toolAttributes(ToolKind toolKind, Tier tier, ToolStatProfile profile) {
        return switch (toolKind) {
            case SWORD -> SwordItem.createAttributes(tier, profile.attackDamageBonus(), profile.attackSpeedBonus());
            case SHOVEL, PICKAXE, AXE, HOE, MATTOCK -> DiggerItem.createAttributes(tier, profile.attackDamageBonus(), profile.attackSpeedBonus());
        };
    }

    private static ItemAttributeModifiers toolAttributes(ToolTypeDefinition definition, Tier tier, ToolStatProfile profile) {
        ItemAttributeModifiers attributes;
        if (definition.builtInKind().isPresent()) {
            attributes = toolAttributes(definition.builtInKind().get(), tier, profile);
        } else if (definition.swordLike()) {
            attributes = SwordItem.createAttributes(tier, profile.attackDamageBonus(), profile.attackSpeedBonus());
        } else {
            attributes = DiggerItem.createAttributes(tier, profile.attackDamageBonus(), profile.attackSpeedBonus());
        }
        return addInteractionRangeAttributes(attributes, definition);
    }

    private static ItemAttributeModifiers addInteractionRangeAttributes(ItemAttributeModifiers attributes, ToolTypeDefinition definition) {
        if (definition.entityInteractionRangeBonus() != 0.0D) {
            attributes = attributes.withModifierAdded(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(attributeModifierId(definition, "entity_interaction_range"), definition.entityInteractionRangeBonus(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
            );
        }
        if (definition.blockInteractionRangeBonus() != 0.0D) {
            attributes = attributes.withModifierAdded(
                    Attributes.BLOCK_INTERACTION_RANGE,
                    new AttributeModifier(attributeModifierId(definition, "block_interaction_range"), definition.blockInteractionRangeBonus(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
            );
        }
        return attributes;
    }

    private static ResourceLocation attributeModifierId(ToolTypeDefinition definition, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(definition.id().getNamespace(), definition.id().getPath() + "_" + suffix);
    }

    private static void validateStarterMaterialAttributes(ResourceLocation materialId) {
        for (ToolKind toolKind : ToolKind.values()) {
            ToolConstructionData construction = ToolConstructionData.basic(toolKind, materialId, MaterialCatalog.OAK);
            ToolTypeDefinition definition = ToolTypeRegistry.toolType(toolKind).orElse(null);
            if (definition == null) {
                MobsToolForging.LOGGER.warn("Tool kind {} has no type definition; cannot validate attributes for {}.", toolKind.id(), materialId);
                continue;
            }
            ToolStatProfile profile = build(definition, construction);
            Tier headTier = MaterialCatalog.definition(materialId).orElseThrow().tier();
            Tier adjustedTier = adjustedTier(headTier, profile, construction);
            ItemAttributeModifiers attributes = toolAttributes(definition, adjustedTier, profile);
            if (profile.maxDamage() <= 0
                    || !Float.isFinite(profile.attackDamageBonus())
                    || !Float.isFinite(profile.attackSpeedBonus())
                    || attributes.modifiers().isEmpty()
                    || !attributes.showInTooltip()) {
                MobsToolForging.LOGGER.warn(
                        "Starter material {} produced suspicious {} attributes: maxDamage={}, attackDamageBonus={}, attackSpeedBonus={}, modifiers={}, showInTooltip={}",
                        materialId,
                        toolKind.id(),
                        profile.maxDamage(),
                        profile.attackDamageBonus(),
                        profile.attackSpeedBonus(),
                        attributes.modifiers().size(),
                        attributes.showInTooltip()
                );
            }
        }
    }

    public static float builtInBaseAttackDamageBonus(ToolKind toolKind, ResourceLocation materialId) {
        return switch (toolKind) {
            case SWORD -> 3.0F;
            case SHOVEL -> 1.5F;
            case PICKAXE -> 1.0F;
            case AXE -> isGemLikeToolMaterial(materialId) || MaterialCatalog.NETHERITE.equals(materialId) ? 5.0F : 6.0F;
            case HOE -> hoeAttackDamage(materialId);
            case MATTOCK -> builtInBaseAttackDamageBonus(ToolKind.AXE, materialId);
        };
    }

    public static float builtInBaseAttackSpeedBonus(ToolKind toolKind, ResourceLocation materialId) {
        return switch (toolKind) {
            case SWORD -> -2.4F;
            case SHOVEL -> -3.0F;
            case PICKAXE -> -2.8F;
            case AXE -> (MaterialCatalog.IRON.equals(materialId) || MaterialCatalog.COPPER.equals(materialId)) ? -3.1F : -3.0F;
            case HOE -> hoeAttackSpeed(materialId);
            case MATTOCK -> builtInBaseAttackSpeedBonus(ToolKind.AXE, materialId);
        };
    }

    private static float hoeAttackDamage(ResourceLocation materialId) {
        if (MaterialCatalog.GOLD.equals(materialId)) {
            return 0.0F;
        }
        if (MaterialCatalog.IRON.equals(materialId)) {
            return -2.0F;
        }
        if (isGemLikeToolMaterial(materialId)) {
            return -3.0F;
        }
        if (MaterialCatalog.NETHERITE.equals(materialId)) {
            return -4.0F;
        }
        return -1.0F;
    }

    private static float hoeAttackSpeed(ResourceLocation materialId) {
        if (MaterialCatalog.GOLD.equals(materialId)) {
            return -3.0F;
        }
        if (MaterialCatalog.IRON.equals(materialId)) {
            return -1.0F;
        }
        if (isGemLikeToolMaterial(materialId) || MaterialCatalog.NETHERITE.equals(materialId)) {
            return 0.0F;
        }
        return -2.0F;
    }

    private static boolean isGemLikeToolMaterial(ResourceLocation materialId) {
        return MaterialCatalog.DIAMOND.equals(materialId)
                || MaterialCatalog.EMERALD.equals(materialId)
                || MaterialCatalog.RUBY.equals(materialId)
                || MaterialCatalog.SAPPHIRE.equals(materialId);
    }

    private static String line(String label, ResourceLocation material, String note) {
        String text = label + ": " + MaterialCatalog.displayNameText(material);
        return note.isBlank() ? text : text + " (" + note + ")";
    }

    private static ResourceLocation affinity(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    public static final class MutableStats {
        private final int baseMaxDamage;
        private float attackDamageBonus;
        private float attackSpeedBonus;
        private float miningSpeedMultiplier = 1.0F;
        private float durabilityMultiplier = 1.0F;
        private boolean fireResistant;
        private final Set<ResourceLocation> affinities = new LinkedHashSet<>();
        private final Set<ResourceLocation> traits = new LinkedHashSet<>();
        private final List<String> debugLines = new ArrayList<>();

        private MutableStats(int baseMaxDamage, float attackDamageBonus, float attackSpeedBonus) {
            this.baseMaxDamage = baseMaxDamage;
            this.attackDamageBonus = attackDamageBonus;
            this.attackSpeedBonus = attackSpeedBonus;
        }

        public void addAffinities(ResourceLocation... values) {
            affinities.addAll(List.of(values));
        }

        public void addTrait(ToolTrait trait) {
            traits.add(trait.id());
        }

        public void addTrait(ResourceLocation trait) {
            traits.add(trait);
        }

        public void addDebug(String line) {
            debugLines.add(line);
        }

        public void multiplyDurability(float multiplier) {
            durabilityMultiplier *= multiplier;
        }

        public void multiplyMiningSpeed(float multiplier) {
            miningSpeedMultiplier *= multiplier;
        }

        public void addAttackDamage(float value) {
            attackDamageBonus += value;
        }

        public void addAttackSpeed(float value) {
            attackSpeedBonus += value;
        }

        public void setFireResistant() {
            fireResistant = true;
        }
    }
}

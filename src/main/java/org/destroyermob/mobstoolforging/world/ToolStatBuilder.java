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

        applyMaterialTraits(stats, definition, construction);
        ToolTypeRegistry.applyStatModifiers(definition, construction, stats);
        applyTraitEffects(stats);
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
        apply(stack, definition, construction, true);
    }

    public static boolean refreshIfOutdated(ItemStack stack, ToolKind toolKind, ToolConstructionData construction) {
        return refreshIfOutdated(stack, ToolTypeRegistry.toolType(toolKind).orElseThrow(), construction);
    }

    public static boolean refreshIfOutdated(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
        ToolStatProfile current = build(definition, construction);
        if (profile(stack).filter(current::equals).isPresent()) {
            return false;
        }
        apply(stack, definition, construction, false, current);
        return true;
    }

    private static void apply(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction, boolean resetDamage) {
        apply(stack, definition, construction, resetDamage, build(definition, construction));
    }

    private static void apply(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction, boolean resetDamage, ToolStatProfile profile) {
        int previousMaxDamage = stack.getMaxDamage();
        int previousDamage = stack.getDamageValue();
        Tier headTier = MaterialCatalog.definition(construction.headMaterial()).orElseThrow().tier();
        Tier adjustedTier = adjustedTier(headTier, profile, construction);

        stack.set(DataComponents.MAX_DAMAGE, profile.maxDamage());
        stack.set(DataComponents.DAMAGE, resetDamage ? 0 : scaledDamage(previousDamage, previousMaxDamage, profile.maxDamage()));
        Tool tool = toolComponent(definition, adjustedTier);
        if (tool != null) {
            stack.set(DataComponents.TOOL, tool);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, toolAttributes(definition, adjustedTier, profile));
        stack.set(ModDataComponents.TOOL_STAT_PROFILE.get(), profile);
        if (profile.fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.FIRE_RESISTANT);
        }
    }

    private static int scaledDamage(int previousDamage, int previousMaxDamage, int newMaxDamage) {
        if (previousMaxDamage <= 0 || previousDamage <= 0 || newMaxDamage <= 0) {
            return 0;
        }
        int scaled = Math.round(previousDamage * (newMaxDamage / (float) previousMaxDamage));
        return Math.max(0, Math.min(newMaxDamage - 1, scaled));
    }

    public static Optional<ToolStatProfile> profile(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.TOOL_STAT_PROFILE.get()));
    }

    public static ToolStatProfile profileForTooltip(ItemStack stack, ToolKind toolKind, ToolConstructionData construction) {
        return profileForTooltip(stack, ToolTypeRegistry.toolType(toolKind).orElseThrow(), construction);
    }

    public static ToolStatProfile profileForTooltip(ItemStack stack, ToolTypeDefinition definition, ToolConstructionData construction) {
        ToolStatProfile current = build(definition, construction);
        return profile(stack).filter(current::equals).orElse(current);
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

    private static void applyMaterialTraits(MutableStats stats, ToolTypeDefinition definition, ToolConstructionData construction) {
        addMaterialTraits(stats, "Head", construction.headMaterial(), TraitSlot.HEAD);
        construction.headBaseMaterial().ifPresent(material -> addMaterialTraits(stats, "Core", material, TraitSlot.CORE));
        addMaterialTraits(stats, "Handle", construction.handleMaterial(), TraitSlot.HANDLE);
        if (definition.averageRequiredHeadDurability()) {
            construction.guardMaterial().ifPresent(material -> addMaterialTraits(stats, "Second Head", material, TraitSlot.HEAD));
        } else {
            construction.guardMaterial().ifPresent(material -> addMaterialTraits(stats, "Guard", material, TraitSlot.GUARD));
        }
        construction.bindingMaterial().ifPresent(material -> addMaterialTraits(stats, "Binding", material, TraitSlot.BINDING));
        construction.wrapMaterial().ifPresent(material -> addMaterialTraits(stats, "Wrap", material, TraitSlot.WRAP));
        construction.treatment().ifPresent(material -> addMaterialTraits(stats, "Treatment", material, TraitSlot.TREATMENT));
    }

    private static void addMaterialTraits(MutableStats stats, String label, ResourceLocation material, TraitSlot slot) {
        List<ToolTrait> traits = new ArrayList<>();
        primaryMaterialTrait(material).ifPresent(traits::add);
        secondaryMaterialTraits(material, slot).forEach(traits::add);
        traits.forEach(stats::addTrait);
        stats.addDebug(line(label, material, traitNote(traits)));
    }

    public static Optional<ToolTrait> primaryTraitForMaterial(ResourceLocation material) {
        return primaryMaterialTrait(material);
    }

    public static List<ToolTrait> supportTraitsForMaterial(ResourceLocation material) {
        return secondaryMaterialTraits(material, TraitSlot.GUARD);
    }

    private static Optional<ToolTrait> primaryMaterialTrait(ResourceLocation material) {
        if (MaterialCatalog.OAK.equals(material) || MaterialCatalog.DARK_OAK.equals(material)) {
            return Optional.of(ToolTrait.STEADY);
        }
        if (MaterialCatalog.BLAZE.equals(material)) {
            return Optional.of(ToolTrait.KINDLED);
        }
        if (MaterialCatalog.BREEZE.equals(material)) {
            return Optional.of(ToolTrait.SWIFT);
        }
        if (MaterialCatalog.IRON.equals(material)) {
            return Optional.of(ToolTrait.REINFORCED);
        }
        if (MaterialCatalog.COPPER.equals(material)) {
            return Optional.of(ToolTrait.CONDUCTIVE);
        }
        if (MaterialCatalog.GOLD.equals(material)) {
            return Optional.of(ToolTrait.GILDED);
        }
        if (MaterialCatalog.DIAMOND.equals(material)) {
            return Optional.of(ToolTrait.STABILIZED);
        }
        if (MaterialCatalog.EMERALD.equals(material)) {
            return Optional.of(ToolTrait.FORTUNATE);
        }
        if (MaterialCatalog.RUBY.equals(material)) {
            return Optional.of(ToolTrait.KEEN);
        }
        if (MaterialCatalog.SAPPHIRE.equals(material)) {
            return Optional.of(ToolTrait.FOCUSED);
        }
        if (MaterialCatalog.NETHERITE.equals(material)) {
            return Optional.of(ToolTrait.NETHER_FORGED);
        }
        if (MaterialCatalog.NETHER.equals(material)) {
            return Optional.of(ToolTrait.NETHER_TREATED);
        }
        if (MaterialCatalog.SCULK.equals(material)) {
            return Optional.of(ToolTrait.ECHOING);
        }
        if (MaterialCatalog.FLINT.equals(material)) {
            return Optional.of(ToolTrait.JAGGED);
        }
        return Optional.empty();
    }

    private static List<ToolTrait> secondaryMaterialTraits(ResourceLocation material, TraitSlot slot) {
        if (!slot.isSupportSlot()) {
            return List.of();
        }
        if (MaterialCatalog.GOLD.equals(material)) {
            return List.of(ToolTrait.CONDUCTIVE);
        }
        if (MaterialCatalog.COPPER.equals(material) || MaterialCatalog.SAPPHIRE.equals(material)) {
            return List.of(ToolTrait.RESONANT);
        }
        if (MaterialCatalog.RUBY.equals(material)) {
            return List.of(ToolTrait.FORTUNATE);
        }
        return List.of();
    }

    private static void applyTraitEffects(MutableStats stats) {
        applyTraitDebug(stats, ToolTrait.STEADY, "steady handling");
        int steady = stats.traitLevel(ToolTrait.STEADY);
        if (steady > 0) {
            stats.multiplyDurability(multiplier(1.02F, steady));
        }

        applyTraitDebug(stats, ToolTrait.STURDY, "+durability, -speed");
        int sturdy = stats.traitLevel(ToolTrait.STURDY);
        if (sturdy > 0) {
            stats.multiplyDurability(multiplier(1.08F, sturdy));
            stats.multiplyMiningSpeed(multiplier(0.98F, sturdy));
            stats.addAttackSpeed(-0.05F * sturdy);
        }

        applyTraitDebug(stats, ToolTrait.SWIFT, "+speed, -durability");
        int swift = stats.traitLevel(ToolTrait.SWIFT);
        if (swift > 0) {
            stats.addAttackSpeed(0.12F * swift);
            stats.multiplyMiningSpeed(multiplier(1.02F, swift));
            stats.multiplyDurability(multiplier(0.94F, swift));
        }

        applyTraitDebug(stats, ToolTrait.KINDLED, "+fireproof, +nether");
        int kindled = stats.traitLevel(ToolTrait.KINDLED);
        if (kindled > 0) {
            stats.fireResistant = true;
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
            stats.multiplyDurability(multiplier(0.97F, kindled));
        }

        applyTraitDebug(stats, ToolTrait.REINFORCED, "+durability");
        int reinforced = stats.traitLevel(ToolTrait.REINFORCED);
        if (reinforced > 0) {
            stats.multiplyDurability(multiplier(1.06F, reinforced));
        }

        applyTraitDebug(stats, ToolTrait.CONDUCTIVE, "+enchanting");
        int conductive = stats.traitLevel(ToolTrait.CONDUCTIVE);
        if (conductive > 0) {
            stats.addAffinities(AFFINITY_ENCHANTING, AFFINITY_RESONANCE);
            stats.multiplyDurability(multiplier(0.99F, conductive));
        }

        applyTraitDebug(stats, ToolTrait.RESONANT, "+resonance");
        int resonant = stats.traitLevel(ToolTrait.RESONANT);
        if (resonant > 0) {
            stats.addAffinities(AFFINITY_RESONANCE, AFFINITY_ENCHANTING);
            stats.multiplyMiningSpeed(multiplier(1.005F, resonant));
        }

        applyTraitDebug(stats, ToolTrait.GILDED, "+experience, +trade");
        int gilded = stats.traitLevel(ToolTrait.GILDED);
        if (gilded > 0) {
            stats.addAffinities(AFFINITY_ENCHANTING, AFFINITY_TRADE);
        }

        applyTraitDebug(stats, ToolTrait.STABILIZED, "+stability");
        int stabilized = stats.traitLevel(ToolTrait.STABILIZED);
        if (stabilized > 0) {
            stats.multiplyDurability(multiplier(1.08F, stabilized));
            stats.multiplyMiningSpeed(multiplier(1.01F, stabilized));
        }

        applyTraitDebug(stats, ToolTrait.FORTUNATE, "+luck");
        int fortunate = stats.traitLevel(ToolTrait.FORTUNATE);
        if (fortunate > 0) {
            stats.addAffinities(AFFINITY_LUCK, AFFINITY_TRADE, AFFINITY_FORTUNE);
        }

        applyTraitDebug(stats, ToolTrait.KEEN, "+damage");
        int keen = stats.traitLevel(ToolTrait.KEEN);
        if (keen > 0) {
            stats.addAttackDamage(0.15F * keen);
        }

        applyTraitDebug(stats, ToolTrait.JAGGED, "+damage, -durability");
        int jagged = stats.traitLevel(ToolTrait.JAGGED);
        if (jagged > 0) {
            stats.addAttackDamage(0.10F * jagged);
            stats.multiplyDurability(multiplier(0.96F, jagged));
        }

        applyTraitDebug(stats, ToolTrait.SURE_GRIP, "+control");
        int sureGrip = stats.traitLevel(ToolTrait.SURE_GRIP);
        if (sureGrip > 0) {
            stats.addAttackSpeed(0.05F * sureGrip);
            stats.multiplyMiningSpeed(multiplier(1.01F, sureGrip));
        }

        applyTraitDebug(stats, ToolTrait.FOCUSED, "+control, +enchanting");
        int focused = stats.traitLevel(ToolTrait.FOCUSED);
        if (focused > 0) {
            stats.addAffinities(AFFINITY_ENCHANT_CONTROL, AFFINITY_ENCHANTING, AFFINITY_RESONANCE);
            stats.addAttackSpeed(0.03F * focused);
        }

        applyTraitDebug(stats, ToolTrait.NETHER_FORGED, "+durability, +damage, +fireproof");
        int netherForged = stats.traitLevel(ToolTrait.NETHER_FORGED);
        if (netherForged > 0) {
            stats.fireResistant = true;
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
            stats.multiplyDurability(multiplier(1.12F, netherForged));
            stats.addAttackDamage(0.45F * netherForged);
            stats.addAttackSpeed(-0.02F * netherForged);
        }

        applyTraitDebug(stats, ToolTrait.NETHER_TREATED, "+nether");
        int netherTreated = stats.traitLevel(ToolTrait.NETHER_TREATED);
        if (netherTreated > 0) {
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
        }

        applyTraitDebug(stats, ToolTrait.ECHOING, "+echo");
        int echoing = stats.traitLevel(ToolTrait.ECHOING);
        if (echoing > 0) {
            stats.addAffinities(AFFINITY_SCULK, AFFINITY_SILENCE, AFFINITY_ECHO);
        }
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

    private static void applyTraitDebug(MutableStats stats, ToolTrait trait, String note) {
        int level = stats.traitLevel(trait);
        if (level > 0) {
            stats.addDebug("Trait: " + ToolTrait.fallbackName(trait.id()) + " " + level + " (" + note + ")");
        }
    }

    private static float multiplier(float perLevel, int level) {
        return (float) Math.pow(perLevel, level);
    }

    private static String line(String label, ResourceLocation material, String note) {
        String text = label + ": " + MaterialCatalog.displayNameText(material);
        return note.isBlank() ? text : text + " (" + note + ")";
    }

    private static String traitNote(List<ToolTrait> traits) {
        if (traits.isEmpty()) {
            return "";
        }
        return "+" + String.join(", ", traits.stream()
                .map(trait -> ToolTrait.fallbackName(trait.id()))
                .toList());
    }

    private static ResourceLocation affinity(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private enum TraitSlot {
        HEAD,
        CORE,
        HANDLE,
        GUARD,
        BINDING,
        WRAP,
        TREATMENT;

        private boolean isSupportSlot() {
            return this == GUARD || this == BINDING || this == WRAP;
        }
    }

    public static final class MutableStats {
        private final int baseMaxDamage;
        private float attackDamageBonus;
        private float attackSpeedBonus;
        private float miningSpeedMultiplier = 1.0F;
        private float durabilityMultiplier = 1.0F;
        private boolean fireResistant;
        private final Set<ResourceLocation> affinities = new LinkedHashSet<>();
        private final List<ResourceLocation> traits = new ArrayList<>();
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

        public int traitLevel(ToolTrait trait) {
            return traitLevel(trait.id());
        }

        public int traitLevel(ResourceLocation trait) {
            int level = 0;
            for (ResourceLocation value : traits) {
                if (value.equals(trait)) {
                    level++;
                }
            }
            return level;
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

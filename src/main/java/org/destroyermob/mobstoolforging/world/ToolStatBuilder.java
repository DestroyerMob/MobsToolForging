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
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import org.destroyermob.mobstoolforging.MobsToolForging;
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
        Tier headTier = MaterialCatalog.definition(construction.headMaterial()).orElseThrow().tier();
        WorkingStats stats = new WorkingStats(
                headTier.getUses(),
                baseAttackDamageBonus(toolKind, construction.headMaterial()),
                baseAttackSpeedBonus(toolKind, construction.headMaterial())
        );

        stats.addDebug(line("Head", construction.headMaterial(), ""));
        if (MaterialCatalog.NETHERITE.equals(construction.headMaterial())) {
            stats.fireResistant = true;
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
        }
        applyHandle(stats, construction.handleMaterial());
        applyBindingOrGuard(stats, toolKind, construction.bindingMaterial());
        applyWrap(stats, construction.wrapMaterial());
        applyFocus(stats, construction.focusMaterial());
        applyTreatment(stats, construction);
        applyQuality(stats, construction.quality());

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
        ToolStatProfile profile = build(toolKind, construction);
        Tier headTier = MaterialCatalog.definition(construction.headMaterial()).orElseThrow().tier();
        Tier adjustedTier = adjustedTier(headTier, profile);

        stack.set(DataComponents.MAX_DAMAGE, profile.maxDamage());
        stack.set(DataComponents.DAMAGE, 0);
        stack.set(DataComponents.TOOL, toolComponent(toolKind, adjustedTier));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, toolAttributes(toolKind, adjustedTier, profile));
        stack.set(ModDataComponents.TOOL_STAT_PROFILE.get(), profile);
        if (profile.fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }
    }

    public static Optional<ToolStatProfile> profile(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.TOOL_STAT_PROFILE.get()));
    }

    public static ToolStatProfile profileForTooltip(ItemStack stack, ToolKind toolKind, ToolConstructionData construction) {
        return profile(stack)
                .filter(profile -> !profile.traits().isEmpty() && !profile.traits().contains(LEGACY_NETHER_TOUCHED_TRAIT))
                .orElseGet(() -> build(toolKind, construction));
    }

    public static List<ResourceLocation> enchantAffinities(ItemStack stack) {
        return profile(stack).map(ToolStatProfile::enchantAffinity).orElseGet(List::of);
    }

    public static boolean hasEnchantAffinity(ItemStack stack, ResourceLocation affinity) {
        return profile(stack).map(profile -> profile.hasAffinity(affinity)).orElse(false);
    }

    public static boolean shouldBeFireResistant(ItemStack stack, ToolKind toolKind) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction != null) {
            return build(toolKind, construction).fireResistant();
        }
        return profile(stack).map(ToolStatProfile::fireResistant).orElseGet(() -> stack.has(DataComponents.FIRE_RESISTANT));
    }

    public static void ensureFireResistanceComponent(ItemStack stack, ToolKind toolKind) {
        if (shouldBeFireResistant(stack, toolKind) && !stack.has(DataComponents.FIRE_RESISTANT)) {
            stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }
    }

    private static void applyHandle(WorkingStats stats, ResourceLocation handle) {
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

    private static void applyBindingOrGuard(WorkingStats stats, ToolKind toolKind, Optional<ResourceLocation> material) {
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
        stats.addDebug(line(toolKind == ToolKind.SWORD ? "Guard" : "Binding", value, note));
    }

    private static void applyWrap(WorkingStats stats, Optional<ResourceLocation> material) {
        if (material.isEmpty()) {
            return;
        }
        ResourceLocation value = material.get();
        String note = "";
        if (MaterialCatalog.LEATHER.equals(value)) {
            stats.durabilityMultiplier *= 1.05F;
            note = "+Grip";
            stats.addTrait(ToolTrait.SURE_GRIP);
        }
        stats.addDebug(line("Wrap", value, note));
    }

    private static void applyFocus(WorkingStats stats, Optional<ResourceLocation> material) {
        if (material.isEmpty()) {
            return;
        }
        ResourceLocation value = material.get();
        String note = "";
        if (MaterialCatalog.AMETHYST.equals(value)) {
            stats.addAffinities(AFFINITY_RESONANCE, AFFINITY_ENCHANT_CONTROL);
            note = "+Enchant Control";
            stats.addTrait(ToolTrait.FOCUSED);
        }
        stats.addDebug(line("Focus", value, note));
    }

    private static void applyTreatment(WorkingStats stats, ToolConstructionData construction) {
        if (construction.treatment().isEmpty()) {
            return;
        }
        ResourceLocation treatment = construction.treatment().get();
        String note = "";
        if (MaterialCatalog.NETHER.equals(treatment)) {
            stats.addAffinities(AFFINITY_FIRE, AFFINITY_NETHER);
            if (MaterialCatalog.BLAZE.equals(construction.handleMaterial())
                    || MaterialCatalog.NETHERITE.equals(construction.headMaterial())
                    || construction.bindingMaterial().filter(MaterialCatalog.NETHERITE::equals).isPresent()) {
                stats.fireResistant = true;
                note = "+Nether, +Fireproof";
            } else {
                note = "+Nether";
            }
            stats.addTrait(ToolTrait.NETHER_TREATED);
        } else if (MaterialCatalog.SCULK.equals(treatment)) {
            stats.addAffinities(AFFINITY_SCULK, AFFINITY_SILENCE, AFFINITY_ECHO);
            note = "+Echo";
            stats.addTrait(ToolTrait.ECHOING);
        }
        stats.addDebug(line("Treatment", treatment, note));
    }

    private static void applyQuality(WorkingStats stats, int quality) {
        float qualityMultiplier = Math.max(0.90F, Math.min(1.10F, 1.0F + (quality - ToolConstructionData.DEFAULT_QUALITY) * 0.002F));
        stats.durabilityMultiplier *= qualityMultiplier;
        stats.miningSpeedMultiplier *= 1.0F + (qualityMultiplier - 1.0F) * 0.5F;
        if (quality != ToolConstructionData.DEFAULT_QUALITY) {
            stats.addTrait(quality > ToolConstructionData.DEFAULT_QUALITY ? ToolTrait.WORKMANSHIP_GOOD : ToolTrait.WORKMANSHIP_ROUGH);
            stats.addDebug("Quality: " + quality + "%");
        }
    }

    private static Tier adjustedTier(Tier headTier, ToolStatProfile profile) {
        return new SimpleTier(
                headTier.getIncorrectBlocksForDrops(),
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

    private static Tool toolComponent(ToolKind toolKind, Tier tier) {
        return switch (toolKind) {
            case SWORD -> SwordItem.createToolProperties();
            case SHOVEL -> tier.createToolProperties(BlockTags.MINEABLE_WITH_SHOVEL);
            case PICKAXE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_AXE);
            case HOE -> tier.createToolProperties(BlockTags.MINEABLE_WITH_HOE);
        };
    }

    private static ItemAttributeModifiers toolAttributes(ToolKind toolKind, Tier tier, ToolStatProfile profile) {
        return switch (toolKind) {
            case SWORD -> SwordItem.createAttributes(tier, profile.attackDamageBonus(), profile.attackSpeedBonus());
            case SHOVEL, PICKAXE, AXE, HOE -> DiggerItem.createAttributes(tier, profile.attackDamageBonus(), profile.attackSpeedBonus());
        };
    }

    private static float baseAttackDamageBonus(ToolKind toolKind, ResourceLocation materialId) {
        return switch (toolKind) {
            case SWORD -> 3.0F;
            case SHOVEL -> 1.5F;
            case PICKAXE -> 1.0F;
            case AXE -> (MaterialCatalog.DIAMOND.equals(materialId) || MaterialCatalog.NETHERITE.equals(materialId) || MaterialCatalog.EMERALD.equals(materialId)) ? 5.0F : 6.0F;
            case HOE -> hoeAttackDamage(materialId);
        };
    }

    private static float baseAttackSpeedBonus(ToolKind toolKind, ResourceLocation materialId) {
        return switch (toolKind) {
            case SWORD -> -2.4F;
            case SHOVEL -> -3.0F;
            case PICKAXE -> -2.8F;
            case AXE -> (MaterialCatalog.IRON.equals(materialId) || MaterialCatalog.COPPER.equals(materialId)) ? -3.1F : -3.0F;
            case HOE -> hoeAttackSpeed(materialId);
        };
    }

    private static float hoeAttackDamage(ResourceLocation materialId) {
        if (MaterialCatalog.GOLD.equals(materialId)) {
            return 0.0F;
        }
        if (MaterialCatalog.IRON.equals(materialId)) {
            return -2.0F;
        }
        if (MaterialCatalog.DIAMOND.equals(materialId) || MaterialCatalog.EMERALD.equals(materialId)) {
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
        if (MaterialCatalog.DIAMOND.equals(materialId) || MaterialCatalog.EMERALD.equals(materialId) || MaterialCatalog.NETHERITE.equals(materialId)) {
            return 0.0F;
        }
        return -2.0F;
    }

    private static String line(String label, ResourceLocation material, String note) {
        String text = label + ": " + MaterialCatalog.displayNameText(material);
        return note.isBlank() ? text : text + " (" + note + ")";
    }

    private static ResourceLocation affinity(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static final class WorkingStats {
        private final int baseMaxDamage;
        private float attackDamageBonus;
        private float attackSpeedBonus;
        private float miningSpeedMultiplier = 1.0F;
        private float durabilityMultiplier = 1.0F;
        private boolean fireResistant;
        private final Set<ResourceLocation> affinities = new LinkedHashSet<>();
        private final Set<ResourceLocation> traits = new LinkedHashSet<>();
        private final List<String> debugLines = new ArrayList<>();

        private WorkingStats(int baseMaxDamage, float attackDamageBonus, float attackSpeedBonus) {
            this.baseMaxDamage = baseMaxDamage;
            this.attackDamageBonus = attackDamageBonus;
            this.attackSpeedBonus = attackSpeedBonus;
        }

        private void addAffinities(ResourceLocation... values) {
            affinities.addAll(List.of(values));
        }

        private void addTrait(ToolTrait trait) {
            traits.add(trait.id());
        }

        private void addDebug(String line) {
            debugLines.add(line);
        }
    }
}

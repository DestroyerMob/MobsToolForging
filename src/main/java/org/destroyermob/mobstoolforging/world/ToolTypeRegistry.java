package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class ToolTypeRegistry {
    public static final ResourceLocation SMITHING_HAMMER_TOOL_TYPE = modLoc("smithing_hammer");
    public static final ResourceLocation SMITHING_HAMMER_HEAD_TEMPLATE = modLoc("smithing_hammer_head");
    public static final ResourceLocation SCREWDRIVER_TOOL_TYPE = modLoc("screwdriver");
    public static final ResourceLocation SCREWDRIVER_HEAD_TEMPLATE = modLoc("screwdriver_head");
    public static final ResourceLocation GEM_CUTTERS_KNIFE_TOOL_TYPE = modLoc("gem_cutters_knife");
    public static final ResourceLocation GEM_CUTTERS_BLADE_TEMPLATE = modLoc("gem_cutters_blade");
    public static final ResourceLocation HELMET_CHAINMAIL_TEMPLATE = modLoc(ArmorPartData.HELMET_CHAINMAIL);
    public static final ResourceLocation HELMET_PLATE_TEMPLATE = modLoc(ArmorPartData.HELMET_PLATE);
    public static final ResourceLocation CHESTPLATE_CHAINMAIL_TEMPLATE = modLoc(ArmorPartData.CHESTPLATE_CHAINMAIL);
    public static final ResourceLocation CHESTPLATE_BODY_TEMPLATE = modLoc(ArmorPartData.CHESTPLATE_BODY);
    public static final ResourceLocation LEGGINGS_CHAINMAIL_TEMPLATE = modLoc(ArmorPartData.LEGGINGS_CHAINMAIL);
    public static final ResourceLocation LEGGINGS_PLATE_TEMPLATE = modLoc(ArmorPartData.LEGGINGS_PLATE);
    public static final ResourceLocation BOOTS_CHAINMAIL_TEMPLATE = modLoc(ArmorPartData.BOOTS_CHAINMAIL);
    public static final ResourceLocation BOOTS_PLATE_TEMPLATE = modLoc(ArmorPartData.BOOTS_PLATE);
    public static final ResourceLocation LEATHER_HELMET_TEMPLATE = modLoc("leather_helmet");
    public static final ResourceLocation LEATHER_CHESTPLATE_TEMPLATE = modLoc("leather_chestplate");
    public static final ResourceLocation LEATHER_LEGGINGS_TEMPLATE = modLoc("leather_leggings");
    public static final ResourceLocation LEATHER_BOOTS_TEMPLATE = modLoc("leather_boots");
    private static final Map<ResourceLocation, ToolTypeDefinition> TOOL_TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ForgeTemplateDefinition> TEMPLATES = new LinkedHashMap<>();
    private static final List<ToolStatModifier> STAT_MODIFIERS = new ArrayList<>();
    private static final List<ToolStatModifier> DATAPACK_STAT_MODIFIERS = new ArrayList<>();
    private static final Set<ResourceLocation> DATAPACK_TOOL_TYPES = new LinkedHashSet<>();
    private static boolean bootstrapped;

    private ToolTypeRegistry() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        registerBuiltIn(ToolKind.SWORD);
        registerBuiltIn(ToolKind.SHOVEL);
        registerBuiltIn(ToolKind.PICKAXE);
        registerBuiltIn(ToolKind.AXE);
        registerBuiltIn(ToolKind.HOE);
        registerBuiltIn(ToolKind.MATTOCK);
        registerBuiltInTemplates();
    }

    public static synchronized void resetTemplatesToBuiltIns() {
        bootstrap();
        TEMPLATES.clear();
        registerBuiltInTemplates();
    }

    private static void registerBuiltInTemplates() {
        registerTemplate(ForgeTemplate.SWORD_BLADE.definition());
        registerTemplate(ForgeTemplate.SWORD_GUARD.definition());
        registerTemplate(ForgeTemplate.SHOVEL_HEAD.definition());
        registerTemplate(ForgeTemplate.PICKAXE_HEAD.definition());
        registerTemplate(ForgeTemplate.AXE_HEAD.definition());
        registerTemplate(ForgeTemplate.HOE_HEAD.definition());
        registerTemplate(new ForgeTemplateDefinition(
                SMITHING_HAMMER_HEAD_TEMPLATE,
                SMITHING_HAMMER_TOOL_TYPE,
                ToolPartData.SMITHING_HAMMER_HEAD,
                2,
                5,
                "forge_template.mobstoolforging.smithing_hammer_head",
                Float.NaN,
                Set.of(MaterialCatalog.IRON),
                Set.of(),
                SmithingHammerLevel.STONE.level(),
                Map.of(),
                ModItems.SMITHING_HAMMER_HEAD.getId(),
                Map.of(),
                Set.of(SMITHING_HAMMER_TOOL_TYPE),
                true,
                1
        ));
        registerTemplate(new ForgeTemplateDefinition(
                SCREWDRIVER_HEAD_TEMPLATE,
                SCREWDRIVER_TOOL_TYPE,
                ToolPartData.SCREWDRIVER_HEAD,
                1,
                5,
                "forge_template.mobstoolforging.screwdriver_head",
                Float.NaN,
                Set.of(MaterialCatalog.COPPER),
                Set.of(),
                SmithingHammerLevel.STONE.level(),
                Map.of(),
                ModItems.SCREWDRIVER_HEAD.getId(),
                Map.of(),
                Set.of(SCREWDRIVER_TOOL_TYPE),
                true,
                1
        ));
        registerTemplate(new ForgeTemplateDefinition(
                GEM_CUTTERS_BLADE_TEMPLATE,
                GEM_CUTTERS_KNIFE_TOOL_TYPE,
                ToolPartData.GEM_CUTTERS_BLADE,
                1,
                5,
                "forge_template.mobstoolforging.gem_cutters_blade",
                Float.NaN,
                Set.of(MaterialCatalog.COPPER),
                Set.of(),
                SmithingHammerLevel.STONE.level(),
                Map.of(),
                ModItems.GEM_CUTTERS_BLADE.getId(),
                Map.of(),
                Set.of(GEM_CUTTERS_KNIFE_TOOL_TYPE),
                true,
                1
        ));
        registerArmorTemplate(HELMET_CHAINMAIL_TEMPLATE, ArmorConstructionData.HELMET_TYPE, ArmorPartData.HELMET_CHAINMAIL, 2, ModItems.HELMET_CHAINMAIL.getId(), Set.of(MaterialCatalog.IRON));
        registerArmorTemplate(HELMET_PLATE_TEMPLATE, ArmorConstructionData.HELMET_TYPE, ArmorPartData.HELMET_PLATE, 2, ModItems.HELMET_PLATE.getId());
        registerArmorTemplate(CHESTPLATE_CHAINMAIL_TEMPLATE, ArmorConstructionData.CHESTPLATE_TYPE, ArmorPartData.CHESTPLATE_CHAINMAIL, 4, ModItems.CHESTPLATE_CHAINMAIL.getId(), Set.of(MaterialCatalog.IRON));
        registerArmorTemplate(CHESTPLATE_BODY_TEMPLATE, ArmorConstructionData.CHESTPLATE_TYPE, ArmorPartData.CHESTPLATE_BODY, 4, ModItems.CHESTPLATE_BODY.getId());
        registerArmorTemplate(LEGGINGS_CHAINMAIL_TEMPLATE, ArmorConstructionData.LEGGINGS_TYPE, ArmorPartData.LEGGINGS_CHAINMAIL, 3, ModItems.LEGGINGS_CHAINMAIL.getId(), Set.of(MaterialCatalog.IRON));
        registerArmorTemplate(LEGGINGS_PLATE_TEMPLATE, ArmorConstructionData.LEGGINGS_TYPE, ArmorPartData.LEGGINGS_PLATE, 3, ModItems.LEGGINGS_PLATE.getId());
        registerArmorTemplate(BOOTS_CHAINMAIL_TEMPLATE, ArmorConstructionData.BOOTS_TYPE, ArmorPartData.BOOTS_CHAINMAIL, 2, ModItems.BOOTS_CHAINMAIL.getId(), Set.of(MaterialCatalog.IRON));
        registerArmorTemplate(BOOTS_PLATE_TEMPLATE, ArmorConstructionData.BOOTS_TYPE, ArmorPartData.BOOTS_PLATE, 2, ModItems.BOOTS_PLATE.getId());
        registerLeatherArmorTemplate(LEATHER_HELMET_TEMPLATE, ArmorConstructionData.HELMET_TYPE, "leather_helmet", 5);
        registerLeatherArmorTemplate(LEATHER_CHESTPLATE_TEMPLATE, ArmorConstructionData.CHESTPLATE_TYPE, "leather_chestplate", 8);
        registerLeatherArmorTemplate(LEATHER_LEGGINGS_TEMPLATE, ArmorConstructionData.LEGGINGS_TYPE, "leather_leggings", 7);
        registerLeatherArmorTemplate(LEATHER_BOOTS_TEMPLATE, ArmorConstructionData.BOOTS_TYPE, "leather_boots", 4);
    }

    private static void registerArmorTemplate(ResourceLocation templateId, ResourceLocation armorType, String partType, int requiredMaterials, ResourceLocation outputItem) {
        registerArmorTemplate(templateId, armorType, partType, requiredMaterials, outputItem, armorForgeMaterials());
    }

    private static void registerArmorTemplate(ResourceLocation templateId, ResourceLocation armorType, String partType, int requiredMaterials, ResourceLocation outputItem, Set<ResourceLocation> allowedMaterials) {
        registerTemplate(new ForgeTemplateDefinition(
                templateId,
                armorType,
                partType,
                requiredMaterials,
                5,
                "forge_template.mobstoolforging." + partType,
                Float.NaN,
                allowedMaterials,
                Set.of(),
                SmithingHammerLevel.STONE.level(),
                Map.of(),
                outputItem,
                Map.of(),
                Set.of(armorType),
                true,
                1
        ));
    }

    private static void registerLeatherArmorTemplate(ResourceLocation templateId, ResourceLocation armorType, String partType, int requiredMaterials) {
        registerTemplate(new ForgeTemplateDefinition(
                templateId,
                armorType,
                partType,
                requiredMaterials,
                5,
                "forge_template.mobstoolforging." + partType,
                Float.NaN,
                Set.of(),
                Set.of(),
                SmithingHammerLevel.STONE.level(),
                Map.of(),
                null,
                Map.of(),
                Set.of(armorType),
                true,
                1
        ));
    }

    private static Set<ResourceLocation> armorForgeMaterials() {
        return Set.of(
                MaterialCatalog.COPPER,
                MaterialCatalog.IRON,
                MaterialCatalog.GOLD,
                MaterialCatalog.DIAMOND,
                MaterialCatalog.EMERALD,
                MaterialCatalog.RUBY,
                MaterialCatalog.SAPPHIRE
        );
    }

    public static synchronized void registerToolType(ToolTypeDefinition definition) {
        bootstrap();
        TOOL_TYPES.put(definition.id(), definition);
    }

    public static synchronized boolean registerDatapackToolType(ToolTypeDefinition definition) {
        bootstrap();
        if (TOOL_TYPES.containsKey(definition.id()) && !DATAPACK_TOOL_TYPES.contains(definition.id())) {
            MobsToolForging.LOGGER.warn("Skipping datapack tool type {} because a Java or built-in tool type already uses that id.", definition.id());
            return false;
        }
        TOOL_TYPES.put(definition.id(), definition);
        DATAPACK_TOOL_TYPES.add(definition.id());
        return true;
    }

    public static synchronized void resetDatapackToolTypes() {
        bootstrap();
        DATAPACK_TOOL_TYPES.forEach(TOOL_TYPES::remove);
        DATAPACK_TOOL_TYPES.clear();
    }

    public static synchronized void registerTemplate(ForgeTemplateDefinition definition) {
        TEMPLATES.put(definition.id(), definition);
    }

    public static synchronized void registerStatModifier(ToolStatModifier modifier) {
        bootstrap();
        STAT_MODIFIERS.add(modifier);
    }

    public static synchronized void registerDatapackStatModifier(ToolStatModifier modifier) {
        bootstrap();
        DATAPACK_STAT_MODIFIERS.add(modifier);
    }

    public static synchronized void resetDatapackStatModifiers() {
        bootstrap();
        DATAPACK_STAT_MODIFIERS.clear();
    }

    public static List<ToolStatRule> statRules() {
        bootstrap();
        List<ToolStatRule> rules = new ArrayList<>();
        appendStatRules(rules, STAT_MODIFIERS);
        appendStatRules(rules, DATAPACK_STAT_MODIFIERS);
        return List.copyOf(rules);
    }

    public static Optional<ToolTypeDefinition> toolType(ResourceLocation id) {
        bootstrap();
        return Optional.ofNullable(TOOL_TYPES.get(id));
    }

    public static Optional<ToolTypeDefinition> toolType(ToolKind toolKind) {
        return toolType(ToolConstructionData.toolType(toolKind));
    }

    public static Collection<ToolTypeDefinition> toolTypes() {
        bootstrap();
        return List.copyOf(TOOL_TYPES.values());
    }

    public static Optional<ForgeTemplateDefinition> template(ResourceLocation id) {
        bootstrap();
        return Optional.ofNullable(TEMPLATES.get(id));
    }

    public static Optional<ForgeTemplateDefinition> template(String id) {
        ResourceLocation location = id.contains(":")
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, id);
        return template(location);
    }

    public static List<ForgeTemplateDefinition> templates() {
        bootstrap();
        return TEMPLATES.values().stream()
                .sorted(Comparator.comparing(template -> template.id().toString()))
                .toList();
    }

    public static List<ForgeTemplateDefinition> patternStationTemplates() {
        return templates().stream()
                .filter(ForgeTemplateDefinition::patternStationEnabled)
                .filter(template -> template.patternStationPaperCost() > 0)
                .toList();
    }

    public static ItemStack createPart(ForgeTemplateDefinition template, ResourceLocation materialId) {
        return createPart(template, materialId, ToolPartData.DEFAULT_QUALITY);
    }

    public static ItemStack createPart(ForgeTemplateDefinition template, ResourceLocation materialId, int quality) {
        return toolType(template.toolType())
                .map(type -> type.createPart(template.partType(), materialId, quality))
                .orElse(ItemStack.EMPTY);
    }

    static void applyStatModifiers(ToolTypeDefinition definition, ToolConstructionData construction, ToolStatBuilder.MutableStats stats) {
        bootstrap();
        for (ToolStatModifier modifier : STAT_MODIFIERS) {
            modifier.apply(definition, construction, stats);
        }
        for (ToolStatModifier modifier : DATAPACK_STAT_MODIFIERS) {
            modifier.apply(definition, construction, stats);
        }
    }

    private static void registerBuiltIn(ToolKind toolKind) {
        ToolTypeDefinition.Builder builder = ToolTypeDefinition.builder(ToolConstructionData.toolType(toolKind), toolKind.partType())
                .builtInKind(toolKind)
                .toolItem(toolKind.toolItem()::get)
                .partItem(toolKind.partType(), toolKind.partItem()::get);
        if (toolKind == ToolKind.SWORD) {
            builder.requiredAssemblyPart(ToolPartData.SWORD_GUARD, ModItems.SWORD_GUARD::get);
        } else if (toolKind == ToolKind.MATTOCK) {
            builder.requiredAssemblyPart(ToolPartData.HOE_HEAD, ModItems.HOE_HEAD::get)
                    .averageRequiredPartQuality(true)
                    .averageRequiredHeadDurability(true);
        }
        TOOL_TYPES.put(ToolConstructionData.toolType(toolKind), builder.build());
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static void appendStatRules(List<ToolStatRule> rules, List<ToolStatModifier> modifiers) {
        modifiers.stream()
                .filter(ToolStatRule.class::isInstance)
                .map(ToolStatRule.class::cast)
                .forEach(rules::add);
    }

    @FunctionalInterface
    public interface ToolStatModifier {
        void apply(ToolTypeDefinition definition, ToolConstructionData construction, ToolStatBuilder.MutableStats stats);
    }
}

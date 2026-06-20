package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class ToolTypeRegistry {
    private static final Map<ResourceLocation, ToolTypeDefinition> TOOL_TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ForgeTemplateDefinition> TEMPLATES = new LinkedHashMap<>();
    private static final List<ToolStatModifier> STAT_MODIFIERS = new ArrayList<>();
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
    }

    public static synchronized void registerToolType(ToolTypeDefinition definition) {
        bootstrap();
        TOOL_TYPES.put(definition.id(), definition);
    }

    public static synchronized void registerTemplate(ForgeTemplateDefinition definition) {
        TEMPLATES.put(definition.id(), definition);
    }

    public static synchronized void registerStatModifier(ToolStatModifier modifier) {
        bootstrap();
        STAT_MODIFIERS.add(modifier);
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

    public static ItemStack createPart(ForgeTemplateDefinition template, ResourceLocation materialId) {
        return toolType(template.toolType())
                .map(type -> type.createPart(template.partType(), materialId))
                .orElse(ItemStack.EMPTY);
    }

    static void applyStatModifiers(ToolTypeDefinition definition, ToolConstructionData construction, ToolStatBuilder.MutableStats stats) {
        bootstrap();
        for (ToolStatModifier modifier : STAT_MODIFIERS) {
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
        }
        TOOL_TYPES.put(ToolConstructionData.toolType(toolKind), builder.build());
    }

    @FunctionalInterface
    public interface ToolStatModifier {
        void apply(ToolTypeDefinition definition, ToolConstructionData construction, ToolStatBuilder.MutableStats stats);
    }
}

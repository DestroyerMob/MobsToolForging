package org.destroyermob.mobstoolforging.world;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

/**
 * Resolves an assembled item's affixes from the physical components retained in its assembly data.
 * Each affix is evaluated on a temporary copy of the completed item, preserving the original
 * component's rarity while using the completed item's equipment category and slot behavior.
 */
public final class CompositeAffixCompatibility {
    private static final String APOTH_COMPONENTS = "dev.shadowsoffire.apotheosis.Apoth$Components";
    private static final String AFFIX_HELPER = "dev.shadowsoffire.apotheosis.affix.AffixHelper";

    private static ApotheosisComponents apotheosisComponents;
    private static boolean apotheosisComponentsUnavailable;
    private static Method getAffixes;
    private static Method affixLevel;

    private CompositeAffixCompatibility() {
    }

    /**
     * Returns a replacement map only when at least one stored physical component owns affixes.
     * Raw types keep this optional integration safe when Apotheosis is absent.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Optional<Map> affixesFor(ItemStack assembled) {
        ToolAssemblyParts assemblyParts = assembled.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (assemblyParts == null || assemblyParts.stacks().isEmpty()) {
            return Optional.empty();
        }

        ApotheosisComponents components = components();
        Method resolver = affixResolver();
        if (components == null || resolver == null) {
            return Optional.empty();
        }

        Map<Object, Object> resolved = new LinkedHashMap<>();
        for (ItemStack part : assemblyParts.stacks()) {
            if (!part.has(components.affixes())) {
                continue;
            }

            ItemStack proxy = affixProxy(assembled, part, components);
            for (Map.Entry<?, ?> entry : invokeAffixResolver(resolver, proxy).entrySet()) {
                resolved.merge(entry.getKey(), entry.getValue(), CompositeAffixCompatibility::strongerInstance);
            }
        }
        return resolved.isEmpty() ? Optional.empty() : Optional.of(Map.copyOf(resolved));
    }

    /**
     * Identifies the physical components that currently contribute Apothic affixes.
     * This is deliberately separate from the aggregate resolver so the tooltip can explain
     * ownership without exposing or rewriting Apothic's own affix presentation.
     */
    public static List<ItemStack> affixedParts(ItemStack assembled) {
        ToolAssemblyParts assemblyParts = assembled.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (assemblyParts == null) {
            return List.of();
        }
        ApotheosisComponents components = components();
        if (components == null) {
            return List.of();
        }
        return assemblyParts.stacks().stream()
                .filter(part -> part.has(components.affixes()))
                .map(ItemStack::copy)
                .toList();
    }

    public static boolean hasComponentAffixes(ItemStack assembled) {
        ToolAssemblyParts assemblyParts = assembled.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (assemblyParts == null) {
            return false;
        }
        ApotheosisComponents components = components();
        return components != null
                && assemblyParts.stacks().stream().anyMatch(part -> part.has(components.affixes()));
    }

    public static boolean hasDirectAffixes(ItemStack stack) {
        ApotheosisComponents components = components();
        return components != null && !stack.isEmpty() && stack.has(components.affixes());
    }

    /**
     * Mirrors one contributing part's presentation components onto the completed item.
     * The stored parts remain authoritative and {@link #affixesFor(ItemStack)} still supplies
     * the aggregate affix map, but Apotheosis checks these top-level components before it asks
     * for that map in several tooltip and event paths.
     */
    public static boolean syncCompatibilityMirror(ItemStack assembled) {
        ToolAssemblyParts assemblyParts = assembled.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (assemblyParts == null) {
            return false;
        }
        ApotheosisComponents components = components();
        if (components == null) {
            return false;
        }
        ItemStack contributor = assemblyParts.stacks().stream()
                .filter(part -> part.has(components.affixes()))
                .findFirst()
                .orElse(ItemStack.EMPTY);
        if (contributor.isEmpty()) {
            return false;
        }
        boolean changed = copyComponentIfDifferent(contributor, assembled, components.affixes());
        changed |= copyComponentIfDifferent(contributor, assembled, components.rarity());
        changed |= copyComponentIfDifferent(contributor, assembled, components.affixName());
        return changed;
    }

    private static ItemStack affixProxy(ItemStack assembled, ItemStack part, ApotheosisComponents components) {
        ItemStack proxy = assembled.copy();
        proxy.remove(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        proxy.remove(components.affixes());
        proxy.remove(components.rarity());
        proxy.remove(components.affixName());
        copyComponent(part, proxy, components.affixes());
        copyComponent(part, proxy, components.rarity());
        copyComponent(part, proxy, components.affixName());
        return proxy;
    }

    private static Object strongerInstance(Object current, Object candidate) {
        return affixLevel(candidate) > affixLevel(current) ? candidate : current;
    }

    private static float affixLevel(Object instance) {
        Method accessor = affixLevelAccessor();
        if (accessor == null) {
            return 0.0F;
        }
        try {
            Object value = accessor.invoke(instance);
            return value instanceof Float level ? level : 0.0F;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return 0.0F;
        }
    }

    private static Map<?, ?> invokeAffixResolver(Method resolver, ItemStack stack) {
        try {
            Object value = resolver.invoke(null, stack);
            return value instanceof Map<?, ?> affixes ? affixes : Map.of();
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return Map.of();
        }
    }

    private static ApotheosisComponents components() {
        if (apotheosisComponents != null) {
            return apotheosisComponents;
        }
        if (apotheosisComponentsUnavailable) {
            return null;
        }
        try {
            Class<?> type = Class.forName(APOTH_COMPONENTS);
            apotheosisComponents = new ApotheosisComponents(
                    component(type, "AFFIXES"),
                    component(type, "RARITY"),
                    component(type, "AFFIX_NAME")
            );
            return apotheosisComponents;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            apotheosisComponentsUnavailable = true;
            return null;
        }
    }

    private static Method affixResolver() {
        if (getAffixes != null) {
            return getAffixes;
        }
        try {
            getAffixes = Class.forName(AFFIX_HELPER).getMethod("getAffixes", ItemStack.class);
            return getAffixes;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Method affixLevelAccessor() {
        if (affixLevel != null) {
            return affixLevel;
        }
        try {
            affixLevel = Class.forName("dev.shadowsoffire.apotheosis.affix.AffixInstance").getMethod("level");
            return affixLevel;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static DataComponentType<Object> component(Class<?> owner, String name) throws ReflectiveOperationException {
        Field field = owner.getField(name);
        return (DataComponentType<Object>) field.get(null);
    }

    private static <T> void copyComponent(ItemStack source, ItemStack target, DataComponentType<T> component) {
        T value = source.get(component);
        if (value != null) {
            target.set(component, value);
        }
    }

    private static <T> boolean copyComponentIfDifferent(ItemStack source, ItemStack target, DataComponentType<T> component) {
        T value = source.get(component);
        if (value == null || Objects.equals(value, target.get(component))) {
            return false;
        }
        target.set(component, value);
        return true;
    }

    private record ApotheosisComponents(
            DataComponentType<Object> affixes,
            DataComponentType<Object> rarity,
            DataComponentType<Object> affixName
    ) {
    }
}

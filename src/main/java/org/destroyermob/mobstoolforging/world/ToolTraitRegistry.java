package org.destroyermob.mobstoolforging.world;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ToolTraitRegistry {
    private static final Map<ResourceLocation, ToolTraitDefinition> TRAITS = new LinkedHashMap<>();
    private static final Set<ResourceLocation> DATAPACK_TRAITS = new LinkedHashSet<>();
    private static boolean bootstrapped;

    private ToolTraitRegistry() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        for (ToolTrait trait : ToolTrait.values()) {
            TRAITS.put(trait.id(), ToolTraitDefinition.from(trait));
        }
    }

    public static synchronized void registerTrait(ToolTraitDefinition definition) {
        bootstrap();
        TRAITS.put(definition.id(), definition);
    }

    public static synchronized void registerDatapackTrait(ToolTraitDefinition definition) {
        registerTrait(definition);
        DATAPACK_TRAITS.add(definition.id());
    }

    public static synchronized void resetDatapackTraits() {
        bootstrap();
        DATAPACK_TRAITS.forEach(TRAITS::remove);
        DATAPACK_TRAITS.clear();
    }

    public static Optional<ToolTraitDefinition> definition(ResourceLocation id) {
        bootstrap();
        return Optional.ofNullable(TRAITS.get(id));
    }

    public static Collection<ToolTraitDefinition> definitions() {
        bootstrap();
        return List.copyOf(TRAITS.values());
    }
}

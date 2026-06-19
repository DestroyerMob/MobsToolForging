package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ToolTraitDisplay {
    private ToolTraitDisplay() {
    }

    public static List<ResourceLocation> resolve(List<ResourceLocation> rawTraits) {
        Set<ResourceLocation> unique = new LinkedHashSet<>(rawTraits);
        if (unique.contains(ToolTrait.NETHER_TREATED.id()) || unique.contains(ToolTrait.NETHER_FORGED.id())) {
            unique.remove(ToolTrait.KINDLED.id());
        }
        List<ResourceLocation> resolved = new ArrayList<>(unique);
        resolved.sort(Comparator.comparingInt(ToolTraitDisplay::priority));
        return List.copyOf(resolved);
    }

    private static int priority(ResourceLocation traitId) {
        return ToolTrait.byId(traitId)
                .flatMap(ToolTrait::category)
                .map(ToolTraitDisplay::categoryPriority)
                .orElse(50);
    }

    private static int categoryPriority(String category) {
        return switch (category) {
            case "treatment" -> 0;
            case "structure" -> 10;
            case "handling", "heat" -> 20;
            case "affinity", "focus" -> 30;
            case "quality" -> 40;
            default -> 50;
        };
    }
}

package org.destroyermob.mobstoolforging.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class FoundryMeltingPointRegistry {
    public static final float DEFAULT_MELTING_POINT_C = 1000.0F;
    private static Map<ResourceLocation, FoundryMeltingPoint> byMaterial = Map.of();

    private FoundryMeltingPointRegistry() {
    }

    public static synchronized void replace(Map<ResourceLocation, FoundryMeltingPoint> loaded) {
        Map<ResourceLocation, FoundryMeltingPoint> next = new LinkedHashMap<>();
        loaded.values().forEach(point -> next.put(point.material(), point));
        byMaterial = Map.copyOf(next);
    }

    public static synchronized Map<ResourceLocation, FoundryMeltingPoint> snapshot() {
        Map<ResourceLocation, FoundryMeltingPoint> snapshot = new LinkedHashMap<>();
        byMaterial.values().forEach(point -> snapshot.put(point.id(), point));
        return snapshot;
    }

    public static List<FoundryMeltingPoint> values() {
        return List.copyOf(byMaterial.values());
    }

    public static float celsius(ResourceLocation material) {
        FoundryMeltingPoint point = byMaterial.get(material);
        return point == null ? DEFAULT_MELTING_POINT_C : point.celsius();
    }
}

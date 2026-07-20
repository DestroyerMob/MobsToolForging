package org.destroyermob.mobstoolforging.world;

import net.minecraft.resources.ResourceLocation;

/** A material's datapack-defined melting or foundry processing point in degrees Celsius. */
public record FoundryMeltingPoint(ResourceLocation id, ResourceLocation material, float celsius) {
    public FoundryMeltingPoint {
        celsius = Math.max(0.0F, celsius);
    }
}

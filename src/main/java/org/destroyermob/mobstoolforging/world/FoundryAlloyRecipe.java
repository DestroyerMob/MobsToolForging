package org.destroyermob.mobstoolforging.world;

import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.resources.ResourceLocation;

public record FoundryAlloyRecipe(
        ResourceLocation id,
        ResourceLocation result,
        Map<ResourceLocation, Integer> inputs,
        int outputAmountMb
) {
    public FoundryAlloyRecipe(ResourceLocation id, ResourceLocation result, Map<ResourceLocation, Integer> inputs) {
        this(id, result, inputs, inputs.values().stream().mapToInt(Integer::intValue).sum());
    }

    public FoundryAlloyRecipe {
        if (id == null || result == null) {
            throw new IllegalArgumentException("Foundry alloy recipe needs an id and result material");
        }
        inputs = Map.copyOf(inputs);
        if (inputs.size() < 2) {
            throw new IllegalArgumentException("Foundry alloy recipe needs at least two different input materials");
        }
        if (inputs.containsKey(result)) {
            throw new IllegalArgumentException("Foundry alloy result cannot also be one of its inputs");
        }
        inputs.forEach((material, amount) -> {
            if (material == null || amount == null || amount <= 0) {
                throw new IllegalArgumentException("Foundry alloy input amounts must be positive");
            }
        });
        if (outputAmountMb <= 0) {
            throw new IllegalArgumentException("Foundry alloy output amount must be positive");
        }
        long inputVolumeMb = inputs.values().stream().mapToLong(Integer::longValue).sum();
        if (outputAmountMb > inputVolumeMb) {
            throw new IllegalArgumentException(
                    "Foundry alloy output cannot exceed its total input volume ("
                            + outputAmountMb + " > " + inputVolumeMb + " mB)"
            );
        }
    }

    public int craftableBatches(ToIntFunction<ResourceLocation> availableAmount) {
        int batches = Integer.MAX_VALUE;
        for (Map.Entry<ResourceLocation, Integer> input : inputs.entrySet()) {
            batches = Math.min(batches, availableAmount.applyAsInt(input.getKey()) / input.getValue());
        }
        return Math.max(0, batches);
    }
}

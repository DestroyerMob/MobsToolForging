package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** A datapack-defined fluid fuel and the heat delivered by one consumed batch. */
public record FoundryFuelRecipe(
        ResourceLocation id,
        Input input,
        float temperatureC,
        int amountMb,
        int burnTicks
) {
    public FoundryFuelRecipe {
        temperatureC = Math.max(0.0F, temperatureC);
        amountMb = Math.max(1, amountMb);
        burnTicks = Math.max(1, burnTicks);
    }

    public boolean matches(FluidStack stack) {
        return !stack.isEmpty() && input.matches(stack.getFluid());
    }

    public record Input(Optional<ResourceLocation> fluidId, Optional<TagKey<Fluid>> tag) {
        public boolean matches(Fluid fluid) {
            if (fluidId.isPresent() && BuiltInRegistries.FLUID.getKey(fluid).equals(fluidId.get())) {
                return true;
            }
            return tag.isPresent() && BuiltInRegistries.FLUID.wrapAsHolder(fluid).is(tag.get());
        }

        public boolean isFluid() {
            return fluidId.isPresent();
        }

        public static Input fluid(ResourceLocation id) {
            return new Input(Optional.of(id), Optional.empty());
        }

        public static Input tag(ResourceLocation id) {
            return new Input(Optional.empty(), Optional.of(TagKey.create(Registries.FLUID, id)));
        }
    }
}

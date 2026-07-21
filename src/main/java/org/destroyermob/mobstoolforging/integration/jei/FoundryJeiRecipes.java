package org.destroyermob.mobstoolforging.integration.jei;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** Client-side display models for the foundry's datapack registries. */
public final class FoundryJeiRecipes {
    private FoundryJeiRecipes() {
    }

    public record Melting(
            ResourceLocation id,
            List<ItemStack> inputs,
            ItemStack materialDisplay,
            Component materialName,
            int amountMb,
            int ticks,
            float temperatureC
    ) {
        public Melting {
            inputs = List.copyOf(inputs);
            materialDisplay = materialDisplay.copy();
        }
    }

    public record Alloying(
            ResourceLocation id,
            List<MaterialAmount> inputs,
            ItemStack resultDisplay,
            Component resultName,
            int outputAmountMb
    ) {
        public Alloying {
            inputs = List.copyOf(inputs);
            resultDisplay = resultDisplay.copy();
        }
    }

    public record MaterialAmount(
            ResourceLocation material,
            List<ItemStack> displays,
            Component name,
            int amountMb
    ) {
        public MaterialAmount {
            displays = List.copyOf(displays);
        }
    }

    public record Casting(
            ResourceLocation id,
            Kind kind,
            List<ItemStack> forms,
            List<ItemStack> materialDisplays,
            Component materialName,
            int amountMb,
            ItemStack output
    ) {
        public Casting {
            forms = List.copyOf(forms);
            materialDisplays = List.copyOf(materialDisplays);
            output = output.copy();
        }

        public enum Kind {
            CREATE_CAST,
            CAST_PART,
            CAST_INGOT,
            CAST_BLOCK
        }
    }

    public record Fuel(
            ResourceLocation id,
            List<Fluid> fluids,
            int amountMb,
            int burnTicks,
            float temperatureC
    ) {
        public Fuel {
            fluids = List.copyOf(fluids);
        }
    }
}

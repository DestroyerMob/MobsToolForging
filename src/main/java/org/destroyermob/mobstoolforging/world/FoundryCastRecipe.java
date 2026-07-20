package org.destroyermob.mobstoolforging.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.item.CastingMoldItem;
import org.destroyermob.mobstoolforging.registry.ModItems;

public record FoundryCastRecipe(
        ResourceLocation id,
        FoundryMeltingRecipe.Input input,
        ResourceLocation template,
        int goldAmountMb,
        int amountMb
) {
    public FoundryCastRecipe {
        goldAmountMb = Math.max(1, goldAmountMb);
        amountMb = Math.max(1, amountMb);
    }

    public boolean matchesInput(ItemStack stack) {
        return input.matches(stack);
    }

    public boolean matchesCast(ItemStack stack) {
        return stack.is(ModItems.CASTING_MOLD.get())
                && stack.getItem() instanceof CastingMoldItem mold
                && mold.templateId(stack).filter(template::equals).isPresent();
    }
}

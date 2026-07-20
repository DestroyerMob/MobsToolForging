package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record FoundryMeltingRecipe(
        ResourceLocation id,
        Input input,
        ResourceLocation material,
        int amountMb,
        int ticks
) {
    public FoundryMeltingRecipe {
        amountMb = Math.max(1, amountMb);
        ticks = Math.max(1, ticks);
    }

    public boolean matches(ItemStack stack) {
        return input.matches(stack);
    }

    public record Input(Optional<ResourceLocation> itemId, Optional<TagKey<Item>> tag) {
        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (itemId.isPresent() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId.get())) {
                return true;
            }
            return tag.isPresent() && stack.is(tag.get());
        }

        public boolean isItem() {
            return itemId.isPresent();
        }

        public static Input item(ResourceLocation id) {
            return new Input(Optional.of(id), Optional.empty());
        }

        public static Input tag(ResourceLocation id) {
            return new Input(Optional.empty(), Optional.of(TagKey.create(Registries.ITEM, id)));
        }
    }
}

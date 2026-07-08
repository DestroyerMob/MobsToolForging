package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record DryingRecipe(
        ResourceLocation id,
        Input input,
        ItemStack output,
        int ticks
) {
    public DryingRecipe {
        output = output.copy();
        ticks = Math.max(1, ticks);
    }

    public boolean matches(ItemStack stack) {
        return input.matches(stack);
    }

    public ItemStack inputCopy(ItemStack source) {
        return source.copyWithCount(input.count());
    }

    public ItemStack outputCopy() {
        return output.copy();
    }

    public record Input(Optional<ResourceLocation> itemId, Optional<TagKey<Item>> tag, int count) {
        public Input {
            count = Math.max(1, count);
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty() || stack.getCount() < count) {
                return false;
            }
            if (itemId.isPresent() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId.get())) {
                return true;
            }
            return tag.isPresent() && stack.is(tag.get());
        }

        @Nullable
        public ResourceLocation tagId() {
            return tag.map(TagKey::location).orElse(null);
        }

        public static Input item(ResourceLocation itemId) {
            return item(itemId, 1);
        }

        public static Input item(ResourceLocation itemId, int count) {
            return new Input(Optional.of(itemId), Optional.empty(), count);
        }

        public static Input tag(ResourceLocation tagId) {
            return tag(tagId, 1);
        }

        public static Input tag(ResourceLocation tagId, int count) {
            return new Input(Optional.empty(), Optional.of(TagKey.create(Registries.ITEM, tagId)), count);
        }
    }
}

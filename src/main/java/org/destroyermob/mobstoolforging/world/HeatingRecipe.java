package org.destroyermob.mobstoolforging.world;

import java.util.EnumSet;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record HeatingRecipe(
        ResourceLocation id,
        Input input,
        EnumSet<HeatingSource> sources,
        float targetTemperature,
        int ticks,
        boolean workable
) {
    public HeatingRecipe {
        sources = sources.isEmpty() ? EnumSet.allOf(HeatingSource.class) : EnumSet.copyOf(sources);
        targetTemperature = clamp(targetTemperature);
        ticks = Math.max(1, ticks);
    }

    public boolean matches(HeatingSource source, ItemStack stack) {
        return sources.contains(source) && input.matches(stack);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public record Input(Optional<ResourceLocation> itemId, Optional<TagKey<Item>> tag, int count) {
        public Input {
            count = Math.max(1, count);
        }

        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
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

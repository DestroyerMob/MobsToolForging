package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record StationWorkRecipe(
        ResourceLocation id,
        WorkstationKind workstationKind,
        Optional<ResourceLocation> patternId,
        Input input,
        Optional<Input> secondaryInput,
        ItemStack output,
        int requiredHits,
        int minimumHammerLevel,
        QualityMode qualityMode
) {
    public StationWorkRecipe {
        output = output.copy();
        secondaryInput = secondaryInput == null ? Optional.empty() : secondaryInput;
        requiredHits = Math.max(1, requiredHits);
        minimumHammerLevel = Math.max(0, minimumHammerLevel);
        qualityMode = qualityMode == null ? QualityMode.NONE : qualityMode;
    }

    public StationWorkRecipe(
            ResourceLocation id,
            WorkstationKind workstationKind,
            Optional<ResourceLocation> patternId,
            Input input,
            Optional<Input> secondaryInput,
            ItemStack output,
            int requiredHits,
            int minimumHammerLevel
    ) {
        this(id, workstationKind, patternId, input, secondaryInput, output, requiredHits, minimumHammerLevel, QualityMode.NONE);
    }

    public boolean canStart(WorkstationKind kind, @Nullable ResourceLocation selectedPattern, ItemStack stack) {
        if (kind != workstationKind || !input.matches(stack)) {
            return false;
        }
        return patternId.map(pattern -> pattern.equals(selectedPattern)).orElse(selectedPattern == null);
    }

    public ItemStack outputCopy() {
        return output.copy();
    }

    public boolean requiresSecondaryInput() {
        return secondaryInput.isPresent();
    }

    public enum QualityMode {
        NONE,
        STATIC,
        TIMED;

        public boolean appliesQuality() {
            return this != NONE;
        }

        public boolean usesTimingQte() {
            return this == TIMED;
        }

        public static QualityMode parse(String value) {
            String normalized = value == null ? "none" : value.trim().toUpperCase(Locale.ROOT);
            try {
                return valueOf(normalized);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Unknown station work quality_mode '" + value + "'. Expected none, static, or timed.", exception);
            }
        }
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

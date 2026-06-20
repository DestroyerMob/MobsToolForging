package org.destroyermob.mobstoolforging.world;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public record ToolTraitDefinition(
        ResourceLocation id,
        String translationKey,
        String descriptionTranslationKey,
        ChatFormatting color,
        @Nullable String category,
        List<ResourceLocation> suppresses
) {
    public ToolTraitDefinition(ResourceLocation id, String translationKey, String descriptionTranslationKey, ChatFormatting color, @Nullable String category) {
        this(id, translationKey, descriptionTranslationKey, color, category, List.of());
    }

    public ToolTraitDefinition {
        suppresses = List.copyOf(suppresses);
    }

    public static ToolTraitDefinition from(ToolTrait trait) {
        return new ToolTraitDefinition(
                trait.id(),
                trait.translationKey(),
                trait.descriptionTranslationKey(),
                trait.color(),
                trait.category().orElse(null),
                List.of()
        );
    }

    public Optional<String> categoryName() {
        return Optional.ofNullable(category);
    }

    public MutableComponent displayName() {
        return Component.translatable(translationKey).withStyle(color);
    }

    public MutableComponent description() {
        return Component.translatable(descriptionTranslationKey).withStyle(ChatFormatting.GRAY);
    }
}

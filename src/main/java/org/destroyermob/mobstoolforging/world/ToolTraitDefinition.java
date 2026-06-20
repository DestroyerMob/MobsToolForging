package org.destroyermob.mobstoolforging.world;

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
        @Nullable String category
) {
    public static ToolTraitDefinition from(ToolTrait trait) {
        return new ToolTraitDefinition(
                trait.id(),
                trait.translationKey(),
                trait.descriptionTranslationKey(),
                trait.color(),
                trait.category().orElse(null)
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

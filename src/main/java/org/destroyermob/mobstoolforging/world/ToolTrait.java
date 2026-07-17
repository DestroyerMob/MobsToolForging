package org.destroyermob.mobstoolforging.world;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public enum ToolTrait {
    STEADY("steady", ChatFormatting.DARK_GREEN, "handling"),
    SWIFT("swift", ChatFormatting.AQUA, "handling"),
    KINDLED("kindled", ChatFormatting.GOLD, "handling"),
    REINFORCED("reinforced", ChatFormatting.GRAY, "structure"),
    RESONANT("resonant", ChatFormatting.LIGHT_PURPLE, "affinity"),
    WORK_HARDENED("work_hardened", ChatFormatting.GOLD, "structure"),
    GILDED("gilded", ChatFormatting.GOLD, "affinity"),
    ADAMANT("adamant", ChatFormatting.AQUA, "edge"),
    NETHER_FORGED("nether_forged", ChatFormatting.DARK_RED, "structure"),
    FORTUNATE("fortunate", ChatFormatting.GREEN, "affinity"),
    KEEN("keen", ChatFormatting.RED, "edge"),
    FORCEFUL("forceful", ChatFormatting.YELLOW, "edge"),
    JAGGED("jagged", ChatFormatting.DARK_GRAY, "edge"),
    TENSIONED("tensioned", ChatFormatting.DARK_GREEN, "handling"),
    FOCUSED("focused", ChatFormatting.LIGHT_PURPLE, "affinity"),
    WORKMANSHIP_GOOD("workmanship_good", ChatFormatting.GREEN, "quality"),
    WORKMANSHIP_ROUGH("workmanship_rough", ChatFormatting.RED, "quality");

    private static final Map<ResourceLocation, ToolTrait> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ToolTrait::id, Function.identity()));

    private final ResourceLocation id;
    private final ChatFormatting color;
    @Nullable
    private final String category;

    ToolTrait(String path, ChatFormatting color, @Nullable String category) {
        this.id = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
        this.color = color;
        this.category = category;
    }

    public ResourceLocation id() {
        return id;
    }

    public String translationKey() {
        return "tooltip.mobstoolforging.trait." + id.getPath();
    }

    public String descriptionTranslationKey() {
        return translationKey() + ".desc";
    }

    public ChatFormatting color() {
        return color;
    }

    public Optional<String> category() {
        return Optional.ofNullable(category);
    }

    public MutableComponent displayName() {
        return Component.translatable(translationKey()).withStyle(color);
    }

    public MutableComponent description() {
        return Component.translatable(descriptionTranslationKey()).withStyle(ChatFormatting.GRAY);
    }

    public static Optional<ToolTrait> byId(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static String fallbackName(ResourceLocation id) {
        String[] words = id.getPath().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.isEmpty() ? id.toString() : builder.toString();
    }
}

package org.destroyermob.mobstoolforging.world;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModItems;

public enum SmithingHammerLevel {
    STONE(0, "stone", "message.mobstoolforging.hammer_level.stone"),
    IRON(1, "iron", "message.mobstoolforging.hammer_level.iron");

    private final int level;
    private final String id;
    private final String translationKey;

    SmithingHammerLevel(int level, String id, String translationKey) {
        this.level = level;
        this.id = id;
        this.translationKey = translationKey;
    }

    public int level() {
        return level;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public boolean meets(int requiredLevel) {
        return level >= requiredLevel;
    }

    public static boolean isHammer(ItemStack stack) {
        return levelOf(stack) >= STONE.level;
    }

    public static int levelOf(ItemStack stack) {
        if (stack.is(ModItems.IRON_SMITHING_HAMMER.get())) {
            return IRON.level;
        }
        if (stack.is(ModItems.SMITHING_HAMMER.get())) {
            return STONE.level;
        }
        return -1;
    }

    public static int parseLevel(String id, int fallback) {
        String normalized = id.toLowerCase(Locale.ROOT);
        for (SmithingHammerLevel level : values()) {
            if (level.id.equals(normalized)) {
                return level.level;
            }
        }
        try {
            return Math.max(0, Integer.parseInt(normalized));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static Component displayName(int level) {
        SmithingHammerLevel closest = STONE;
        for (SmithingHammerLevel hammerLevel : values()) {
            if (hammerLevel.level <= level && hammerLevel.level >= closest.level) {
                closest = hammerLevel;
            }
        }
        return closest.displayName();
    }
}

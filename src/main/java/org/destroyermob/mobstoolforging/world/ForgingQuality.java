package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum ForgingQuality implements StringRepresentable {
    CRUDE("crude", 40, 0.82F, 0.97F, 0.00F, ChatFormatting.RED),
    WORKED("worked", 75, 0.94F, 0.99F, 0.00F, ChatFormatting.GRAY),
    WELL_FORGED("well_forged", 100, 1.00F, 1.00F, 0.00F, ChatFormatting.WHITE),
    FINE("fine", 125, 1.08F, 1.02F, 0.05F, ChatFormatting.GREEN),
    MASTERWORK("masterwork", 155, 1.18F, 1.03F, 0.10F, ChatFormatting.GOLD);

    public static final Codec<ForgingQuality> CODEC = StringRepresentable.fromEnum(ForgingQuality::values);
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 200;
    public static final int DEFAULT_SCORE = WELL_FORGED.score;

    private final String serializedName;
    private final int score;
    private final float durabilityMultiplier;
    private final float miningSpeedMultiplier;
    private final float attackDamageBonus;
    private final ChatFormatting color;

    ForgingQuality(String serializedName, int score, float durabilityMultiplier, float miningSpeedMultiplier, float attackDamageBonus, ChatFormatting color) {
        this.serializedName = serializedName;
        this.score = score;
        this.durabilityMultiplier = durabilityMultiplier;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.attackDamageBonus = attackDamageBonus;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public int score() {
        return score;
    }

    public float durabilityMultiplier() {
        return durabilityMultiplier;
    }

    public float miningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    public float attackDamageBonus() {
        return attackDamageBonus;
    }

    public Component displayName() {
        return Component.translatable("quality.mobstoolforging." + serializedName).withStyle(color);
    }

    public ForgingQuality previous() {
        int index = ordinal();
        return index <= 0 ? this : values()[index - 1];
    }

    public static ForgingQuality fromScore(int score) {
        int clamped = clampScore(score);
        if (clamped < 60) {
            return CRUDE;
        }
        if (clamped < 90) {
            return WORKED;
        }
        if (clamped < 120) {
            return WELL_FORGED;
        }
        if (clamped < 145) {
            return FINE;
        }
        return MASTERWORK;
    }

    public static int clampScore(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    public static int scoreFor(ForgingQuality quality) {
        return quality == null ? DEFAULT_SCORE : quality.score();
    }

    public static ForgingQuality parse(String value, ForgingQuality fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (ForgingQuality quality : values()) {
            if (quality.serializedName.equals(normalized) || quality.name().equalsIgnoreCase(normalized)) {
                return quality;
            }
        }
        return fallback;
    }
}

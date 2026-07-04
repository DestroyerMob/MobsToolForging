package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum HeatLevel implements StringRepresentable {
    NONE("none", 0.0F, -25),
    LOW("low", 0.55F, -12),
    HOT("hot", 1.0F, 8),
    HIGH("high", 1.0F, 16);

    public static final Codec<HeatLevel> CODEC = StringRepresentable.fromEnum(HeatLevel::values);

    private final String serializedName;
    private final float temperature;
    private final int qualityBonus;

    HeatLevel(String serializedName, float temperature, int qualityBonus) {
        this.serializedName = serializedName;
        this.temperature = temperature;
        this.qualityBonus = qualityBonus;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public float temperature() {
        return temperature;
    }

    public int qualityBonus() {
        return qualityBonus;
    }

    public boolean atLeast(HeatLevel other) {
        return ordinal() >= other.ordinal();
    }

    public static HeatLevel fromTemperature(float temperature) {
        if (temperature >= 0.95F) {
            return HOT;
        }
        if (temperature > 0.0F) {
            return LOW;
        }
        return NONE;
    }

    public static HeatLevel parse(String value, HeatLevel fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (HeatLevel level : values()) {
            if (level.serializedName.equals(normalized) || level.name().equalsIgnoreCase(normalized)) {
                return level;
            }
        }
        return fallback;
    }
}

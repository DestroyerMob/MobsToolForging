package org.destroyermob.mobstoolforging.world;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum HeatingSource implements StringRepresentable {
    CAMPFIRE("campfire"),
    FORGE("forge");

    private final String serializedName;

    HeatingSource(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static HeatingSource parse(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("heating_forge") || normalized.equals("forge")) {
            return FORGE;
        }
        if (normalized.equals("campfire") || normalized.equals("soul_campfire")) {
            return CAMPFIRE;
        }
        throw new IllegalArgumentException("Unknown heating source " + value);
    }
}

package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;

public enum ToolPartFinish implements StringRepresentable {
    UNPOLISHED("unpolished"),
    POLISHED("polished");

    public static final Codec<ToolPartFinish> CODEC = StringRepresentable.fromEnum(ToolPartFinish::values);

    private final String serializedName;

    ToolPartFinish(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable("finish.mobstoolforging." + serializedName);
    }

    public int effectiveQualityScore(int score, boolean capApplies) {
        int clamped = ForgingQuality.clampScore(score);
        if (this != UNPOLISHED || !capApplies || !MobsToolForgingConfig.ENABLE_QUALITY.get()) {
            return clamped;
        }
        ForgingQuality rawQuality = ForgingQuality.fromScore(clamped);
        if (rawQuality == ForgingQuality.CRUDE) {
            return clamped;
        }
        return rawQuality.previous().score();
    }
}

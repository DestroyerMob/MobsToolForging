package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HeatedWorkpieceData(long expiresAtGameTime, float temperature, long lastUpdateGameTime, boolean workable) {
    public static final Codec<HeatedWorkpieceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("expires_at_game_time", 0L).forGetter(HeatedWorkpieceData::expiresAtGameTime),
            Codec.FLOAT.optionalFieldOf("temperature", 1.0F).forGetter(HeatedWorkpieceData::temperature),
            Codec.LONG.optionalFieldOf("last_update_game_time", 0L).forGetter(HeatedWorkpieceData::lastUpdateGameTime),
            Codec.BOOL.optionalFieldOf("workable", true).forGetter(HeatedWorkpieceData::workable)
    ).apply(instance, HeatedWorkpieceData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeatedWorkpieceData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public HeatedWorkpieceData {
        temperature = Math.max(0.0F, Math.min(1.0F, temperature));
    }

    public boolean isHot(long gameTime) {
        return workable && expiresAtGameTime > gameTime;
    }

    public long remainingTicks(long gameTime) {
        return Math.max(0L, expiresAtGameTime - gameTime);
    }

    public float temperatureAt(long gameTime, int coolingTicks) {
        if (coolingTicks <= 0) {
            return 0.0F;
        }
        if (lastUpdateGameTime <= 0L && expiresAtGameTime > 0L) {
            return Math.max(0.0F, Math.min(1.0F, remainingTicks(gameTime) / (float) coolingTicks));
        }
        long elapsedTicks = Math.max(0L, gameTime - lastUpdateGameTime);
        return Math.max(0.0F, Math.min(1.0F, temperature - elapsedTicks / (float) coolingTicks));
    }
}

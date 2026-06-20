package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HeatedWorkpieceData(long expiresAtGameTime) {
    public static final Codec<HeatedWorkpieceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("expires_at_game_time").forGetter(HeatedWorkpieceData::expiresAtGameTime)
    ).apply(instance, HeatedWorkpieceData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeatedWorkpieceData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean isHot(long gameTime) {
        return gameTime < expiresAtGameTime;
    }

    public long remainingTicks(long gameTime) {
        return Math.max(0L, expiresAtGameTime - gameTime);
    }
}

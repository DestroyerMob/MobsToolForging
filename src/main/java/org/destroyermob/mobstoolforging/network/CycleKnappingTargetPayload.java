package org.destroyermob.mobstoolforging.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record CycleKnappingTargetPayload(BlockPos pos, int delta) implements CustomPacketPayload {
    public static final Type<CycleKnappingTargetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "cycle_knapping_target")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleKnappingTargetPayload> STREAM_CODEC =
            StreamCodec.ofMember(CycleKnappingTargetPayload::write, CycleKnappingTargetPayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(delta);
    }

    private static CycleKnappingTargetPayload read(RegistryFriendlyByteBuf buffer) {
        return new CycleKnappingTargetPayload(buffer.readBlockPos(), buffer.readVarInt());
    }

    @Override
    public Type<CycleKnappingTargetPayload> type() {
        return TYPE;
    }
}

package org.destroyermob.mobstoolforging.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record OpenForgeTemplateScreenPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenForgeTemplateScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "open_forge_template_screen")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenForgeTemplateScreenPayload> STREAM_CODEC =
            StreamCodec.ofMember(OpenForgeTemplateScreenPayload::write, OpenForgeTemplateScreenPayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    private static OpenForgeTemplateScreenPayload read(RegistryFriendlyByteBuf buffer) {
        return new OpenForgeTemplateScreenPayload(buffer.readBlockPos());
    }

    @Override
    public Type<OpenForgeTemplateScreenPayload> type() {
        return TYPE;
    }
}

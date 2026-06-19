package org.destroyermob.mobstoolforging.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;

public record SetForgeTemplatePayload(BlockPos pos, ForgeTemplate template) implements CustomPacketPayload {
    public static final Type<SetForgeTemplatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "set_forge_template")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetForgeTemplatePayload> STREAM_CODEC =
            StreamCodec.ofMember(SetForgeTemplatePayload::write, SetForgeTemplatePayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(template.id());
    }

    private static SetForgeTemplatePayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ForgeTemplate template = ForgeTemplate.byId(buffer.readUtf()).orElse(ForgeTemplate.PICKAXE_HEAD);
        return new SetForgeTemplatePayload(pos, template);
    }

    @Override
    public Type<SetForgeTemplatePayload> type() {
        return TYPE;
    }
}

package org.destroyermob.mobstoolforging.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;

public record SetForgeTemplatePayload(BlockPos pos, ResourceLocation templateId) implements CustomPacketPayload {
    public static final Type<SetForgeTemplatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "set_forge_template")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetForgeTemplatePayload> STREAM_CODEC =
            StreamCodec.ofMember(SetForgeTemplatePayload::write, SetForgeTemplatePayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeResourceLocation(templateId);
    }

    private static SetForgeTemplatePayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new SetForgeTemplatePayload(pos, buffer.readResourceLocation());
    }

    public ForgeTemplateDefinition template() {
        return ToolTypeRegistry.template(templateId).orElse(null);
    }

    @Override
    public Type<SetForgeTemplatePayload> type() {
        return TYPE;
    }
}

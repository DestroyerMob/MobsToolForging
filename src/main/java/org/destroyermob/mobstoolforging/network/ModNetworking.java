package org.destroyermob.mobstoolforging.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.destroyermob.mobstoolforging.client.MobsToolForgingClient;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.DebugFeedback;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;

public final class ModNetworking {
    private static final String NETWORK_VERSION = "1";
    private static final double MAX_TEMPLATE_DISTANCE_SQUARED = 64.0;

    private ModNetworking() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloadHandlers);
    }

    public static void openTemplateSelector(ServerPlayer player, BlockPos pos) {
        PacketDistributor.sendToPlayer(player, new OpenForgeTemplateScreenPayload(pos));
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        registrar.playToClient(OpenForgeTemplateScreenPayload.TYPE, OpenForgeTemplateScreenPayload.STREAM_CODEC, ModNetworking::handleOpenTemplateScreen);
        registrar.playToServer(SetForgeTemplatePayload.TYPE, SetForgeTemplatePayload.STREAM_CODEC, ModNetworking::handleSetTemplate);
        registrar.playToServer(CycleKnappingTargetPayload.TYPE, CycleKnappingTargetPayload.STREAM_CODEC, ModNetworking::handleCycleKnappingTarget);
    }

    private static void handleOpenTemplateScreen(OpenForgeTemplateScreenPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            MobsToolForgingClient.openTemplateScreen(payload.pos());
        }
    }

    private static void handleSetTemplate(SetForgeTemplatePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.distanceToSqr(Vec3.atCenterOf(payload.pos())) > MAX_TEMPLATE_DISTANCE_SQUARED) {
            return;
        }
        ForgeTemplateDefinition template = payload.template();
        if (template == null) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof ToolForgeBlockEntity forge) {
            if (forge.selectTemplate(template)) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.template_selected", template.displayName()));
            } else {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.forge_busy"));
            }
        }
    }

    private static void handleCycleKnappingTarget(CycleKnappingTargetPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.distanceToSqr(Vec3.atCenterOf(payload.pos())) > MAX_TEMPLATE_DISTANCE_SQUARED) {
            return;
        }
        if (!player.getMainHandItem().is(ModTags.Items.KNAPPING_TOOLS) && !player.getOffhandItem().is(ModTags.Items.KNAPPING_TOOLS)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof KnappingFlintBlockEntity knapping) {
            knapping.cycleTarget(payload.delta());
        }
    }
}

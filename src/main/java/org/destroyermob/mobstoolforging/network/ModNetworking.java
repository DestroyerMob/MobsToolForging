package org.destroyermob.mobstoolforging.network;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.destroyermob.mobstoolforging.client.MobsToolForgingClient;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.DebugFeedback;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlockEntity;
import org.destroyermob.mobstoolforging.world.PatternRackSelection;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public final class ModNetworking {
    private static final String NETWORK_VERSION = FoundryRegistrySyncPayload.NETWORK_VERSION;
    private static final long TEMPLATE_SELECTION_SESSION_TICKS = 20L * 30L;
    private static final Map<ServerPlayer, TemplateSelectionSession> TEMPLATE_SELECTION_SESSIONS = Collections.synchronizedMap(new WeakHashMap<>());

    private ModNetworking() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloadHandlers);
    }

    public static void openTemplateSelector(ServerPlayer player, BlockPos pos) {
        ToolForgeBlockEntity forge = authorizedTemplateForge(player, pos);
        if (forge == null || !player.isShiftKeyDown() || forge.template() != null || !forge.canChangeTemplate()) {
            return;
        }
        WorkstationKind kind = forge.workstationKind();
        if (kind == WorkstationKind.TOOLMAKERS_BENCH || kind == WorkstationKind.LAPIDARY_TABLE) {
            return;
        }
        TEMPLATE_SELECTION_SESSIONS.put(player, new TemplateSelectionSession(
                player.level().dimension(),
                pos.immutable(),
                kind,
                player.level().getGameTime() + TEMPLATE_SELECTION_SESSION_TICKS
        ));
        PacketDistributor.sendToPlayer(player, new OpenForgeTemplateScreenPayload(pos));
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar requiredRegistrar = event.registrar(NETWORK_VERSION);
        requiredRegistrar.playToClient(
                FoundryRegistrySyncPayload.TYPE,
                FoundryRegistrySyncPayload.STREAM_CODEC,
                FoundryRegistrySync::handleClient
        );
        requiredRegistrar.playToClient(
                GameplayRegistrySyncPayload.TYPE,
                GameplayRegistrySyncPayload.STREAM_CODEC,
                GameplayRegistrySync::handleClient
        );
        PayloadRegistrar registrar = requiredRegistrar.optional();
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
        TemplateSelectionSession session = TEMPLATE_SELECTION_SESSIONS.remove(player);
        if (session == null
                || !session.dimension().equals(player.level().dimension())
                || !session.pos().equals(payload.pos())
                || player.level().getGameTime() > session.expiresAt()) {
            return;
        }
        ForgeTemplateDefinition template = payload.template();
        if (template == null) {
            return;
        }
        ToolForgeBlockEntity forge = authorizedTemplateForge(player, payload.pos());
        if (forge == null
                || forge.workstationKind() != session.workstationKind()
                || forge.template() != null
                || !PatternRackSelection.canAssign(ItemStack.EMPTY, template, session.workstationKind())) {
            return;
        }
        if (forge.selectTemplate(template)) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.template_selected", template.displayName()));
        } else {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.forge_busy"));
        }
    }

    private static void handleCycleKnappingTarget(CycleKnappingTargetPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!MobsToolForgingConfig.crudeFlintToolsEnabled()
                || (payload.delta() != -1 && payload.delta() != 1)
                || player.isSpectator()
                || !player.isAlive()) {
            return;
        }
        Level level = player.level();
        if (!level.isLoaded(payload.pos())
                || !player.canInteractWithBlock(payload.pos(), 1.0D)
                || !level.mayInteract(player, payload.pos())) {
            return;
        }
        InteractionHand hand = player.getMainHandItem().is(ModTags.Items.KNAPPING_TOOLS)
                ? InteractionHand.MAIN_HAND
                : player.getOffhandItem().is(ModTags.Items.KNAPPING_TOOLS) ? InteractionHand.OFF_HAND : null;
        if (hand == null) {
            return;
        }
        HitResult lookedAt = player.pick(player.blockInteractionRange() + 1.0D, 1.0F, false);
        if (!(lookedAt instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(payload.pos())) {
            return;
        }
        if (!(level.getBlockEntity(payload.pos()) instanceof KnappingFlintBlockEntity)) {
            return;
        }
        var interactionEvent = CommonHooks.onRightClickBlock(player, hand, payload.pos(), blockHit);
        if (interactionEvent.isCanceled() || interactionEvent.getUseBlock().isFalse()) {
            return;
        }
        if (level.getBlockEntity(payload.pos()) instanceof KnappingFlintBlockEntity knapping) {
            knapping.cycleTarget(payload.delta());
        }
    }

    private static ToolForgeBlockEntity authorizedTemplateForge(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        if (!MobsToolForgingConfig.DEBUG_TEMPLATE_SELECTOR.get()
                || player.isSpectator()
                || !player.isAlive()
                || !level.isLoaded(pos)
                || !player.canInteractWithBlock(pos, 1.0D)
                || !level.mayInteract(player, pos)
                || !(level.getBlockState(pos).getBlock() instanceof ToolWorkstationBlock)
                || !(level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge)) {
            return null;
        }
        return forge;
    }

    private record TemplateSelectionSession(
            ResourceKey<Level> dimension,
            BlockPos pos,
            WorkstationKind workstationKind,
            long expiresAt
    ) {
    }
}

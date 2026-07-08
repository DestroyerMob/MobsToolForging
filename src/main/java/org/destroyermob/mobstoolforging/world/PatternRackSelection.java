package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public final class PatternRackSelection {
    private static final int PENDING_TICKS = 200;
    private static final Map<UUID, PendingSelection> PENDING = new HashMap<>();
    private static final Map<UUID, PendingRackLink> PENDING_RACK_LINKS = new HashMap<>();

    private PatternRackSelection() {
    }

    public static void begin(ServerPlayer player, BlockPos stationPos, WorkstationKind kind) {
        PENDING.put(player.getUUID(), new PendingSelection(
                player.level().dimension(),
                stationPos.immutable(),
                kind,
                player.level().getGameTime() + PENDING_TICKS
        ));
        player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_select"), true);
    }

    public static void beginRackLink(Player player, Level level, BlockPos rackPos) {
        PENDING_RACK_LINKS.put(player.getUUID(), new PendingRackLink(
                level.dimension(),
                rackPos.immutable(),
                level.getGameTime() + PENDING_TICKS
        ));
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_link_select_station"), true);
        }
    }

    public static boolean hasPendingRackLink(Player player, Level level) {
        PendingRackLink link = PENDING_RACK_LINKS.get(player.getUUID());
        if (link == null) {
            return false;
        }
        if (link.expiresAtGameTime() < level.getGameTime() || !link.dimension().equals(level.dimension())) {
            PENDING_RACK_LINKS.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public static boolean linkPendingRackToStation(ServerPlayer player, Level level, BlockPos stationPos, WorkstationKind kind, ToolForgeBlockEntity forge) {
        PendingRackLink link = pendingRackLink(player, level);
        if (link == null) {
            return false;
        }
        if (!isPatternRackSelectable(kind)) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_wrong_station"), true);
            return true;
        }
        if (!(level.getBlockEntity(link.rackPos()) instanceof PatternRackBlockEntity)) {
            PENDING_RACK_LINKS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_missing"), true);
            return true;
        }
        if (!isInRange(stationPos, link.rackPos())) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_too_far"), true);
            return true;
        }
        ToolForgeBlockEntity.PatternRackLinkResult result = forge.linkPatternRack(link.rackPos());
        if (result == ToolForgeBlockEntity.PatternRackLinkResult.FULL) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_link_station_full", ToolForgeBlockEntity.maxLinkedPatternRacks()), true);
            return true;
        }
        PENDING_RACK_LINKS.remove(player.getUUID());
        if (result == ToolForgeBlockEntity.PatternRackLinkResult.ALREADY_LINKED) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_already_linked"), true);
            return true;
        }
        level.playSound(null, stationPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.15F);
        player.displayClientMessage(Component.translatable(
                "message.mobstoolforging.pattern_rack_linked",
                stationName(kind),
                forge.linkedRackCount(),
                ToolForgeBlockEntity.maxLinkedPatternRacks()
        ), true);
        return true;
    }

    public static void handleRackRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!SmithingHammerLevel.isHammer(stack)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof PatternRackBlock && level.getBlockEntity(pos) instanceof PatternRackBlockEntity rack) {
            if (!MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()) {
                consume(event);
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_disabled"), true);
                }
                return;
            }
            if (player.isShiftKeyDown()) {
                consume(event);
                beginRackLink(player, level, pos);
                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 1.25F);
                }
                return;
            }
            int slot = PatternRackBlock.slotFromHit(state, pos, event.getHitVec());
            ItemStack pattern = rack.patternStack(slot);
            if (slot < 0 || pattern.isEmpty()) {
                return;
            }
            consume(event);
            if (level.isClientSide) {
                return;
            }
            if (!assignFromRack(player, level, pos, slot, pattern) && !assignToLinkedStations(player, level, pos, slot, pattern)) {
                inspectPattern(player, pattern);
            }
            return;
        }

        if (state.getBlock() instanceof ToolWorkstationBlock workstation
                && level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge
                && hasPendingRackLink(player, level)) {
            consume(event);
            if (level.isClientSide) {
                return;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                linkPendingRackToStation(serverPlayer, level, pos, workstation.kind(), forge);
            }
        }
    }

    public static void handleRackLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !player.isShiftKeyDown()
                || !SmithingHammerLevel.isHammer(player.getMainHandItem())
                || !(level.getBlockState(pos).getBlock() instanceof PatternRackBlock)
                || !(level.getBlockEntity(pos) instanceof PatternRackBlockEntity)) {
            return;
        }

        event.setCanceled(true);
        if (!MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_disabled"), true);
            }
            return;
        }
        beginRackLink(player, level, pos);
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 1.25F);
        }
    }

    public static boolean assignFromRack(Player player, Level level, BlockPos rackPos, int slot, ItemStack pattern) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        PendingSelection selection = pending(serverPlayer, level);
        if (selection == null) {
            return false;
        }
        if (slot < 0 || pattern.isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.no_pattern_selected"), true);
            return true;
        }
        if (!isInRange(selection.stationPos(), rackPos)) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_too_far"), true);
            return true;
        }
        if (!(level.getBlockEntity(selection.stationPos()) instanceof ToolForgeBlockEntity forge)
                || !(level.getBlockState(selection.stationPos()).getBlock() instanceof ToolWorkstationBlock workstation)
                || workstation.kind() != selection.kind()) {
            PENDING.remove(serverPlayer.getUUID());
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.no_pattern_selected"), true);
            return true;
        }
        ForgeTemplateDefinition template = PatternRackBlockEntity.template(pattern).orElse(null);
        if (template == null || !canAssign(pattern, template, selection.kind())) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_wrong_station"), true);
            return true;
        }
        if (!forge.setTemplateFromRack(template, rackPos, slot)) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.forge_busy"), true);
            return true;
        }
        PENDING.remove(serverPlayer.getUUID());
        level.playSound(null, selection.stationPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.1F);
        serverPlayer.displayClientMessage(Component.translatable(
                "message.mobstoolforging.station_pattern_set",
                stationName(selection.kind()),
                template.displayName()
        ), true);
        return true;
    }

    public static boolean assignToLinkedStations(Player player, Level level, BlockPos rackPos, int slot, ItemStack pattern) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (slot < 0 || pattern.isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.no_pattern_selected"), true);
            return true;
        }
        ForgeTemplateDefinition template = PatternRackBlockEntity.template(pattern).orElse(null);
        if (template == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.invalid_template_pattern"), true);
            return true;
        }

        int linkedStations = 0;
        int changedStations = 0;
        int busyStations = 0;
        int incompatibleStations = 0;
        for (BlockPos stationPos : nearbyStationPositions(rackPos)) {
            if (!(level.getBlockEntity(stationPos) instanceof ToolForgeBlockEntity forge)
                    || !(level.getBlockState(stationPos).getBlock() instanceof ToolWorkstationBlock workstation)
                    || !isPatternRackSelectable(workstation.kind())
                    || !forge.hasLinkedRack(rackPos)) {
                continue;
            }
            linkedStations++;
            WorkstationKind kind = workstation.kind();
            if (!canAssign(pattern, template, kind)) {
                incompatibleStations++;
                continue;
            }
            boolean sameTemplate = template.id().equals(forge.templateId());
            if (!forge.canChangeTemplate() && !sameTemplate) {
                busyStations++;
                continue;
            }
            if (forge.setTemplateFromRack(template, rackPos, slot)) {
                changedStations++;
                level.playSound(null, stationPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.65F, 1.08F);
            } else {
                busyStations++;
            }
        }

        if (linkedStations == 0) {
            return false;
        }
        if (changedStations > 0) {
            if (busyStations > 0) {
                serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_linked_set_some", template.displayName(), changedStations, busyStations), true);
            } else {
                serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_linked_set", template.displayName(), changedStations), true);
            }
            return true;
        }
        if (busyStations > 0) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_linked_busy"), true);
            return true;
        }
        if (incompatibleStations > 0) {
            serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_linked_wrong_station"), true);
            return true;
        }
        serverPlayer.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_linked_none"), true);
        return true;
    }

    public static boolean canAssign(ItemStack pattern, ForgeTemplateDefinition template, WorkstationKind kind) {
        if (kind == WorkstationKind.TOOLMAKERS_BENCH) {
            return false;
        }
        if (pattern.getItem() instanceof org.destroyermob.mobstoolforging.item.ToolTemplateItem templateItem && !templateItem.canUseOn(kind)) {
            return false;
        }
        if (StationWorkRecipeRegistry.hasStartRecipe(kind, template.id())) {
            return true;
        }
        MaterialCategory category = kind.materialCategory();
        return MaterialCatalog.starterMaterialIds().stream()
                .map(MaterialCatalog::definition)
                .flatMap(java.util.Optional::stream)
                .filter(material -> material.category() == category)
                .map(ToolMaterialDefinition::id)
                .anyMatch(template::allowsMaterial);
    }

    public static Component compatibleStations(ItemStack pattern, ForgeTemplateDefinition template) {
        List<Component> stations = new ArrayList<>();
        for (WorkstationKind kind : List.of(WorkstationKind.CRUDE_ANVIL, WorkstationKind.TOOL_FORGE, WorkstationKind.LAPIDARY_TABLE, WorkstationKind.LEATHER_STATION)) {
            if (canAssign(pattern, template, kind)) {
                stations.add(stationName(kind));
            }
        }
        if (stations.isEmpty()) {
            return Component.translatable("message.mobstoolforging.pattern_rack_no_compatible_station");
        }
        Component result = Component.empty();
        for (int index = 0; index < stations.size(); index++) {
            if (index > 0) {
                result = result.copy().append(Component.literal(" / "));
            }
            result = result.copy().append(stations.get(index));
        }
        return result;
    }

    private static void inspectPattern(Player player, ItemStack pattern) {
        ForgeTemplateDefinition template = PatternRackBlockEntity.template(pattern).orElse(null);
        if (template == null) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.no_pattern_selected"), true);
            return;
        }
        player.displayClientMessage(Component.translatable(
                "message.mobstoolforging.pattern_rack_inspect",
                template.displayName(),
                compatibleStations(pattern, template)
        ), true);
    }

    private static void consume(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
        PENDING_RACK_LINKS.remove(event.getEntity().getUUID());
    }

    public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PENDING.remove(event.getEntity().getUUID());
        PENDING_RACK_LINKS.remove(event.getEntity().getUUID());
    }

    private static PendingSelection pending(ServerPlayer player, Level level) {
        PendingSelection selection = PENDING.get(player.getUUID());
        if (selection == null) {
            return null;
        }
        if (selection.expiresAtGameTime() < level.getGameTime() || !selection.dimension().equals(level.dimension())) {
            PENDING.remove(player.getUUID());
            return null;
        }
        return selection;
    }

    private static PendingRackLink pendingRackLink(ServerPlayer player, Level level) {
        PendingRackLink link = PENDING_RACK_LINKS.get(player.getUUID());
        if (link == null) {
            return null;
        }
        if (link.expiresAtGameTime() < level.getGameTime() || !link.dimension().equals(level.dimension())) {
            PENDING_RACK_LINKS.remove(player.getUUID());
            return null;
        }
        return link;
    }

    private static boolean isInRange(BlockPos stationPos, BlockPos rackPos) {
        int range = MobsToolForgingConfig.WORKSHOP_PATTERN_RANGE.get();
        return Math.abs(stationPos.getX() - rackPos.getX()) <= range
                && Math.abs(stationPos.getZ() - rackPos.getZ()) <= range
                && Math.abs(stationPos.getY() - rackPos.getY()) <= 1;
    }

    private static Iterable<BlockPos> nearbyStationPositions(BlockPos rackPos) {
        int range = MobsToolForgingConfig.WORKSHOP_PATTERN_RANGE.get();
        return BlockPos.betweenClosed(
                rackPos.getX() - range,
                rackPos.getY() - 1,
                rackPos.getZ() - range,
                rackPos.getX() + range,
                rackPos.getY() + 1,
                rackPos.getZ() + range
        );
    }

    private static boolean isPatternRackSelectable(WorkstationKind kind) {
        return kind == WorkstationKind.CRUDE_ANVIL
                || kind == WorkstationKind.TOOL_FORGE
                || kind == WorkstationKind.LAPIDARY_TABLE
                || kind == WorkstationKind.LEATHER_STATION;
    }

    private static Component stationName(WorkstationKind kind) {
        return switch (kind) {
            case CRUDE_ANVIL -> new ItemStack(ModBlocks.CRUDE_ANVIL.get()).getHoverName();
            case TOOL_FORGE -> new ItemStack(ModBlocks.TOOL_FORGE.get()).getHoverName();
            case LAPIDARY_TABLE -> new ItemStack(ModBlocks.LAPIDARY_TABLE.get()).getHoverName();
            case LEATHER_STATION -> new ItemStack(ModBlocks.LEATHER_STATION.get()).getHoverName();
            case TOOLMAKERS_BENCH -> new ItemStack(ModBlocks.TOOLMAKERS_BENCH.get()).getHoverName();
        };
    }

    private record PendingSelection(ResourceKey<Level> dimension, BlockPos stationPos, WorkstationKind kind, long expiresAtGameTime) {
    }

    private record PendingRackLink(ResourceKey<Level> dimension, BlockPos rackPos, long expiresAtGameTime) {
    }
}

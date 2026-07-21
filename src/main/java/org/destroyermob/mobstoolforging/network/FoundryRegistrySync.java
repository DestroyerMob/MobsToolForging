package org.destroyermob.mobstoolforging.network;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.destroyermob.mobstoolforging.MobsToolForging;

/** Login/reload delivery and client application for foundry registry snapshots. */
public final class FoundryRegistrySync {
    private FoundryRegistrySync() {
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        FoundryRegistrySyncPayload payload = FoundryRegistrySyncPayload.capture();
        event.getRelevantPlayers().forEach(player -> {
            if (player.connection.hasChannel(FoundryRegistrySyncPayload.TYPE)) {
                PacketDistributor.sendToPlayer(player, payload);
            } else {
                MobsToolForging.LOGGER.error(
                        "Disconnecting {}; the client did not negotiate required foundry synchronization payload {}.",
                        player.getGameProfile().getName(),
                        FoundryRegistrySyncPayload.TYPE.id()
                );
                player.connection.disconnect(Component.translatableWithFallback(
                        "disconnect.mobstoolforging.missing_foundry_registry_sync",
                        "Your Mobs Tool Forging version cannot synchronize this server's foundry data."
                ));
            }
        });
    }

    public static void handleClient(FoundryRegistrySyncPayload payload, IPayloadContext context) {
        try {
            payload.apply();
            MobsToolForging.LOGGER.debug(
                    "Synchronized foundry datapacks: {} melting, {} points, {} fuels, {} alloys, {} casts.",
                    payload.meltingRecipes().size(),
                    payload.meltingPoints().size(),
                    payload.fuelRecipes().size(),
                    payload.alloyRecipes().size(),
                    payload.castRecipes().size()
            );
        } catch (RuntimeException exception) {
            MobsToolForging.LOGGER.error("Rejected invalid foundry registry synchronization payload.", exception);
            context.disconnect(Component.translatableWithFallback(
                    "disconnect.mobstoolforging.invalid_foundry_registry_sync",
                    "Mobs Tool Forging received incompatible foundry data from the server."
            ));
        }
    }
}

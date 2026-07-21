package org.destroyermob.mobstoolforging.network;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.destroyermob.mobstoolforging.MobsToolForging;

/** Login/reload delivery and client application for non-foundry gameplay registries. */
public final class GameplayRegistrySync {
    private GameplayRegistrySync() {
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        GameplayRegistrySyncPayload payload = GameplayRegistrySyncPayload.capture();
        event.getRelevantPlayers().forEach(player -> {
            if (player.connection.hasChannel(GameplayRegistrySyncPayload.TYPE)) {
                PacketDistributor.sendToPlayer(player, payload);
            } else {
                MobsToolForging.LOGGER.error(
                        "Disconnecting {}; the client did not negotiate required gameplay registry synchronization payload {}.",
                        player.getGameProfile().getName(),
                        GameplayRegistrySyncPayload.TYPE.id()
                );
                player.connection.disconnect(Component.translatableWithFallback(
                        "disconnect.mobstoolforging.missing_gameplay_registry_sync",
                        "Your Mobs Tool Forging version cannot synchronize this server's gameplay data."
                ));
            }
        });
    }

    public static void handleClient(GameplayRegistrySyncPayload payload, IPayloadContext context) {
        try {
            payload.apply();
            MobsToolForging.LOGGER.debug(
                    "Synchronized {} entries across {} custom gameplay registries.",
                    payload.entryCount(),
                    payload.sections().size()
            );
        } catch (RuntimeException exception) {
            MobsToolForging.LOGGER.error("Rejected invalid gameplay registry synchronization payload.", exception);
            context.disconnect(Component.translatableWithFallback(
                    "disconnect.mobstoolforging.invalid_gameplay_registry_sync",
                    "Mobs Tool Forging received incompatible gameplay data from the server."
            ));
        }
    }
}

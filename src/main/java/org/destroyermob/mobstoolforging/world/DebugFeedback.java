package org.destroyermob.mobstoolforging.world;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;

public final class DebugFeedback {
    private DebugFeedback() {
    }

    public static void actionBar(Player player, Component message) {
        if (MobsToolForgingConfig.DEBUG_ACTIONBAR_FEEDBACK.get()) {
            player.displayClientMessage(message, true);
        }
    }
}

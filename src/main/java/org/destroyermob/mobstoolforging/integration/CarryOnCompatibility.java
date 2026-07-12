package org.destroyermob.mobstoolforging.integration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.DryingRackBlock;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.ToolmakersBenchBlock;

/**
 * Adds Mobs Tool Forging workstations to Carry On after every mod has registered its blocks.
 *
 * <p>Carry On normally discovers blacklisted tags while it initializes its config.
 * Every Compat creates its workstation blocks later in the load sequence, so a tag
 * alone can miss those blocks. Its public blacklist method is used reflectively to
 * keep Carry On an optional dependency.</p>
 */
public final class CarryOnCompatibility {
    private static final String CARRY_ON_MOD_ID = "carryon";
    private static final String LIST_HANDLER_CLASS = "tschipp.carryon.common.config.ListHandler";

    private CarryOnCompatibility() {
    }

    public static void blacklistStations() {
        if (!ModList.get().isLoaded(CARRY_ON_MOD_ID)) {
            return;
        }

        try {
            Method addForbiddenTiles = Class.forName(LIST_HANDLER_CLASS).getMethod("addForbiddenTiles", String.class);
            List<String> stationIds = BuiltInRegistries.BLOCK.stream()
                    .filter(block -> block instanceof ToolmakersBenchBlock
                            || block instanceof PatternRackBlock
                            || block instanceof LeatherStationBlock
                            || block instanceof DryingRackBlock)
                    .map(BuiltInRegistries.BLOCK::getKey)
                    .map(Object::toString)
                    .distinct()
                    .toList();
            stationIds.forEach(id -> addForbiddenTile(addForbiddenTiles, id));
            MobsToolForging.LOGGER.info("Blocked {} Mobs Tool Forging workstation variant(s) from Carry On.", stationIds.size());
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            MobsToolForging.LOGGER.warn("Carry On is installed but its blacklist API was unavailable.", exception);
        }
    }

    private static void addForbiddenTile(Method addForbiddenTiles, String id) {
        try {
            addForbiddenTiles.invoke(null, id);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not blacklist " + id + " in Carry On.", exception);
        }
    }
}

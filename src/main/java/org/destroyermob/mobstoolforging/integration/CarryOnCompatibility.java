package org.destroyermob.mobstoolforging.integration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

/**
 * Adds Toolmaker's Stations to Carry On after every mod has registered its blocks.
 *
 * <p>Carry On normally discovers blacklisted tags while it initializes its config.
 * Every Compat creates its workstation blocks later in the load sequence, so a tag
 * alone can miss those blocks. Its public blacklist method is used reflectively to
 * keep Carry On an optional dependency.</p>
 */
public final class CarryOnCompatibility {
    private static final String CARRY_ON_MOD_ID = "carryon";
    private static final String EVERY_COMPAT_MOD_ID = "everycomp";
    private static final String LIST_HANDLER_CLASS = "tschipp.carryon.common.config.ListHandler";

    private CarryOnCompatibility() {
    }

    public static void blacklistToolmakersStations() {
        if (!ModList.get().isLoaded(CARRY_ON_MOD_ID)) {
            return;
        }

        try {
            Method addForbiddenTiles = Class.forName(LIST_HANDLER_CLASS).getMethod("addForbiddenTiles", String.class);
            List<String> stationIds = Stream.concat(
                            Arrays.stream(ModBlocks.toolmakersBenchBlocks())
                                    .map(BuiltInRegistries.BLOCK::getKey),
                            everyCompatToolmakersStations())
                    .map(ResourceLocation::toString)
                    .distinct()
                    .toList();
            stationIds.forEach(id -> addForbiddenTile(addForbiddenTiles, id));
            MobsToolForging.LOGGER.info("Blocked {} Toolmaker's Station variant(s) from Carry On.", stationIds.size());
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            MobsToolForging.LOGGER.warn("Carry On is installed but its blacklist API was unavailable.", exception);
        }
    }

    private static Stream<ResourceLocation> everyCompatToolmakersStations() {
        if (!ModList.get().isLoaded(EVERY_COMPAT_MOD_ID)) {
            return Stream.empty();
        }
        return BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(EVERY_COMPAT_MOD_ID))
                .filter(id -> id.getPath().endsWith("toolmakers_bench"));
    }

    private static void addForbiddenTile(Method addForbiddenTiles, String id) {
        try {
            addForbiddenTiles.invoke(null, id);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not blacklist " + id + " in Carry On.", exception);
        }
    }
}

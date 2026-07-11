package org.destroyermob.mobstoolforging.integration.everycomp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;

/** Runtime bridge used by optional wood-compat implementations without exposing their API to core MTF code. */
public final class CompatWorkstationRegistry {
    private static final Map<Kind, List<Item>> ITEMS = new EnumMap<>(Kind.class);
    private static final List<LeatherStationDefinition> LEATHER_STATIONS = new ArrayList<>();

    static {
        for (Kind kind : Kind.values()) {
            ITEMS.put(kind, new ArrayList<>());
        }
    }

    private CompatWorkstationRegistry() {
    }

    public static <T extends Item> T registerItem(Kind kind, T item) {
        ITEMS.get(kind).add(item);
        return item;
    }

    public static List<Item> items(Kind kind) {
        return Collections.unmodifiableList(ITEMS.get(kind));
    }

    public static LeatherStationBlock registerLeatherStation(Block planks, Block log, LeatherStationBlock station) {
        LEATHER_STATIONS.add(new LeatherStationDefinition(planks, log, station));
        return station;
    }

    public static List<LeatherStationDefinition> leatherStations() {
        return Collections.unmodifiableList(LEATHER_STATIONS);
    }

    public enum Kind {
        PATTERN_RACK,
        TOOLMAKERS_BENCH,
        LEATHER_STATION,
        DRYING_RACK
    }

    public record LeatherStationDefinition(Block planks, Block log, LeatherStationBlock station) {
    }
}

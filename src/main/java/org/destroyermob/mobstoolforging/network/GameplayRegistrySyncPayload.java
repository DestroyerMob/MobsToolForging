package org.destroyermob.mobstoolforging.network;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.GameplayRegistrySyncStore;

/** Server-authoritative JSON snapshot for every non-foundry custom gameplay registry. */
public record GameplayRegistrySyncPayload(
        int formatVersion,
        Map<GameplayRegistrySyncStore.Section, Map<ResourceLocation, String>> sections
) implements CustomPacketPayload {
    public static final int FORMAT_VERSION = 1;
    public static final String NETWORK_VERSION = "1";
    private static final int MAX_ENTRIES_PER_SECTION = 16_384;
    private static final int MAX_JSON_CHARS = 262_144;
    private static final int MAX_TOTAL_JSON_CHARS = 4_194_304;
    private static final int MAX_SECTION_ID_CHARS = 64;

    public static final Type<GameplayRegistrySyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "gameplay_registry_sync")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GameplayRegistrySyncPayload> STREAM_CODEC =
            StreamCodec.ofMember(GameplayRegistrySyncPayload::write, GameplayRegistrySyncPayload::read);

    public GameplayRegistrySyncPayload {
        EnumMap<GameplayRegistrySyncStore.Section, Map<ResourceLocation, String>> copy =
                new EnumMap<>(GameplayRegistrySyncStore.Section.class);
        int totalCharacters = 0;
        for (GameplayRegistrySyncStore.Section section : GameplayRegistrySyncStore.Section.values()) {
            Map<ResourceLocation, String> values = sections.get(section);
            if (values == null) {
                throw new IllegalArgumentException("Missing gameplay registry section " + section.id());
            }
            checkSize(section.id(), values.size(), MAX_ENTRIES_PER_SECTION);
            Map<ResourceLocation, String> entries = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, String> entry : values.entrySet()) {
                String json = entry.getValue();
                if (json == null || json.length() > MAX_JSON_CHARS) {
                    throw new IllegalArgumentException("Invalid synchronized JSON length for " + entry.getKey());
                }
                totalCharacters = Math.addExact(totalCharacters, json.length());
                entries.put(entry.getKey(), json);
            }
            copy.put(section, Map.copyOf(entries));
        }
        if (sections.size() != copy.size()) {
            throw new IllegalArgumentException("Unknown gameplay registry section in synchronization snapshot");
        }
        checkSize("total gameplay registry JSON", totalCharacters, MAX_TOTAL_JSON_CHARS);
        sections = Map.copyOf(copy);
    }

    public static GameplayRegistrySyncPayload capture() {
        return new GameplayRegistrySyncPayload(FORMAT_VERSION, GameplayRegistrySyncStore.snapshot());
    }

    public void apply() {
        if (formatVersion != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported gameplay registry format " + formatVersion
                    + "; expected " + FORMAT_VERSION);
        }
        GameplayRegistrySyncStore.apply(sections);
    }

    public int entryCount() {
        return sections.values().stream().mapToInt(Map::size).sum();
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(formatVersion);
        GameplayRegistrySyncStore.Section[] ordered = GameplayRegistrySyncStore.Section.values();
        buffer.writeVarInt(ordered.length);
        for (GameplayRegistrySyncStore.Section section : ordered) {
            buffer.writeUtf(section.id(), MAX_SECTION_ID_CHARS);
            List<Map.Entry<ResourceLocation, String>> entries = sections.get(section).entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                    .toList();
            buffer.writeVarInt(entries.size());
            for (Map.Entry<ResourceLocation, String> entry : entries) {
                buffer.writeResourceLocation(entry.getKey());
                buffer.writeUtf(entry.getValue(), MAX_JSON_CHARS);
            }
        }
    }

    private static GameplayRegistrySyncPayload read(RegistryFriendlyByteBuf buffer) {
        int version = buffer.readVarInt();
        int sectionCount = buffer.readVarInt();
        checkSize("gameplay registry sections", sectionCount, GameplayRegistrySyncStore.Section.values().length);
        if (sectionCount != GameplayRegistrySyncStore.Section.values().length) {
            throw new IllegalArgumentException("Expected " + GameplayRegistrySyncStore.Section.values().length
                    + " gameplay registry sections, received " + sectionCount);
        }
        EnumMap<GameplayRegistrySyncStore.Section, Map<ResourceLocation, String>> sections =
                new EnumMap<>(GameplayRegistrySyncStore.Section.class);
        for (int sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {
            String sectionId = buffer.readUtf(MAX_SECTION_ID_CHARS);
            GameplayRegistrySyncStore.Section section = section(sectionId);
            if (sections.containsKey(section)) {
                throw new IllegalArgumentException("Duplicate gameplay registry section " + sectionId);
            }
            int entryCount = buffer.readVarInt();
            checkSize(sectionId, entryCount, MAX_ENTRIES_PER_SECTION);
            Map<ResourceLocation, String> entries = new LinkedHashMap<>();
            for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
                ResourceLocation id = buffer.readResourceLocation();
                String json = buffer.readUtf(MAX_JSON_CHARS);
                if (entries.putIfAbsent(id, json) != null) {
                    throw new IllegalArgumentException("Duplicate " + sectionId + " id " + id);
                }
            }
            sections.put(section, entries);
        }
        return new GameplayRegistrySyncPayload(version, sections);
    }

    private static GameplayRegistrySyncStore.Section section(String id) {
        for (GameplayRegistrySyncStore.Section section : GameplayRegistrySyncStore.Section.values()) {
            if (section.id().equals(id)) {
                return section;
            }
        }
        throw new IllegalArgumentException("Unknown gameplay registry section " + id);
    }

    private static void checkSize(String name, int count, int maximum) {
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("Invalid " + name + " count " + count + "; maximum is " + maximum);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

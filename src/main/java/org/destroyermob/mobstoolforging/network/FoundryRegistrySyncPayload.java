package org.destroyermob.mobstoolforging.network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.FoundryAlloyRecipe;
import org.destroyermob.mobstoolforging.world.FoundryAlloyRegistry;
import org.destroyermob.mobstoolforging.world.FoundryCastRecipe;
import org.destroyermob.mobstoolforging.world.FoundryCastRegistry;
import org.destroyermob.mobstoolforging.world.FoundryFuelRecipe;
import org.destroyermob.mobstoolforging.world.FoundryFuelRegistry;
import org.destroyermob.mobstoolforging.world.FoundryMeltingPoint;
import org.destroyermob.mobstoolforging.world.FoundryMeltingPointRegistry;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRecipe;
import org.destroyermob.mobstoolforging.world.FoundryMeltingRegistry;

/**
 * Server-authoritative snapshot of the foundry-specific datapack registries
 * used directly by client rendering and interaction previews.
 *
 * <p>The payload is sent when a player joins and after a server datapack reload.
 * Bump both {@link #FORMAT_VERSION} and the payload registration's negotiated
 * network version before making an incompatible wire-format change.</p>
 */
public record FoundryRegistrySyncPayload(
        int formatVersion,
        List<FoundryMeltingRecipe> meltingRecipes,
        List<FoundryMeltingPoint> meltingPoints,
        List<FoundryFuelRecipe> fuelRecipes,
        List<FoundryAlloyRecipe> alloyRecipes,
        List<FoundryCastRecipe> castRecipes
) implements CustomPacketPayload {
    public static final int FORMAT_VERSION = 1;
    public static final String NETWORK_VERSION = "1";
    private static final int MAX_ENTRIES_PER_REGISTRY = 16_384;
    private static final int MAX_ALLOY_INPUTS = 256;

    public static final Type<FoundryRegistrySyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "foundry_registry_sync")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FoundryRegistrySyncPayload> STREAM_CODEC =
            StreamCodec.ofMember(FoundryRegistrySyncPayload::write, FoundryRegistrySyncPayload::read);

    public FoundryRegistrySyncPayload {
        meltingRecipes = List.copyOf(meltingRecipes);
        meltingPoints = List.copyOf(meltingPoints);
        fuelRecipes = List.copyOf(fuelRecipes);
        alloyRecipes = List.copyOf(alloyRecipes);
        castRecipes = List.copyOf(castRecipes);
        checkSize("melting recipes", meltingRecipes.size(), MAX_ENTRIES_PER_REGISTRY);
        checkSize("melting points", meltingPoints.size(), MAX_ENTRIES_PER_REGISTRY);
        checkSize("fuel recipes", fuelRecipes.size(), MAX_ENTRIES_PER_REGISTRY);
        checkSize("alloy recipes", alloyRecipes.size(), MAX_ENTRIES_PER_REGISTRY);
        checkSize("cast recipes", castRecipes.size(), MAX_ENTRIES_PER_REGISTRY);
        alloyRecipes.forEach(recipe -> checkSize("alloy inputs", recipe.inputs().size(), MAX_ALLOY_INPUTS));
    }

    /** Captures one internally consistent snapshot on the server main thread. */
    public static FoundryRegistrySyncPayload capture() {
        return new FoundryRegistrySyncPayload(
                FORMAT_VERSION,
                sorted(FoundryMeltingRegistry.recipes(), FoundryMeltingRecipe::id),
                sorted(FoundryMeltingPointRegistry.values(), FoundryMeltingPoint::id),
                sorted(FoundryFuelRegistry.recipes(), FoundryFuelRecipe::id),
                sorted(FoundryAlloyRegistry.recipes(), FoundryAlloyRecipe::id),
                sorted(FoundryCastRegistry.recipes(), FoundryCastRecipe::id)
        );
    }

    /**
     * Validates the entire received snapshot before replacing any client registry.
     * This keeps a malformed or version-mismatched packet from publishing a partial
     * mixture of old and new foundry data.
     */
    public void apply() {
        if (formatVersion != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported foundry registry format " + formatVersion
                    + "; expected " + FORMAT_VERSION);
        }

        Map<ResourceLocation, FoundryMeltingRecipe> melting = indexById(
                meltingRecipes, FoundryMeltingRecipe::id, "melting recipe");
        Map<ResourceLocation, FoundryMeltingPoint> points = indexById(
                meltingPoints, FoundryMeltingPoint::id, "melting point");
        Map<ResourceLocation, FoundryFuelRecipe> fuels = indexById(
                fuelRecipes, FoundryFuelRecipe::id, "fuel recipe");
        Map<ResourceLocation, FoundryAlloyRecipe> alloys = indexById(
                alloyRecipes, FoundryAlloyRecipe::id, "alloy recipe");
        Map<ResourceLocation, FoundryCastRecipe> casts = indexById(
                castRecipes, FoundryCastRecipe::id, "cast recipe");

        FoundryMeltingRegistry.replace(melting);
        FoundryMeltingPointRegistry.replace(points);
        FoundryFuelRegistry.replace(fuels);
        FoundryAlloyRegistry.replace(alloys);
        FoundryCastRegistry.replace(casts);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(formatVersion);
        writeList(buffer, meltingRecipes, FoundryRegistrySyncPayload::writeMeltingRecipe);
        writeList(buffer, meltingPoints, FoundryRegistrySyncPayload::writeMeltingPoint);
        writeList(buffer, fuelRecipes, FoundryRegistrySyncPayload::writeFuelRecipe);
        writeList(buffer, alloyRecipes, FoundryRegistrySyncPayload::writeAlloyRecipe);
        writeList(buffer, castRecipes, FoundryRegistrySyncPayload::writeCastRecipe);
    }

    private static FoundryRegistrySyncPayload read(RegistryFriendlyByteBuf buffer) {
        return new FoundryRegistrySyncPayload(
                buffer.readVarInt(),
                readList(buffer, "melting recipes", FoundryRegistrySyncPayload::readMeltingRecipe),
                readList(buffer, "melting points", FoundryRegistrySyncPayload::readMeltingPoint),
                readList(buffer, "fuel recipes", FoundryRegistrySyncPayload::readFuelRecipe),
                readList(buffer, "alloy recipes", FoundryRegistrySyncPayload::readAlloyRecipe),
                readList(buffer, "cast recipes", FoundryRegistrySyncPayload::readCastRecipe)
        );
    }

    private static void writeMeltingRecipe(RegistryFriendlyByteBuf buffer, FoundryMeltingRecipe recipe) {
        buffer.writeResourceLocation(recipe.id());
        writeItemInput(buffer, recipe.input());
        buffer.writeResourceLocation(recipe.material());
        buffer.writeVarInt(recipe.amountMb());
        buffer.writeVarInt(recipe.ticks());
    }

    private static FoundryMeltingRecipe readMeltingRecipe(RegistryFriendlyByteBuf buffer) {
        return new FoundryMeltingRecipe(
                buffer.readResourceLocation(),
                readItemInput(buffer),
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    private static void writeMeltingPoint(RegistryFriendlyByteBuf buffer, FoundryMeltingPoint point) {
        buffer.writeResourceLocation(point.id());
        buffer.writeResourceLocation(point.material());
        buffer.writeFloat(point.celsius());
    }

    private static FoundryMeltingPoint readMeltingPoint(RegistryFriendlyByteBuf buffer) {
        return new FoundryMeltingPoint(
                buffer.readResourceLocation(),
                buffer.readResourceLocation(),
                buffer.readFloat()
        );
    }

    private static void writeFuelRecipe(RegistryFriendlyByteBuf buffer, FoundryFuelRecipe recipe) {
        buffer.writeResourceLocation(recipe.id());
        writeFluidInput(buffer, recipe.input());
        buffer.writeFloat(recipe.temperatureC());
        buffer.writeVarInt(recipe.amountMb());
        buffer.writeVarInt(recipe.burnTicks());
    }

    private static FoundryFuelRecipe readFuelRecipe(RegistryFriendlyByteBuf buffer) {
        return new FoundryFuelRecipe(
                buffer.readResourceLocation(),
                readFluidInput(buffer),
                buffer.readFloat(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    private static void writeAlloyRecipe(RegistryFriendlyByteBuf buffer, FoundryAlloyRecipe recipe) {
        buffer.writeResourceLocation(recipe.id());
        buffer.writeResourceLocation(recipe.result());
        List<Map.Entry<ResourceLocation, Integer>> inputs = recipe.inputs().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .toList();
        checkSize("alloy inputs", inputs.size(), MAX_ALLOY_INPUTS);
        buffer.writeVarInt(inputs.size());
        inputs.forEach(input -> {
            buffer.writeResourceLocation(input.getKey());
            buffer.writeVarInt(input.getValue());
        });
        buffer.writeVarInt(recipe.outputAmountMb());
    }

    private static FoundryAlloyRecipe readAlloyRecipe(RegistryFriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        ResourceLocation result = buffer.readResourceLocation();
        int inputCount = readCount(buffer, "alloy inputs", MAX_ALLOY_INPUTS);
        Map<ResourceLocation, Integer> inputs = new LinkedHashMap<>();
        for (int index = 0; index < inputCount; index++) {
            ResourceLocation material = buffer.readResourceLocation();
            int amount = buffer.readVarInt();
            if (inputs.putIfAbsent(material, amount) != null) {
                throw new IllegalArgumentException("Duplicate foundry alloy input " + material + " in " + id);
            }
        }
        return new FoundryAlloyRecipe(id, result, inputs, buffer.readVarInt());
    }

    private static void writeCastRecipe(RegistryFriendlyByteBuf buffer, FoundryCastRecipe recipe) {
        buffer.writeResourceLocation(recipe.id());
        writeItemInput(buffer, recipe.input());
        buffer.writeResourceLocation(recipe.template());
        buffer.writeVarInt(recipe.goldAmountMb());
        buffer.writeVarInt(recipe.amountMb());
    }

    private static FoundryCastRecipe readCastRecipe(RegistryFriendlyByteBuf buffer) {
        return new FoundryCastRecipe(
                buffer.readResourceLocation(),
                readItemInput(buffer),
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    private static void writeItemInput(RegistryFriendlyByteBuf buffer, FoundryMeltingRecipe.Input input) {
        boolean item = input.itemId().isPresent();
        if (item == input.tag().isPresent()) {
            throw new IllegalArgumentException("Foundry item input must contain exactly one item or tag");
        }
        buffer.writeBoolean(item);
        buffer.writeResourceLocation(item ? input.itemId().orElseThrow() : input.tag().orElseThrow().location());
    }

    private static FoundryMeltingRecipe.Input readItemInput(RegistryFriendlyByteBuf buffer) {
        boolean item = buffer.readBoolean();
        ResourceLocation id = buffer.readResourceLocation();
        return item ? FoundryMeltingRecipe.Input.item(id) : FoundryMeltingRecipe.Input.tag(id);
    }

    private static void writeFluidInput(RegistryFriendlyByteBuf buffer, FoundryFuelRecipe.Input input) {
        boolean fluid = input.fluidId().isPresent();
        if (fluid == input.tag().isPresent()) {
            throw new IllegalArgumentException("Foundry fuel input must contain exactly one fluid or tag");
        }
        buffer.writeBoolean(fluid);
        buffer.writeResourceLocation(fluid ? input.fluidId().orElseThrow() : input.tag().orElseThrow().location());
    }

    private static FoundryFuelRecipe.Input readFluidInput(RegistryFriendlyByteBuf buffer) {
        boolean fluid = buffer.readBoolean();
        ResourceLocation id = buffer.readResourceLocation();
        return fluid ? FoundryFuelRecipe.Input.fluid(id) : FoundryFuelRecipe.Input.tag(id);
    }

    private static <T> void writeList(
            RegistryFriendlyByteBuf buffer,
            List<T> values,
            BiConsumer<RegistryFriendlyByteBuf, T> encoder
    ) {
        checkSize("registry entries", values.size(), MAX_ENTRIES_PER_REGISTRY);
        buffer.writeVarInt(values.size());
        values.forEach(value -> encoder.accept(buffer, value));
    }

    private static <T> List<T> readList(
            RegistryFriendlyByteBuf buffer,
            String name,
            Function<RegistryFriendlyByteBuf, T> decoder
    ) {
        int count = readCount(buffer, name, MAX_ENTRIES_PER_REGISTRY);
        List<T> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            values.add(decoder.apply(buffer));
        }
        return values;
    }

    private static int readCount(RegistryFriendlyByteBuf buffer, String name, int maximum) {
        int count = buffer.readVarInt();
        checkSize(name, count, maximum);
        return count;
    }

    private static void checkSize(String name, int count, int maximum) {
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("Invalid " + name + " count " + count + "; maximum is " + maximum);
        }
    }

    private static <T> List<T> sorted(List<T> values, Function<T, ResourceLocation> idGetter) {
        return values.stream().sorted(Comparator.comparing(value -> idGetter.apply(value).toString())).toList();
    }

    private static <T> Map<ResourceLocation, T> indexById(
            List<T> values,
            Function<T, ResourceLocation> idGetter,
            String kind
    ) {
        Map<ResourceLocation, T> indexed = new LinkedHashMap<>();
        for (T value : values) {
            ResourceLocation id = idGetter.apply(value);
            if (indexed.putIfAbsent(id, value) != null) {
                throw new IllegalArgumentException("Duplicate foundry " + kind + " id " + id);
            }
        }
        return indexed;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class StationWorkRecipeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public StationWorkRecipeReloadListener() {
        super(GSON, "mobstoolforging/station_work");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, StationWorkRecipe> loaded = new LinkedHashMap<>();
        recipes.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "station work recipe")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid station work recipe {}.", id, exception);
            }
        });
        StationWorkRecipeRegistry.replace(loaded);
        MobsToolForging.LOGGER.info("Loaded {} station work recipe(s).", loaded.size());
    }

    private static StationWorkRecipe parse(ResourceLocation id, JsonObject json) {
        WorkstationKind workstationKind = parseWorkstation(GsonHelper.getAsString(json, "workstation", "tool_forge"));
        Optional<ResourceLocation> pattern = parsePattern(json);
        StationWorkRecipe.Input input = parseInput(GsonHelper.getAsJsonObject(json, "input"));
        ItemStack output = parseOutput(GsonHelper.getAsJsonObject(json, "output"));
        int requiredHits = Math.max(1, GsonHelper.getAsInt(json, "required_hits", 1));
        int minimumHammerLevel = parseHammerLevel(json, SmithingHammerLevel.STONE.level());
        return new StationWorkRecipe(id, workstationKind, pattern, input, output, requiredHits, minimumHammerLevel);
    }

    private static WorkstationKind parseWorkstation(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.equals("smithing_anvil") || normalized.equals("tool_forge")) {
            return WorkstationKind.TOOL_FORGE;
        }
        if (normalized.equals("lapidary_table")) {
            return WorkstationKind.LAPIDARY_TABLE;
        }
        return WorkstationKind.valueOf(normalized.toUpperCase(Locale.ROOT));
    }

    private static Optional<ResourceLocation> parsePattern(JsonObject json) {
        if (!json.has("pattern")) {
            return Optional.empty();
        }
        JsonElement element = json.get("pattern");
        if (element.isJsonNull()) {
            return Optional.empty();
        }
        String value = element.getAsString();
        if (value.isBlank() || value.equalsIgnoreCase("none")) {
            return Optional.empty();
        }
        return Optional.of(ResourceLocation.parse(value));
    }

    private static StationWorkRecipe.Input parseInput(JsonObject input) {
        if (input.has("item")) {
            return StationWorkRecipe.Input.item(ResourceLocation.parse(GsonHelper.getAsString(input, "item")));
        }
        if (input.has("tag")) {
            return StationWorkRecipe.Input.tag(ResourceLocation.parse(GsonHelper.getAsString(input, "tag")));
        }
        throw new IllegalArgumentException("Station work input needs an item or tag");
    }

    private static ItemStack parseOutput(JsonObject output) {
        ResourceLocation itemId = ResourceLocation.parse(output.has("id") ? GsonHelper.getAsString(output, "id") : GsonHelper.getAsString(output, "item"));
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Unknown output item " + itemId);
        }
        return new ItemStack(item, Math.max(1, GsonHelper.getAsInt(output, "count", 1)));
    }

    private static int parseHammerLevel(JsonObject json, int fallback) {
        if (!json.has("minimum_hammer_level")) {
            return fallback;
        }
        JsonElement element = json.get("minimum_hammer_level");
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return Math.max(0, element.getAsInt());
        }
        return SmithingHammerLevel.parseLevel(element.getAsString(), fallback);
    }
}

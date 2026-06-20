package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class ToolStatRuleReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ToolStatRuleReloadListener() {
        super(GSON, "mobstoolforging/stat_rules");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> rules, ResourceManager resourceManager, ProfilerFiller profiler) {
        ToolTypeRegistry.resetDatapackStatModifiers();
        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : rules.entrySet()) {
            try {
                ToolTypeRegistry.registerDatapackStatModifier(parse(entry.getKey(), GsonHelper.convertToJsonObject(entry.getValue(), "tool stat rule")));
                loaded++;
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid tool stat rule {}.", entry.getKey(), exception);
            }
        }
        MobsToolForging.LOGGER.info("Loaded {} datapack tool stat rule(s).", loaded);
    }

    private static ToolStatRule parse(ResourceLocation id, JsonObject json) {
        Optional<ResourceLocation> toolType = optionalLocation(json, "tool_type");
        String slot = GsonHelper.getAsString(json, "slot", GsonHelper.getAsString(json, "part", "any"));
        ResourceLocation material = ResourceLocation.parse(GsonHelper.getAsString(json, "material"));
        return new ToolStatRule(
                id,
                toolType,
                slot,
                material,
                Math.max(0.0F, GsonHelper.getAsFloat(json, "durability_multiplier", 1.0F)),
                Math.max(0.0F, GsonHelper.getAsFloat(json, "mining_speed_multiplier", 1.0F)),
                GsonHelper.getAsFloat(json, "attack_damage_bonus", 0.0F),
                GsonHelper.getAsFloat(json, "attack_speed_bonus", 0.0F),
                GsonHelper.getAsBoolean(json, "fire_resistant", false),
                resourceLocations(json, "traits"),
                resourceLocations(json, "affinities"),
                nullableString(json, "debug")
        );
    }

    private static Optional<ResourceLocation> optionalLocation(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? Optional.of(ResourceLocation.parse(GsonHelper.getAsString(json, key)))
                : Optional.empty();
    }

    @Nullable
    private static String nullableString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? GsonHelper.getAsString(json, key) : null;
    }

    private static List<ResourceLocation> resourceLocations(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, key);
        List<ResourceLocation> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(ResourceLocation.parse(element.getAsString()));
        }
        return List.copyOf(values);
    }
}

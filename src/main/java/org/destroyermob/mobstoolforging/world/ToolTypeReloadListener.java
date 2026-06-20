package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class ToolTypeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ToolTypeReloadListener() {
        super(GSON, "mobstoolforging/tool_types");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> toolTypes, ResourceManager resourceManager, ProfilerFiller profiler) {
        ToolTypeRegistry.resetDatapackToolTypes();
        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : toolTypes.entrySet()) {
            try {
                if (ToolTypeRegistry.registerDatapackToolType(parse(entry.getKey(), GsonHelper.convertToJsonObject(entry.getValue(), "tool type")))) {
                    loaded++;
                }
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid tool type {}.", entry.getKey(), exception);
            }
        }
        MobsToolForging.LOGGER.info("Loaded {} datapack tool type definition(s).", loaded);
    }

    private static ToolTypeDefinition parse(ResourceLocation id, JsonObject json) {
        String primaryPartType = GsonHelper.getAsString(json, "primary_part_type", id.getPath() + "_head");
        ToolTypeDefinition.Builder builder = ToolTypeDefinition.builder(id, primaryPartType)
                .toolItem(() -> parseItem(GsonHelper.getAsString(json, "tool_item"), "tool_item"))
                .visual(json.has("visual") ? ResourceLocation.parse(GsonHelper.getAsString(json, "visual")) : id)
                .baseStats(
                        GsonHelper.getAsFloat(json, "base_attack_damage_bonus", 1.0F),
                        GsonHelper.getAsFloat(json, "base_attack_speed_bonus", -2.8F)
                );

        boolean swordLike = GsonHelper.getAsBoolean(json, "sword_like", false);
        if (swordLike) {
            builder.swordLike(true);
        } else if (json.has("mining_tag") && !json.get("mining_tag").isJsonNull()) {
            String miningTag = GsonHelper.getAsString(json, "mining_tag");
            if (miningTag.equals("none")) {
                builder.noMiningTag();
            } else {
                builder.miningTag(TagKey.create(Registries.BLOCK, ResourceLocation.parse(miningTag)));
            }
        } else {
            builder.noMiningTag();
        }

        Map<String, Item> partItems = partItems(json);
        if (json.has("primary_part_item")) {
            partItems.put(primaryPartType, parseItem(GsonHelper.getAsString(json, "primary_part_item"), "primary_part_item"));
        }
        Item primaryPartItem = partItems.get(primaryPartType);
        if (primaryPartItem == null) {
            throw new IllegalArgumentException("Tool type " + id + " needs a part item for primary part " + primaryPartType);
        }
        builder.partItem(primaryPartType, () -> primaryPartItem);

        if (json.has("required_assembly_parts")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "required_assembly_parts")) {
                String partType = element.getAsString();
                Item partItem = partItems.get(partType);
                if (partItem == null) {
                    throw new IllegalArgumentException("Required assembly part " + partType + " has no matching part_items entry");
                }
                builder.requiredAssemblyPart(partType, () -> partItem);
            }
        }

        partItems.forEach((partType, item) -> {
            if (!partType.equals(primaryPartType)) {
                builder.partItem(partType, () -> item);
            }
        });
        return builder.build();
    }

    private static Map<String, Item> partItems(JsonObject json) {
        Map<String, Item> values = new LinkedHashMap<>();
        if (!json.has("part_items")) {
            return values;
        }
        JsonObject object = GsonHelper.getAsJsonObject(json, "part_items");
        object.entrySet().forEach(entry -> values.put(entry.getKey(), parseItem(entry.getValue().getAsString(), "part_items." + entry.getKey())));
        return values;
    }

    private static Item parseItem(String value, String field) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value));
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Unknown item in " + field + ": " + value);
        }
        return item;
    }
}

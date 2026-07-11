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
                .visual(json.has("visual") ? ResourceLocation.parse(GsonHelper.getAsString(json, "visual")) : id)
                .baseStats(
                        GsonHelper.getAsFloat(json, "base_attack_damage_bonus", 1.0F),
                        GsonHelper.getAsFloat(json, "base_attack_speed_bonus", -2.8F)
                )
                .interactionRangeBonuses(
                        GsonHelper.getAsDouble(json, "entity_interaction_range_bonus", 0.0D),
                        GsonHelper.getAsDouble(json, "block_interaction_range_bonus", 0.0D)
                )
                .averageRequiredPartQuality(GsonHelper.getAsBoolean(json, "average_required_part_quality", false))
                .averageRequiredHeadDurability(GsonHelper.getAsBoolean(json, "average_required_head_durability", false));
        if (json.has("tool_item")) {
            Item toolItem = parseItem(GsonHelper.getAsString(json, "tool_item"), "tool_item");
            builder.toolItem(() -> toolItem);
        }
        Map<ResourceLocation, Float> materialDamage = materialFloats(json, "material_base_attack_damage_bonuses");
        Map<ResourceLocation, Float> materialSpeed = materialFloats(json, "material_base_attack_speed_bonuses");
        java.util.LinkedHashSet<ResourceLocation> statMaterials = new java.util.LinkedHashSet<>(materialDamage.keySet());
        statMaterials.addAll(materialSpeed.keySet());
        statMaterials.forEach(materialId -> builder.materialBaseStats(materialId, materialDamage.get(materialId), materialSpeed.get(materialId)));
        if (json.has("tool_items")) {
            JsonObject toolItems = GsonHelper.getAsJsonObject(json, "tool_items");
            toolItems.entrySet().forEach(entry -> {
                ResourceLocation materialId = ResourceLocation.parse(entry.getKey());
                Item item = parseItem(entry.getValue().getAsString(), "tool_items." + entry.getKey());
                builder.toolItem(materialId, () -> item);
            });
        }

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

        ParsedPartItems partItems = partItems(json);
        if (json.has("primary_part_item")) {
            partItems.defaults().put(primaryPartType, parseItem(GsonHelper.getAsString(json, "primary_part_item"), "primary_part_item"));
        }
        if (json.has("primary_part_items")) {
            JsonObject primaryPartItems = GsonHelper.getAsJsonObject(json, "primary_part_items");
            primaryPartItems.entrySet().forEach(entry -> {
                ResourceLocation materialId = ResourceLocation.parse(entry.getKey());
                Item item = parseItem(entry.getValue().getAsString(), "primary_part_items." + entry.getKey());
                partItems.materials().computeIfAbsent(primaryPartType, ignored -> new LinkedHashMap<>()).put(materialId, item);
            });
        }
        if (!partItems.hasPart(primaryPartType)) {
            throw new IllegalArgumentException("Tool type " + id + " needs a part item for primary part " + primaryPartType);
        }
        partItems.defaults().forEach((partType, item) -> builder.partItem(partType, () -> item));
        partItems.materials().forEach((partType, materialItems) -> materialItems.forEach((materialId, item) -> builder.partItem(partType, materialId, () -> item)));

        if (json.has("required_assembly_parts")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "required_assembly_parts")) {
                String partType = element.getAsString();
                if (!partItems.hasPart(partType)) {
                    throw new IllegalArgumentException("Required assembly part " + partType + " has no matching part_items entry");
                }
                builder.requiredAssemblyPart(partType);
            }
        }

        return builder.build();
    }

    private static Map<ResourceLocation, Float> materialFloats(JsonObject json, String field) {
        if (!json.has(field)) {
            return Map.of();
        }
        Map<ResourceLocation, Float> values = new LinkedHashMap<>();
        GsonHelper.getAsJsonObject(json, field).entrySet().forEach(entry ->
                values.put(ResourceLocation.parse(entry.getKey()), entry.getValue().getAsFloat()));
        return values;
    }

    private static ParsedPartItems partItems(JsonObject json) {
        ParsedPartItems values = new ParsedPartItems(new LinkedHashMap<>(), new LinkedHashMap<>());
        if (!json.has("part_items")) {
            return values;
        }
        JsonObject object = GsonHelper.getAsJsonObject(json, "part_items");
        object.entrySet().forEach(entry -> {
            if (entry.getValue().isJsonPrimitive()) {
                values.defaults().put(entry.getKey(), parseItem(entry.getValue().getAsString(), "part_items." + entry.getKey()));
                return;
            }
            JsonObject materialItems = GsonHelper.convertToJsonObject(entry.getValue(), "part_items." + entry.getKey());
            materialItems.entrySet().forEach(materialEntry -> {
                if (materialEntry.getKey().equals("default")) {
                    values.defaults().put(entry.getKey(), parseItem(materialEntry.getValue().getAsString(), "part_items." + entry.getKey() + ".default"));
                    return;
                }
                ResourceLocation materialId = ResourceLocation.parse(materialEntry.getKey());
                Item item = parseItem(materialEntry.getValue().getAsString(), "part_items." + entry.getKey() + "." + materialEntry.getKey());
                values.materials().computeIfAbsent(entry.getKey(), ignored -> new LinkedHashMap<>()).put(materialId, item);
            });
        });
        return values;
    }

    private static Item parseItem(String value, String field) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value));
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Unknown item in " + field + ": " + value);
        }
        return item;
    }

    private record ParsedPartItems(Map<String, Item> defaults, Map<String, Map<ResourceLocation, Item>> materials) {
        private boolean hasPart(String partType) {
            return defaults.containsKey(partType) || materials.containsKey(partType);
        }
    }
}

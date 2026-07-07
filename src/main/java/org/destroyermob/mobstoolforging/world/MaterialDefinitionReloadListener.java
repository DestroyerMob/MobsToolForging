package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class MaterialDefinitionReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public MaterialDefinitionReloadListener() {
        super(GSON, "mobstoolforging/materials");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> materials, ResourceManager resourceManager, ProfilerFiller profiler) {
        MaterialCatalog.resetDatapackMaterials();
        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : materials.entrySet()) {
            try {
                MaterialEntry material = parse(entry.getKey(), GsonHelper.convertToJsonObject(entry.getValue(), "material definition"));
                MaterialCatalog.registerDatapackMaterial(material.definition(), material.sourceItems(), material.sourceTags(), material.visualSlots(), material.handleItems());
                loaded++;
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid material definition {}.", entry.getKey(), exception);
            }
        }
        MobsToolForging.LOGGER.info("Loaded {} datapack material definition(s).", loaded);
    }

    private static MaterialEntry parse(ResourceLocation id, JsonObject json) {
        MaterialCategory category = parseCategory(GsonHelper.getAsString(json, "category", "metal"));
        Item displayItem = parseItem(GsonHelper.getAsString(json, "display_item", id.toString()), "display_item");
        String translationKey = GsonHelper.getAsString(json, "translation_key", null);
        Tier tier = parseTier(json, category, displayItem);
        HeatLevel minimumForgeHeat = parseMinimumForgeHeat(json, category);
        Optional<ResourceLocation> requiredLapidaryAbrasiveTier = parseRequiredLapidaryAbrasiveTier(json);
        List<Item> sourceItems = items(json, "items");
        List<TagKey<Item>> sourceTags = itemTags(json, "tags");
        List<String> visualSlots = strings(json, "visual_slots");
        List<Item> handleItems = items(json, "handle_items");
        return new MaterialEntry(
                new ToolMaterialDefinition(id, category, displayItem, tier, minimumForgeHeat, requiredLapidaryAbrasiveTier, translationKey),
                sourceItems,
                sourceTags,
                visualSlots,
                handleItems
        );
    }

    private static MaterialCategory parseCategory(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "gem", "gems" -> MaterialCategory.GEM;
            case "metal", "metals" -> MaterialCategory.METAL;
            default -> throw new IllegalArgumentException("Unknown material category: " + value);
        };
    }

    private static HeatLevel parseMinimumForgeHeat(JsonObject json, MaterialCategory category) {
        HeatLevel fallback = ToolMaterialDefinition.defaultMinimumForgeHeat(category);
        String value = null;
        if (json.has("minimum_forge_heat")) {
            value = GsonHelper.getAsString(json, "minimum_forge_heat");
        } else if (json.has("minimum_heat_level")) {
            value = GsonHelper.getAsString(json, "minimum_heat_level");
        }
        if (value == null) {
            return fallback;
        }
        HeatLevel parsed = HeatLevel.parse(value, null);
        if (parsed == null) {
            throw new IllegalArgumentException("Unknown minimum forge heat: " + value);
        }
        return parsed;
    }

    private static Optional<ResourceLocation> parseRequiredLapidaryAbrasiveTier(JsonObject json) {
        String value = null;
        if (json.has("required_lapidary_abrasive_tier")) {
            value = GsonHelper.getAsString(json, "required_lapidary_abrasive_tier");
        } else if (json.has("lapidary_abrasive_tier")) {
            value = GsonHelper.getAsString(json, "lapidary_abrasive_tier");
        }
        if (value == null || value.isBlank() || value.equalsIgnoreCase("none")) {
            return Optional.empty();
        }
        return Optional.of(parseAbrasiveTierId(value));
    }

    private static ResourceLocation parseAbrasiveTierId(String value) {
        String trimmed = value.trim();
        if (trimmed.contains(":")) {
            return ResourceLocation.parse(trimmed);
        }
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, trimmed);
    }

    private static Tier parseTier(JsonObject json, MaterialCategory category, Item displayItem) {
        Tier base = category == MaterialCategory.GEM ? Tiers.DIAMOND : Tiers.IRON;
        if (!json.has("tier")) {
            return base;
        }
        JsonElement element = json.get("tier");
        if (element.isJsonPrimitive()) {
            return namedTier(element.getAsString(), base);
        }
        JsonObject object = GsonHelper.convertToJsonObject(element, "tier");
        Tier tierBase = object.has("base") ? namedTier(GsonHelper.getAsString(object, "base"), base) : base;
        TagKey<Block> incorrectBlocks = object.has("incorrect_blocks_tag")
                ? TagKey.create(Registries.BLOCK, ResourceLocation.parse(GsonHelper.getAsString(object, "incorrect_blocks_tag")))
                : tierBase.getIncorrectBlocksForDrops();
        int maxDamage = Math.max(1, GsonHelper.getAsInt(object, "max_damage", tierBase.getUses()));
        float speed = Math.max(0.0F, GsonHelper.getAsFloat(object, "mining_speed", tierBase.getSpeed()));
        float damage = GsonHelper.getAsFloat(object, "attack_damage_bonus", tierBase.getAttackDamageBonus());
        int enchantment = Math.max(0, GsonHelper.getAsInt(object, "enchantment_value", tierBase.getEnchantmentValue()));
        Ingredient repair = repairIngredient(object, displayItem);
        return new SimpleTier(incorrectBlocks, maxDamage, speed, damage, enchantment, () -> repair);
    }

    private static Tier namedTier(String value, Tier fallback) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "wood", "wooden" -> Tiers.WOOD;
            case "stone" -> Tiers.STONE;
            case "iron" -> Tiers.IRON;
            case "gold", "golden" -> Tiers.GOLD;
            case "diamond" -> Tiers.DIAMOND;
            case "netherite" -> Tiers.NETHERITE;
            case "copper" -> MaterialCatalog.definition(MaterialCatalog.COPPER).map(ToolMaterialDefinition::tier).orElse(fallback);
            case "emerald" -> MaterialCatalog.definition(MaterialCatalog.EMERALD).map(ToolMaterialDefinition::tier).orElse(fallback);
            default -> fallback;
        };
    }

    private static Ingredient repairIngredient(JsonObject object, Item displayItem) {
        if (object.has("repair_tag")) {
            return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(GsonHelper.getAsString(object, "repair_tag"))));
        }
        if (object.has("repair_item")) {
            return Ingredient.of(parseItem(GsonHelper.getAsString(object, "repair_item"), "repair_item"));
        }
        return Ingredient.of(displayItem);
    }

    private static Item parseItem(String value, String field) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value));
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Unknown item in " + field + ": " + value);
        }
        return item;
    }

    private static List<String> strings(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, key);
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return List.copyOf(values);
    }

    private static List<Item> items(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, key);
        List<Item> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(parseItem(element.getAsString(), key));
        }
        return List.copyOf(values);
    }

    private static List<TagKey<Item>> itemTags(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, key);
        List<TagKey<Item>> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(TagKey.create(Registries.ITEM, ResourceLocation.parse(element.getAsString())));
        }
        return List.copyOf(values);
    }

    private record MaterialEntry(ToolMaterialDefinition definition, List<Item> sourceItems, List<TagKey<Item>> sourceTags, List<String> visualSlots, List<Item> handleItems) {
    }
}

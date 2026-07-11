package org.destroyermob.mobstoolforging.integration.everycomp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.mehvahdjukaar.every_compat.api.EveryCompatAPI;
import net.mehvahdjukaar.every_compat.api.SimpleEntrySet;
import net.mehvahdjukaar.every_compat.modules.EveryCompatModule;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.LeatherStationBlockItem;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModCreativeTabs;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.DryingRackBlock;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.ToolmakersBenchBlock;

/** Optional Every Compat entry point. This class is loaded only when Every Compat is present. */
public final class MobsToolForgingEveryCompat {
    private MobsToolForgingEveryCompat() {
    }

    public static void register() {
        Module module = new Module();
        EveryCompatAPI.registerModule(module);
        MoonlightEventsHelper.addListener(module::addTranslations, AfterLanguageLoadEvent.class);
    }

    private static final class Module extends EveryCompatModule {
        private final SimpleEntrySet<WoodType, PatternRackBlock> patternRacks;
        private final SimpleEntrySet<WoodType, ToolmakersBenchBlock> toolmakersBenches;
        private final SimpleEntrySet<WoodType, LeatherStationBlock> leatherStations;
        private final SimpleEntrySet<WoodType, DryingRackBlock> dryingRacks;

        private Module() {
            super(MobsToolForging.MOD_ID, "mtf");

            patternRacks = SimpleEntrySet.builder(
                            WoodType.class,
                            "pattern_rack",
                            ModBlocks.PATTERN_RACK,
                            () -> VanillaWoodTypes.OAK,
                            wood -> new PatternRackBlock(woodProperties(wood)))
                    .addTile(ModBlockEntities.PATTERN_RACK)
                    .addCustomItem((wood, block, properties) -> CompatWorkstationRegistry.registerItem(
                            CompatWorkstationRegistry.Kind.PATTERN_RACK,
                            new BlockItem(block, properties)))
                    .addTag(BlockTags.MINEABLE_WITH_AXE, Registries.BLOCK)
                    .setTabKey(ModCreativeTabs.MOBS_TOOL_FORGING.getKey())
                    .copyParentDrop()
                    .build();
            addEntry(patternRacks);

            toolmakersBenches = SimpleEntrySet.builder(
                            WoodType.class,
                            "toolmakers_bench",
                            ModBlocks.TOOLMAKERS_BENCH,
                            () -> VanillaWoodTypes.OAK,
                            wood -> new ToolmakersBenchBlock(woodProperties(wood)))
                    .addTile(ModBlockEntities.TOOL_WORKSTATION)
                    .addCustomItem((wood, block, properties) -> CompatWorkstationRegistry.registerItem(
                            CompatWorkstationRegistry.Kind.TOOLMAKERS_BENCH,
                            new BlockItem(block, properties)))
                    .addTag(BlockTags.MINEABLE_WITH_AXE, Registries.BLOCK)
                    .addTag(ModTags.Blocks.CARRY_ON_BLOCK_BLACKLIST, Registries.BLOCK)
                    .setTabKey(ModCreativeTabs.MOBS_TOOL_FORGING.getKey())
                    .copyParentDrop()
                    .build();
            addEntry(toolmakersBenches);

            leatherStations = SimpleEntrySet.builder(
                            WoodType.class,
                            "leather_station",
                            ModBlocks.LEATHER_STATION,
                            () -> VanillaWoodTypes.OAK,
                            wood -> CompatWorkstationRegistry.registerLeatherStation(
                                    wood.planks,
                                    wood.log,
                                    new LeatherStationBlock(woodProperties(wood))))
                    .addTile(ModBlockEntities.TOOL_WORKSTATION)
                    .addCustomItem((wood, block, properties) -> CompatWorkstationRegistry.registerItem(
                            CompatWorkstationRegistry.Kind.LEATHER_STATION,
                            new LeatherStationBlockItem(block, properties)))
                    .addTag(BlockTags.MINEABLE_WITH_AXE, Registries.BLOCK)
                    .setTabKey(ModCreativeTabs.MOBS_TOOL_FORGING.getKey())
                    .copyParentDrop()
                    .build();
            addEntry(leatherStations);

            dryingRacks = SimpleEntrySet.builder(
                            WoodType.class,
                            "drying_rack",
                            ModBlocks.DRYING_RACK,
                            () -> VanillaWoodTypes.OAK,
                            wood -> new DryingRackBlock(woodProperties(wood)))
                    .requiresChildren("slab")
                    .addTile(ModBlockEntities.DRYING_RACK)
                    .addCustomItem((wood, block, properties) -> CompatWorkstationRegistry.registerItem(
                            CompatWorkstationRegistry.Kind.DRYING_RACK,
                            new BlockItem(block, properties)))
                    .addTag(BlockTags.MINEABLE_WITH_AXE, Registries.BLOCK)
                    .setTabKey(ModCreativeTabs.MOBS_TOOL_FORGING.getKey())
                    .copyParentDrop()
                    .build();
            addEntry(dryingRacks);
        }

        @Override
        public void addDynamicServerResources(Consumer<ResourceGenTask> consumer) {
            super.addDynamicServerResources(consumer);
            consumer.accept((resourceManager, sink) -> {
                patternRacks.blocks.forEach((wood, block) -> addRecipe(
                        sink, block, 1, List.of("PPP", "S S", "PPP"), Map.of('P', wood.planks, 'S', Items.STICK)));
                toolmakersBenches.blocks.forEach((wood, block) -> addRecipe(
                        sink, block, 4, List.of("PPP", "S S", "S S"), Map.of('P', wood.planks, 'S', Items.STICK)));
                dryingRacks.blocks.forEach((wood, block) -> addRecipe(
                        sink, block, 4, List.of("SSS"), Map.of('S', wood.getBlockOfThis("slab"))));
            });
        }

        @Override
        public void addDynamicClientResources(Consumer<ResourceGenTask> consumer) {
            consumer.accept((resourceManager, sink) -> {
                patternRacks.blocks.forEach((wood, block) -> addClientResources(
                        sink,
                        block,
                        facingBlockState(block),
                        texturedModel(
                                "mobstoolforging:block/template_pattern_rack",
                                Map.of(
                                        "log", modelTextures(resourceManager, wood.log).side(),
                                        "planks", modelTextures(resourceManager, wood.planks).side()))));
                toolmakersBenches.blocks.forEach((wood, block) -> {
                    Block strippedLog = wood.getBlockOfThis("stripped_log");
                    if (strippedLog == null) {
                        strippedLog = wood.log;
                    }
                    ModelTextures logTextures = modelTextures(resourceManager, strippedLog);
                    addClientResources(
                            sink,
                            block,
                            facingBlockState(block),
                            texturedModel(
                                    "mobstoolforging:block/template_toolmakers_bench",
                                    Map.of("side", logTextures.side(), "top", logTextures.top())));
                });
                leatherStations.blocks.forEach((wood, block) -> addClientResources(
                        sink,
                        block,
                        leatherStationBlockState(block),
                        texturedModel(
                                "mobstoolforging:block/template_leather_station",
                                Map.of(
                                        "log", modelTextures(resourceManager, wood.log).side(),
                                        "planks", modelTextures(resourceManager, wood.planks).side(),
                                        "display_left", ResourceLocation.withDefaultNamespace("item/leather"),
                                        "display_right", ResourceLocation.fromNamespaceAndPath(
                                                MobsToolForging.MOD_ID, "item/plant_fiber")))));
                dryingRacks.blocks.forEach((wood, block) -> addClientResources(
                        sink,
                        block,
                        facingBlockState(block),
                        texturedModel(
                                "mobstoolforging:block/template_drying_rack",
                                Map.of("planks", modelTextures(resourceManager, wood.planks).side()))));
            });
        }

        @Override
        public List<String> getAlreadySupportedMods() {
            return List.of("minecraft");
        }

        private void addTranslations(AfterLanguageLoadEvent event) {
            addTranslations(event, patternRacks, "Pattern Rack");
            addTranslations(event, toolmakersBenches, "Toolmaker's Station");
            addTranslations(event, leatherStations, "Leather Station");
            addTranslations(event, dryingRacks, "Drying Rack");
        }

        private static void addTranslations(
                AfterLanguageLoadEvent event,
                SimpleEntrySet<WoodType, ? extends Block> entries,
                String suffix
        ) {
            entries.blocks.forEach((wood, block) ->
                    event.addEntry(block.getDescriptionId(), wood.getReadableName() + " " + suffix));
        }

        private static BlockBehaviour.Properties woodProperties(WoodType wood) {
            return BlockBehaviour.Properties.ofFullCopy(wood.planks).strength(1.5F, 3.0F).noOcclusion();
        }

        private static void addRecipe(
                ResourceSink sink,
                Block output,
                int count,
                List<String> pattern,
                Map<Character, ? extends ItemLike> ingredients
        ) {
            ResourceLocation outputId = BuiltInRegistries.BLOCK.getKey(output);
            JsonObject recipe = new JsonObject();
            recipe.addProperty("type", "minecraft:crafting_shaped");
            recipe.addProperty("category", "misc");

            JsonArray rows = new JsonArray();
            pattern.forEach(rows::add);
            recipe.add("pattern", rows);

            JsonObject key = new JsonObject();
            ingredients.forEach((symbol, ingredient) -> {
                JsonObject value = new JsonObject();
                value.addProperty("item", BuiltInRegistries.ITEM.getKey(ingredient.asItem()).toString());
                key.add(symbol.toString(), value);
            });
            recipe.add("key", key);

            JsonObject result = new JsonObject();
            result.addProperty("id", outputId.toString());
            result.addProperty("count", count);
            recipe.add("result", result);
            sink.addJson(outputId, recipe, ResType.RECIPES);
        }

        private static void addClientResources(
                ResourceSink sink,
                Block block,
                JsonObject blockState,
                JsonObject blockModel
        ) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            sink.addBlockState(id, blockState);
            sink.addBlockModel(id, blockModel);

            JsonObject itemModel = new JsonObject();
            itemModel.addProperty("parent", modelId(id));
            sink.addItemModel(id, itemModel);
        }

        private static JsonObject facingBlockState(Block block) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            JsonObject variants = new JsonObject();
            addVariant(variants, "facing=north", modelId(id), 0);
            addVariant(variants, "facing=east", modelId(id), 90);
            addVariant(variants, "facing=south", modelId(id), 180);
            addVariant(variants, "facing=west", modelId(id), 270);
            JsonObject root = new JsonObject();
            root.add("variants", variants);
            return root;
        }

        private static JsonObject leatherStationBlockState(Block block) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            JsonObject variants = new JsonObject();
            for (Map.Entry<String, Integer> facing : Map.of(
                    "north", 0,
                    "east", 90,
                    "south", 180,
                    "west", 270).entrySet()) {
                addVariant(variants, "facing=" + facing.getKey() + ",part=foot", modelId(id), facing.getValue());
                addVariant(
                        variants,
                        "facing=" + facing.getKey() + ",part=head",
                        "mobstoolforging:block/invisible_leather_station",
                        facing.getValue());
            }
            JsonObject root = new JsonObject();
            root.add("variants", variants);
            return root;
        }

        private static void addVariant(JsonObject variants, String state, String model, int rotation) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", model);
            if (rotation != 0) {
                variant.addProperty("y", rotation);
            }
            variants.add(state, variant);
        }

        private static JsonObject texturedModel(String parent, Map<String, ResourceLocation> textures) {
            JsonObject model = new JsonObject();
            model.addProperty("parent", parent);
            JsonObject textureJson = new JsonObject();
            textures.forEach((name, texture) -> textureJson.addProperty(name, texture.toString()));
            model.add("textures", textureJson);
            return model;
        }

        private static ResourceLocation blockTexture(Block block) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
        }

        private static ModelTextures modelTextures(ResourceManager resourceManager, Block block) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            try {
                Map<String, String> textures = readModelTextures(
                        resourceManager,
                        ResourceLocation.fromNamespaceAndPath(
                                blockId.getNamespace(), "block/" + blockId.getPath()),
                        Set.of());
                ResourceLocation fallback = blockTexture(block);
                return new ModelTextures(
                        texture(textures, fallback, "side", "north", "all", "particle"),
                        texture(textures, fallback, "end", "up", "top", "particle", "all"));
            } catch (Exception exception) {
                MobsToolForging.LOGGER.warn("Could not resolve wood textures for {}", blockId, exception);
                ResourceLocation fallback = blockTexture(block);
                return new ModelTextures(fallback, fallback);
            }
        }

        private static Map<String, String> readModelTextures(
                ResourceManager resourceManager,
                ResourceLocation modelId,
                Set<ResourceLocation> visited
        ) throws Exception {
            if (visited.contains(modelId)) {
                return Map.of();
            }
            Set<ResourceLocation> nextVisited = new java.util.HashSet<>(visited);
            nextVisited.add(modelId);
            ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(
                    modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
            JsonObject model;
            try (BufferedReader reader = resourceManager.getResourceOrThrow(resourceId).openAsReader()) {
                model = JsonParser.parseReader(reader).getAsJsonObject();
            }

            Map<String, String> textures = new HashMap<>();
            if (model.has("parent")) {
                ResourceLocation parent = ResourceLocation.parse(model.get("parent").getAsString());
                textures.putAll(readModelTextures(resourceManager, parent, nextVisited));
            }
            if (model.has("textures")) {
                model.getAsJsonObject("textures").entrySet()
                        .forEach(entry -> textures.put(entry.getKey(), entry.getValue().getAsString()));
            }
            return textures;
        }

        private static ResourceLocation texture(
                Map<String, String> textures,
                ResourceLocation fallback,
                String... preferredKeys
        ) {
            for (String key : preferredKeys) {
                String value = resolveTextureReference(textures, textures.get(key), Set.of());
                if (value != null && !value.startsWith("#")) {
                    return ResourceLocation.parse(value);
                }
            }
            return fallback;
        }

        private static String resolveTextureReference(
                Map<String, String> textures,
                String value,
                Set<String> visited
        ) {
            if (value == null || !value.startsWith("#")) {
                return value;
            }
            String key = value.substring(1);
            if (visited.contains(key)) {
                return null;
            }
            Set<String> nextVisited = new java.util.HashSet<>(visited);
            nextVisited.add(key);
            return resolveTextureReference(textures, textures.get(key), nextVisited);
        }

        private static String modelId(ResourceLocation blockId) {
            return blockId.getNamespace() + ":block/" + blockId.getPath();
        }

        private record ModelTextures(ResourceLocation side, ResourceLocation top) {
        }
    }
}

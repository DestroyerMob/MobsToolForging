package org.destroyermob.mobstoolforging.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolMaterialDefinition;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class HeatingForgeInsertVisualManager extends SimplePreparableReloadListener<HeatingForgeInsertVisualManager.LoadedVisuals> {
    public static final HeatingForgeInsertVisualManager INSTANCE = new HeatingForgeInsertVisualManager();
    private static final String PATH = "heating_forge_insert_visuals";
    private static volatile LoadedVisuals loaded = LoadedVisuals.empty();

    private HeatingForgeInsertVisualManager() {
    }

    public static ResolvedInsert fuel(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        LoadedVisuals visuals = loaded;
        HeatingForgeInsertVisual visual = visuals.fuelItemVisuals().getOrDefault(itemId, visuals.fallbackFuel());
        return new ResolvedInsert(visual, visuals.model(visual.model()));
    }

    public static ResolvedInsert workpiece(ItemStack stack) {
        LoadedVisuals visuals = loaded;
        ResourceLocation materialId = materialId(stack);
        if (materialId != null) {
            HeatingForgeInsertVisual materialVisual = visuals.materialVisuals().get(materialId);
            if (materialVisual != null) {
                return new ResolvedInsert(materialVisual, visuals.model(materialVisual.model()));
            }
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        HeatingForgeInsertVisual visual = visuals.workpieceItemVisuals().getOrDefault(itemId, visuals.fallbackWorkpiece());
        return new ResolvedInsert(visual, visuals.model(visual.model()));
    }

    private static ResourceLocation materialId(ItemStack stack) {
        ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
        if (partData != null) {
            return partData.materialId();
        }
        return MaterialCatalog.resolve(stack)
                .map(ToolMaterialDefinition::id)
                .orElse(null);
    }

    @Override
    protected LoadedVisuals prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        MutableVisuals visuals = MutableVisuals.defaults();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(PATH, location -> location.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            loadVisualFile(entry.getKey(), entry.getValue(), visuals);
        }
        Set<ResourceLocation> modelIds = new LinkedHashSet<>();
        modelIds.add(visuals.fallbackFuel().model());
        modelIds.add(visuals.fallbackWorkpiece().model());
        visuals.fuelItemVisuals().values().forEach(visual -> modelIds.add(visual.model()));
        visuals.workpieceItemVisuals().values().forEach(visual -> modelIds.add(visual.model()));
        visuals.materialVisuals().values().forEach(visual -> modelIds.add(visual.model()));

        Map<ResourceLocation, HeatingForgeVoxelModel> models = new LinkedHashMap<>();
        for (ResourceLocation modelId : modelIds) {
            models.put(modelId, HeatingForgeVoxelModel.load(resourceManager, modelId));
        }
        return new LoadedVisuals(
                Map.copyOf(visuals.fuelItemVisuals()),
                Map.copyOf(visuals.workpieceItemVisuals()),
                Map.copyOf(visuals.materialVisuals()),
                visuals.fallbackFuel(),
                visuals.fallbackWorkpiece(),
                Map.copyOf(models)
        );
    }

    @Override
    protected void apply(LoadedVisuals visuals, ResourceManager resourceManager, ProfilerFiller profiler) {
        loaded = visuals;
    }

    private static void loadVisualFile(ResourceLocation id, Resource resource, MutableVisuals visuals) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject json = GsonHelper.parse(reader);
            if (json.has("fallback_fuel")) {
                visuals.fallbackFuel(HeatingForgeInsertVisual.fuelFromJson(GsonHelper.getAsJsonObject(json, "fallback_fuel"), visuals.fallbackFuel()));
            }
            if (json.has("fallback_workpiece")) {
                visuals.fallbackWorkpiece(HeatingForgeInsertVisual.workpieceFromJson(GsonHelper.getAsJsonObject(json, "fallback_workpiece"), visuals.fallbackWorkpiece()));
            }
            JsonArray entries = GsonHelper.getAsJsonArray(json, "entries", new JsonArray());
            for (JsonElement element : entries) {
                JsonObject entry = GsonHelper.convertToJsonObject(element, "entry");
                HeatingForgeInsertVisual fuelVisual = HeatingForgeInsertVisual.fuelFromJson(entry, visuals.fallbackFuel());
                HeatingForgeInsertVisual workpieceVisual = HeatingForgeInsertVisual.workpieceFromJson(entry, visuals.fallbackWorkpiece());
                if (entry.has("fuel")) {
                    visuals.fuelItemVisuals().put(ResourceLocation.parse(GsonHelper.getAsString(entry, "fuel")), fuelVisual);
                }
                if (entry.has("workpiece")) {
                    visuals.workpieceItemVisuals().put(ResourceLocation.parse(GsonHelper.getAsString(entry, "workpiece")), workpieceVisual);
                }
                if (entry.has("item")) {
                    ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(entry, "item"));
                    visuals.fuelItemVisuals().put(itemId, fuelVisual);
                    visuals.workpieceItemVisuals().put(itemId, workpieceVisual);
                }
                if (entry.has("material")) {
                    visuals.materialVisuals().put(ResourceLocation.parse(GsonHelper.getAsString(entry, "material")), workpieceVisual);
                }
            }
        } catch (IOException | RuntimeException exception) {
            MobsToolForging.LOGGER.warn("Failed to load heating forge insert visuals {}", id, exception);
        }
    }

    public record ResolvedInsert(HeatingForgeInsertVisual visual, HeatingForgeVoxelModel model) {
    }

    public record LoadedVisuals(
            Map<ResourceLocation, HeatingForgeInsertVisual> fuelItemVisuals,
            Map<ResourceLocation, HeatingForgeInsertVisual> workpieceItemVisuals,
            Map<ResourceLocation, HeatingForgeInsertVisual> materialVisuals,
            HeatingForgeInsertVisual fallbackFuel,
            HeatingForgeInsertVisual fallbackWorkpiece,
            Map<ResourceLocation, HeatingForgeVoxelModel> models
    ) {
        static LoadedVisuals empty() {
            HeatingForgeInsertVisual fuel = HeatingForgeInsertVisual.defaultFuel();
            HeatingForgeInsertVisual workpiece = HeatingForgeInsertVisual.defaultWorkpiece();
            return new LoadedVisuals(Map.of(), Map.of(), Map.of(), fuel, workpiece, Map.of());
        }

        HeatingForgeVoxelModel model(ResourceLocation modelId) {
            return models.getOrDefault(modelId, HeatingForgeVoxelModel.EMPTY);
        }
    }

    private static final class MutableVisuals {
        private final Map<ResourceLocation, HeatingForgeInsertVisual> fuelItemVisuals;
        private final Map<ResourceLocation, HeatingForgeInsertVisual> workpieceItemVisuals;
        private final Map<ResourceLocation, HeatingForgeInsertVisual> materialVisuals;
        private HeatingForgeInsertVisual fallbackFuel;
        private HeatingForgeInsertVisual fallbackWorkpiece;

        private MutableVisuals(Map<ResourceLocation, HeatingForgeInsertVisual> fuelItemVisuals, Map<ResourceLocation, HeatingForgeInsertVisual> workpieceItemVisuals, Map<ResourceLocation, HeatingForgeInsertVisual> materialVisuals, HeatingForgeInsertVisual fallbackFuel, HeatingForgeInsertVisual fallbackWorkpiece) {
            this.fuelItemVisuals = fuelItemVisuals;
            this.workpieceItemVisuals = workpieceItemVisuals;
            this.materialVisuals = materialVisuals;
            this.fallbackFuel = fallbackFuel;
            this.fallbackWorkpiece = fallbackWorkpiece;
        }

        static MutableVisuals defaults() {
            return new MutableVisuals(
                    new LinkedHashMap<>(),
                    new LinkedHashMap<>(),
                    new LinkedHashMap<>(),
                    HeatingForgeInsertVisual.defaultFuel(),
                    HeatingForgeInsertVisual.defaultWorkpiece()
            );
        }

        Map<ResourceLocation, HeatingForgeInsertVisual> fuelItemVisuals() {
            return fuelItemVisuals;
        }

        Map<ResourceLocation, HeatingForgeInsertVisual> workpieceItemVisuals() {
            return workpieceItemVisuals;
        }

        Map<ResourceLocation, HeatingForgeInsertVisual> materialVisuals() {
            return materialVisuals;
        }

        HeatingForgeInsertVisual fallbackFuel() {
            return fallbackFuel;
        }

        void fallbackFuel(HeatingForgeInsertVisual visual) {
            fallbackFuel = visual;
        }

        HeatingForgeInsertVisual fallbackWorkpiece() {
            return fallbackWorkpiece;
        }

        void fallbackWorkpiece(HeatingForgeInsertVisual visual) {
            fallbackWorkpiece = visual;
        }
    }
}

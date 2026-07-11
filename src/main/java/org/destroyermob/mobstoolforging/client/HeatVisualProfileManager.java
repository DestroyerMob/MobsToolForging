package org.destroyermob.mobstoolforging.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public final class HeatVisualProfileManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static final HeatVisualProfileManager INSTANCE = new HeatVisualProfileManager();
    private volatile List<HeatVisualProfile> profiles = List.of();
    private final Map<ResourceLocation, HeatVisualProfile> materialCache = new ConcurrentHashMap<>();

    private HeatVisualProfileManager() {
        super(GSON, "heating_visual_profiles");
    }

    public HeatVisualProfile resolve(ItemStack stack) {
        List<HeatVisualProfile> loaded = profiles;
        for (HeatVisualProfile profile : loaded) {
            if (stack.is(profile.tag())) {
                return profile;
            }
        }

        ToolPartData part = stack.get(ModDataComponents.TOOL_PART.get());
        ResourceLocation materialId = part == null ? MaterialCatalog.bindingMaterial(stack) : part.materialId();
        return materialCache.computeIfAbsent(materialId, this::resolveMaterial);
    }

    private HeatVisualProfile resolveMaterial(ResourceLocation materialId) {
        List<ItemStack> materialSources = MaterialCatalog.ingredientStacks(materialId);
        for (HeatVisualProfile profile : profiles) {
            if (materialSources.stream().anyMatch(source -> source.is(profile.tag()))) {
                return profile;
            }
        }
        return HeatVisualProfile.GENERIC;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<HeatVisualProfile> loaded = new ArrayList<>();
        entries.forEach((id, element) -> {
            try {
                loaded.add(HeatVisualProfile.fromJson(GsonHelper.convertToJsonObject(element, "heat visual profile")));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid heat visual profile {}.", id, exception);
            }
        });
        loaded.sort(Comparator.comparingInt(HeatVisualProfile::priority).reversed());
        profiles = List.copyOf(loaded);
        materialCache.clear();
        MobsToolForging.LOGGER.info("Loaded {} data-driven heat visual profiles.", profiles.size());
    }
}

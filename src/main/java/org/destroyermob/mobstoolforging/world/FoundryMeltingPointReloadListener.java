package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class FoundryMeltingPointReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public FoundryMeltingPointReloadListener() {
        super(GSON, "mobstoolforging/foundry_melting_points");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FoundryMeltingPoint> loaded = new LinkedHashMap<>();
        entries.forEach((id, element) -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(element, "foundry melting point");
                loaded.put(id, new FoundryMeltingPoint(
                        id,
                        ResourceLocation.parse(GsonHelper.getAsString(json, "material")),
                        GsonHelper.getAsFloat(json, "melting_point_c")
                ));
            } catch (RuntimeException exception) {
                MobsToolForging.LOGGER.warn("Skipping invalid foundry melting point {}.", id, exception);
            }
        });
        FoundryMeltingPointRegistry.replace(loaded);
        MobsToolForging.LOGGER.info("Loaded {} foundry melting point(s).", loaded.size());
    }
}

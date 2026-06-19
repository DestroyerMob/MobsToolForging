package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ToolKind;

public final class ToolVisualManager {
    private ToolVisualManager() {
    }

    public static ToolVisualDefinition resolve(ResourceLocation visualId, ToolKind toolKind) {
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(
                visualId.getNamespace(),
                "tooling/tool_visuals/" + visualId.getPath() + ".json"
        );
        return Minecraft.getInstance().getResourceManager().getResource(resourceId)
                .map(resource -> {
                    try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        return ToolVisualDefinition.fromJson(visualId, GsonHelper.parse(reader));
                    } catch (IOException | JsonParseException exception) {
                        MobsToolForging.LOGGER.warn("Failed to load tool visual definition {}", visualId, exception);
                        return ToolVisualDefinition.fallback(visualId, toolKind);
                    }
                })
                .orElseGet(() -> ToolVisualDefinition.fallback(visualId, toolKind));
    }
}

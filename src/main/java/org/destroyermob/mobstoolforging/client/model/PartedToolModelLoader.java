package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ToolKind;

public final class PartedToolModelLoader implements IGeometryLoader<PartedToolGeometry> {
    private final boolean partModel;

    public PartedToolModelLoader(boolean partModel) {
        this.partModel = partModel;
    }

    @Override
    public PartedToolGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        String visual = GsonHelper.getAsString(jsonObject, "visual", null);
        String toolId = GsonHelper.getAsString(jsonObject, "tool", null);
        if (toolId == null && visual == null) {
            throw new JsonParseException("Parted tool model needs a 'tool' or 'visual' property");
        }
        ResourceLocation visualId = visual == null
                ? ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, toolId)
                : ResourceLocation.parse(visual);
        if (toolId == null) {
            toolId = visualId.getPath();
        }
        String resolvedToolId = toolId;
        ToolKind toolKind = ToolKind.byId(resolvedToolId)
                .orElseThrow(() -> new JsonParseException("Unknown tool type for parted tool model: " + resolvedToolId));
        return new PartedToolGeometry(toolKind, visualId, partModel);
    }
}

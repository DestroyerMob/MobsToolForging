package org.destroyermob.mobstoolforging.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.destroyermob.mobstoolforging.world.ToolKind;

public final class PartedToolModelLoader implements IGeometryLoader<PartedToolGeometry> {
    private final boolean partModel;

    public PartedToolModelLoader(boolean partModel) {
        this.partModel = partModel;
    }

    @Override
    public PartedToolGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        String toolId = GsonHelper.getAsString(jsonObject, "tool");
        ToolKind toolKind = ToolKind.byId(toolId).orElseThrow(() -> new JsonParseException("Unknown tool type for parted tool model: " + toolId));
        return new PartedToolGeometry(toolKind, partModel);
    }
}

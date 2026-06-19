package org.destroyermob.mobstoolforging.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;

public final class PartedToolQuadFactory {
    private final ModelState modelState;

    public PartedToolQuadFactory(ModelState modelState) {
        this.modelState = modelState;
    }

    public List<BakedQuad> bakeLayer(int layerIndex, TextureAtlasSprite sprite) {
        var elements = UnbakedGeometryHelper.createUnbakedItemElements(layerIndex, sprite);
        return List.copyOf(UnbakedGeometryHelper.bakeElements(elements, ignored -> sprite, modelState));
    }
}

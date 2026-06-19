package org.destroyermob.mobstoolforging.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.destroyermob.mobstoolforging.world.ToolKind;

public record PartedToolGeometry(ToolKind toolKind, boolean partModel) implements IUnbakedGeometry<PartedToolGeometry> {
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        var rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }

        PartedToolSpriteSet sprites = PartedToolSpriteSet.from(context, spriteGetter, toolKind);
        PartedToolQuadFactory quadFactory = new PartedToolQuadFactory(modelState);
        if (partModel) {
            return new PartedToolPartBakedModel(toolKind, sprites, quadFactory, context.getTransforms());
        }
        return new PartedToolBakedModel(toolKind, sprites, quadFactory, context.getTransforms());
    }
}

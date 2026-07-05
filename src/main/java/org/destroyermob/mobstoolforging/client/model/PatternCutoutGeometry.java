package org.destroyermob.mobstoolforging.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;

public record PatternCutoutGeometry(ResourceLocation boardTexture) implements IUnbakedGeometry<PatternCutoutGeometry> {
    @Override
    public PatternCutoutBakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        var rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }

        TextureAtlasSprite boardSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, boardTexture));
        return new PatternCutoutBakedModel(boardSprite, boardTexture, modelState, context.getTransforms());
    }
}

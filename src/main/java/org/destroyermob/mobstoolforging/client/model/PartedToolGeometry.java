package org.destroyermob.mobstoolforging.client.model;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;

public record PartedToolGeometry(Optional<ToolTypeDefinition> definition, ResourceLocation visualId, boolean partModel, String partType, String partSlot, Map<String, ResourceLocation> textureOverrides) implements IUnbakedGeometry<PartedToolGeometry> {
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        var rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }

        ToolVisualDefinition visual = definition
                .map(value -> ToolVisualManager.resolve(visualId, value))
                .orElseGet(() -> ToolVisualManager.resolve(visualId, partType));
        PartedToolSpriteSet sprites = PartedToolSpriteSet.from(context, spriteGetter, visual, textureOverrides, partModel);
        PartedToolQuadFactory quadFactory = new PartedToolQuadFactory(modelState);
        if (partModel) {
            return new PartedToolPartBakedModel(visual, partType, partSlot, sprites, quadFactory, context.getTransforms());
        }
        return new PartedToolBakedModel(definition.orElseThrow(), visual, sprites, quadFactory, context.getTransforms());
    }
}

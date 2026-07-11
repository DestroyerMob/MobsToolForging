package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;

public final class HeatRenderTypes {
    private static final ResourceLocation HEAT_MASK_SHADER_ID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "heat_mask");
    private static final ResourceLocation HEAT_SHIMMER_SHADER_ID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "heat_shimmer");
    private static ShaderInstance heatMaskShader;
    private static ShaderInstance heatShimmerShader;
    private static final RenderStateShard.ShaderStateShard HEAT_MASK_SHADER = new RenderStateShard.ShaderStateShard(() -> heatMaskShader);
    private static final RenderStateShard.ShaderStateShard HEAT_SHIMMER_SHADER = new RenderStateShard.ShaderStateShard(() -> heatShimmerShader);
    private static final RenderType HEAT_MASK = RenderType.create(
            "mobstoolforging_heat_mask",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(HEAT_MASK_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );
    private static final RenderType HEAT_SHIMMER = RenderType.create(
            "mobstoolforging_heat_shimmer",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(HEAT_SHIMMER_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private HeatRenderTypes() {
    }

    public static RenderType heatMask() {
        return HEAT_MASK;
    }

    public static RenderType heatShimmer() {
        return HEAT_SHIMMER;
    }

    public static void registerShader(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), HEAT_MASK_SHADER_ID, DefaultVertexFormat.NEW_ENTITY),
                    shader -> heatMaskShader = shader
            );
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), HEAT_SHIMMER_SHADER_ID, DefaultVertexFormat.POSITION_COLOR),
                    shader -> heatShimmerShader = shader
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the MTF heat mask shader", exception);
        }
    }
}

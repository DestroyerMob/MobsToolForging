package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.client.model.PartedToolModelLoader;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public final class MobsToolForgingClient {
    private MobsToolForgingClient() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(MobsToolForgingClient::registerRenderers);
        eventBus.addListener(MobsToolForgingClient::registerGeometryLoaders);
    }

    public static void openTemplateScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new ToolForgeTemplateScreen(pos));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TOOL_WORKSTATION.get(), ToolForgeRenderer::new);
    }

    private static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "parted_tool"), new PartedToolModelLoader(false));
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "parted_tool_part"), new PartedToolModelLoader(true));
    }
}

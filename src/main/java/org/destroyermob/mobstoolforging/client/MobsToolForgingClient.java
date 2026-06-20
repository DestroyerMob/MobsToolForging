package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.client.model.PartedToolModelLoader;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public final class MobsToolForgingClient {
    private MobsToolForgingClient() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(MobsToolForgingClient::registerRenderers);
        eventBus.addListener(MobsToolForgingClient::registerGeometryLoaders);
    }

    public static void openTemplateScreen(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        WorkstationKind kind = WorkstationKind.TOOL_FORGE;
        if (minecraft.level != null) {
            Block block = minecraft.level.getBlockState(pos).getBlock();
            if (block instanceof ToolWorkstationBlock workstation) {
                kind = workstation.kind();
            }
        }
        minecraft.setScreen(new ToolForgeTemplateScreen(pos, kind));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TOOL_WORKSTATION.get(), ToolForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATING_FORGE.get(), HeatingForgeRenderer::new);
    }

    private static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "parted_tool"), new PartedToolModelLoader(false));
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "parted_tool_part"), new PartedToolModelLoader(true));
    }
}

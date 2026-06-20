package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.client.model.ComponentDrivenToolBakedModel;
import org.destroyermob.mobstoolforging.client.model.PartedToolModelLoader;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public final class MobsToolForgingClient {
    private MobsToolForgingClient() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(MobsToolForgingClient::registerRenderers);
        eventBus.addListener(MobsToolForgingClient::registerGeometryLoaders);
        eventBus.addListener(MobsToolForgingClient::wrapComponentDrivenItemModels);
        eventBus.addListener(MobsToolForgingClient::registerItemDecorations);
        eventBus.addListener(MobsToolForgingClient::registerReloadListeners);
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

    private static void wrapComponentDrivenItemModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) -> {
            if (!ModelResourceLocation.INVENTORY_VARIANT.equals(location.variant()) || !ComponentDrivenToolBakedModel.shouldWrap(model)) {
                return model;
            }
            return new ComponentDrivenToolBakedModel(model);
        });
    }

    private static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(Items.IRON_INGOT, HeatItemDecorator.INSTANCE);
        event.register(Items.GOLD_INGOT, HeatItemDecorator.INSTANCE);
        event.register(Items.COPPER_INGOT, HeatItemDecorator.INSTANCE);
        event.register(Items.NETHERITE_INGOT, HeatItemDecorator.INSTANCE);
        event.register(ModItems.SWORD_BLADE.get(), HeatItemDecorator.INSTANCE);
        event.register(ModItems.SWORD_GUARD.get(), HeatItemDecorator.INSTANCE);
        event.register(ModItems.SHOVEL_HEAD.get(), HeatItemDecorator.INSTANCE);
        event.register(ModItems.PICKAXE_HEAD.get(), HeatItemDecorator.INSTANCE);
        event.register(ModItems.AXE_HEAD.get(), HeatItemDecorator.INSTANCE);
        event.register(ModItems.HOE_HEAD.get(), HeatItemDecorator.INSTANCE);
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(HeatingForgeInsertVisualManager.INSTANCE);
    }
}

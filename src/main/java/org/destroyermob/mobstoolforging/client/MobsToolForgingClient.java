package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.client.model.ComponentDrivenToolBakedModel;
import org.destroyermob.mobstoolforging.client.model.PartedToolModelLoader;
import org.destroyermob.mobstoolforging.client.model.ToolMaterialVisualManager;
import org.destroyermob.mobstoolforging.network.CycleKnappingTargetPayload;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModMenuTypes;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlockEntity;
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
        eventBus.addListener(MobsToolForgingClient::registerMenuScreens);
        NeoForge.EVENT_BUS.addListener(MobsToolForgingClient::handleKnappingScroll);
        NeoForge.EVENT_BUS.addListener(MobsToolForgingClient::showKnappingActionbar);
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
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_FORGE.get(), FoundryForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KNAPPING_FLINT.get(), KnappingFlintRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GROUND_TOOL_ASSEMBLY.get(), GroundToolAssemblyRenderer::new);
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
        event.registerReloadListener(ToolMaterialVisualManager.INSTANCE);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.PATTERN_CREATION_STATION.get(), PatternCreationStationScreen::new);
    }

    private static void handleKnappingScroll(InputEvent.MouseScrollingEvent event) {
        KnappingLookTarget target = knappingLookTarget(true);
        if (target == null) {
            return;
        }
        double scrollDelta = event.getScrollDeltaY();
        if (scrollDelta == 0.0D) {
            return;
        }
        PacketDistributor.sendToServer(new CycleKnappingTargetPayload(target.pos(), scrollDelta > 0.0D ? 1 : -1));
        event.setCanceled(true);
    }

    private static void showKnappingActionbar(ClientTickEvent.Post event) {
        KnappingLookTarget target = knappingLookTarget(false);
        if (target == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable(
                    "message.mobstoolforging.knapping_status",
                    target.knapping().target().displayName(),
                    target.knapping().hitCount(),
                    KnappingFlintBlockEntity.REQUIRED_HITS
            ), true);
        }
    }

    private static KnappingLookTarget knappingLookTarget(boolean requireSneaking) {
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return null;
        }
        if (requireSneaking && !minecraft.player.isShiftKeyDown()) {
            return null;
        }
        if (!minecraft.player.getMainHandItem().is(ModTags.Items.KNAPPING_TOOLS) && !minecraft.player.getOffhandItem().is(ModTags.Items.KNAPPING_TOOLS)) {
            return null;
        }
        if (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = ((BlockHitResult) minecraft.hitResult).getBlockPos();
        if (minecraft.level.getBlockEntity(pos) instanceof KnappingFlintBlockEntity knapping) {
            return new KnappingLookTarget(pos, knapping);
        }
        return null;
    }

    private record KnappingLookTarget(BlockPos pos, KnappingFlintBlockEntity knapping) {
    }
}

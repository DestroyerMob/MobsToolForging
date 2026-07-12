package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.client.model.ArmorMaterialTextureManager;
import org.destroyermob.mobstoolforging.client.model.ComponentDrivenToolBakedModel;
import org.destroyermob.mobstoolforging.client.model.HeatAwareItemBakedModel;
import org.destroyermob.mobstoolforging.client.model.ModularBodyArmourModel;
import org.destroyermob.mobstoolforging.client.model.ModularHelmetModel;
import org.destroyermob.mobstoolforging.client.model.ModularLowerArmourModel;
import org.destroyermob.mobstoolforging.client.model.PatternCutoutModelLoader;
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
        eventBus.addListener(MobsToolForgingClient::registerLayerDefinitions);
        eventBus.addListener(MobsToolForgingClient::addEntityLayers);
        eventBus.addListener(MobsToolForgingClient::registerClientExtensions);
        eventBus.addListener(MobsToolForgingClient::registerGeometryLoaders);
        eventBus.addListener(MobsToolForgingClient::wrapComponentDrivenItemModels);
        eventBus.addListener(MobsToolForgingClient::registerReloadListeners);
        eventBus.addListener(MobsToolForgingClient::registerMenuScreens);
        eventBus.addListener(MobsToolForgingClient::registerShaders);
        eventBus.addListener(MobsToolForgingClient::registerRenderBuffers);
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
        event.registerBlockEntityRenderer(ModBlockEntities.PATTERN_RACK.get(), PatternRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATING_FORGE.get(), HeatingForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAVA_HEATING_FORGE.get(), HeatingForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_FORGE.get(), FoundryForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KNAPPING_FLINT.get(), KnappingFlintRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GROUND_TOOL_ASSEMBLY.get(), GroundToolAssemblyRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModularHelmetModel.LAYER_LOCATION, ModularHelmetModel::createBodyLayer);
        event.registerLayerDefinition(ModularBodyArmourModel.LAYER_LOCATION, ModularBodyArmourModel::createBodyLayer);
        event.registerLayerDefinition(ModularLowerArmourModel.LAYER_LOCATION, ModularLowerArmourModel::createBodyLayer);
        event.registerLayerDefinition(ModularHelmetModel.BLANK_ARMOR_LAYER, ModularHelmetModel::createBlankArmorLayer);
    }

    private static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            addArmorLayers(renderer, event.getEntityModels());
            addModularCapeLayer(renderer);
        }
        for (var entityType : event.getEntityTypes()) {
            addArmorLayers(event.getRenderer(entityType), event.getEntityModels());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addArmorLayers(Object renderer, EntityModelSet modelSet) {
        if (renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer livingRenderer
                && livingRenderer.getModel() instanceof HumanoidModel) {
            livingRenderer.addLayer(new ModularHelmetLayer(livingRenderer, new ModularHelmetModel(modelSet.bakeLayer(ModularHelmetModel.LAYER_LOCATION))));
            livingRenderer.addLayer(new ModularBodyArmourLayer(
                    livingRenderer,
                    new ModularBodyArmourModel(modelSet.bakeLayer(ModularBodyArmourModel.LAYER_LOCATION))));
            livingRenderer.addLayer(new ModularLowerArmourLayer(
                    livingRenderer,
                    new ModularLowerArmourModel(modelSet.bakeLayer(ModularLowerArmourModel.LAYER_LOCATION))));
        }
    }

    private static void addModularCapeLayer(PlayerRenderer renderer) {
        if (renderer != null) {
            renderer.addLayer(new ModularCapeLayer(renderer));
        }
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(ModularHelmetClientExtensions.INSTANCE, ModItems.MODULAR_HELMET);
        event.registerItem(ModularHelmetClientExtensions.INSTANCE, ModItems.MODULAR_CHESTPLATE);
        event.registerItem(ModularHelmetClientExtensions.INSTANCE, ModItems.MODULAR_LEGGINGS);
        event.registerItem(ModularHelmetClientExtensions.INSTANCE, ModItems.MODULAR_BOOTS);
    }

    private static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "parted_tool"), new PartedToolModelLoader(false));
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "parted_tool_part"), new PartedToolModelLoader(true));
        event.register(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "pattern_cutout"), new PatternCutoutModelLoader());
    }

    private static void wrapComponentDrivenItemModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) -> {
            if (!ModelResourceLocation.INVENTORY_VARIANT.equals(location.variant())) {
                return model;
            }
            BakedModel resolved = ComponentDrivenToolBakedModel.shouldWrap(model)
                    ? new ComponentDrivenToolBakedModel(model)
                    : model;
            return new HeatAwareItemBakedModel(resolved);
        });
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(HeatingForgeInsertVisualManager.INSTANCE);
        event.registerReloadListener(HeatVisualProfileManager.INSTANCE);
        event.registerReloadListener(ToolMaterialVisualManager.INSTANCE);
        event.registerReloadListener(ArmorMaterialTextureManager.INSTANCE);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.PATTERN_CREATION_STATION.get(), PatternCreationStationScreen::new);
    }

    private static void registerShaders(RegisterShadersEvent event) {
        HeatRenderTypes.registerShader(event);
    }

    private static void registerRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(HeatRenderTypes.heatMask());
        event.registerRenderBuffer(HeatRenderTypes.heatShimmer());
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
        int delta = scrollDelta > 0.0D ? 1 : -1;
        target.knapping().cycleTarget(delta);
        PacketDistributor.sendToServer(new CycleKnappingTargetPayload(target.pos(), delta));
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
        if (!MobsToolForgingConfig.crudeFlintToolsEnabled()) {
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

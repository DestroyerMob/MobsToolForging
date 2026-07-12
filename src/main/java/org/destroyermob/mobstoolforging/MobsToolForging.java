package org.destroyermob.mobstoolforging;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.destroyermob.mobstoolforging.client.MobsToolForgingClient;
import org.destroyermob.mobstoolforging.command.ModCommands;
import org.destroyermob.mobstoolforging.data.ModDataGenerators;
import org.destroyermob.mobstoolforging.integration.CarryOnCompatibility;
import org.destroyermob.mobstoolforging.item.ModularToolItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.network.ModNetworking;
import org.destroyermob.mobstoolforging.integration.everycomp.MobsToolForgingEveryCompat;
import org.destroyermob.mobstoolforging.integration.everycomp.EveryCompatDropFallback;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModCreativeTabs;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModMenuTypes;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.registry.ModLootModifiers;
import org.destroyermob.mobstoolforging.world.AnvilForgingEvents;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorRepairing;
import org.destroyermob.mobstoolforging.world.ArmorStandSwapEvents;
import org.destroyermob.mobstoolforging.world.CampfireWorkpieceHeating;
import org.destroyermob.mobstoolforging.world.CompositeAffixCompatibility;
import org.destroyermob.mobstoolforging.world.CrucibleContents;
import org.destroyermob.mobstoolforging.world.DebugFeedback;
import org.destroyermob.mobstoolforging.world.DryingRecipeReloadListener;
import org.destroyermob.mobstoolforging.world.ExternalToolTooltipOrder;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ForgeTemplateReloadListener;
import org.destroyermob.mobstoolforging.world.FlintKnappingEvents;
import org.destroyermob.mobstoolforging.world.FlintToolStacks;
import org.destroyermob.mobstoolforging.world.GroundAssemblyRecipeReloadListener;
import org.destroyermob.mobstoolforging.world.HeatingRecipeReloadListener;
import org.destroyermob.mobstoolforging.world.LeatherStationAssemblyEvents;
import org.destroyermob.mobstoolforging.world.LapidaryTableAssemblyEvents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MaterialDefinitionReloadListener;
import org.destroyermob.mobstoolforging.world.PatternRackSelection;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeReloadListener;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolStatRuleReloadListener;
import org.destroyermob.mobstoolforging.world.ToolStatBuilder;
import org.destroyermob.mobstoolforging.world.ToolRepairing;
import org.destroyermob.mobstoolforging.world.ToolMaterialDefinition;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolPartPolishing;
import org.destroyermob.mobstoolforging.world.ToolPartWear;
import org.destroyermob.mobstoolforging.world.ToolTrait;
import org.destroyermob.mobstoolforging.world.ToolTraitReloadListener;
import org.destroyermob.mobstoolforging.world.ToolTooltipBuilder;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolTypeReloadListener;
import org.destroyermob.mobstoolforging.world.VanillaToolConverter;
import org.destroyermob.mobstoolforging.world.WorkshopHeat;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;
import org.slf4j.Logger;

@Mod(MobsToolForging.MOD_ID)
public class MobsToolForging {
    public static final String MOD_ID = "mobstoolforging";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobsToolForging(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModNetworking.register(modEventBus);
        ModDataGenerators.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        if (ModList.get().isLoaded("everycomp")) {
            MobsToolForgingEveryCompat.register();
        }
        ToolTypeRegistry.bootstrap();
        ToolStatBuilder.validateStarterMaterialAttributes();
        modContainer.registerConfig(ModConfig.Type.COMMON, MobsToolForgingConfig.COMMON_SPEC);

        modEventBus.addListener(this::addCreativeTabContents);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(FlintKnappingEvents::placeKnappingFlint);
        NeoForge.EVENT_BUS.addListener(FlintKnappingEvents::placeGroundAssembly);
        NeoForge.EVENT_BUS.addListener(FlintKnappingEvents::dropPlantFiber);
        NeoForge.EVENT_BUS.addListener(EveryCompatDropFallback::addMissingSelfDrop);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, LeatherStationAssemblyEvents::assembleInWorld);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, LapidaryTableAssemblyEvents::assembleInWorld);
        NeoForge.EVENT_BUS.addListener(AnvilForgingEvents::forgeAnvilInWorld);
        NeoForge.EVENT_BUS.addListener(ArmorStandSwapEvents::swapPlayerArmorWithStand);
        NeoForge.EVENT_BUS.addListener(this::lowerCopperHarvestTier);
        NeoForge.EVENT_BUS.addListener(CampfireWorkpieceHeating::placeWorkpiece);
        NeoForge.EVENT_BUS.addListener(ToolPartPolishing::polishOnGrindstone);
        NeoForge.EVENT_BUS.addListener(PatternRackSelection::handleRackRightClick);
        NeoForge.EVENT_BUS.addListener(PatternRackSelection::handleRackLeftClick);
        NeoForge.EVENT_BUS.addListener(PatternRackSelection::playerLoggedOut);
        NeoForge.EVENT_BUS.addListener(PatternRackSelection::playerChangedDimension);
        NeoForge.EVENT_BUS.addListener(this::quenchInWaterCauldron);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::blacklistStationsFromCarryOn);
        NeoForge.EVENT_BUS.addListener(this::coolPlayerWorkpieces);
        NeoForge.EVENT_BUS.addListener(this::coolDroppedWorkpieces);
        NeoForge.EVENT_BUS.addListener(this::blockHeatedCrafting);
        NeoForge.EVENT_BUS.addListener(this::blockVanillaModularEquipmentAnvilUse);
        NeoForge.EVENT_BUS.addListener(this::addExternalModularToolTooltip);
        NeoForge.EVENT_BUS.addListener(this::addHeatTooltip);
        NeoForge.EVENT_BUS.addListener(this::addCrucibleTooltip);
        NeoForge.EVENT_BUS.addListener(this::convertHostileMobEquipment);
        NeoForge.EVENT_BUS.addListener(this::boostGildedBlockExperience);
        NeoForge.EVENT_BUS.addListener(this::boostGildedMobExperience);
        NeoForge.EVENT_BUS.addListener(ModCommands::register);

        if (FMLEnvironment.dist.isClient()) {
            MobsToolForgingClient.register(modEventBus);
        }
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.CRUDE_ANVIL);
            event.accept(ModBlocks.TOOL_FORGE);
            event.accept(ModBlocks.LAPIDARY_TABLE);
            event.accept(ModBlocks.PATTERN_CREATION_STATION);
            ModBlocks.PATTERN_RACK_VARIANTS.forEach(variant -> event.accept(variant.block()));
            ModBlocks.TOOLMAKER_STATION_VARIANTS.forEach(variant -> event.accept(variant.block()));
            ModBlocks.LEATHER_STATION_VARIANTS.forEach(variant -> event.accept(variant.block()));
            ModBlocks.DRYING_RACK_VARIANTS.forEach(variant -> event.accept(variant.block()));
            event.accept(ModBlocks.HEATING_FORGE);
            if (MobsToolForgingConfig.ENABLE_CRUCIBLE.get()) {
                event.accept(ModBlocks.CRUCIBLE);
                event.accept(ModBlocks.FOUNDRY_FORGE);
            }
            event.accept(ModBlocks.ASH);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SMITHING_HAMMER);
            event.accept(ModItems.IRON_SMITHING_HAMMER);
            event.accept(ModItems.GEM_CUTTERS_KNIFE);
            event.accept(ModItems.FIRE_STICK);
            if (MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
                flintToolKinds().forEach(toolKind -> event.accept(FlintToolStacks.create(toolKind)));
            }
            event.accept(ModItems.PICKAXE_HEAD_PATTERN);
            event.accept(ModItems.AXE_HEAD_PATTERN);
            event.accept(ModItems.SHOVEL_HEAD_PATTERN);
            event.accept(ModItems.HOE_HEAD_PATTERN);
            event.accept(ModItems.SWORD_BLADE_PATTERN);
            event.accept(ModItems.SWORD_GUARD_PATTERN);
            event.accept(ModItems.SMITHING_HAMMER_HEAD_PATTERN);
            event.accept(ModItems.GEM_CUTTERS_BLADE_PATTERN);
            event.accept(ModItems.HELMET_CHAINMAIL_PATTERN);
            event.accept(ModItems.HELMET_PLATE_PATTERN);
            event.accept(ModItems.CHESTPLATE_CHAINMAIL_PATTERN);
            event.accept(ModItems.CHESTPLATE_BODY_PATTERN);
            event.accept(ModItems.LEGGINGS_CHAINMAIL_PATTERN);
            event.accept(ModItems.LEGGINGS_PLATE_PATTERN);
            event.accept(ModItems.BOOTS_CHAINMAIL_PATTERN);
            event.accept(ModItems.BOOTS_PLATE_PATTERN);
            ToolTypeRegistry.templates().stream()
                    .filter(template -> !template.id().getNamespace().equals(MOD_ID))
                    .map(MobsToolForging::templatePattern)
                    .forEach(event::accept);
            event.accept(ModItems.TEMPLATE_PATTERN);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.MODULAR_HELMET.get().create(MaterialCatalog.LEATHER, Optional.empty()));
            event.accept(ModItems.MODULAR_CHESTPLATE.get().createBase(MaterialCatalog.LEATHER));
            event.accept(ModItems.MODULAR_LEGGINGS.get().create(MaterialCatalog.LEATHER, Optional.empty()));
            event.accept(ModItems.MODULAR_BOOTS.get().create(MaterialCatalog.LEATHER, Optional.empty()));
            event.accept(ModItems.MODULAR_HELMET.get().createChainmail());
            event.accept(ModItems.MODULAR_CHESTPLATE.get().createChainmail());
            event.accept(ModItems.MODULAR_LEGGINGS.get().createChainmail());
            event.accept(ModItems.MODULAR_BOOTS.get().createChainmail());
            event.accept(ModItems.MODULAR_HELMET.get().create(MaterialCatalog.IRON));
            event.accept(ModItems.MODULAR_CHESTPLATE.get().create(MaterialCatalog.IRON));
            event.accept(ModItems.MODULAR_LEGGINGS.get().create(MaterialCatalog.IRON));
            event.accept(ModItems.MODULAR_BOOTS.get().create(MaterialCatalog.IRON));
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.PATTERN_BOARD);
            event.accept(ModItems.FLINT_SHARD);
            event.accept(ModItems.PLANT_FIBER);
            event.accept(ModItems.ASH);
            event.accept(ModItems.SMITHING_HAMMER_HEAD);
            event.accept(ModItems.GEM_CUTTERS_BLADE);
            if (ModList.get().isLoaded("farmersdelight")) {
                event.accept(ModItems.COOKING_KNIFE_HEAD.get().createPart(MaterialCatalog.IRON));
            }
            event.accept(ModItems.DIAMOND_POWDER);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.HEATING_FORGE.get(),
                (forge, side) -> forge.itemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DRYING_RACK.get(),
                (rack, side) -> rack.itemHandler(side)
        );
    }

    private static ItemStack templatePattern(ForgeTemplateDefinition template) {
        ItemStack stack = new ItemStack(ModItems.TEMPLATE_PATTERN.get());
        stack.set(ModDataComponents.FORGE_TEMPLATE.get(), template.id());
        return stack;
    }

    private void lowerCopperHarvestTier(PlayerEvent.HarvestCheck event) {
        if (!MobsToolForgingConfig.COPPER_REQUIRES_WOODEN_TOOL.get() || event.canHarvest()) {
            return;
        }
        if (isCopperProgressionBlock(event.getTargetBlock()) && event.getEntity().getMainHandItem().is(ItemTags.PICKAXES)) {
            event.setCanHarvest(true);
        }
    }

    private void boostGildedBlockExperience(BlockDropsEvent event) {
        int experience = event.getDroppedExperience();
        if (experience <= 0 || !(event.getBreaker() instanceof Player) || !hasTrait(event.getTool(), ToolTrait.GILDED) || isWeaponLike(event.getTool())) {
            return;
        }
        event.setDroppedExperience(boostExperience(experience));
    }

    private void boostGildedMobExperience(LivingExperienceDropEvent event) {
        int experience = event.getDroppedExperience();
        Player player = event.getAttackingPlayer();
        if (experience <= 0 || player == null) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        if (!hasTrait(weapon, ToolTrait.GILDED) || !isWeaponLike(weapon)) {
            return;
        }
        event.setDroppedExperience(boostExperience(experience));
    }

    private void convertHostileMobEquipment(EntityJoinLevelEvent event) {
        if (!MobsToolForgingConfig.CONVERT_VANILLA_LOOT_TO_MODULAR_TOOLS.get()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !(mob instanceof Enemy)) {
            return;
        }
        ResourceLocation handleMaterial = event.getLevel().dimension() == Level.NETHER
                ? MaterialCatalog.BLAZE
                : MaterialCatalog.OAK;
        convertMobEquipment(mob, EquipmentSlot.MAINHAND, handleMaterial);
        convertMobEquipment(mob, EquipmentSlot.OFFHAND, handleMaterial);
        convertMobEquipment(mob, EquipmentSlot.HEAD, handleMaterial);
        convertMobEquipment(mob, EquipmentSlot.CHEST, handleMaterial);
        convertMobEquipment(mob, EquipmentSlot.LEGS, handleMaterial);
        convertMobEquipment(mob, EquipmentSlot.FEET, handleMaterial);
    }

    private static void convertMobEquipment(Mob mob, EquipmentSlot slot, ResourceLocation handleMaterial) {
        ItemStack converted = VanillaToolConverter.convertLootOrEquipment(mob.getItemBySlot(slot), handleMaterial);
        if (!converted.isEmpty()) {
            mob.setItemSlot(slot, converted);
        }
    }

    public static <T> Map<ResourceLocation, T> filterDisabledProgressionRecipes(Map<ResourceLocation, T> recipes) {
        Set<ResourceLocation> disabledRecipes = disabledProgressionRecipeIds();
        if (disabledRecipes.isEmpty()) {
            return recipes;
        }

        Map<ResourceLocation, T> filteredRecipes = null;
        int removed = 0;
        for (ResourceLocation recipeId : disabledRecipes) {
            if (recipes.containsKey(recipeId)) {
                if (filteredRecipes == null) {
                    filteredRecipes = new LinkedHashMap<>(recipes);
                }
                filteredRecipes.remove(recipeId);
                removed++;
            }
        }
        if (removed == 0) {
            return recipes;
        }
        LOGGER.info("Filtered {} recipe(s) for MTF progression during recipe reload.", removed);
        return filteredRecipes;
    }

    private static Set<ResourceLocation> disabledProgressionRecipeIds() {
        Set<ResourceLocation> disabledRecipes = new HashSet<>();
        disabledRecipes.add(ResourceLocation.withDefaultNamespace("repair_item"));
        if (!MobsToolForgingConfig.ENABLE_VANILLA_TOOL_RECIPES.get()) {
            disabledRecipes.addAll(vanillaToolRecipes());
        } else {
            if (MobsToolForgingConfig.DISABLE_STONE_TOOLS.get()) {
                disabledRecipes.addAll(vanillaMaterialToolRecipes("stone"));
            }
            if (MobsToolForgingConfig.DISABLE_WOODEN_TOOLS.get()) {
                disabledRecipes.addAll(vanillaMaterialToolRecipes("wooden"));
            }
        }
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
            disabledRecipes.addAll(flintToolRecipeIds());
        }
        if (!MobsToolForgingConfig.ENABLE_ANVIL_CRAFTING_RECIPES.get()) {
            disabledRecipes.add(modRecipe("tool_forge"));
        }
        if (!MobsToolForgingConfig.ENABLE_ANVIL_CRAFTING_RECIPES.get() || !MobsToolForgingConfig.ENABLE_CRUDE_ANVIL.get()) {
            disabledRecipes.add(modRecipe("crude_anvil"));
        }
        if (!MobsToolForgingConfig.ENABLE_PATTERN_RACK.get()) {
            ModBlocks.PATTERN_RACK_VARIANTS.forEach(variant -> disabledRecipes.add(modRecipe(variant.id())));
        }
        if (!MobsToolForgingConfig.ENABLE_CRUCIBLE.get()) {
            disabledRecipes.add(modRecipe("crucible"));
            disabledRecipes.add(modRecipe("foundry_forge"));
        }
        return disabledRecipes;
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDefinitionReloadListener());
        event.addListener(new ToolTraitReloadListener());
        event.addListener(new ToolTypeReloadListener());
        event.addListener(new ForgeTemplateReloadListener());
        event.addListener(new ToolStatRuleReloadListener());
        event.addListener(new StationWorkRecipeReloadListener());
        event.addListener(new HeatingRecipeReloadListener());
        event.addListener(new DryingRecipeReloadListener());
        event.addListener(new GroundAssemblyRecipeReloadListener());
    }

    private void blacklistStationsFromCarryOn(TagsUpdatedEvent event) {
        CarryOnCompatibility.blacklistStations();
    }

    private void coolPlayerWorkpieces(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        for (int slot = 0; slot < event.getEntity().getInventory().getContainerSize(); slot++) {
            WorkpieceHeat.clearIfCooled(event.getEntity().getInventory().getItem(slot), event.getEntity().level());
        }
    }

    private void coolDroppedWorkpieces(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        ItemStack stack = itemEntity.getItem();
        if (WorkpieceHeat.hasHeat(stack) && isQuenching(itemEntity)) {
            float temperature = WorkpieceHeat.temperature(stack, itemEntity.level());
            if (WorkpieceHeat.quench(stack)) {
                itemEntity.setItem(stack);
                playQuenchEffects(itemEntity.level(), itemEntity.blockPosition(), temperature);
            }
            return;
        }
        if (CampfireWorkpieceHeating.insertDroppedWorkpiece(itemEntity)) {
            return;
        }
        WorkpieceHeat.clearIfCooled(stack, itemEntity.level());
        itemEntity.setItem(stack);
    }

    private void quenchInWaterCauldron(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!WorkpieceHeat.hasHeat(stack) || !event.getLevel().getBlockState(event.getPos()).is(Blocks.WATER_CAULDRON)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel().isClientSide) {
            return;
        }
        float temperature = WorkpieceHeat.temperature(stack, event.getLevel());
        WorkpieceHeat.quench(stack);
        event.getEntity().setItemInHand(event.getHand(), stack);
        playQuenchEffects(event.getLevel(), event.getPos(), temperature);
    }

    private void blockHeatedCrafting(PlayerEvent.ItemCraftedEvent event) {
        Container inventory = event.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (WorkpieceHeat.hasHeat(inventory.getItem(slot))) {
                event.getCrafting().setCount(0);
                DebugFeedback.actionBar(event.getEntity(), Component.translatable("message.mobstoolforging.heated_parts_cannot_craft"));
                return;
            }
        }
    }

    private void blockVanillaModularEquipmentAnvilUse(AnvilUpdateEvent event) {
        if (ToolRepairing.shouldBlockVanillaAnvilUse(event.getLeft())
                || ArmorRepairing.shouldBlockVanillaAnvilUse(event.getLeft())) {
            event.setCanceled(true);
        }
    }

    private void addHeatTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        float temperature = event.getEntity() == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, event.getEntity().level());
        if (temperature <= 0.0F) {
            return;
        }
        float minimumTemperature = heatTooltipMinimumTemperature(stack);
        boolean forgeReady = event.getEntity() == null
                ? WorkpieceHeat.data(stack).map(data -> data.temperature() >= minimumTemperature).orElse(false)
                : WorkpieceHeat.isForgeReady(stack, event.getEntity().level(), minimumTemperature);
        event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.workpiece_temperature", WorkpieceHeat.displayPercent(temperature)).withStyle(forgeReady ? ChatFormatting.GOLD : ChatFormatting.RED));
        String statusKey = WorkpieceHeat.statusKey(temperature, WorkpieceHeat.isWorkable(stack), minimumTemperature);
        event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.workpiece_status." + statusKey).withStyle(forgeReady ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY));
        event.getToolTip().add(Component.translatable(forgeReady ? "tooltip.mobstoolforging.workpiece_ready" : "tooltip.mobstoolforging.workpiece_not_ready").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static float heatTooltipMinimumTemperature(ItemStack stack) {
        ToolPartData part = stack.get(ModDataComponents.TOOL_PART.get());
        ArmorPartData armorPart = stack.get(ModDataComponents.ARMOR_PART.get());
        Optional<ToolMaterialDefinition> material = part != null
                ? MaterialCatalog.definition(part.materialId()).or(() -> part.coatingBaseMaterial().flatMap(MaterialCatalog::definition))
                : armorPart != null
                ? MaterialCatalog.definition(armorPart.materialId()).or(() -> armorPart.coatingBaseMaterial().flatMap(MaterialCatalog::definition))
                : MaterialCatalog.resolve(stack);
        return material
                .map(definition -> WorkshopHeat.minimumForgeTemperature(definition, null))
                .orElse(MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue());
    }

    private void addExternalModularToolTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction != null && !(stack.getItem() instanceof ModularToolItem)) {
            ToolTypeRegistry.toolType(construction.toolType()).ifPresent(definition -> {
                List<Component> toolDetails = new ArrayList<>();
                if (Boolean.TRUE.equals(stack.get(ModDataComponents.TOOL_BROKEN.get()))) {
                    toolDetails.add(Component.translatable("tooltip.mobstoolforging.broken_tool").withStyle(ChatFormatting.RED));
                }
                toolDetails.addAll(ToolTooltipBuilder.tooltip(stack, definition, event.getFlags()));
                ExternalToolTooltipOrder.insertBeforeEnchantments(
                        stack,
                        event.getToolTip(),
                        toolDetails
                );
            });
        }

        List<ItemStack> affixedParts = CompositeAffixCompatibility.affixedParts(stack);
        if (!affixedParts.isEmpty()) {
            Component partNames = Component.empty();
            for (int index = 0; index < affixedParts.size(); index++) {
                if (index > 0) {
                    partNames = partNames.copy().append(", ");
                }
                partNames = partNames.copy().append(affixedParts.get(index).getHoverName());
            }
            event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.component_affixes", partNames)
                    .withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.component_affixes_rework")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        ToolPartData part = stack.get(ModDataComponents.TOOL_PART.get());
        if (part == null || stack.getItem() instanceof ModularToolPartItem) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.quality")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(part.effectiveQualityLevel().displayName()));
        if (part.finishAffectsQuality()) {
            event.getToolTip().add(Component.translatable(part.isCoated() ? "tooltip.mobstoolforging.core_finish" : "tooltip.mobstoolforging.finish")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(part.finish().displayName()));
        }
        part.coatingBaseMaterial().ifPresent(baseMaterial -> event.getToolTip().add(Component.translatable(
                "tooltip.mobstoolforging.coating_base",
                MaterialCatalog.displayName(baseMaterial)
        ).withStyle(ChatFormatting.DARK_GRAY)));
        part.treatment().ifPresent(treatment -> event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.part_treatment", MaterialCatalog.displayName(treatment)).withStyle(ChatFormatting.DARK_GRAY)));
        int remainingDurability = ToolPartWear.remainingDurabilityPercent(stack);
        if (remainingDurability < 100) {
            event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.part_durability", remainingDurability).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private void addCrucibleTooltip(ItemTooltipEvent event) {
        CrucibleContents contents = event.getItemStack().get(ModDataComponents.CRUCIBLE_CONTENTS.get());
        if (contents == null || contents.isEmpty()) {
            return;
        }
        if (contents.hasItem()) {
            event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.crucible_contains_item", contents.item().getHoverName()).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        contents.moltenMaterial().ifPresent(material -> {
            event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.crucible_contains_molten", MaterialCatalog.displayName(material), contents.moltenAmount()).withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.crucible_heat", Math.round(contents.heat() * 100.0F)).withStyle(contents.isWhiteHot() ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY));
        });
    }

    private static boolean isQuenching(ItemEntity itemEntity) {
        BlockState state = itemEntity.level().getBlockState(itemEntity.blockPosition());
        return itemEntity.isInWater() || state.is(Blocks.WATER_CAULDRON);
    }

    private static boolean hasTrait(ItemStack stack, ToolTrait trait) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction != null) {
            return ToolTypeRegistry.toolType(construction.toolType())
                    .map(definition -> ToolStatBuilder.build(definition, construction).traits().contains(trait.id()))
                    .orElse(false);
        }
        return ToolStatBuilder.profile(stack)
                .map(profile -> profile.traits().contains(trait.id()))
                .orElse(false);
    }

    private static boolean isWeaponLike(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction != null) {
            return ToolTypeRegistry.toolType(construction.toolType())
                    .map(ToolTypeDefinition::swordLike)
                    .orElse(false);
        }
        return stack.getItem() instanceof SwordItem;
    }

    private static int boostExperience(int experience) {
        return experience + Math.max(1, (int) Math.ceil(experience * 0.25D));
    }

    private static boolean isCopperProgressionBlock(BlockState state) {
        return state.is(BlockTags.COPPER_ORES) || state.is(Blocks.COPPER_BLOCK) || state.is(Blocks.RAW_COPPER_BLOCK);
    }

    private static void playQuenchEffects(Level level, BlockPos pos, float temperature) {
        float strength = Math.max(0.2F, Math.min(1.0F, temperature));
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F + strength * 0.35F, 1.25F + strength * 0.25F);
        if (level instanceof ServerLevel serverLevel) {
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.72D;
            double z = pos.getZ() + 0.5D;
            serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 10 + Math.round(strength * 18.0F), 0.20D, 0.16D, 0.20D, 0.035D);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.08D, z, 3 + Math.round(strength * 7.0F), 0.16D, 0.12D, 0.16D, 0.025D);
            serverLevel.sendParticles(ParticleTypes.SPLASH, x, y - 0.18D, z, 6 + Math.round(strength * 10.0F), 0.22D, 0.04D, 0.22D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, x, y - 0.12D, z, 4 + Math.round(strength * 6.0F), 0.16D, 0.04D, 0.16D, 0.035D);
        }
    }

    private static List<ResourceLocation> vanillaToolRecipes() {
        return List.of(
                vanillaMaterialToolRecipes("wooden"),
                vanillaMaterialToolRecipes("stone"),
                vanillaMaterialToolRecipes("iron"),
                vanillaMaterialToolRecipes("golden"),
                vanillaMaterialToolRecipes("diamond"),
                vanillaNetheriteToolRecipes()
        ).stream().flatMap(List::stream).toList();
    }

    private static List<ResourceLocation> vanillaMaterialToolRecipes(String materialPrefix) {
        return List.of(
                ResourceLocation.withDefaultNamespace(materialPrefix + "_sword"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_shovel"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_pickaxe"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_axe"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_hoe")
        );
    }

    private static List<ResourceLocation> vanillaNetheriteToolRecipes() {
        return List.of(
                ResourceLocation.withDefaultNamespace("netherite_sword_smithing"),
                ResourceLocation.withDefaultNamespace("netherite_shovel_smithing"),
                ResourceLocation.withDefaultNamespace("netherite_pickaxe_smithing"),
                ResourceLocation.withDefaultNamespace("netherite_axe_smithing"),
                ResourceLocation.withDefaultNamespace("netherite_hoe_smithing")
        );
    }

    private static List<ToolKind> flintToolKinds() {
        return List.of(ToolKind.SWORD, ToolKind.PICKAXE, ToolKind.AXE, ToolKind.SHOVEL, ToolKind.HOE);
    }

    private static List<ResourceLocation> flintToolRecipeIds() {
        return List.of(
                modRecipe("flint_sword"),
                modRecipe("flint_pickaxe"),
                modRecipe("flint_axe"),
                modRecipe("flint_shovel"),
                modRecipe("flint_hoe")
        );
    }

    private static ResourceLocation modRecipe(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}

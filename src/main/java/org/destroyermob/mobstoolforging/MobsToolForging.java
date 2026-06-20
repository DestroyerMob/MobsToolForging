package org.destroyermob.mobstoolforging;

import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.destroyermob.mobstoolforging.client.MobsToolForgingClient;
import org.destroyermob.mobstoolforging.data.ModDataGenerators;
import org.destroyermob.mobstoolforging.network.ModNetworking;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.world.ForgeTemplateReloadListener;
import org.destroyermob.mobstoolforging.world.MaterialDefinitionReloadListener;
import org.destroyermob.mobstoolforging.world.StationWorkRecipeReloadListener;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolStatRuleReloadListener;
import org.destroyermob.mobstoolforging.world.ToolStatBuilder;
import org.destroyermob.mobstoolforging.world.ToolTrait;
import org.destroyermob.mobstoolforging.world.ToolTraitReloadListener;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.ToolTypeReloadListener;
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
        ModBlockEntities.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModNetworking.register(modEventBus);
        ModDataGenerators.register(modEventBus);
        ToolTypeRegistry.bootstrap();
        modContainer.registerConfig(ModConfig.Type.COMMON, MobsToolForgingConfig.COMMON_SPEC);

        modEventBus.addListener(this::addCreativeTabContents);
        NeoForge.EVENT_BUS.addListener(this::knapFlintShard);
        NeoForge.EVENT_BUS.addListener(this::lowerCopperHarvestTier);
        NeoForge.EVENT_BUS.addListener(this::quenchInWaterCauldron);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::removeDisabledEarlyToolRecipes);
        NeoForge.EVENT_BUS.addListener(this::coolPlayerWorkpieces);
        NeoForge.EVENT_BUS.addListener(this::coolDroppedWorkpieces);
        NeoForge.EVENT_BUS.addListener(this::blockHeatedCrafting);
        NeoForge.EVENT_BUS.addListener(this::addHeatTooltip);
        NeoForge.EVENT_BUS.addListener(this::boostGildedBlockExperience);
        NeoForge.EVENT_BUS.addListener(this::boostGildedMobExperience);

        if (FMLEnvironment.dist.isClient()) {
            MobsToolForgingClient.register(modEventBus);
        }
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.TOOL_FORGE);
            event.accept(ModBlocks.LAPIDARY_TABLE);
            event.accept(ModBlocks.HEATING_FORGE);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SMITHING_HAMMER);
            event.accept(ModItems.IRON_SMITHING_HAMMER);
            if (MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
                event.accept(ModItems.FLINT_KNIFE);
                event.accept(ModItems.FLINT_HATCHET);
                event.accept(ModItems.FLINT_PICK);
            }
            event.accept(ModItems.PICKAXE_HEAD_PATTERN);
            event.accept(ModItems.AXE_HEAD_PATTERN);
            event.accept(ModItems.SHOVEL_HEAD_PATTERN);
            event.accept(ModItems.HOE_HEAD_PATTERN);
            event.accept(ModItems.SWORD_BLADE_PATTERN);
            event.accept(ModItems.SWORD_GUARD_PATTERN);
            event.accept(ModItems.SMITHING_HAMMER_HEAD_PATTERN);
            event.accept(ModItems.TEMPLATE_PATTERN);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.FLINT_SHARD);
            event.accept(ModItems.SMITHING_HAMMER_HEAD);
            event.accept(ModItems.DIAMOND_POWDER);
        }
    }

    private void knapFlintShard(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get() || !held.is(Items.FLINT)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        boolean success = level.random.nextDouble() < MobsToolForgingConfig.FLINT_KNAPPING_SUCCESS_CHANCE.get();
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.35F, 1.25F + level.random.nextFloat() * 0.25F);
        if (!success) {
            return;
        }

        held.shrink(1);
        int shardCount = 1;
        if (level.random.nextDouble() < MobsToolForgingConfig.FLINT_KNAPPING_BONUS_SHARD_CHANCE.get()) {
            shardCount++;
        }
        ItemStack shards = new ItemStack(ModItems.FLINT_SHARD.get(), shardCount);
        if (!event.getEntity().getInventory().add(shards)) {
            event.getEntity().drop(shards, false);
        }
        level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 0.35F, 1.15F + level.random.nextFloat() * 0.2F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM, new ItemStack(Items.FLINT)),
                    event.getHitVec().getLocation().x,
                    event.getHitVec().getLocation().y,
                    event.getHitVec().getLocation().z,
                    8,
                    0.08,
                    0.08,
                    0.08,
                    0.02
            );
        }
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

    private void removeDisabledEarlyToolRecipes(ServerStartedEvent event) {
        Set<ResourceLocation> disabledRecipes = new HashSet<>();
        if (MobsToolForgingConfig.DISABLE_STONE_TOOLS.get()) {
            disabledRecipes.addAll(vanillaToolRecipes("stone"));
        }
        if (MobsToolForgingConfig.DISABLE_WOODEN_TOOLS.get()) {
            disabledRecipes.addAll(vanillaToolRecipes("wooden"));
        }
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
            disabledRecipes.addAll(List.of(
                    modRecipe("flint_knife"),
                    modRecipe("flint_hatchet"),
                    modRecipe("flint_pick")
            ));
        }
        if (disabledRecipes.isEmpty()) {
            return;
        }
        List<RecipeHolder<?>> keptRecipes = event.getServer().getRecipeManager().getRecipes().stream()
                .filter(recipe -> !disabledRecipes.contains(recipe.id()))
                .toList();
        int removed = event.getServer().getRecipeManager().getRecipes().size() - keptRecipes.size();
        if (removed > 0) {
            event.getServer().getRecipeManager().replaceRecipes(keptRecipes);
            LOGGER.info("Removed {} early-game tool recipe(s) from config.", removed);
        }
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDefinitionReloadListener());
        event.addListener(new ToolTraitReloadListener());
        event.addListener(new ToolTypeReloadListener());
        event.addListener(new ForgeTemplateReloadListener());
        event.addListener(new ToolStatRuleReloadListener());
        event.addListener(new StationWorkRecipeReloadListener());
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
            if (WorkpieceHeat.quench(stack)) {
                itemEntity.setItem(stack);
                playQuenchEffects(itemEntity.level(), itemEntity.blockPosition());
            }
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
        WorkpieceHeat.quench(stack);
        event.getEntity().setItemInHand(event.getHand(), stack);
        playQuenchEffects(event.getLevel(), event.getPos());
    }

    private void blockHeatedCrafting(PlayerEvent.ItemCraftedEvent event) {
        Container inventory = event.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (WorkpieceHeat.hasHeat(inventory.getItem(slot))) {
                event.getCrafting().setCount(0);
                event.getEntity().displayClientMessage(Component.translatable("message.mobstoolforging.heated_parts_cannot_craft"), true);
                return;
            }
        }
    }

    private void addHeatTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        float temperature = event.getEntity() == null ? WorkpieceHeat.storedTemperature(stack) : WorkpieceHeat.temperature(stack, event.getEntity().level());
        if (temperature <= 0.0F) {
            return;
        }
        boolean forgeReady = event.getEntity() == null
                ? WorkpieceHeat.data(stack).map(data -> data.temperature() >= MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get()).orElse(false)
                : WorkpieceHeat.isForgeReady(stack, event.getEntity().level(), MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue());
        event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.workpiece_temperature", Math.round(temperature * 100.0F)).withStyle(forgeReady ? ChatFormatting.GOLD : ChatFormatting.RED));
        String statusKey = event.getEntity() == null
                ? WorkpieceHeat.statusKey(temperature, WorkpieceHeat.isWorkable(stack), MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue())
                : WorkpieceHeat.statusKey(stack, event.getEntity().level(), MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue());
        event.getToolTip().add(Component.translatable("tooltip.mobstoolforging.workpiece_status." + statusKey).withStyle(forgeReady ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY));
        event.getToolTip().add(Component.translatable(forgeReady ? "tooltip.mobstoolforging.workpiece_ready" : "tooltip.mobstoolforging.workpiece_not_ready").withStyle(ChatFormatting.DARK_GRAY));
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

    private static void playQuenchEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 1.45F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 12, 0.18D, 0.12D, 0.18D, 0.02D);
        }
    }

    private static List<ResourceLocation> vanillaToolRecipes(String materialPrefix) {
        return List.of(
                ResourceLocation.withDefaultNamespace(materialPrefix + "_sword"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_shovel"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_pickaxe"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_axe"),
                ResourceLocation.withDefaultNamespace(materialPrefix + "_hoe")
        );
    }

    private static ResourceLocation modRecipe(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}

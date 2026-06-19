package org.destroyermob.mobstoolforging;

import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.destroyermob.mobstoolforging.client.MobsToolForgingClient;
import org.destroyermob.mobstoolforging.data.ModDataGenerators;
import org.destroyermob.mobstoolforging.network.ModNetworking;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
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
        modContainer.registerConfig(ModConfig.Type.COMMON, MobsToolForgingConfig.COMMON_SPEC);

        modEventBus.addListener(this::addCreativeTabContents);
        NeoForge.EVENT_BUS.addListener(this::knapFlintShard);
        NeoForge.EVENT_BUS.addListener(this::removeDisabledEarlyToolRecipes);

        if (FMLEnvironment.dist.isClient()) {
            MobsToolForgingClient.register(modEventBus);
        }
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.TOOL_FORGE);
            event.accept(ModBlocks.LAPIDARY_TABLE);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SMITHING_HAMMER);
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
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.FLINT_SHARD);
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

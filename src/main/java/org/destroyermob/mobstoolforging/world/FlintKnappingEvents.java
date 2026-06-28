package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class FlintKnappingEvents {
    private static final float PLANT_FIBER_DROP_CHANCE = 0.5F;

    private FlintKnappingEvents() {
    }

    public static void placeKnappingFlint(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()
                || !event.getEntity().isShiftKeyDown()
                || !held.is(Items.FLINT)
                || event.getFace() != Direction.UP) {
            return;
        }
        Level level = event.getLevel();
        BlockPos supportPos = event.getPos();
        BlockPos placePos = supportPos.above();
        if (!canPlaceGroundWork(level, supportPos, placePos)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }
        level.setBlock(placePos, ModBlocks.KNAPPING_FLINT.get().defaultBlockState().setValue(KnappingFlintBlock.FACING, event.getEntity().getDirection()), Block.UPDATE_ALL);
        if (!event.getEntity().getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, placePos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 0.95F + level.random.nextFloat() * 0.1F);
    }

    public static void placeGroundAssembly(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()
                || !event.getEntity().isShiftKeyDown()
                || !StarterFlintAssembly.isStarterPrimaryPart(held)
                || event.getFace() != Direction.UP) {
            return;
        }
        Level level = event.getLevel();
        BlockPos supportPos = event.getPos();
        BlockPos placePos = supportPos.above();
        if (!canPlaceGroundWork(level, supportPos, placePos)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }
        ItemStack placed = held.copyWithCount(1);
        level.setBlock(placePos, ModBlocks.GROUND_TOOL_ASSEMBLY.get().defaultBlockState().setValue(GroundToolAssemblyBlock.FACING, event.getEntity().getDirection()), Block.UPDATE_ALL);
        if (level.getBlockEntity(placePos) instanceof GroundToolAssemblyBlockEntity assembly && assembly.seed(placed)) {
            if (!event.getEntity().getAbilities().instabuild) {
                held.shrink(1);
            }
            level.playSound(null, placePos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 1.0F + level.random.nextFloat() * 0.1F);
        } else {
            level.removeBlock(placePos, false);
        }
    }

    public static void dropPlantFiber(BlockDropsEvent event) {
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get() || event.getBreaker() == null || !isFiberPlant(event.getState())) {
            return;
        }
        if (event.getLevel().random.nextFloat() > PLANT_FIBER_DROP_CHANCE) {
            return;
        }
        BlockPos pos = event.getPos();
        event.getDrops().add(new ItemEntity(
                event.getLevel(),
                pos.getX() + 0.5,
                pos.getY() + 0.25,
                pos.getZ() + 0.5,
                new ItemStack(ModItems.PLANT_FIBER.get())
        ));
    }

    private static boolean canPlaceGroundWork(Level level, BlockPos supportPos, BlockPos placePos) {
        return level.isEmptyBlock(placePos) && level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
    }

    private static boolean isFiberPlant(BlockState state) {
        return state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.LARGE_FERN);
    }
}

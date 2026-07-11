package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.destroyermob.mobstoolforging.integration.everycomp.CompatWorkstationRegistry;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public final class LeatherStationAssemblyEvents {
    private LeatherStationAssemblyEvents() {
    }

    public static void assembleInWorld(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!player.isShiftKeyDown() || !SmithingHammerLevel.isHammer(stack)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos footPos = event.getPos();
        Direction facing = player.getDirection().getOpposite();
        Direction otherDirection = LeatherStationBlock.otherHalfDirection(facing);
        LeatherStationAssembly variant = matchingVariant(level, footPos, otherDirection).orElse(null);
        if (variant == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        BlockPos headPos = footPos.relative(otherDirection);
        BlockState footState = variant.block().defaultBlockState()
                .setValue(LeatherStationBlock.FACING, facing)
                .setValue(LeatherStationBlock.PART, BedPart.FOOT);
        BlockState headState = footState.setValue(LeatherStationBlock.PART, BedPart.HEAD);

        level.setBlock(footPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(headPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(footPos, footState, Block.UPDATE_ALL);
        level.setBlock(headPos, headState, Block.UPDATE_ALL);
        level.playSound(null, footPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.75F, 1.05F);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, slotFor(event.getHand()));
        }
    }

    private static Optional<LeatherStationAssembly> matchingVariant(Level level, BlockPos footPos, Direction otherDirection) {
        BlockPos headPos = footPos.relative(otherDirection);
        BlockState footPlanks = level.getBlockState(footPos);
        BlockState headPlanks = level.getBlockState(headPos);
        BlockState footLog = level.getBlockState(footPos.above());
        BlockState headLog = level.getBlockState(headPos.above());
        Optional<LeatherStationAssembly> builtIn = ModBlocks.LEATHER_STATION_VARIANTS.stream()
                .filter(variant -> footPlanks.is(variant.recipePlanks())
                        && headPlanks.is(variant.recipePlanks())
                        && footLog.is(variant.recipeLog())
                        && headLog.is(variant.recipeLog()))
                .map(variant -> new LeatherStationAssembly(variant.block().get()))
                .findFirst();
        return builtIn.or(() -> CompatWorkstationRegistry.leatherStations().stream()
                .filter(variant -> footPlanks.is(variant.planks())
                        && headPlanks.is(variant.planks())
                        && footLog.is(variant.log())
                        && headLog.is(variant.log()))
                .map(variant -> new LeatherStationAssembly(variant.station()))
                .findFirst());
    }

    private record LeatherStationAssembly(LeatherStationBlock block) {
    }

    private static EquipmentSlot slotFor(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }
}

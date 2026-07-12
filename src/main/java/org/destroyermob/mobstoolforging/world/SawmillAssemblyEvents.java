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

/** Builds the two-block sawmill from the four placed construction blocks. */
public final class SawmillAssemblyEvents {
    private SawmillAssemblyEvents() {
    }

    public static void assembleInWorld(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack hammer = event.getItemStack();
        if (!player.isShiftKeyDown() || !SmithingHammerLevel.isHammer(hammer)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos bottomRight = event.getPos();
        Direction facing = player.getDirection().getOpposite();
        BlockPos bottomLeft = bottomRight.relative(facing.getClockWise());
        BlockPos topRight = bottomRight.above();
        BlockPos topLeft = bottomLeft.above();
        Optional<SawmillAssembly> assembly = findAssembly(level, bottomRight, bottomLeft, topRight, topLeft);
        if (assembly.isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        level.setBlock(topLeft, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(topRight, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockState footState = assembly.get().block().defaultBlockState()
                .setValue(ToolWorkstationBlock.FACING, facing)
                .setValue(LapidaryTableBlock.PART, BedPart.FOOT);
        level.setBlock(bottomRight, footState, Block.UPDATE_ALL);
        level.setBlock(bottomLeft, footState.setValue(LapidaryTableBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);
        level.playSound(null, bottomRight, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.95F);
        if (!player.getAbilities().instabuild) {
            hammer.hurtAndBreak(1, player, slotFor(event.getHand()));
        }
    }

    private static Optional<SawmillAssembly> findAssembly(Level level, BlockPos bottomRight, BlockPos bottomLeft, BlockPos topRight, BlockPos topLeft) {
        if (!level.getBlockState(topLeft).is(Blocks.STONECUTTER)) {
            return Optional.empty();
        }
        BlockState rightPlanks = level.getBlockState(bottomRight);
        BlockState leftPlanks = level.getBlockState(bottomLeft);
        BlockState log = level.getBlockState(topRight);
        Optional<SawmillAssembly> builtIn = ModBlocks.SAWMILL_VARIANTS.stream()
                .filter(variant -> rightPlanks.is(variant.recipePlanks())
                        && leftPlanks.is(variant.recipePlanks())
                        && log.is(variant.recipeLog()))
                .map(variant -> new SawmillAssembly(variant.block().get()))
                .findFirst();
        if (builtIn.isPresent()) {
            return builtIn;
        }
        return CompatWorkstationRegistry.sawmills().stream()
                .filter(variant -> rightPlanks.is(variant.planks())
                        && leftPlanks.is(variant.planks())
                        && log.is(variant.log()))
                .map(variant -> new SawmillAssembly(variant.sawmill()))
                .findFirst();
    }

    private static EquipmentSlot slotFor(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }

    private record SawmillAssembly(SawmillBlock block) {
    }
}

package org.destroyermob.mobstoolforging.world;

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
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public final class LapidaryTableAssemblyEvents {
    private LapidaryTableAssemblyEvents() {
    }

    public static void assembleInWorld(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!player.isShiftKeyDown() || !SmithingHammerLevel.isHammer(stack)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos bottomRight = event.getPos();
        Direction facing = player.getDirection().getOpposite();
        BlockPos bottomLeft = bottomRight.relative(facing.getClockWise());
        BlockPos topRight = bottomRight.above();
        BlockPos topLeft = bottomLeft.above();
        if (!matches(level, bottomRight, bottomLeft, topRight, topLeft)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        level.setBlock(topLeft, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(topRight, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockState footState = ModBlocks.LAPIDARY_TABLE.get().defaultBlockState()
                .setValue(ToolWorkstationBlock.FACING, facing)
                .setValue(LapidaryTableBlock.PART, BedPart.FOOT);
        BlockState headState = footState.setValue(LapidaryTableBlock.PART, BedPart.HEAD);
        level.setBlock(bottomRight, footState, Block.UPDATE_ALL);
        level.setBlock(bottomLeft, headState, Block.UPDATE_ALL);
        level.playSound(null, bottomRight, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.75F, 1.15F);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, slotFor(event.getHand()));
        }
    }

    private static boolean matches(Level level, BlockPos bottomRight, BlockPos bottomLeft, BlockPos topRight, BlockPos topLeft) {
        return level.getBlockState(bottomRight).is(Blocks.LAPIS_BLOCK)
                && level.getBlockState(bottomLeft).is(Blocks.LAPIS_BLOCK)
                && level.getBlockState(topRight).is(Blocks.SMOOTH_STONE)
                && level.getBlockState(topLeft).is(ModBlocks.DIAMOND_SAW.get());
    }

    private static EquipmentSlot slotFor(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }
}

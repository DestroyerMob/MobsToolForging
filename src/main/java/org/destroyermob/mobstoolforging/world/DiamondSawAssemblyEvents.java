package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public final class DiamondSawAssemblyEvents {
    private DiamondSawAssemblyEvents() {
    }

    public static void assembleInWorld(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack abrasive = event.getItemStack();
        BlockState stonecutter = event.getLevel().getBlockState(event.getPos());
        if (!player.isShiftKeyDown()
                || !stonecutter.is(Blocks.STONECUTTER)
                || !LapidaryAbrasives.satisfiesTier(abrasive, LapidaryAbrasives.DIAMOND_TIER)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = event.getPos();
        Direction facing = stonecutter.getValue(StonecutterBlock.FACING);
        BlockState diamondSaw = ModBlocks.DIAMOND_SAW.get().defaultBlockState()
                .setValue(StonecutterBlock.FACING, facing);
        level.setBlock(pos, diamondSaw, Block.UPDATE_ALL);
        if (!player.getAbilities().instabuild) {
            abrasive.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 0.85F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.75D,
                    pos.getZ() + 0.5D,
                    12,
                    0.35D,
                    0.1D,
                    0.35D,
                    0.03D
            );
        }
    }
}

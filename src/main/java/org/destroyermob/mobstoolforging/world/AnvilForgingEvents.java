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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public final class AnvilForgingEvents {
    private AnvilForgingEvents() {
    }

    public static void forgeAnvilInWorld(PlayerInteractEvent.RightClickBlock event) {
        if (MobsToolForgingConfig.ENABLE_ANVIL_CRAFTING_RECIPES.get()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!player.isShiftKeyDown() || !SmithingHammerLevel.isHammer(stack)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState replacement = replacementFor(level.getBlockState(pos), player.getDirection());
        if (replacement == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        level.setBlock(pos, replacement, Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.65F, 1.1F);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, slotFor(event.getHand()));
        }
    }

    private static BlockState replacementFor(BlockState state, Direction playerDirection) {
        Direction facing = playerDirection.getOpposite();
        if (MobsToolForgingConfig.ENABLE_CRUDE_ANVIL.get() && isCrudeAnvilBase(state)) {
            return ModBlocks.CRUDE_ANVIL.get().defaultBlockState().setValue(ToolWorkstationBlock.FACING, facing);
        }
        if (state.is(Blocks.IRON_BLOCK)) {
            return ModBlocks.TOOL_FORGE.get().defaultBlockState().setValue(ToolWorkstationBlock.FACING, facing);
        }
        return null;
    }

    private static boolean isCrudeAnvilBase(BlockState state) {
        return state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE);
    }

    private static EquipmentSlot slotFor(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }
}

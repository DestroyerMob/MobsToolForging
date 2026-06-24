package org.destroyermob.mobstoolforging.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class FireStickItem extends Item {
    public FireStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !hasFireSticksInBothHands(player)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockState modifiedState = clickedState.getToolModifiedState(context, ItemAbilities.FIRESTARTER_LIGHT, false);
        if (modifiedState != null) {
            playUseSound(level, clickedPos, player);
            if (!level.isClientSide()) {
                level.setBlock(clickedPos, modifiedState, 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, clickedPos);
                consumeFireSticks(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPos firePos = clickedPos.relative(context.getClickedFace());
        if (!BaseFireBlock.canBePlacedAt(level, firePos, context.getHorizontalDirection())) {
            return InteractionResult.FAIL;
        }

        playUseSound(level, firePos, player);
        if (!level.isClientSide()) {
            BlockState fireState = BaseFireBlock.getState(level, firePos);
            level.setBlock(firePos, fireState, 11);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, clickedPos);
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, firePos, context.getItemInHand());
            }
            consumeFireSticks(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_FLINT_ACTIONS.contains(itemAbility);
    }

    private static boolean hasFireSticksInBothHands(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.FIRE_STICK.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.FIRE_STICK.get());
    }

    private static void consumeFireSticks(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        player.getItemInHand(InteractionHand.OFF_HAND).shrink(1);
    }

    private static void playUseSound(Level level, BlockPos pos, Player player) {
        level.playSound(
                player,
                pos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F
        );
    }
}

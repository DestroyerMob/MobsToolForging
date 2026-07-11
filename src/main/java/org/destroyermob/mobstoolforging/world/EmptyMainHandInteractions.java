package org.destroyermob.mobstoolforging.world;

import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class EmptyMainHandInteractions {
    private EmptyMainHandInteractions() {
    }

    static boolean shouldFallbackToEmptyHand(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND || player.getMainHandItem().isEmpty();
    }

    static boolean shouldDeferToOffhand(Player player, Predicate<ItemStack> canHandle) {
        ItemStack offhand = player.getOffhandItem();
        return !offhand.isEmpty() && canHandle.test(offhand);
    }

    static ItemInteractionResult itemResult(InteractionResult result, Level level) {
        return result.consumesAction()
                ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}

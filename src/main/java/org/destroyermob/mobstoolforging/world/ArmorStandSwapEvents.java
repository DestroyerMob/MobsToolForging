package org.destroyermob.mobstoolforging.world;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ArmorStandSwapEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private ArmorStandSwapEvents() {
    }

    public static void swapPlayerArmorWithStand(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !event.getEntity().isShiftKeyDown()
                || event.getEntity().isSpectator()
                || !(event.getTarget() instanceof ArmorStand armorStand)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel().isClientSide) {
            return;
        }

        Player player = event.getEntity();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack playerStack = player.getItemBySlot(slot).copy();
            ItemStack standStack = armorStand.getItemBySlot(slot).copy();
            player.setItemSlot(slot, standStack);
            armorStand.setItemSlot(slot, playerStack);
        }
        event.getLevel().playSound(null, armorStand.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 0.65F, 1.0F);
    }
}

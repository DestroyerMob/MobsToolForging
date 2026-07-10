package org.destroyermob.mobstoolforging.mixin.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.CompositeAffixCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reforging creates a new affix directly on its output stack.  A composite has no arbitrary
 * owner for that new affix, so require the player to reforge an individual part before assembly.
 */
@Pseudo
@Mixin(targets = "dev.shadowsoffire.apotheosis.affix.reforging.ReforgingMenu$ReforgingResultSlot", remap = false)
public abstract class ApotheosisReforgingResultSlotMixin {
    private static Field menuField;
    private static Method getSlot;
    private static Method getItem;

    @Inject(
            method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void mobstoolforging$protectComponentOwnedAffixes(
            Player player,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (isCompositeReforge(this)) {
            callback.setReturnValue(false);
        }
    }

    private static boolean isCompositeReforge(Object resultSlot) {
        try {
            if (menuField == null) {
                menuField = resultSlot.getClass().getDeclaredField("this$0");
                menuField.setAccessible(true);
            }
            Object menu = menuField.get(resultSlot);
            if (getSlot == null) {
                getSlot = menu.getClass().getMethod("getSlot", int.class);
            }
            Object inputSlot = getSlot.invoke(menu, 0);
            if (getItem == null) {
                getItem = inputSlot.getClass().getMethod("getItem");
            }
            return CompositeAffixCompatibility.hasComponentAffixes((ItemStack) getItem.invoke(inputSlot));
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return false;
        }
    }
}

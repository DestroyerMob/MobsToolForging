package org.destroyermob.mobstoolforging.mixin.compat;

import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent", remap = false)
public abstract class ApothicStackAttributeModifiersEventMixin {
    @Unique
    private boolean mobstoolforging$brokenArmor;

    @Shadow(remap = false)
    public abstract void clearModifiers();

    @Inject(
            method = "<init>(Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apothic_attributes/modifiers/StackAttributeModifiers;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void mobstoolforging$clearBrokenArmorModifiers(
            ItemStack stack,
            @Coerce Object defaultModifiers,
            CallbackInfo callback
    ) {
        if (Boolean.TRUE.equals(stack.get(ModDataComponents.ARMOR_BROKEN.get()))) {
            mobstoolforging$brokenArmor = true;
            clearModifiers();
        }
    }

    @Inject(method = "build", at = @At("HEAD"), remap = false)
    private void mobstoolforging$clearBrokenArmorModifiersBeforeBuild(CallbackInfoReturnable<?> callback) {
        if (mobstoolforging$brokenArmor) {
            clearModifiers();
        }
    }
}

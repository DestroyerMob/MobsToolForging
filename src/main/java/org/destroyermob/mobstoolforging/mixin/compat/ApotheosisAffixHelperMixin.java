package org.destroyermob.mobstoolforging.mixin.compat;

import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.world.CompositeAffixCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.shadowsoffire.apotheosis.affix.AffixHelper", remap = false)
public abstract class ApotheosisAffixHelperMixin {
    @Inject(
            method = "getAffixes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Map;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mobstoolforging$resolveComponentOwnedAffixes(
            ItemStack stack,
            CallbackInfoReturnable<Map> callback
    ) {
        CompositeAffixCompatibility.affixesFor(stack).ifPresent(callback::setReturnValue);
    }

    @Inject(
            method = "copyToProjectile(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mobstoolforging$copyCompositeAffixesToProjectile(
            ItemStack weapon,
            Entity projectile,
            CallbackInfo callback
    ) {
        if (!CompositeAffixCompatibility.hasComponentAffixes(weapon)) {
            return;
        }
        projectile.getPersistentData().put("apoth.source_weapon", weapon.save(projectile.level().registryAccess()));
        callback.cancel();
    }
}

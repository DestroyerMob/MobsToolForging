package org.destroyermob.mobstoolforging.mixin.compat;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.world.CompositeAffixCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps salvaging from consuming a composite whose affixes are owned by its stored parts. */
@Pseudo
@Mixin(targets = "dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingMenu", remap = false)
public abstract class ApotheosisSalvagingMenuMixin {
    @Inject(
            method = "findMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mobstoolforging$requirePartLevelSalvaging(
            Level level,
            ItemStack stack,
            CallbackInfoReturnable<List> callback
    ) {
        if (CompositeAffixCompatibility.hasComponentAffixes(stack)) {
            callback.setReturnValue(List.of());
        }
    }
}

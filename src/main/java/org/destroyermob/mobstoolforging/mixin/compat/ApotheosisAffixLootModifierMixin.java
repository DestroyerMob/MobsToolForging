package org.destroyermob.mobstoolforging.mixin.compat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.destroyermob.mobstoolforging.loot.ReplaceVanillaToolsLootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier", remap = false)
public abstract class ApotheosisAffixLootModifierMixin {
    @Inject(
            method = "doApply(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;Ldev/shadowsoffire/apotheosis/tiers/GenContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"),
            remap = false
    )
    private void mobstoolforging$convertApotheosisAffixLoot(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context,
            @Coerce Object genContext,
            CallbackInfoReturnable<ObjectArrayList<ItemStack>> callback
    ) {
        ReplaceVanillaToolsLootModifier.convertGeneratedLoot(callback.getReturnValue(), context);
    }
}

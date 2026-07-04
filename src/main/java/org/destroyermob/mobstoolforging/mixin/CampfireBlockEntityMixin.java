package org.destroyermob.mobstoolforging.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.world.CampfireWorkpieceHeating;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Inject(method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V", at = @At("HEAD"))
    private static void mobstoolforging$warmWorkpieces(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo callbackInfo) {
        CampfireWorkpieceHeating.warmCampfireSlots(level, pos, state, campfire);
    }
}

package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class ToolPartPolishing {
    private ToolPartPolishing() {
    }

    public static void polishOnGrindstone(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!level.getBlockState(event.getPos()).is(Blocks.GRINDSTONE)) {
            return;
        }
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data == null || !data.needsPolishing()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (level.isClientSide()) {
            return;
        }

        stack.set(ModDataComponents.TOOL_PART.get(), data.polished());
        level.playSound(null, event.getPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    event.getPos().getX() + 0.5D,
                    event.getPos().getY() + 0.75D,
                    event.getPos().getZ() + 0.5D,
                    8,
                    0.25D,
                    0.1D,
                    0.25D,
                    0.02D
            );
        }
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.part_polished", stack.getHoverName()));
    }
}

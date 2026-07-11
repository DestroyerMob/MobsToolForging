package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class HeatingInteractionEffects {
    private HeatingInteractionEffects() {
    }

    public static void movedHotMetal(Level level, BlockPos pos, float temperature) {
        if (!(level instanceof ServerLevel serverLevel) || temperature < 0.22F) {
            return;
        }
        int sparks = 2 + Math.round(temperature * 5.0F);
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + 0.5D,
                pos.getY() + 1.02D,
                pos.getZ() + 0.5D,
                sparks,
                0.20D,
                0.07D,
                0.20D,
                0.045D
        );
        if (temperature >= 0.62F) {
            serverLevel.sendParticles(
                    ParticleTypes.LAVA,
                    pos.getX() + 0.5D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.5D,
                    1 + Math.round(temperature * 2.0F),
                    0.12D,
                    0.04D,
                    0.12D,
                    0.01D
            );
        }
    }

    public static void hammerStrike(Level level, BlockPos pos, float temperature) {
        if (!(level instanceof ServerLevel serverLevel) || temperature < 0.12F) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.06D;
        double z = pos.getZ() + 0.5D;
        int scale = 3 + Math.round(temperature * 7.0F);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, scale, 0.18D, 0.035D, 0.18D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 2 + Math.round(temperature * 5.0F), 0.20D, 0.025D, 0.20D, 0.025D);
        if (temperature >= 0.58F) {
            serverLevel.sendParticles(ParticleTypes.FLASH, x, y + 0.02D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (temperature >= 0.76F) {
            serverLevel.sendParticles(ParticleTypes.LAVA, x, y, z, 1 + Math.round(temperature), 0.10D, 0.02D, 0.10D, 0.01D);
        }
    }
}

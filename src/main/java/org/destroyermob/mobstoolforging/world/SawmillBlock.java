package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The wooden counterpart to the metal and gem workstations.
 */
public class SawmillBlock extends LapidaryTableBlock {
    public static final MapCodec<SawmillBlock> CODEC = simpleCodec(SawmillBlock::new);

    public SawmillBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.SAWMILL);
    }

    @Override
    protected MapCodec<? extends SawmillBlock> codec() {
        return CODEC;
    }
}

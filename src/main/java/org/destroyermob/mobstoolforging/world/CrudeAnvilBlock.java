package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CrudeAnvilBlock extends ToolForgeBlock {
    public static final MapCodec<CrudeAnvilBlock> CODEC = simpleCodec(CrudeAnvilBlock::new);

    public CrudeAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties, WorkstationKind.CRUDE_ANVIL);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}

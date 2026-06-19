package org.destroyermob.mobstoolforging.registry;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;
import org.destroyermob.mobstoolforging.world.ToolForgeBlock;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MobsToolForging.MOD_ID);

    public static final DeferredBlock<ToolForgeBlock> TOOL_FORGE = BLOCKS.register(
            "tool_forge",
            () -> new ToolForgeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion())
    );
    public static final DeferredBlock<LapidaryTableBlock> LAPIDARY_TABLE = BLOCKS.register(
            "lapidary_table",
            () -> new LapidaryTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER).noOcclusion())
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

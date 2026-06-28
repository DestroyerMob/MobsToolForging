package org.destroyermob.mobstoolforging.registry;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.CrucibleBlock;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlock;
import org.destroyermob.mobstoolforging.world.GroundToolAssemblyBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlock;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;
import org.destroyermob.mobstoolforging.world.PatternCreationStationBlock;
import org.destroyermob.mobstoolforging.world.ToolForgeBlock;
import org.destroyermob.mobstoolforging.world.ToolmakersBenchBlock;

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
    public static final DeferredBlock<PatternCreationStationBlock> PATTERN_CREATION_STATION = BLOCKS.register(
            "pattern_creation_station",
            () -> new PatternCreationStationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER).noOcclusion())
    );
    public static final DeferredBlock<ToolmakersBenchBlock> TOOLMAKERS_BENCH = BLOCKS.register(
            "toolmakers_bench",
            () -> new ToolmakersBenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion())
    );
    public static final DeferredBlock<HeatingForgeBlock> HEATING_FORGE = BLOCKS.register(
            "heating_forge",
            () -> new HeatingForgeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).noOcclusion())
    );
    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.register(
            "crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion())
    );
    public static final DeferredBlock<FoundryForgeBlock> FOUNDRY_FORGE = BLOCKS.register(
            "foundry_forge",
            () -> new FoundryForgeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).noOcclusion())
    );
    public static final DeferredBlock<KnappingFlintBlock> KNAPPING_FLINT = BLOCKS.register(
            "knapping_flint",
            () -> new KnappingFlintBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).strength(0.1F).noCollission().noOcclusion())
    );
    public static final DeferredBlock<GroundToolAssemblyBlock> GROUND_TOOL_ASSEMBLY = BLOCKS.register(
            "ground_tool_assembly",
            () -> new GroundToolAssemblyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON).strength(0.1F).noCollission().noOcclusion())
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

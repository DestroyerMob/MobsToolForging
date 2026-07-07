package org.destroyermob.mobstoolforging.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.AshBlock;
import org.destroyermob.mobstoolforging.world.CrucibleBlock;
import org.destroyermob.mobstoolforging.world.CrudeAnvilBlock;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlock;
import org.destroyermob.mobstoolforging.world.GroundToolAssemblyBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlock;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;
import org.destroyermob.mobstoolforging.world.PatternCreationStationBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.ToolForgeBlock;
import org.destroyermob.mobstoolforging.world.ToolmakersBenchBlock;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MobsToolForging.MOD_ID);

    public static final DeferredBlock<ToolForgeBlock> TOOL_FORGE = BLOCKS.register(
            "tool_forge",
            () -> new ToolForgeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion())
    );
    public static final DeferredBlock<CrudeAnvilBlock> CRUDE_ANVIL = BLOCKS.register(
            "crude_anvil",
            () -> new CrudeAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2.0F, 4.0F).noOcclusion())
    );
    public static final DeferredBlock<LapidaryTableBlock> LAPIDARY_TABLE = BLOCKS.register(
            "lapidary_table",
            () -> new LapidaryTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER).noOcclusion())
    );
    public static final DeferredBlock<PatternCreationStationBlock> PATTERN_CREATION_STATION = BLOCKS.register(
            "pattern_creation_station",
            () -> new PatternCreationStationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER).noOcclusion())
    );
    public static final DeferredBlock<PatternRackBlock> PATTERN_RACK = registerPatternRack("pattern_rack", Blocks.OAK_PLANKS);
    public static final DeferredBlock<PatternRackBlock> SPRUCE_PATTERN_RACK = registerPatternRack("spruce_pattern_rack", Blocks.SPRUCE_PLANKS);
    public static final DeferredBlock<PatternRackBlock> BIRCH_PATTERN_RACK = registerPatternRack("birch_pattern_rack", Blocks.BIRCH_PLANKS);
    public static final DeferredBlock<PatternRackBlock> JUNGLE_PATTERN_RACK = registerPatternRack("jungle_pattern_rack", Blocks.JUNGLE_PLANKS);
    public static final DeferredBlock<PatternRackBlock> ACACIA_PATTERN_RACK = registerPatternRack("acacia_pattern_rack", Blocks.ACACIA_PLANKS);
    public static final DeferredBlock<PatternRackBlock> DARK_OAK_PATTERN_RACK = registerPatternRack("dark_oak_pattern_rack", Blocks.DARK_OAK_PLANKS);
    public static final DeferredBlock<PatternRackBlock> MANGROVE_PATTERN_RACK = registerPatternRack("mangrove_pattern_rack", Blocks.MANGROVE_PLANKS);
    public static final DeferredBlock<PatternRackBlock> CHERRY_PATTERN_RACK = registerPatternRack("cherry_pattern_rack", Blocks.CHERRY_PLANKS);
    public static final DeferredBlock<PatternRackBlock> BAMBOO_PATTERN_RACK = registerPatternRack("bamboo_pattern_rack", Blocks.BAMBOO_PLANKS);
    public static final DeferredBlock<PatternRackBlock> CRIMSON_PATTERN_RACK = registerPatternRack("crimson_pattern_rack", Blocks.CRIMSON_PLANKS);
    public static final DeferredBlock<PatternRackBlock> WARPED_PATTERN_RACK = registerPatternRack("warped_pattern_rack", Blocks.WARPED_PLANKS);
    public static final List<PatternRackVariant> PATTERN_RACK_VARIANTS = List.of(
            patternRackVariant("pattern_rack", "Oak Pattern Rack", PATTERN_RACK, Blocks.OAK_PLANKS, "oak_log", "oak_planks"),
            patternRackVariant("spruce_pattern_rack", "Spruce Pattern Rack", SPRUCE_PATTERN_RACK, Blocks.SPRUCE_PLANKS, "spruce_log", "spruce_planks"),
            patternRackVariant("birch_pattern_rack", "Birch Pattern Rack", BIRCH_PATTERN_RACK, Blocks.BIRCH_PLANKS, "birch_log", "birch_planks"),
            patternRackVariant("jungle_pattern_rack", "Jungle Pattern Rack", JUNGLE_PATTERN_RACK, Blocks.JUNGLE_PLANKS, "jungle_log", "jungle_planks"),
            patternRackVariant("acacia_pattern_rack", "Acacia Pattern Rack", ACACIA_PATTERN_RACK, Blocks.ACACIA_PLANKS, "acacia_log", "acacia_planks"),
            patternRackVariant("dark_oak_pattern_rack", "Dark Oak Pattern Rack", DARK_OAK_PATTERN_RACK, Blocks.DARK_OAK_PLANKS, "dark_oak_log", "dark_oak_planks"),
            patternRackVariant("mangrove_pattern_rack", "Mangrove Pattern Rack", MANGROVE_PATTERN_RACK, Blocks.MANGROVE_PLANKS, "mangrove_log", "mangrove_planks"),
            patternRackVariant("cherry_pattern_rack", "Cherry Pattern Rack", CHERRY_PATTERN_RACK, Blocks.CHERRY_PLANKS, "cherry_log", "cherry_planks"),
            patternRackVariant("bamboo_pattern_rack", "Bamboo Pattern Rack", BAMBOO_PATTERN_RACK, Blocks.BAMBOO_PLANKS, "bamboo_block", "bamboo_planks"),
            patternRackVariant("crimson_pattern_rack", "Crimson Pattern Rack", CRIMSON_PATTERN_RACK, Blocks.CRIMSON_PLANKS, "crimson_stem", "crimson_planks"),
            patternRackVariant("warped_pattern_rack", "Warped Pattern Rack", WARPED_PATTERN_RACK, Blocks.WARPED_PLANKS, "warped_stem", "warped_planks")
    );
    public static final DeferredBlock<ToolmakersBenchBlock> TOOLMAKERS_BENCH = BLOCKS.register(
            "toolmakers_bench",
            () -> new ToolmakersBenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion())
    );
    public static final DeferredBlock<ToolmakersBenchBlock> SPRUCE_TOOLMAKERS_BENCH = registerToolmakersBench("spruce_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> BIRCH_TOOLMAKERS_BENCH = registerToolmakersBench("birch_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> JUNGLE_TOOLMAKERS_BENCH = registerToolmakersBench("jungle_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> ACACIA_TOOLMAKERS_BENCH = registerToolmakersBench("acacia_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> DARK_OAK_TOOLMAKERS_BENCH = registerToolmakersBench("dark_oak_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> MANGROVE_TOOLMAKERS_BENCH = registerToolmakersBench("mangrove_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> CHERRY_TOOLMAKERS_BENCH = registerToolmakersBench("cherry_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> BAMBOO_TOOLMAKERS_BENCH = registerToolmakersBench("bamboo_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> CRIMSON_TOOLMAKERS_BENCH = registerToolmakersBench("crimson_toolmakers_bench");
    public static final DeferredBlock<ToolmakersBenchBlock> WARPED_TOOLMAKERS_BENCH = registerToolmakersBench("warped_toolmakers_bench");
    public static final List<ToolmakerStationVariant> TOOLMAKER_STATION_VARIANTS = List.of(
            toolmakerStationVariant("toolmakers_bench", "Oak Toolmaker's Station", TOOLMAKERS_BENCH, Blocks.OAK_PLANKS, "stripped_oak_log", "stripped_oak_log_top"),
            toolmakerStationVariant("spruce_toolmakers_bench", "Spruce Toolmaker's Station", SPRUCE_TOOLMAKERS_BENCH, Blocks.SPRUCE_PLANKS, "stripped_spruce_log", "stripped_spruce_log_top"),
            toolmakerStationVariant("birch_toolmakers_bench", "Birch Toolmaker's Station", BIRCH_TOOLMAKERS_BENCH, Blocks.BIRCH_PLANKS, "stripped_birch_log", "stripped_birch_log_top"),
            toolmakerStationVariant("jungle_toolmakers_bench", "Jungle Toolmaker's Station", JUNGLE_TOOLMAKERS_BENCH, Blocks.JUNGLE_PLANKS, "stripped_jungle_log", "stripped_jungle_log_top"),
            toolmakerStationVariant("acacia_toolmakers_bench", "Acacia Toolmaker's Station", ACACIA_TOOLMAKERS_BENCH, Blocks.ACACIA_PLANKS, "stripped_acacia_log", "stripped_acacia_log_top"),
            toolmakerStationVariant("dark_oak_toolmakers_bench", "Dark Oak Toolmaker's Station", DARK_OAK_TOOLMAKERS_BENCH, Blocks.DARK_OAK_PLANKS, "stripped_dark_oak_log", "stripped_dark_oak_log_top"),
            toolmakerStationVariant("mangrove_toolmakers_bench", "Mangrove Toolmaker's Station", MANGROVE_TOOLMAKERS_BENCH, Blocks.MANGROVE_PLANKS, "stripped_mangrove_log", "stripped_mangrove_log_top"),
            toolmakerStationVariant("cherry_toolmakers_bench", "Cherry Toolmaker's Station", CHERRY_TOOLMAKERS_BENCH, Blocks.CHERRY_PLANKS, "stripped_cherry_log", "stripped_cherry_log_top"),
            toolmakerStationVariant("bamboo_toolmakers_bench", "Bamboo Toolmaker's Station", BAMBOO_TOOLMAKERS_BENCH, Blocks.BAMBOO_PLANKS, "stripped_bamboo_block", "stripped_bamboo_block_top"),
            toolmakerStationVariant("crimson_toolmakers_bench", "Crimson Toolmaker's Station", CRIMSON_TOOLMAKERS_BENCH, Blocks.CRIMSON_PLANKS, "stripped_crimson_stem", "stripped_crimson_stem_top"),
            toolmakerStationVariant("warped_toolmakers_bench", "Warped Toolmaker's Station", WARPED_TOOLMAKERS_BENCH, Blocks.WARPED_PLANKS, "stripped_warped_stem", "stripped_warped_stem_top")
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
    public static final DeferredBlock<AshBlock> ASH = BLOCKS.register(
            "ash",
            () -> new AshBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE_POWDER).strength(0.1F).sound(SoundType.SAND).noOcclusion())
    );

    private ModBlocks() {
    }

    private static DeferredBlock<ToolmakersBenchBlock> registerToolmakersBench(String id) {
        return BLOCKS.register(
                id,
                () -> new ToolmakersBenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion())
        );
    }

    private static DeferredBlock<PatternRackBlock> registerPatternRack(String id, Block baseBlock) {
        return BLOCKS.register(
                id,
                () -> new PatternRackBlock(BlockBehaviour.Properties.ofFullCopy(baseBlock).strength(1.5F, 3.0F).noOcclusion())
        );
    }

    private static PatternRackVariant patternRackVariant(String id, String displayName, DeferredBlock<PatternRackBlock> block, Block recipePlanks, String logTexture, String planksTexture) {
        return new PatternRackVariant(
                id,
                displayName,
                block,
                recipePlanks,
                ResourceLocation.withDefaultNamespace("block/" + logTexture),
                ResourceLocation.withDefaultNamespace("block/" + planksTexture)
        );
    }

    private static ToolmakerStationVariant toolmakerStationVariant(String id, String displayName, DeferredBlock<ToolmakersBenchBlock> block, Block recipePlanks, String sideTexture, String topTexture) {
        return new ToolmakerStationVariant(
                id,
                displayName,
                block,
                recipePlanks,
                ResourceLocation.withDefaultNamespace("block/" + sideTexture),
                ResourceLocation.withDefaultNamespace("block/" + topTexture)
        );
    }

    public static Block[] patternRackBlocks() {
        return PATTERN_RACK_VARIANTS.stream()
                .map(variant -> variant.block().get())
                .toArray(Block[]::new);
    }

    public static Block[] toolmakersBenchBlocks() {
        return TOOLMAKER_STATION_VARIANTS.stream()
                .map(variant -> variant.block().get())
                .toArray(Block[]::new);
    }

    public static Block[] toolWorkstationBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(TOOL_FORGE.get());
        blocks.add(CRUDE_ANVIL.get());
        blocks.add(LAPIDARY_TABLE.get());
        TOOLMAKER_STATION_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
        return blocks.toArray(Block[]::new);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public record PatternRackVariant(
            String id,
            String displayName,
            DeferredBlock<PatternRackBlock> block,
            Block recipePlanks,
            ResourceLocation logTexture,
            ResourceLocation planksTexture
    ) {
    }

    public record ToolmakerStationVariant(
            String id,
            String displayName,
            DeferredBlock<ToolmakersBenchBlock> block,
            Block recipePlanks,
            ResourceLocation sideTexture,
            ResourceLocation topTexture
    ) {
    }
}

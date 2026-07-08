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
import org.destroyermob.mobstoolforging.world.DryingRackBlock;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlock;
import org.destroyermob.mobstoolforging.world.GroundToolAssemblyBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.KnappingFlintBlock;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;
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
    public static final DeferredBlock<LeatherStationBlock> LEATHER_STATION = registerLeatherStation("leather_station", Blocks.OAK_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> SPRUCE_LEATHER_STATION = registerLeatherStation("spruce_leather_station", Blocks.SPRUCE_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> BIRCH_LEATHER_STATION = registerLeatherStation("birch_leather_station", Blocks.BIRCH_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> JUNGLE_LEATHER_STATION = registerLeatherStation("jungle_leather_station", Blocks.JUNGLE_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> ACACIA_LEATHER_STATION = registerLeatherStation("acacia_leather_station", Blocks.ACACIA_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> DARK_OAK_LEATHER_STATION = registerLeatherStation("dark_oak_leather_station", Blocks.DARK_OAK_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> MANGROVE_LEATHER_STATION = registerLeatherStation("mangrove_leather_station", Blocks.MANGROVE_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> CHERRY_LEATHER_STATION = registerLeatherStation("cherry_leather_station", Blocks.CHERRY_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> BAMBOO_LEATHER_STATION = registerLeatherStation("bamboo_leather_station", Blocks.BAMBOO_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> CRIMSON_LEATHER_STATION = registerLeatherStation("crimson_leather_station", Blocks.CRIMSON_PLANKS);
    public static final DeferredBlock<LeatherStationBlock> WARPED_LEATHER_STATION = registerLeatherStation("warped_leather_station", Blocks.WARPED_PLANKS);
    public static final List<LeatherStationVariant> LEATHER_STATION_VARIANTS = List.of(
            leatherStationVariant("leather_station", "Oak Leather Station", LEATHER_STATION, Blocks.OAK_PLANKS, Blocks.OAK_LOG, "oak_log", "oak_planks"),
            leatherStationVariant("spruce_leather_station", "Spruce Leather Station", SPRUCE_LEATHER_STATION, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, "spruce_log", "spruce_planks"),
            leatherStationVariant("birch_leather_station", "Birch Leather Station", BIRCH_LEATHER_STATION, Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, "birch_log", "birch_planks"),
            leatherStationVariant("jungle_leather_station", "Jungle Leather Station", JUNGLE_LEATHER_STATION, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, "jungle_log", "jungle_planks"),
            leatherStationVariant("acacia_leather_station", "Acacia Leather Station", ACACIA_LEATHER_STATION, Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, "acacia_log", "acacia_planks"),
            leatherStationVariant("dark_oak_leather_station", "Dark Oak Leather Station", DARK_OAK_LEATHER_STATION, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, "dark_oak_log", "dark_oak_planks"),
            leatherStationVariant("mangrove_leather_station", "Mangrove Leather Station", MANGROVE_LEATHER_STATION, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_LOG, "mangrove_log", "mangrove_planks"),
            leatherStationVariant("cherry_leather_station", "Cherry Leather Station", CHERRY_LEATHER_STATION, Blocks.CHERRY_PLANKS, Blocks.CHERRY_LOG, "cherry_log", "cherry_planks"),
            leatherStationVariant("bamboo_leather_station", "Bamboo Leather Station", BAMBOO_LEATHER_STATION, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_BLOCK, "bamboo_block", "bamboo_planks"),
            leatherStationVariant("crimson_leather_station", "Crimson Leather Station", CRIMSON_LEATHER_STATION, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STEM, "crimson_stem", "crimson_planks"),
            leatherStationVariant("warped_leather_station", "Warped Leather Station", WARPED_LEATHER_STATION, Blocks.WARPED_PLANKS, Blocks.WARPED_STEM, "warped_stem", "warped_planks")
    );
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = registerDryingRack("drying_rack", Blocks.OAK_PLANKS);
    public static final DeferredBlock<DryingRackBlock> SPRUCE_DRYING_RACK = registerDryingRack("spruce_drying_rack", Blocks.SPRUCE_PLANKS);
    public static final DeferredBlock<DryingRackBlock> BIRCH_DRYING_RACK = registerDryingRack("birch_drying_rack", Blocks.BIRCH_PLANKS);
    public static final DeferredBlock<DryingRackBlock> JUNGLE_DRYING_RACK = registerDryingRack("jungle_drying_rack", Blocks.JUNGLE_PLANKS);
    public static final DeferredBlock<DryingRackBlock> ACACIA_DRYING_RACK = registerDryingRack("acacia_drying_rack", Blocks.ACACIA_PLANKS);
    public static final DeferredBlock<DryingRackBlock> DARK_OAK_DRYING_RACK = registerDryingRack("dark_oak_drying_rack", Blocks.DARK_OAK_PLANKS);
    public static final DeferredBlock<DryingRackBlock> MANGROVE_DRYING_RACK = registerDryingRack("mangrove_drying_rack", Blocks.MANGROVE_PLANKS);
    public static final DeferredBlock<DryingRackBlock> CHERRY_DRYING_RACK = registerDryingRack("cherry_drying_rack", Blocks.CHERRY_PLANKS);
    public static final DeferredBlock<DryingRackBlock> BAMBOO_DRYING_RACK = registerDryingRack("bamboo_drying_rack", Blocks.BAMBOO_PLANKS);
    public static final DeferredBlock<DryingRackBlock> CRIMSON_DRYING_RACK = registerDryingRack("crimson_drying_rack", Blocks.CRIMSON_PLANKS);
    public static final DeferredBlock<DryingRackBlock> WARPED_DRYING_RACK = registerDryingRack("warped_drying_rack", Blocks.WARPED_PLANKS);
    public static final List<DryingRackVariant> DRYING_RACK_VARIANTS = List.of(
            dryingRackVariant("drying_rack", "Oak Drying Rack", DRYING_RACK, Blocks.OAK_SLAB, "oak_planks"),
            dryingRackVariant("spruce_drying_rack", "Spruce Drying Rack", SPRUCE_DRYING_RACK, Blocks.SPRUCE_SLAB, "spruce_planks"),
            dryingRackVariant("birch_drying_rack", "Birch Drying Rack", BIRCH_DRYING_RACK, Blocks.BIRCH_SLAB, "birch_planks"),
            dryingRackVariant("jungle_drying_rack", "Jungle Drying Rack", JUNGLE_DRYING_RACK, Blocks.JUNGLE_SLAB, "jungle_planks"),
            dryingRackVariant("acacia_drying_rack", "Acacia Drying Rack", ACACIA_DRYING_RACK, Blocks.ACACIA_SLAB, "acacia_planks"),
            dryingRackVariant("dark_oak_drying_rack", "Dark Oak Drying Rack", DARK_OAK_DRYING_RACK, Blocks.DARK_OAK_SLAB, "dark_oak_planks"),
            dryingRackVariant("mangrove_drying_rack", "Mangrove Drying Rack", MANGROVE_DRYING_RACK, Blocks.MANGROVE_SLAB, "mangrove_planks"),
            dryingRackVariant("cherry_drying_rack", "Cherry Drying Rack", CHERRY_DRYING_RACK, Blocks.CHERRY_SLAB, "cherry_planks"),
            dryingRackVariant("bamboo_drying_rack", "Bamboo Drying Rack", BAMBOO_DRYING_RACK, Blocks.BAMBOO_SLAB, "bamboo_planks"),
            dryingRackVariant("crimson_drying_rack", "Crimson Drying Rack", CRIMSON_DRYING_RACK, Blocks.CRIMSON_SLAB, "crimson_planks"),
            dryingRackVariant("warped_drying_rack", "Warped Drying Rack", WARPED_DRYING_RACK, Blocks.WARPED_SLAB, "warped_planks")
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

    private static DeferredBlock<LeatherStationBlock> registerLeatherStation(String id, Block baseBlock) {
        return BLOCKS.register(
                id,
                () -> new LeatherStationBlock(BlockBehaviour.Properties.ofFullCopy(baseBlock).strength(1.5F, 3.0F).noOcclusion())
        );
    }

    private static DeferredBlock<DryingRackBlock> registerDryingRack(String id, Block baseBlock) {
        return BLOCKS.register(
                id,
                () -> new DryingRackBlock(BlockBehaviour.Properties.ofFullCopy(baseBlock).strength(1.5F, 3.0F).noOcclusion())
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

    private static LeatherStationVariant leatherStationVariant(String id, String displayName, DeferredBlock<LeatherStationBlock> block, Block recipePlanks, Block recipeLog, String logTexture, String planksTexture) {
        return new LeatherStationVariant(
                id,
                displayName,
                block,
                recipePlanks,
                recipeLog,
                ResourceLocation.withDefaultNamespace("block/" + logTexture),
                ResourceLocation.withDefaultNamespace("block/" + planksTexture)
        );
    }

    private static DryingRackVariant dryingRackVariant(String id, String displayName, DeferredBlock<DryingRackBlock> block, Block recipeSlab, String planksTexture) {
        return new DryingRackVariant(
                id,
                displayName,
                block,
                recipeSlab,
                ResourceLocation.withDefaultNamespace("block/" + planksTexture)
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

    public static Block[] leatherStationBlocks() {
        return LEATHER_STATION_VARIANTS.stream()
                .map(variant -> variant.block().get())
                .toArray(Block[]::new);
    }

    public static Block[] dryingRackBlocks() {
        return DRYING_RACK_VARIANTS.stream()
                .map(variant -> variant.block().get())
                .toArray(Block[]::new);
    }

    public static Block[] toolWorkstationBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(TOOL_FORGE.get());
        blocks.add(CRUDE_ANVIL.get());
        blocks.add(LAPIDARY_TABLE.get());
        TOOLMAKER_STATION_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
        LEATHER_STATION_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
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

    public record LeatherStationVariant(
            String id,
            String displayName,
            DeferredBlock<LeatherStationBlock> block,
            Block recipePlanks,
            Block recipeLog,
            ResourceLocation logTexture,
            ResourceLocation planksTexture
    ) {
    }

    public record DryingRackVariant(
            String id,
            String displayName,
            DeferredBlock<DryingRackBlock> block,
            Block recipeSlab,
            ResourceLocation planksTexture
    ) {
    }
}

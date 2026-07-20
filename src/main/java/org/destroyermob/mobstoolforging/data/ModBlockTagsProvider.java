package org.destroyermob.mobstoolforging.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModTags;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MobsToolForging.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Blocks.PATTERN_RACKS).add(ModBlocks.patternRackBlocks());
        tag(ModTags.Blocks.TOOLMAKERS_BENCHES).add(ModBlocks.toolmakersBenchBlocks());
        tag(ModTags.Blocks.SAWMILLS).add(ModBlocks.sawmillBlocks());
        tag(ModTags.Blocks.LEATHER_STATIONS).add(ModBlocks.leatherStationBlocks());
        tag(ModTags.Blocks.DRYING_RACKS).add(ModBlocks.dryingRackBlocks());
        tag(ModTags.Blocks.FOUNDRY_STRUCTURE_BLOCKS).add(
                Blocks.BLACKSTONE,
                Blocks.POLISHED_BLACKSTONE,
                Blocks.POLISHED_BLACKSTONE_BRICKS,
                Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                Blocks.CHISELED_POLISHED_BLACKSTONE,
                Blocks.GILDED_BLACKSTONE,
                ModBlocks.FOUNDRY_FUEL_TANK.get(),
                ModBlocks.FOUNDRY_GLASS.get(),
                ModBlocks.FOUNDRY_DRAIN.get()
        );
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.CRUDE_ANVIL.get(), ModBlocks.TOOL_FORGE.get(), ModBlocks.LAPIDARY_TABLE.get(), ModBlocks.DIAMOND_SAW.get(), ModBlocks.PATTERN_CREATION_STATION.get(), ModBlocks.HEATING_FORGE.get(), ModBlocks.LAVA_HEATING_FORGE.get(), ModBlocks.CRUCIBLE.get(), ModBlocks.FOUNDRY_FORGE.get(), ModBlocks.FOUNDRY_FUEL_TANK.get(), ModBlocks.FOUNDRY_GLASS.get(), ModBlocks.FOUNDRY_DRAIN.get(), ModBlocks.FOUNDRY_FAUCET.get(), ModBlocks.FOUNDRY_CASTING_TABLE.get(), ModBlocks.FOUNDRY_CASTING_BASIN.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.sawmillBlocks()).add(ModBlocks.toolmakersBenchBlocks()).add(ModBlocks.patternRackBlocks()).add(ModBlocks.leatherStationBlocks()).add(ModBlocks.dryingRackBlocks());
        tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.TOOL_FORGE.get(), ModBlocks.HEATING_FORGE.get(), ModBlocks.LAVA_HEATING_FORGE.get(), ModBlocks.CRUCIBLE.get(), ModBlocks.FOUNDRY_FORGE.get(), ModBlocks.FOUNDRY_FUEL_TANK.get(), ModBlocks.FOUNDRY_GLASS.get(), ModBlocks.FOUNDRY_DRAIN.get(), ModBlocks.FOUNDRY_FAUCET.get(), ModBlocks.FOUNDRY_CASTING_TABLE.get(), ModBlocks.FOUNDRY_CASTING_BASIN.get());
        tag(ModTags.Blocks.CARRY_ON_BLOCK_BLACKLIST)
                .add(ModBlocks.toolmakersBenchBlocks())
                .add(ModBlocks.sawmillBlocks())
                .add(ModBlocks.patternRackBlocks())
                .add(ModBlocks.leatherStationBlocks())
                .add(ModBlocks.dryingRackBlocks())
                .add(ModBlocks.FOUNDRY_FORGE.get(), ModBlocks.FOUNDRY_FUEL_TANK.get(), ModBlocks.FOUNDRY_GLASS.get(), ModBlocks.FOUNDRY_DRAIN.get(), ModBlocks.FOUNDRY_FAUCET.get(), ModBlocks.FOUNDRY_CASTING_TABLE.get(), ModBlocks.FOUNDRY_CASTING_BASIN.get());
        tag(ModTags.Blocks.RELOCATION_NOT_SUPPORTED)
                .addTag(ModTags.Blocks.CARRY_ON_BLOCK_BLACKLIST);
    }
}

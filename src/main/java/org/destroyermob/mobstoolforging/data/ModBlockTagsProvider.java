package org.destroyermob.mobstoolforging.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MobsToolForging.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.CRUDE_ANVIL.get(), ModBlocks.TOOL_FORGE.get(), ModBlocks.LAPIDARY_TABLE.get(), ModBlocks.PATTERN_CREATION_STATION.get(), ModBlocks.TOOLMAKERS_BENCH.get(), ModBlocks.HEATING_FORGE.get(), ModBlocks.CRUCIBLE.get(), ModBlocks.FOUNDRY_FORGE.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.patternRackBlocks());
        tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.TOOL_FORGE.get(), ModBlocks.HEATING_FORGE.get(), ModBlocks.CRUCIBLE.get(), ModBlocks.FOUNDRY_FORGE.get());
    }
}

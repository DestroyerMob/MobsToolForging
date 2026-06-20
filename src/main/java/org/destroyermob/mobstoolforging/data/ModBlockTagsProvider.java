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
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.TOOL_FORGE.get(), ModBlocks.LAPIDARY_TABLE.get(), ModBlocks.HEATING_FORGE.get());
        tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.TOOL_FORGE.get(), ModBlocks.HEATING_FORGE.get());
        tag(ModTags.Blocks.INCORRECT_FOR_FLINT_TOOL)
                .addTag(BlockTags.NEEDS_IRON_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL)
                .addTag(BlockTags.IRON_ORES)
                .addTag(BlockTags.LAPIS_ORES)
                .add(Blocks.IRON_BLOCK, Blocks.RAW_IRON_BLOCK, Blocks.LAPIS_BLOCK);
    }
}

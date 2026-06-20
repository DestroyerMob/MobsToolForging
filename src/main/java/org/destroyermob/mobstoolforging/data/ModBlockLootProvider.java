package org.destroyermob.mobstoolforging.data;

import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.loot.BlockLootSubProvider;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.TOOL_FORGE.get());
        dropSelf(ModBlocks.LAPIDARY_TABLE.get());
        dropSelf(ModBlocks.HEATING_FORGE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(ModBlocks.TOOL_FORGE.get(), ModBlocks.LAPIDARY_TABLE.get(), ModBlocks.HEATING_FORGE.get());
    }
}

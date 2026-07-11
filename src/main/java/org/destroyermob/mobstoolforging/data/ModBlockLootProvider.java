package org.destroyermob.mobstoolforging.data;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.world.AshBlock;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;

public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.CRUDE_ANVIL.get());
        dropSelf(ModBlocks.TOOL_FORGE.get());
        dropSelf(ModBlocks.LAPIDARY_TABLE.get());
        dropSelf(ModBlocks.PATTERN_CREATION_STATION.get());
        ModBlocks.PATTERN_RACK_VARIANTS.forEach(variant -> dropSelf(variant.block().get()));
        ModBlocks.TOOLMAKER_STATION_VARIANTS.forEach(variant -> dropSelf(variant.block().get()));
        ModBlocks.LEATHER_STATION_VARIANTS.forEach(variant -> add(variant.block().get(), this::createLeatherStationDrops));
        ModBlocks.DRYING_RACK_VARIANTS.forEach(variant -> dropSelf(variant.block().get()));
        dropSelf(ModBlocks.HEATING_FORGE.get());
        dropSelf(ModBlocks.CRUCIBLE.get());
        dropSelf(ModBlocks.FOUNDRY_FORGE.get());
        add(ModBlocks.KNAPPING_FLINT.get(), noDrop());
        add(ModBlocks.GROUND_TOOL_ASSEMBLY.get(), noDrop());
        add(ModBlocks.ASH.get(), this::createAshDrops);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        Set<Block> blocks = new LinkedHashSet<>(Set.of(
                ModBlocks.CRUDE_ANVIL.get(),
                ModBlocks.TOOL_FORGE.get(),
                ModBlocks.LAPIDARY_TABLE.get(),
                ModBlocks.PATTERN_CREATION_STATION.get(),
                ModBlocks.HEATING_FORGE.get(),
                ModBlocks.CRUCIBLE.get(),
                ModBlocks.FOUNDRY_FORGE.get(),
                ModBlocks.KNAPPING_FLINT.get(),
                ModBlocks.GROUND_TOOL_ASSEMBLY.get(),
                ModBlocks.ASH.get()
        ));
        ModBlocks.PATTERN_RACK_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
        ModBlocks.TOOLMAKER_STATION_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
        ModBlocks.LEATHER_STATION_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
        ModBlocks.DRYING_RACK_VARIANTS.forEach(variant -> blocks.add(variant.block().get()));
        return blocks;
    }

    private LootTable.Builder createLeatherStationDrops(Block block) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(applyExplosionDecay(
                                        block,
                                        LootItem.lootTableItem(block)
                                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(LeatherStationBlock.PART, BedPart.FOOT)))
                                ))
                );
    }

    private LootTable.Builder createAshDrops(Block block) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add((LootPoolEntryContainer.Builder<?>) applyExplosionDecay(
                                        block,
                                        LootItem.lootTableItem(block)
                                                .apply(AshBlock.LAYERS.getPossibleValues(), layers -> SetItemCountFunction.setCount(ConstantValue.exactly(layers.floatValue()))
                                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(AshBlock.LAYERS, layers))))
                                ))
                );
    }
}

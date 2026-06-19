package org.destroyermob.mobstoolforging.data;

import java.util.List;
import java.util.Set;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModDataGenerators::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        var output = event.getGenerator().getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        event.createProvider(ModLanguageProvider::new);
        event.addProvider(new ModBlockStateProvider(output, existingFileHelper));
        event.addProvider(new ModVisualDefinitionProvider(output));
        event.createProvider(ModRecipeProvider::new);
        event.addProvider(new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider
        ));
        ModBlockTagsProvider blockTags = event.addProvider(new ModBlockTagsProvider(output, lookupProvider, existingFileHelper));
        event.addProvider(new ModItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
    }
}

package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.PatternCreationStationBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            MobsToolForging.MOD_ID
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ToolForgeBlockEntity>> TOOL_WORKSTATION = BLOCK_ENTITIES.register(
            "tool_workstation",
            () -> BlockEntityType.Builder.of(ToolForgeBlockEntity::new, ModBlocks.TOOL_FORGE.get(), ModBlocks.LAPIDARY_TABLE.get(), ModBlocks.TOOLMAKERS_BENCH.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatingForgeBlockEntity>> HEATING_FORGE = BLOCK_ENTITIES.register(
            "heating_forge",
            () -> BlockEntityType.Builder.of(HeatingForgeBlockEntity::new, ModBlocks.HEATING_FORGE.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PatternCreationStationBlockEntity>> PATTERN_CREATION_STATION = BLOCK_ENTITIES.register(
            "pattern_creation_station",
            () -> BlockEntityType.Builder.of(PatternCreationStationBlockEntity::new, ModBlocks.PATTERN_CREATION_STATION.get()).build(null)
    );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

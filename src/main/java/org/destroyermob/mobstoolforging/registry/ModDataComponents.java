package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            MobsToolForging.MOD_ID
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolPartData>> TOOL_PART = DATA_COMPONENTS.registerComponentType(
            "tool_part",
            builder -> builder.persistent(ToolPartData.CODEC).networkSynchronized(ToolPartData.STREAM_CODEC).cacheEncoding()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolConstructionData>> TOOL_CONSTRUCTION = DATA_COMPONENTS.registerComponentType(
            "tool_construction",
            builder -> builder.persistent(ToolConstructionData.CODEC).networkSynchronized(ToolConstructionData.STREAM_CODEC).cacheEncoding()
    );

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}

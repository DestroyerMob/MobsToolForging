package org.destroyermob.mobstoolforging.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.CrucibleContents;
import org.destroyermob.mobstoolforging.world.HeatedWorkpieceData;
import org.destroyermob.mobstoolforging.world.ToolAssemblyParts;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolStatProfile;

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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolAssemblyParts>> TOOL_ASSEMBLY_PARTS = DATA_COMPONENTS.registerComponentType(
            "tool_assembly_parts",
            builder -> builder.persistent(ToolAssemblyParts.CODEC).networkSynchronized(ToolAssemblyParts.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolStatProfile>> TOOL_STAT_PROFILE = DATA_COMPONENTS.registerComponentType(
            "tool_stat_profile",
            builder -> builder.persistent(ToolStatProfile.CODEC).networkSynchronized(ToolStatProfile.STREAM_CODEC).cacheEncoding()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TOOL_BROKEN = DATA_COMPONENTS.registerComponentType(
            "tool_broken",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HeatedWorkpieceData>> HEATED_WORKPIECE = DATA_COMPONENTS.registerComponentType(
            "heated_workpiece",
            builder -> builder.persistent(HeatedWorkpieceData.CODEC).networkSynchronized(HeatedWorkpieceData.STREAM_CODEC).cacheEncoding()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CrucibleContents>> CRUCIBLE_CONTENTS = DATA_COMPONENTS.registerComponentType(
            "crucible_contents",
            builder -> builder.persistent(CrucibleContents.CODEC).networkSynchronized(CrucibleContents.STREAM_CODEC).cacheEncoding()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> FORGE_TEMPLATE = DATA_COMPONENTS.registerComponentType(
            "forge_template",
            builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding()
    );

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}

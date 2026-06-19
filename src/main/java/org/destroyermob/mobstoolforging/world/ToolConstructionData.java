package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record ToolConstructionData(
        ResourceLocation toolType,
        ResourceLocation headMaterial,
        ResourceLocation handleMaterial,
        Optional<ResourceLocation> bindingMaterial,
        Optional<ResourceLocation> wrapMaterial,
        Optional<ResourceLocation> focusMaterial,
        Optional<ResourceLocation> treatment,
        int quality
) {
    public static final int DEFAULT_QUALITY = 100;

    public static final Codec<ToolConstructionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("tool_type").forGetter(ToolConstructionData::toolType),
            ResourceLocation.CODEC.fieldOf("head_material").forGetter(ToolConstructionData::headMaterial),
            ResourceLocation.CODEC.fieldOf("handle_material").forGetter(ToolConstructionData::handleMaterial),
            ResourceLocation.CODEC.optionalFieldOf("binding_material").forGetter(ToolConstructionData::bindingMaterial),
            ResourceLocation.CODEC.optionalFieldOf("wrap_material").forGetter(ToolConstructionData::wrapMaterial),
            ResourceLocation.CODEC.optionalFieldOf("focus_material").forGetter(ToolConstructionData::focusMaterial),
            ResourceLocation.CODEC.optionalFieldOf("treatment").forGetter(ToolConstructionData::treatment),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ToolConstructionData::quality)
    ).apply(instance, ToolConstructionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolConstructionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static ToolConstructionData basic(ToolKind toolKind, ResourceLocation headMaterial, ResourceLocation handleMaterial) {
        return new ToolConstructionData(
                toolType(toolKind),
                headMaterial,
                handleMaterial,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                DEFAULT_QUALITY
        );
    }

    public static ResourceLocation toolType(ToolKind toolKind) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, toolKind.id());
    }
}

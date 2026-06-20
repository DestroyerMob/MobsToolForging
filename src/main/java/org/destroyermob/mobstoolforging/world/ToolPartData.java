package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ToolPartData(String partType, ResourceLocation materialId, int quality) {
    public static final int DEFAULT_QUALITY = ToolConstructionData.DEFAULT_QUALITY;
    public static final String SWORD_BLADE = "sword_blade";
    public static final String SWORD_GUARD = "sword_guard";
    public static final String SHOVEL_HEAD = "shovel_head";
    public static final String PICKAXE_HEAD = "pickaxe_head";
    public static final String AXE_HEAD = "axe_head";
    public static final String HOE_HEAD = "hoe_head";
    public static final String SMITHING_HAMMER_HEAD = "smithing_hammer_head";

    public static final Codec<ToolPartData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("part_type").forGetter(ToolPartData::partType),
            ResourceLocation.CODEC.fieldOf("material_id").forGetter(ToolPartData::materialId),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ToolPartData::quality)
    ).apply(instance, ToolPartData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolPartData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ToolPartData::partType,
            ResourceLocation.STREAM_CODEC,
            ToolPartData::materialId,
            ByteBufCodecs.INT,
            ToolPartData::quality,
            ToolPartData::new
    );

    public ToolPartData(String partType, ResourceLocation materialId) {
        this(partType, materialId, DEFAULT_QUALITY);
    }

    public ToolPartData {
        quality = Math.max(1, quality);
    }
}

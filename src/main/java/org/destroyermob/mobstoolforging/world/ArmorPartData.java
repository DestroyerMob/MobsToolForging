package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ArmorPartData(String partType, ResourceLocation materialId, int quality) {
    public static final int DEFAULT_QUALITY = ArmorConstructionData.DEFAULT_QUALITY;
    public static final String HELMET_SKULL = "helmet_skull";
    public static final String HELMET_COMB = "helmet_comb";
    public static final String HELMET_VISOR = "helmet_visor";
    public static final String CHESTPLATE_CHAINMAIL = "chestplate_chainmail";
    public static final String CHESTPLATE_BODY = "chestplate_body";
    public static final String LEGGINGS_LEGS = "leggings_legs";
    public static final String LEGGINGS_KNEES = "leggings_knees";
    public static final String LEGGINGS_TASSETS = "leggings_tassets";
    public static final String BOOTS_FEET = "boots_feet";

    public static final Codec<ArmorPartData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("part_type").forGetter(ArmorPartData::partType),
            ResourceLocation.CODEC.fieldOf("material_id").forGetter(ArmorPartData::materialId),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ArmorPartData::quality)
    ).apply(instance, ArmorPartData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorPartData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ArmorPartData(String partType, ResourceLocation materialId) {
        this(partType, materialId, DEFAULT_QUALITY);
    }

    public ArmorPartData {
        quality = DEFAULT_QUALITY;
    }
}

package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ArmorPartData(
        String partType,
        ResourceLocation materialId,
        int quality,
        Optional<ResourceLocation> coatingBaseMaterial
) {
    public static final int DEFAULT_QUALITY = ArmorConstructionData.DEFAULT_QUALITY;
    public static final String HELMET_CHAINMAIL = "helmet_chainmail";
    public static final String HELMET_PLATE = "helmet_plate";
    public static final String CHESTPLATE_CHAINMAIL = "chestplate_chainmail";
    public static final String CHESTPLATE_BODY = "chestplate_body";
    public static final String LEGGINGS_CHAINMAIL = "leggings_chainmail";
    public static final String LEGGINGS_PLATE = "leggings_plate";
    public static final String BOOTS_CHAINMAIL = "boots_chainmail";
    public static final String BOOTS_PLATE = "boots_plate";

    public static final Codec<ArmorPartData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("part_type").forGetter(ArmorPartData::partType),
            ResourceLocation.CODEC.fieldOf("material_id").forGetter(ArmorPartData::materialId),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ArmorPartData::quality),
            ResourceLocation.CODEC.optionalFieldOf("coating_base_material").forGetter(ArmorPartData::coatingBaseMaterial)
    ).apply(instance, ArmorPartData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorPartData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ArmorPartData(String partType, ResourceLocation materialId) {
        this(partType, materialId, DEFAULT_QUALITY);
    }

    public ArmorPartData(String partType, ResourceLocation materialId, int quality) {
        this(partType, materialId, quality, Optional.empty());
    }

    public ArmorPartData {
        quality = ForgingQuality.clampScore(quality);
        coatingBaseMaterial = coatingBaseMaterial == null ? Optional.empty() : coatingBaseMaterial;
    }

    public ArmorPartData withCoating(ResourceLocation coatingMaterial, int coatingQuality) {
        return new ArmorPartData(partType, coatingMaterial, coatingQuality, Optional.of(materialId));
    }

    public ArmorPartData withCoatingBaseMaterial(ResourceLocation baseMaterial) {
        return new ArmorPartData(partType, materialId, quality, Optional.of(baseMaterial));
    }

    public boolean isCoated() {
        return coatingBaseMaterial.isPresent();
    }

    public ForgingQuality qualityLevel() {
        return ForgingQuality.fromScore(quality);
    }
}

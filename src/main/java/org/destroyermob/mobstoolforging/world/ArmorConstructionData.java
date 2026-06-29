package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public record ArmorConstructionData(
        ResourceLocation armorType,
        ResourceLocation skullMaterial,
        Optional<ResourceLocation> combMaterial,
        Optional<ResourceLocation> visorMaterial,
        int quality
) {
    public static final int DEFAULT_QUALITY = 100;
    public static final ResourceLocation HELMET_TYPE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "helmet");

    public ArmorConstructionData {
        combMaterial = combMaterial == null ? Optional.empty() : combMaterial;
        visorMaterial = visorMaterial == null ? Optional.empty() : visorMaterial;
        quality = DEFAULT_QUALITY;
    }

    public static final Codec<ArmorConstructionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("armor_type").forGetter(ArmorConstructionData::armorType),
            ResourceLocation.CODEC.fieldOf("skull_material").forGetter(ArmorConstructionData::skullMaterial),
            ResourceLocation.CODEC.optionalFieldOf("comb_material").forGetter(ArmorConstructionData::combMaterial),
            ResourceLocation.CODEC.optionalFieldOf("visor_material").forGetter(ArmorConstructionData::visorMaterial),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ArmorConstructionData::quality)
    ).apply(instance, ArmorConstructionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorConstructionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static ArmorConstructionData helmet(ResourceLocation skullMaterial, Optional<ResourceLocation> combMaterial, Optional<ResourceLocation> visorMaterial) {
        return new ArmorConstructionData(HELMET_TYPE, skullMaterial, combMaterial, visorMaterial, DEFAULT_QUALITY);
    }
}

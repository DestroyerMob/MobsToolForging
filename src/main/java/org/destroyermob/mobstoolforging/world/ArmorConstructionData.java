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
    public static final ResourceLocation CHESTPLATE_TYPE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "chestplate");
    public static final ResourceLocation LEGGINGS_TYPE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "leggings");
    public static final ResourceLocation BOOTS_TYPE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "boots");

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

    public static ArmorConstructionData chestplate(ResourceLocation bodyMaterial) {
        return new ArmorConstructionData(CHESTPLATE_TYPE, bodyMaterial, Optional.empty(), Optional.empty(), DEFAULT_QUALITY);
    }

    public static ArmorConstructionData leggings(ResourceLocation legMaterial) {
        return new ArmorConstructionData(LEGGINGS_TYPE, legMaterial, Optional.empty(), Optional.empty(), DEFAULT_QUALITY);
    }

    public static ArmorConstructionData boots(ResourceLocation footMaterial) {
        return new ArmorConstructionData(BOOTS_TYPE, footMaterial, Optional.empty(), Optional.empty(), DEFAULT_QUALITY);
    }
}

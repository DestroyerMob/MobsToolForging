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
        Optional<ResourceLocation> overlayBaseMaterial,
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
        overlayBaseMaterial = overlayBaseMaterial == null ? Optional.empty() : overlayBaseMaterial;
        visorMaterial = visorMaterial == null ? Optional.empty() : visorMaterial;
        quality = ForgingQuality.clampScore(quality);
    }

    public ArmorConstructionData(ResourceLocation armorType, ResourceLocation skullMaterial, Optional<ResourceLocation> combMaterial, Optional<ResourceLocation> visorMaterial, int quality) {
        this(armorType, skullMaterial, combMaterial, Optional.empty(), visorMaterial, quality);
    }

    public static final Codec<ArmorConstructionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("armor_type").forGetter(ArmorConstructionData::armorType),
            ResourceLocation.CODEC.fieldOf("skull_material").forGetter(ArmorConstructionData::skullMaterial),
            ResourceLocation.CODEC.optionalFieldOf("comb_material").forGetter(ArmorConstructionData::combMaterial),
            ResourceLocation.CODEC.optionalFieldOf("overlay_base_material").forGetter(ArmorConstructionData::overlayBaseMaterial),
            ResourceLocation.CODEC.optionalFieldOf("visor_material").forGetter(ArmorConstructionData::visorMaterial),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ArmorConstructionData::quality)
    ).apply(instance, ArmorConstructionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorConstructionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static ArmorConstructionData helmet(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial) {
        return helmet(chainmailMaterial, plateMaterial, DEFAULT_QUALITY);
    }

    public static ArmorConstructionData helmet(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, int quality) {
        return new ArmorConstructionData(HELMET_TYPE, chainmailMaterial, plateMaterial, Optional.empty(), quality);
    }

    public static ArmorConstructionData helmet(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, Optional<ResourceLocation> legacyVisorMaterial) {
        return helmet(chainmailMaterial, plateMaterial.or(() -> legacyVisorMaterial), DEFAULT_QUALITY);
    }

    public static ArmorConstructionData helmet(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, Optional<ResourceLocation> legacyVisorMaterial, int quality) {
        return helmet(chainmailMaterial, plateMaterial.or(() -> legacyVisorMaterial), quality);
    }

    public static ArmorConstructionData chainmailHelmet() {
        return chainmailHelmet(DEFAULT_QUALITY);
    }

    public static ArmorConstructionData chainmailHelmet(int quality) {
        return helmet(MaterialCatalog.IRON, Optional.empty(), quality);
    }

    public ResourceLocation helmetChainmailMaterial() {
        return chainmailMaterial();
    }

    public Optional<ResourceLocation> helmetPlateMaterial() {
        return overlayMaterial();
    }

    public static ArmorConstructionData chestplateBase(ResourceLocation chainmailMaterial) {
        return chestplateBase(chainmailMaterial, DEFAULT_QUALITY);
    }

    public static ArmorConstructionData chestplateBase(ResourceLocation chainmailMaterial, int quality) {
        return new ArmorConstructionData(CHESTPLATE_TYPE, chainmailMaterial, Optional.empty(), Optional.empty(), quality);
    }

    public static ArmorConstructionData chestplate(ResourceLocation bodyMaterial) {
        return chestplate(bodyMaterial, DEFAULT_QUALITY);
    }

    public static ArmorConstructionData chestplate(ResourceLocation bodyMaterial, int quality) {
        return new ArmorConstructionData(CHESTPLATE_TYPE, MaterialCatalog.IRON, Optional.of(bodyMaterial), Optional.empty(), quality);
    }

    public static ArmorConstructionData chainmailChestplate() {
        return chainmailChestplate(DEFAULT_QUALITY);
    }

    public static ArmorConstructionData chainmailChestplate(int quality) {
        return new ArmorConstructionData(CHESTPLATE_TYPE, MaterialCatalog.IRON, Optional.empty(), Optional.empty(), quality);
    }

    public boolean isChestplate() {
        return CHESTPLATE_TYPE.equals(armorType);
    }

    public ResourceLocation chestplateChainmailMaterial() {
        return isChestplate() ? chainmailMaterial() : MaterialCatalog.IRON;
    }

    public Optional<ResourceLocation> chestplatePlateMaterial() {
        return isChestplate() ? overlayMaterial() : Optional.empty();
    }

    public static ArmorConstructionData leggings(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial) {
        return leggings(chainmailMaterial, plateMaterial, DEFAULT_QUALITY);
    }

    public static ArmorConstructionData leggings(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, int quality) {
        return new ArmorConstructionData(LEGGINGS_TYPE, chainmailMaterial, plateMaterial, Optional.empty(), quality);
    }

    public static ArmorConstructionData leggings(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, Optional<ResourceLocation> legacyTassetMaterial) {
        return leggings(chainmailMaterial, plateMaterial.or(() -> legacyTassetMaterial), DEFAULT_QUALITY);
    }

    public static ArmorConstructionData leggings(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, Optional<ResourceLocation> legacyTassetMaterial, int quality) {
        return leggings(chainmailMaterial, plateMaterial.or(() -> legacyTassetMaterial), quality);
    }

    public static ArmorConstructionData chainmailLeggings() {
        return chainmailLeggings(DEFAULT_QUALITY);
    }

    public static ArmorConstructionData chainmailLeggings(int quality) {
        return leggings(MaterialCatalog.IRON, Optional.empty(), quality);
    }

    public ResourceLocation leggingsChainmailMaterial() {
        return chainmailMaterial();
    }

    public Optional<ResourceLocation> leggingsPlateMaterial() {
        return overlayMaterial();
    }

    public static ArmorConstructionData boots(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial) {
        return boots(chainmailMaterial, plateMaterial, DEFAULT_QUALITY);
    }

    public static ArmorConstructionData boots(ResourceLocation chainmailMaterial, Optional<ResourceLocation> plateMaterial, int quality) {
        return new ArmorConstructionData(BOOTS_TYPE, chainmailMaterial, plateMaterial, Optional.empty(), quality);
    }

    public static ArmorConstructionData chainmailBoots() {
        return chainmailBoots(DEFAULT_QUALITY);
    }

    public static ArmorConstructionData chainmailBoots(int quality) {
        return boots(MaterialCatalog.IRON, Optional.empty(), quality);
    }

    public ResourceLocation bootsChainmailMaterial() {
        return chainmailMaterial();
    }

    public Optional<ResourceLocation> bootsPlateMaterial() {
        return overlayMaterial();
    }

    public ResourceLocation chainmailMaterial() {
        if (storesLegacyOverlayAsSkull()) {
            return MaterialCatalog.IRON;
        }
        return skullMaterial;
    }

    public Optional<ResourceLocation> overlayMaterial() {
        if (combMaterial.isPresent()) {
            return combMaterial;
        }
        if (storesLegacyOverlayAsSkull()) {
            return Optional.of(skullMaterial);
        }
        if (!isChestplate() && visorMaterial.isPresent()) {
            return visorMaterial;
        }
        return Optional.empty();
    }

    public ArmorConstructionData withOverlayBaseMaterial(Optional<ResourceLocation> baseMaterial) {
        return new ArmorConstructionData(armorType, skullMaterial, combMaterial, baseMaterial, visorMaterial, quality);
    }

    public boolean hasLeatherBase() {
        return MaterialCatalog.LEATHER.equals(chainmailMaterial());
    }

    private boolean storesLegacyOverlayAsSkull() {
        return combMaterial.isEmpty()
                && visorMaterial.isEmpty()
                && !MaterialCatalog.IRON.equals(skullMaterial)
                && !MaterialCatalog.LEATHER.equals(skullMaterial);
    }

    public ForgingQuality qualityLevel() {
        return ForgingQuality.fromScore(quality);
    }
}

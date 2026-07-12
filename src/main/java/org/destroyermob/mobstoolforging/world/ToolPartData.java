package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ToolPartData(
        String partType,
        ResourceLocation materialId,
        int quality,
        Optional<ResourceLocation> treatment,
        ToolPartFinish finish,
        Optional<ResourceLocation> coatingBaseMaterial
) {
    public static final int DEFAULT_QUALITY = ToolConstructionData.DEFAULT_QUALITY;
    public static final String SWORD_BLADE = "sword_blade";
    public static final String SWORD_GUARD = "sword_guard";
    public static final String SHOVEL_HEAD = "shovel_head";
    public static final String PICKAXE_HEAD = "pickaxe_head";
    public static final String AXE_HEAD = "axe_head";
    public static final String HOE_HEAD = "hoe_head";
    public static final String COOKING_KNIFE_HEAD = "cooking_knife_head";
    public static final String SMITHING_HAMMER_HEAD = "smithing_hammer_head";
    public static final String SCREWDRIVER_HEAD = "screwdriver_head";
    public static final String GEM_CUTTERS_BLADE = "gem_cutters_blade";
    public static final String CROSSBOW_BODY = "crossbow_body";
    public static final String CROSSBOW_LIMBS = "crossbow_limbs";

    public static final Codec<ToolPartData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("part_type").forGetter(ToolPartData::partType),
            ResourceLocation.CODEC.fieldOf("material_id").forGetter(ToolPartData::materialId),
            Codec.INT.optionalFieldOf("quality", DEFAULT_QUALITY).forGetter(ToolPartData::quality),
            ResourceLocation.CODEC.optionalFieldOf("treatment").forGetter(ToolPartData::treatment),
            ToolPartFinish.CODEC.optionalFieldOf("finish", ToolPartFinish.POLISHED).forGetter(ToolPartData::finish),
            ResourceLocation.CODEC.optionalFieldOf("coating_base_material").forGetter(ToolPartData::coatingBaseMaterial)
    ).apply(instance, ToolPartData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolPartData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ToolPartData(String partType, ResourceLocation materialId) {
        this(partType, materialId, DEFAULT_QUALITY);
    }

    public ToolPartData(String partType, ResourceLocation materialId, int quality) {
        this(partType, materialId, quality, Optional.empty());
    }

    public ToolPartData(String partType, ResourceLocation materialId, int quality, Optional<ResourceLocation> treatment) {
        this(partType, materialId, quality, treatment, ToolPartFinish.POLISHED);
    }

    public ToolPartData(String partType, ResourceLocation materialId, int quality, Optional<ResourceLocation> treatment, ToolPartFinish finish) {
        this(partType, materialId, quality, treatment, finish, Optional.empty());
    }

    public ToolPartData {
        quality = ForgingQuality.clampScore(quality);
        treatment = treatment == null ? Optional.empty() : treatment;
        finish = finish == null ? ToolPartFinish.POLISHED : finish;
        coatingBaseMaterial = coatingBaseMaterial == null ? Optional.empty() : coatingBaseMaterial;
        if (!MaterialCatalog.supportsTreatment(coatingBaseMaterial.orElse(materialId))) {
            treatment = Optional.empty();
        }
    }

    public ToolPartData withTreatment(ResourceLocation treatment) {
        return new ToolPartData(partType, materialId, quality, Optional.of(treatment), finish, coatingBaseMaterial);
    }

    public boolean supportsTreatment() {
        return MaterialCatalog.supportsTreatment(coatingBaseMaterial.orElse(materialId));
    }

    public ToolPartData withFinish(ToolPartFinish finish) {
        return new ToolPartData(partType, materialId, quality, treatment, finish, coatingBaseMaterial);
    }

    public ToolPartData polished() {
        return new ToolPartData(partType, materialId, quality, treatment, ToolPartFinish.POLISHED, coatingBaseMaterial);
    }

    public ToolPartData withCoating(ResourceLocation coatingMaterial, int coatingQuality) {
        return new ToolPartData(partType, coatingMaterial, coatingQuality, treatment, finish, Optional.of(materialId));
    }

    public ToolPartData withCoatingBaseMaterial(ResourceLocation baseMaterial) {
        return new ToolPartData(partType, materialId, quality, treatment, finish, Optional.of(baseMaterial));
    }

    public boolean isCoated() {
        return coatingBaseMaterial.isPresent();
    }

    public ForgingQuality qualityLevel() {
        return ForgingQuality.fromScore(quality);
    }

    public int effectiveQuality() {
        return finish.effectiveQualityScore(quality, finishAffectsQuality());
    }

    public ForgingQuality effectiveQualityLevel() {
        return ForgingQuality.fromScore(effectiveQuality());
    }

    public boolean isPolishable() {
        return isPolishable(partType, materialId);
    }

    public boolean finishAffectsQuality() {
        return isPolishable();
    }

    public boolean needsPolishing() {
        return finish == ToolPartFinish.UNPOLISHED && isPolishable();
    }

    public static ToolPartFinish initialForgedFinish(ForgeTemplateDefinition template, ResourceLocation materialId) {
        if (template == null || !isMetal(materialId)) {
            return ToolPartFinish.POLISHED;
        }
        return ToolTypeRegistry.toolType(template.toolType())
                .filter(type -> template.partType().equals(type.primaryPartType()))
                .filter(type -> !isStationToolType(type.id()))
                .isPresent() ? ToolPartFinish.UNPOLISHED : ToolPartFinish.POLISHED;
    }

    public static boolean isPolishable(String partType, ResourceLocation materialId) {
        if (!isMetalOrGem(materialId)) {
            return false;
        }
        return ToolTypeRegistry.toolTypes().stream()
                .filter(type -> partType.equals(type.primaryPartType()) || type.requiresAssemblyPart(partType))
                .anyMatch(type -> !isStationToolType(type.id()));
    }

    private static boolean isMetal(ResourceLocation materialId) {
        return MaterialCatalog.definition(materialId)
                .map(definition -> definition.category() == MaterialCategory.METAL)
                .orElse(false);
    }

    private static boolean isMetalOrGem(ResourceLocation materialId) {
        return MaterialCatalog.definition(materialId)
                .map(definition -> definition.category() == MaterialCategory.METAL || definition.category() == MaterialCategory.GEM)
                .orElse(false);
    }

    private static boolean isStationToolType(ResourceLocation toolType) {
        return ToolTypeRegistry.SMITHING_HAMMER_TOOL_TYPE.equals(toolType)
                || ToolTypeRegistry.SCREWDRIVER_TOOL_TYPE.equals(toolType)
                || ToolTypeRegistry.GEM_CUTTERS_KNIFE_TOOL_TYPE.equals(toolType);
    }
}

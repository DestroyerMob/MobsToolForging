package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record MetallurgyData(
        Origin origin,
        CastDefect castDefect,
        HeatTreatment heatTreatment,
        Optional<ResourceLocation> alloyRecipe,
        Map<ResourceLocation, Integer> composition
) {
    public static final Codec<MetallurgyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Origin.CODEC.optionalFieldOf("origin", Origin.FORGED).forGetter(MetallurgyData::origin),
            CastDefect.CODEC.optionalFieldOf("cast_defect", CastDefect.NONE).forGetter(MetallurgyData::castDefect),
            HeatTreatment.CODEC.optionalFieldOf("heat_treatment", HeatTreatment.UNTREATED).forGetter(MetallurgyData::heatTreatment),
            ResourceLocation.CODEC.optionalFieldOf("alloy_recipe").forGetter(MetallurgyData::alloyRecipe),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("composition", Map.of()).forGetter(MetallurgyData::composition)
    ).apply(instance, MetallurgyData::new));

    public MetallurgyData {
        origin = origin == null ? Origin.FORGED : origin;
        castDefect = castDefect == null ? CastDefect.NONE : castDefect;
        heatTreatment = heatTreatment == null ? HeatTreatment.UNTREATED : heatTreatment;
        alloyRecipe = alloyRecipe == null ? Optional.empty() : alloyRecipe;
        Map<ResourceLocation, Integer> sanitized = new LinkedHashMap<>();
        if (composition != null) {
            composition.forEach((material, amount) -> {
                if (material != null && amount != null && amount > 0) {
                    sanitized.put(material, amount);
                }
            });
        }
        composition = Map.copyOf(sanitized);
    }

    public static MetallurgyData cast(ResourceLocation material, int amountMb) {
        FoundryAlloyRecipe alloy = FoundryAlloyRegistry.findByResult(material).orElse(null);
        Map<ResourceLocation, Integer> composition = alloy == null
                ? Map.of(material, Math.max(1, amountMb))
                : alloy.inputs();
        return new MetallurgyData(
                Origin.CAST,
                CastDefect.POROSITY,
                HeatTreatment.UNTREATED,
                alloy == null ? Optional.empty() : Optional.of(alloy.id()),
                composition
        );
    }

    public static MetallurgyData forged(ResourceLocation material) {
        return new MetallurgyData(Origin.FORGED, CastDefect.NONE, HeatTreatment.UNTREATED, Optional.empty(), Map.of(material, FoundryForgeBlockEntity.INGOT_MB));
    }

    public MetallurgyData reforged() {
        return new MetallurgyData(Origin.REFORGED, CastDefect.NONE, HeatTreatment.UNTREATED, alloyRecipe, composition);
    }

    public MetallurgyData heated(float temperature) {
        if ((heatTreatment == HeatTreatment.HARDENED || heatTreatment == HeatTreatment.BRITTLE)
                && temperature >= 0.30F && temperature <= 0.65F) {
            return withHeatTreatment(HeatTreatment.TEMPERING);
        }
        return this;
    }

    public MetallurgyData quenched(float temperature) {
        if (temperature >= 0.90F) {
            return withHeatTreatment(HeatTreatment.BRITTLE);
        }
        if (temperature >= 0.60F) {
            return withHeatTreatment(HeatTreatment.HARDENED);
        }
        return this;
    }

    public MetallurgyData cooled() {
        return heatTreatment == HeatTreatment.TEMPERING ? withHeatTreatment(HeatTreatment.TEMPERED) : this;
    }

    public int qualityAdjustment() {
        int adjustment = castDefect == CastDefect.POROSITY ? -5 : 0;
        return adjustment + switch (heatTreatment) {
            case BRITTLE -> -20;
            case TEMPERED -> 5;
            default -> 0;
        };
    }

    private MetallurgyData withHeatTreatment(HeatTreatment value) {
        return new MetallurgyData(origin, castDefect, value, alloyRecipe, composition);
    }

    public enum Origin implements StringRepresentable {
        FORGED("forged"), CAST("cast"), REFORGED("reforged"), RECYCLED("recycled");
        public static final Codec<Origin> CODEC = StringRepresentable.fromEnum(Origin::values);
        private final String name;
        Origin(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum CastDefect implements StringRepresentable {
        NONE("none"), POROSITY("porosity");
        public static final Codec<CastDefect> CODEC = StringRepresentable.fromEnum(CastDefect::values);
        private final String name;
        CastDefect(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum HeatTreatment implements StringRepresentable {
        UNTREATED("untreated"), HARDENED("hardened"), TEMPERING("tempering"), TEMPERED("tempered"), BRITTLE("brittle");
        public static final Codec<HeatTreatment> CODEC = StringRepresentable.fromEnum(HeatTreatment::values);
        private final String name;
        HeatTreatment(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}

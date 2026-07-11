package org.destroyermob.mobstoolforging.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

public record HeatVisualProfile(
        TagKey<Item> tag,
        int priority,
        List<Integer> colors,
        float visibleThreshold,
        float colorCurveExponent,
        float surfaceTintStrength,
        float emissionStart,
        float emissionStrength,
        float haloStart,
        float haloAlpha,
        float haloBaseScale,
        float haloExtraScale,
        float lightStart,
        int maximumBlockLight
) {
    private static final List<Integer> GENERIC_COLORS = List.of(
            0xFF240301, 0xFF5B0803, 0xFF991306, 0xFFD12A0B, 0xFFF24C14,
            0xFFFF7625, 0xFFFFA440, 0xFFFFCB63, 0xFFFFE595, 0xFFFFF3CA, 0xFFFFFCF2
    );
    public static final HeatVisualProfile GENERIC = new HeatVisualProfile(
            null, Integer.MIN_VALUE, GENERIC_COLORS, 0.10F, 1.28F, 0.48F,
            0.14F, 0.72F, 0.34F, 0.065F, 1.016F, 0.012F, 0.22F, 13
    );

    public HeatVisualProfile {
        colors = List.copyOf(colors);
        if (colors.size() < 2) {
            throw new IllegalArgumentException("A heat visual profile requires at least two colors");
        }
        visibleThreshold = clamp(visibleThreshold);
        colorCurveExponent = Math.max(0.1F, colorCurveExponent);
        surfaceTintStrength = clamp(surfaceTintStrength);
        emissionStart = clamp(emissionStart);
        emissionStrength = clamp(emissionStrength);
        haloStart = clamp(haloStart);
        haloAlpha = clamp(haloAlpha);
        haloBaseScale = Math.max(1.0F, haloBaseScale);
        haloExtraScale = Math.max(0.0F, haloExtraScale);
        lightStart = clamp(lightStart);
        maximumBlockLight = Math.max(0, Math.min(15, maximumBlockLight));
    }

    public static HeatVisualProfile fromJson(JsonObject json) {
        ResourceLocation tagId = ResourceLocation.parse(GsonHelper.getAsString(json, "tag"));
        JsonArray colorJson = GsonHelper.getAsJsonArray(json, "colors");
        List<Integer> colors = new ArrayList<>();
        colorJson.forEach(element -> colors.add(parseColor(element)));
        return new HeatVisualProfile(
                TagKey.create(Registries.ITEM, tagId),
                GsonHelper.getAsInt(json, "priority", 0),
                colors,
                GsonHelper.getAsFloat(json, "visible_threshold", GENERIC.visibleThreshold()),
                GsonHelper.getAsFloat(json, "color_curve_exponent", GENERIC.colorCurveExponent()),
                GsonHelper.getAsFloat(json, "surface_tint_strength", GENERIC.surfaceTintStrength()),
                GsonHelper.getAsFloat(json, "emission_start", GENERIC.emissionStart()),
                GsonHelper.getAsFloat(json, "emission_strength", GENERIC.emissionStrength()),
                GsonHelper.getAsFloat(json, "halo_start", GENERIC.haloStart()),
                GsonHelper.getAsFloat(json, "halo_alpha", GENERIC.haloAlpha()),
                GsonHelper.getAsFloat(json, "halo_base_scale", GENERIC.haloBaseScale()),
                GsonHelper.getAsFloat(json, "halo_extra_scale", GENERIC.haloExtraScale()),
                GsonHelper.getAsFloat(json, "light_start", GENERIC.lightStart()),
                GsonHelper.getAsInt(json, "maximum_block_light", GENERIC.maximumBlockLight())
        );
    }

    private static int parseColor(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        String value = element.getAsString().trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }
        long parsed = Long.parseLong(value, 16);
        return value.length() <= 6 ? (int) (0xFF000000L | parsed) : (int) parsed;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

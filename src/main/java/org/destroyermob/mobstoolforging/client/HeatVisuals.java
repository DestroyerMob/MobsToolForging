package org.destroyermob.mobstoolforging.client;

import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public final class HeatVisuals {
    public static final float VISIBLE_HEAT_THRESHOLD = 0.10F;

    private HeatVisuals() {
    }

    public static int heatColor(float heat) {
        return heatColor(HeatVisualProfile.GENERIC, heat);
    }

    public static int heatColor(HeatVisualProfile profile, float heat) {
        float visibleHeat = smoothstep(profile.visibleThreshold(), WorkpieceHeat.WHITE_HOT_TEMPERATURE, heat);
        float scaled = (float) Math.pow(visibleHeat, profile.colorCurveExponent()) * (profile.colors().size() - 1);
        int index = Math.min(profile.colors().size() - 2, (int) Math.floor(scaled));
        float amount = smoothstep(0.0F, 1.0F, scaled - index);
        return lerpColor(profile.colors().get(index), profile.colors().get(index + 1), amount);
    }

    /** Keeps the real material texture visible while carrying the heat colour. */
    public static int surfaceTint(float heat) {
        return surfaceTint(HeatVisualProfile.GENERIC, heat);
    }

    public static int surfaceTint(HeatVisualProfile profile, float heat) {
        float clamped = clamp(heat);
        float strength = smoothstep(profile.visibleThreshold(), WorkpieceHeat.WHITE_HOT_TEMPERATURE, clamped) * profile.surfaceTintStrength();
        return lerpColor(0xFFFFFFFF, heatColor(profile, clamped), strength);
    }

    public static int interfaceColor(float heat) {
        return heatColor(0.35F + clamp(heat) * 0.65F);
    }

    public static float overlayAlpha(float heat) {
        return overlayAlpha(HeatVisualProfile.GENERIC, heat);
    }

    public static float overlayAlpha(HeatVisualProfile profile, float heat) {
        float visibleHeat = smoothstep(profile.emissionStart(), WorkpieceHeat.WHITE_HOT_TEMPERATURE, heat);
        return visibleHeat * visibleHeat * profile.emissionStrength();
    }

    public static float itemHaloStrength(float heat) {
        return itemHaloStrength(HeatVisualProfile.GENERIC, heat);
    }

    public static float itemHaloStrength(HeatVisualProfile profile, float heat) {
        return smoothstep(profile.haloStart(), WorkpieceHeat.WHITE_HOT_TEMPERATURE, heat);
    }

    public static float itemHaloAlpha(float heat) {
        return itemHaloAlpha(HeatVisualProfile.GENERIC, heat);
    }

    public static float itemHaloAlpha(HeatVisualProfile profile, float heat) {
        return itemHaloStrength(profile, heat) * profile.haloAlpha();
    }

    public static float itemHaloScale(float heat) {
        return itemHaloScale(HeatVisualProfile.GENERIC, heat);
    }

    public static float itemHaloScale(HeatVisualProfile profile, float heat) {
        return profile.haloBaseScale() + itemHaloStrength(profile, heat) * profile.haloExtraScale();
    }

    public static int heatedLight(int packedLight, float heat) {
        return heatedLight(packedLight, HeatVisualProfile.GENERIC, heat);
    }

    public static int heatedLight(int packedLight, HeatVisualProfile profile, float heat) {
        int minimumLight = Math.round(smoothstep(profile.lightStart(), WorkpieceHeat.WHITE_HOT_TEMPERATURE, heat) * profile.maximumBlockLight()) << 4;
        int blockLight = Math.max(packedLight & 0xFFFF, minimumLight);
        int skyLight = packedLight >>> 16 & 0xFFFF;
        return blockLight | skyLight << 16;
    }

    public static int withAlpha(int color, float alpha) {
        return Math.round(clamp(alpha) * 255.0F) << 24 | color & 0x00FFFFFF;
    }

    public static int multiplyAlpha(int color, float multiplier) {
        float alpha = (color >>> 24 & 0xFF) / 255.0F;
        return withAlpha(color, alpha * multiplier);
    }

    public static float smoothstep(float from, float to, float value) {
        if (from >= to) {
            return clamp(value);
        }
        float t = clamp((value - from) / (to - from));
        return t * t * (3.0F - 2.0F * t);
    }

    public static int lerpColor(int from, int to, float amount) {
        float clamped = clamp(amount);
        int alpha = Math.round(lerp(from >>> 24 & 0xFF, to >>> 24 & 0xFF, clamped));
        int red = Math.round(lerp(from >>> 16 & 0xFF, to >>> 16 & 0xFF, clamped));
        int green = Math.round(lerp(from >>> 8 & 0xFF, to >>> 8 & 0xFF, clamped));
        int blue = Math.round(lerp(from & 0xFF, to & 0xFF, clamped));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * clamp(amount);
    }
}

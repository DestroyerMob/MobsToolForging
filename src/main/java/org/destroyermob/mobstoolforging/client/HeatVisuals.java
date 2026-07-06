package org.destroyermob.mobstoolforging.client;

final class HeatVisuals {
    private static final int[] HEAT_COLOR_STOPS = {
            0xFF3A0903,
            0xFF520D04,
            0xFF6D1205,
            0xFF861806,
            0xFFA11F07,
            0xFFB92908,
            0xFFD2350A,
            0xFFE4430E,
            0xFFF15316,
            0xFFFF6420,
            0xFFFF762B,
            0xFFFF8836,
            0xFFFF9A42,
            0xFFFFAD50,
            0xFFFFBE61,
            0xFFFFCE73,
            0xFFFFDC8A,
            0xFFFFE8A3,
            0xFFFFF1C0,
            0xFFFFF8DF,
            0xFFFFFFFF
    };

    private HeatVisuals() {
    }

    static int heatColor(float heat) {
        float scaled = clamp(heat) * (HEAT_COLOR_STOPS.length - 1);
        int index = Math.min(HEAT_COLOR_STOPS.length - 2, (int) Math.floor(scaled));
        float amount = smoothstep(0.0F, 1.0F, scaled - index);
        return lerpColor(HEAT_COLOR_STOPS[index], HEAT_COLOR_STOPS[index + 1], amount);
    }

    static int withAlpha(int color, float alpha) {
        return Math.round(clamp(alpha) * 255.0F) << 24 | color & 0x00FFFFFF;
    }

    static int multiplyAlpha(int color, float multiplier) {
        float alpha = (color >>> 24 & 0xFF) / 255.0F;
        return withAlpha(color, alpha * multiplier);
    }

    static float smoothstep(float from, float to, float value) {
        if (from >= to) {
            return clamp(value);
        }
        float t = clamp((value - from) / (to - from));
        return t * t * (3.0F - 2.0F * t);
    }

    static int lerpColor(int from, int to, float amount) {
        float clamped = clamp(amount);
        int alpha = Math.round(lerp(from >>> 24 & 0xFF, to >>> 24 & 0xFF, clamped));
        int red = Math.round(lerp(from >>> 16 & 0xFF, to >>> 16 & 0xFF, clamped));
        int green = Math.round(lerp(from >>> 8 & 0xFF, to >>> 8 & 0xFF, clamped));
        int blue = Math.round(lerp(from & 0xFF, to & 0xFF, clamped));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * clamp(amount);
    }
}

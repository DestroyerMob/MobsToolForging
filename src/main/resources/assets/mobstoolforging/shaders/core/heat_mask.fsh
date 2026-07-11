#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float GameTime;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 textureColor = texture(Sampler0, texCoord0);
    if (textureColor.a < 0.1) {
        discard;
    }

    // Preserve surface detail instead of replacing the workpiece with a flat silhouette.
    float luminance = dot(textureColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    float surfaceDetail = mix(0.58, 1.0, smoothstep(0.08, 0.92, luminance));

    // A deliberately tiny, slow thermal pulse: alive at forging heat, never flickery.
    float phase = (texCoord0.x * 173.0) + (texCoord0.y * 211.0) + (GameTime * 125.0);
    float thermalPulse = 0.985 + sin(phase) * 0.015;
    float alpha = textureColor.a * vertexColor.a * ColorModulator.a * surfaceDetail * thermalPulse;
    vec4 maskColor = vec4(vertexColor.rgb * ColorModulator.rgb, alpha);
    fragColor = maskColor * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}

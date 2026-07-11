#version 150

uniform vec4 ColorModulator;
uniform float GameTime;

in vec3 localPosition;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float height = clamp((localPosition.y - 0.02) / 0.62, 0.0, 1.0);
    float verticalFade = sin(height * 3.14159265);
    float edgeFade = 1.0 - smoothstep(0.12, 0.34, max(abs(localPosition.x), abs(localPosition.z)));
    float risingBand = 0.52 + 0.48 * sin(
        localPosition.y * 31.0
        + localPosition.x * 17.0
        + localPosition.z * 13.0
        - GameTime * 520.0
    );
    float secondaryBand = 0.72 + 0.28 * sin(localPosition.y * 19.0 + GameTime * 310.0);
    float alpha = vertexColor.a * ColorModulator.a * verticalFade * edgeFade * risingBand * secondaryBand;
    if (alpha < 0.003) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb * ColorModulator.rgb, alpha);
}

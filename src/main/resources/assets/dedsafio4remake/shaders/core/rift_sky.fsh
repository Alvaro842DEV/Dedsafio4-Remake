#version 150

uniform vec4 ColorModulator;
uniform vec3 ModelOffset;

in vec3 skyDir;

out vec4 fragColor;

const float PI = 3.14159265;
const float RIFT_HALF_LENGTH = 0.95;

const vec3 VOID = vec3(0.018, 0.0, 0.006);
const vec3 WINE = vec3(0.20, 0.0, 0.035);
const vec3 CRIMSON = vec3(0.60, 0.02, 0.06);
const vec3 SCARLET = vec3(0.96, 0.14, 0.10);
const vec3 ROSE = vec3(1.0, 0.45, 0.40);
const vec3 PLUM = vec3(0.24, 0.01, 0.13);
const vec3 EMBER = vec3(1.0, 0.38, 0.08);

const mat2 OCTAVE = mat2(1.6, 1.2, -1.2, 1.6);

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash13(vec3 p3) {
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3, p3.zyx + 31.32);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), u.x),
               mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p = OCTAVE * p;
        amplitude *= 0.5;
    }
    return value / 0.96875;
}

float ridged(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        float n = 1.0 - abs(noise(p) * 2.0 - 1.0);
        value += amplitude * n * n;
        p = OCTAVE * p;
        amplitude *= 0.5;
    }
    return value / 0.9375;
}

vec3 shoulder(vec3 color) {
    const float KNEE = 0.7;
    float peak = max(color.r, max(color.g, color.b));
    if (peak <= KNEE) return color;
    float compressed = KNEE + (1.0 - KNEE) * (1.0 - exp(-(peak - KNEE) / (1.0 - KNEE)));
    return color * (compressed / peak);
}

mat2 rot(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat2(c, -s, s, c);
}

float heartbeat(float t) {
    float phase = fract(t / 1.6);
    return exp(-pow((phase - 0.04) / 0.03, 2.0)) + 0.55 * exp(-pow((phase - 0.19) / 0.04, 2.0));
}

vec3 vortex(vec2 p, float r, float t, vec2 so, float pulse) {
    // E centro gira más rápido y eso pone a las nubes en espiral
    vec2 q = rot(2.2 / (r + 0.3) - t * 0.10) * p;

    vec2 warp = vec2(fbm(q * 1.6 + so + vec2(0.0, t * 0.04)),
                     fbm(q * 1.6 - so + vec2(5.2, -t * 0.035)));
    float clouds = fbm(q * 2.3 + warp * 1.7 + vec2(t * 0.02, 0.0));

    float angle = atan(p.y, p.x + 1e-6);
    float arms = 0.5 + 0.5 * sin(3.0 * angle - 4.5 * log(r + 0.05) + t * 0.35 + clouds * 3.5);
    float armWeight = smoothstep(0.15, 0.7, r) * (1.0 - smoothstep(2.4, 3.1, r));

    float density = clamp(clouds * 0.85 + (arms - 0.5) * 0.55 * armWeight, 0.0, 1.0);
    density = smoothstep(0.18, 0.92, density);

    vec3 color = mix(VOID, WINE, smoothstep(0.0, 0.3, density));
    color = mix(color, CRIMSON, smoothstep(0.28, 0.68, density));
    color = mix(color, SCARLET, smoothstep(0.62, 0.95, density) * 0.85);
    color = mix(color, PLUM, (1.0 - density) * smoothstep(0.45, 0.85, warp.x) * 0.7);
    color += EMBER * smoothstep(0.78, 1.0, clouds) * 0.18;

    if (density > 0.0) {
        float veins = pow(ridged(q * 3.2 + warp * 2.4 - vec2(t * 0.03)), 4.0);
        color += ROSE * veins * density * (0.25 + 0.45 * pulse);
    }

    float eye = 1.0 - smoothstep(0.08, 0.40, r);
    float wall = exp(-pow((r - 0.40) / 0.13, 2.0));
    color = mix(color, VOID * 0.4, eye * 0.9);
    color += SCARLET * wall * (0.18 + 0.22 * pulse) * (0.6 + 0.4 * clouds);

    vec2 ep = rot(1.4 / (r + 0.25) - t * 0.22) * p * 9.0;
    vec2 cell = floor(ep);
    float h = hash12(cell + so * 3.0);
    if (h > 0.93) {
        vec2 spot = vec2(hash12(cell + 17.3), hash12(cell + 41.7)) - 0.5;
        float d = length(fract(ep) - 0.5 - spot * 0.6);
        float twinkle = 0.5 + 0.5 * sin(t * (2.0 + h * 6.0) + h * 50.0);
        color += EMBER * smoothstep(0.09, 0.0, d) * twinkle * smoothstep(0.3, 0.8, r) * 1.4;
    }

    return color * (0.92 + 0.12 * pulse);
}

float stars(vec3 dir, float scale, float threshold, float radius, float halo, float t) {
    vec3 g = dir * scale;
    vec3 cell = floor(g);
    float h = hash13(cell);
    if (h < threshold) return 0.0;
    vec3 offset = vec3(hash13(cell + 11.1), hash13(cell + 23.7), hash13(cell + 37.3)) - 0.5;
    float d = length(fract(g) - 0.5 - offset * 0.4);
    float twinkle = 0.7 + 0.3 * sin(t * (1.5 + h * 4.0) + h * 80.0);
    float brightness = 0.8 + 2.2 * (h - threshold) / (1.0 - threshold);
    return (smoothstep(radius, 0.0, d) + halo * exp(-d * 9.0)) * twinkle * brightness;
}

// Las estrellas están fijas en el mundo
vec3 riftSpace(vec3 dir, float t, vec2 so) {
    vec2 sp = dir.xz * 3.0 + so;
    float nebula = fbm(sp * 1.3);
    float wisps = fbm(sp * 2.6 + vec2(nebula * 2.0, -t * 0.01));

    vec3 color = vec3(0.004, 0.025, 0.022);
    color = mix(color, vec3(0.02, 0.14, 0.10), smoothstep(0.35, 0.85, nebula));
    color += vec3(0.08, 0.40, 0.26) * pow(smoothstep(0.5, 0.95, wisps), 2.0) * 0.6;
    color += vec3(0.30, 0.60, 0.18) * pow(nebula * wisps, 3.0) * 0.5;

    color += stars(dir, 240.0, 0.96, 0.24, 0.0, t) * vec3(0.70, 0.95, 0.85) * 0.6;
    color += stars(dir, 110.0, 0.955, 0.30, 0.15, t) * vec3(0.88, 1.0, 0.94);
    color += stars(dir, 38.0, 0.975, 0.16, 0.6, t) * vec3(0.95, 1.0, 0.97);
    return color;
}

void main() {
    vec3 dir = normalize(skyDir);
    float t = ColorModulator.x;
    float spread = ColorModulator.y;
    float line = ColorModulator.z;
    float open = ColorModulator.w;
    float fade = ModelOffset.x;
    float seed = ModelOffset.y;
    float sweep = ModelOffset.z;
    vec2 so = vec2(seed * 173.0, seed * 59.0);

    float r = acos(clamp(dir.y, -1.0, 1.0));
    float horizontal = length(dir.xz);
    vec2 p = horizontal > 1e-5 ? dir.xz / horizontal * r : vec2(0.0);

    float frontBase = spread * 2.45 - 0.25 + sweep * 1.6;

    if (r > frontBase + 0.6) discard;

    float pulse = heartbeat(t);
    vec3 color = vortex(p, r, t, so, pulse);

    color = mix(color, CRIMSON * 0.8 + EMBER * 0.1, exp(-pow(dir.y / 0.12, 2.0)) * 0.45);
    color *= mix(0.35, 1.0, smoothstep(-0.35, 0.05, dir.y));

    float alpha = 1.0;
    float noiseReach = 0.4 * min(spread * 5.0, 1.0);
    if (r >= frontBase - noiseReach - 0.9) {
        float frontNoise = (fbm(p * 1.4 + so * 2.0 + vec2(t * 0.05)) - 0.5) * 0.8;
        float front = frontBase + frontNoise * min(spread * 5.0, 1.0);
        float cover = 1.0 - smoothstep(front - 0.16, front, r);
        float maturity = 1.0 - smoothstep(front - 0.9, front - 0.05, r);
        color = mix(mix(VOID, WINE, 0.35), color, maturity);
        float burning = exp(-pow((r - front) / 0.06, 2.0)) * (1.0 - smoothstep(0.8, 1.0, spread)) * step(0.001, spread);
        color += SCARLET * burning * 0.9;
        alpha = max(cover, burning * 0.85);
    }

    color = shoulder(color);

    if (line > 0.0) {
        vec2 cp = rot(seed * 2.0 * PI) * p;
        float u = cp.x;
        float v = cp.y;
        if (abs(u) < RIFT_HALF_LENGTH + 0.6 && abs(v) < 1.0) {
            float center = (fbm(vec2(u * 2.0, so.x)) - 0.5) * 0.10 + (noise(vec2(u * 11.0, so.y)) - 0.5) * 0.035;
            float along = clamp(abs(u) / RIFT_HALF_LENGTH, 0.0, 1.0);
            float taper = 1.0 - along * along;
            float reveal = 1.0 - smoothstep(line * RIFT_HALF_LENGTH - 0.06, line * RIFT_HALF_LENGTH + 0.02, abs(u));
            float jag = (ridged(vec2(u, v) * 16.0 + so) - 0.45) * 0.028 * (0.35 + open);
            float d = abs(v - center) + jag;
            // Los bordes son irregulares
            float wobbleTop = noise(vec2(u * 3.5 + so.x, t * 0.15));
            float wobbleBottom = noise(vec2(u * 3.5 + so.x + 37.0, t * 0.15 + 11.0));
            float wobble = mix(wobbleBottom, wobbleTop, smoothstep(-0.05, 0.05, v - center));
            float width = open * 0.37 * taper * (0.75 + 0.5 * wobble);
            float inside = (1.0 - smoothstep(width - 0.012, width, d)) * step(0.002, width) * reveal;
            float edge = abs(d - width);

            float flicker = mix(0.55 + 0.45 * noise(vec2(t * 14.0, so.x)), 1.0, open);
            float crackle = 0.7 + 0.3 * sin(u * 45.0 - t * 8.0 + noise(vec2(u * 7.0, t * 1.5)) * 6.0);
            float tips = 1.0 - smoothstep(0.97, 1.0, along);
            float steadyEnergy = reveal * line * flicker * tips * (0.85 + 0.25 * pulse);
            float energy = steadyEnergy * crackle;

            color += vec3(0.9, 0.06, 0.2) * exp(-max(d - width, 0.0) * 5.0) * (0.15 + 0.35 * open) * reveal * line;
            if (inside > 0.0) color = mix(color, riftSpace(dir, t, so), inside);

            float core = exp(-edge * edge / (0.00006 + 0.00025 * open));
            float falloff = 14.0 - 6.0 * open;
            float innerEdge = max(width - abs(v - center), 0.0);
            float outerBloom = exp(-edge * falloff) * energy;
            float innerBloom = 0.4 * exp(-innerEdge * falloff) * steadyEnergy;
            float bloom = mix(outerBloom, innerBloom, inside);
            color += vec3(1.0, 0.82, 0.88) * core * energy * 1.3;
            color += vec3(1.0, 0.08, 0.30) * bloom * 0.9;
            alpha = max(alpha, max(inside, clamp(core * energy, 0.0, 1.0)));
        }
    }

    fragColor = vec4(min(color, vec3(1.0)), clamp(alpha * fade, 0.0, 1.0));
}

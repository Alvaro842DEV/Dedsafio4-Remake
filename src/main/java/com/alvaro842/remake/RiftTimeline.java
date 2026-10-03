package com.alvaro842.remake;

public final class RiftTimeline {
    public static final float TICKS_PER_SECOND = 20.0F;

    public static final float SPREAD_SECONDS = 8.0F;
    public static final float SWEEP_SECONDS = 3.0F;

    public static final float LINE_START_SECONDS = 11.0F;
    public static final float LINE_SECONDS = 2.5F;
    public static final float OPEN_START_SECONDS = 13.5F;
    public static final float OPEN_SECONDS = 8.0F;
    public static final float OPEN_END_SECONDS = OPEN_START_SECONDS + OPEN_SECONDS;
    public static final float HOLD_SECONDS = 10.0F;

    public static final float REVERSE_START_SECONDS = OPEN_END_SECONDS + HOLD_SECONDS;
    public static final float END_SECONDS = REVERSE_START_SECONDS + OPEN_END_SECONDS;
    public static final float FADE_OUT_SECONDS = 4.0F;

    private static final long END_TICKS = (long) (END_SECONDS * TICKS_PER_SECOND);
    private static final long FADE_OUT_TICKS = (long) (FADE_OUT_SECONDS * TICKS_PER_SECOND);

    private RiftTimeline() {}

    public static float phase(float seconds) {
        if (seconds < REVERSE_START_SECONDS) return Math.min(seconds, OPEN_END_SECONDS);
        return Math.max(0.0F, END_SECONDS - seconds);
    }

    public static float spread(float phase) {
        return easeInOut(clamp01(phase / SPREAD_SECONDS));
    }

    public static float sweep(float phase) {
        return clamp01((phase - SPREAD_SECONDS) / SWEEP_SECONDS);
    }

    public static float line(float phase) {
        float x = clamp01((phase - LINE_START_SECONDS) / LINE_SECONDS);
        return 1.0F - (1.0F - x) * (1.0F - x);
    }

    public static float open(float phase) {
        float x = easeInOut(clamp01((phase - OPEN_START_SECONDS) / OPEN_SECONDS));
        return clamp01(x + 0.03F * (float) Math.sin(x * 25.0F) * (1.0F - x) * x);
    }

    public static float fade(long nowTick, float partialTick, long stopTick) {
        if (stopTick < 0) return 1.0F;
        float seconds = (nowTick - stopTick + partialTick) / TICKS_PER_SECOND;
        return 1.0F - clamp01(seconds / FADE_OUT_SECONDS);
    }

    public static boolean isRunning(long nowTick, long startTick, long stopTick) {
        return startTick >= 0 && stopTick < 0 && nowTick - startTick < END_TICKS;
    }

    public static boolean isVisible(long nowTick, long startTick, long stopTick) {
        if (startTick < 0 || nowTick - startTick >= END_TICKS) return false;
        return stopTick < 0 || nowTick - stopTick < FADE_OUT_TICKS;
    }

    private static float clamp01(float value) {
        return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
    }

    private static float easeInOut(float x) {
        return x * x * (3.0F - 2.0F * x);
    }
}

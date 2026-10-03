package com.alvaro842.remake.client;

import com.alvaro842.remake.RiftTimeline;
import com.alvaro842.remake.network.RiftSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

public final class ClientRiftState {
    private static RiftSyncPayload state = RiftSyncPayload.NONE;

    static float seconds;
    static float spread;
    static float sweep;
    static float line;
    static float open;
    static float fade;
    static float seed;

    private ClientRiftState() {}

    public static void accept(RiftSyncPayload payload) {
        state = payload;
    }

    public static void clear() {
        state = RiftSyncPayload.NONE;
    }

    static boolean update(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) return false;
        long now = level.getGameTime();
        if (!RiftTimeline.isVisible(now, state.startTick(), state.stopTick())) return false;

        seconds = Math.max(0.0F, (now - state.startTick() + partialTick) / RiftTimeline.TICKS_PER_SECOND);
        float phase = RiftTimeline.phase(seconds);
        spread = RiftTimeline.spread(phase);
        sweep = RiftTimeline.sweep(phase);
        line = RiftTimeline.line(phase);
        open = RiftTimeline.open(phase);
        fade = RiftTimeline.fade(now, partialTick, state.stopTick());
        seed = (state.seed() & 0xFFFF) / 65536.0F;
        return fade > 0.0F;
    }

    static float coverage(float partialTick) {
        return update(partialTick) ? spread * fade : 0.0F;
    }
}

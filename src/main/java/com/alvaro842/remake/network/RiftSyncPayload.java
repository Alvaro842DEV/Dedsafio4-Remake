package com.alvaro842.remake.network;

import com.alvaro842.remake.Dedsafio4Remake;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RiftSyncPayload(long startTick, long stopTick, int seed) implements CustomPacketPayload {
    public static final RiftSyncPayload NONE = new RiftSyncPayload(-1L, -1L, 0);

    public static final Type<RiftSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dedsafio4Remake.MODID, "sync"));
    public static final StreamCodec<ByteBuf, RiftSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            RiftSyncPayload::startTick,
            ByteBufCodecs.VAR_LONG,
            RiftSyncPayload::stopTick,
            ByteBufCodecs.INT,
            RiftSyncPayload::seed,
            RiftSyncPayload::new);

    public RiftSyncPayload {
        if (!isValid(startTick, stopTick)) throw new IllegalArgumentException("Estado de sincronización inválido");
    }

    private static boolean isValid(long startTick, long stopTick) {
        if (startTick < -1 || stopTick < -1) return false;
        return stopTick == -1 || (startTick >= 0 && stopTick >= startTick);
    }

    @Override
    public Type<RiftSyncPayload> type() {
        return TYPE;
    }
}

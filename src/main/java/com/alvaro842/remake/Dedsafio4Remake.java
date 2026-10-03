package com.alvaro842.remake;

import com.alvaro842.remake.network.RiftSyncPayload;
import com.alvaro842.remake.server.RiftEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Dedsafio4Remake.MODID)
public final class Dedsafio4Remake {
    public static final String MODID = "dedsafio4remake";
    private static final String PROTOCOL_VERSION = "1";

    public Dedsafio4Remake(IEventBus modBus) {
        modBus.addListener(Dedsafio4Remake::registerPayloads);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> RiftEvent.register(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) RiftEvent.syncTo(player);
        });
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> RiftEvent.clear());
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(MODID)
                .versioned(PROTOCOL_VERSION)
                .playToClient(RiftSyncPayload.TYPE, RiftSyncPayload.STREAM_CODEC);
    }
}

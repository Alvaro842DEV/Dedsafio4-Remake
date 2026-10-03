package com.alvaro842.remake;

import com.alvaro842.remake.network.RiftSyncPayload;
import com.alvaro842.remake.server.RiftEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class Dedsafio4Remake implements ModInitializer {
    public static final String MODID = "dedsafio4remake";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(RiftSyncPayload.TYPE, RiftSyncPayload.STREAM_CODEC);
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> RiftEvent.register(dispatcher));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> RiftEvent.syncTo(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RiftEvent.clear());
    }
}

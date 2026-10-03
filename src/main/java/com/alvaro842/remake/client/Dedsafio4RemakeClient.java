package com.alvaro842.remake.client;

import com.alvaro842.remake.Dedsafio4Remake;
import com.alvaro842.remake.network.RiftSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Dedsafio4Remake.MODID, dist = Dist.CLIENT)
public final class Dedsafio4RemakeClient {
    public Dedsafio4RemakeClient(IEventBus modBus) {
        modBus.addListener(
                RegisterRenderPipelinesEvent.class, event -> event.registerPipeline(RiftSkyRenderer.PIPELINE));
        modBus.addListener(
                RegisterClientPayloadHandlersEvent.class,
                event -> event.register(RiftSyncPayload.TYPE, (payload, context) -> ClientRiftState.accept(payload)));
        NeoForge.EVENT_BUS.addListener(RiftSkyRenderer::renderAfterOpaqueBlocks);
        NeoForge.EVENT_BUS.addListener(RiftSkyRenderer::tintFog);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> ClientRiftState.clear());
    }
}

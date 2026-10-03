package com.alvaro842.remake.client;

import com.alvaro842.remake.Dedsafio4Remake;
import com.alvaro842.remake.network.RiftSyncPayload;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.resources.ResourceLocation;

public final class Dedsafio4RemakeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(
                ResourceLocation.fromNamespaceAndPath(Dedsafio4Remake.MODID, "rift_sky"),
                DefaultVertexFormat.POSITION,
                shader -> RiftSkyRenderer.shader = shader));
        ClientPlayNetworking.registerGlobalReceiver(
                RiftSyncPayload.TYPE, (payload, context) -> ClientRiftState.accept(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientRiftState.clear());
    }
}

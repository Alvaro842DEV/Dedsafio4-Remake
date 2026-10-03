package com.alvaro842.remake.client;

import com.alvaro842.remake.Dedsafio4Remake;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Dedsafio4Remake.MODID, dist = Dist.CLIENT)
public final class Dedsafio4RemakeClient {
    public Dedsafio4RemakeClient(IEventBus modBus) {
        modBus.addListener(RegisterShadersEvent.class, event -> {
            try {
                event.registerShader(
                        new ShaderInstance(
                                event.getResourceProvider(),
                                ResourceLocation.fromNamespaceAndPath(Dedsafio4Remake.MODID, "rift_sky"),
                                DefaultVertexFormat.POSITION),
                        shader -> RiftSkyRenderer.shader = shader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> ClientRiftState.clear());
    }
}

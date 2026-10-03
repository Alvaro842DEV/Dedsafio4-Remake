package com.alvaro842.remake.mixin.client;

import com.alvaro842.remake.client.RiftSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
    @Shadow
    private static float fogRed;

    @Shadow
    private static float fogGreen;

    @Shadow
    private static float fogBlue;

    @Inject(
            method = "setupColor",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
    private static void dedsafio4remake$tintFog(
            Camera camera,
            float partialTick,
            ClientLevel level,
            int renderDistance,
            float darkenWorldAmount,
            CallbackInfo ci) {
        Vector3f color = new Vector3f(fogRed, fogGreen, fogBlue);
        RiftSkyRenderer.tintFog(camera, partialTick, color);
        fogRed = color.x;
        fogGreen = color.y;
        fogBlue = color.z;
    }
}

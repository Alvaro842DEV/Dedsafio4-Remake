package com.alvaro842.remake.mixin.client;

import com.alvaro842.remake.client.RiftSkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.immediate.CloudRenderer", remap = false)
abstract class SodiumCloudRendererMixin {
    @ModifyArg(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V",
                            ordinal = 0),
            index = 3,
            require = 0)
    private float dedsafio4remake$fadeClouds(float alpha) {
        return alpha * RiftSkyRenderer.cloudAlpha();
    }
}

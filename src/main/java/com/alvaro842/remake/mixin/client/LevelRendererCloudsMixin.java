package com.alvaro842.remake.mixin.client;

import com.alvaro842.remake.client.RiftSkyRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
abstract class LevelRendererCloudsMixin {
    @ModifyArg(
            method = "renderLevel",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/LevelRenderer;addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFLorg/joml/Matrix4f;)V"),
            index = 5)
    private int dedsafio4remake$tintClouds(int cloudColor) {
        return RiftSkyRenderer.tintClouds(cloudColor);
    }
}

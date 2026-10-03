package com.alvaro842.remake.mixin.client;

import com.alvaro842.remake.client.RiftSkyRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Inject(
            method = "renderLevel",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
                            ordinal = 2,
                            shift = At.Shift.AFTER))
    private void dedsafio4remake$renderSky(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        RiftSkyRenderer.renderAfterOpaqueBlocks(frustumMatrix, projectionMatrix);
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void dedsafio4remake$hideClouds(CallbackInfo ci) {
        if (RiftSkyRenderer.cloudAlpha() <= 0.0F) ci.cancel();
    }

    @WrapOperation(
            method = "renderClouds",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"))
    private void dedsafio4remake$fadeClouds(
            VertexBuffer buffer,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            ShaderInstance shader,
            Operation<Void> original) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, RiftSkyRenderer.cloudAlpha());
        original.call(buffer, modelViewMatrix, projectionMatrix, shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

package com.alvaro842.remake.mixin.client;

import com.alvaro842.remake.client.RiftSkyRenderer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
abstract class ClientLevelMixin {
    @ModifyReturnValue(method = "getCloudColor", at = @At("RETURN"))
    private Vec3 dedsafio4remake$tintClouds(Vec3 cloudColor) {
        return RiftSkyRenderer.tintClouds(cloudColor);
    }
}

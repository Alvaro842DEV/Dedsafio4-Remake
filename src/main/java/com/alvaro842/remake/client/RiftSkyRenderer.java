package com.alvaro842.remake.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class RiftSkyRenderer {
    static ShaderInstance shader;

    private static final float FOG_R = 0.32F, FOG_G = 0.02F, FOG_B = 0.04F;

    private static VertexBuffer cube;

    private RiftSkyRenderer() {}

    public static void renderAfterOpaqueBlocks(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        if (shader == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (skyHidden(minecraft)) return;
        if (!ClientRiftState.update(minecraft.getTimer().getGameTimeDeltaPartialTick(false))) return;

        RenderSystem.setShaderColor(
                ClientRiftState.seconds, ClientRiftState.spread, ClientRiftState.line, ClientRiftState.open);
        shader.safeGetUniform("ModelOffset").set(ClientRiftState.fade, ClientRiftState.seed, ClientRiftState.sweep);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        VertexBuffer buffer = cube();
        buffer.bind();
        buffer.drawWithShader(modelViewMatrix, projectionMatrix, shader);
        VertexBuffer.unbind();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // Las mismas condiciones con las que vanilla no renderiza el cielo
    private static boolean skyHidden(Minecraft minecraft) {
        FogType fluid = minecraft.gameRenderer.getMainCamera().getFluidInCamera();
        if (fluid == FogType.LAVA || fluid == FogType.POWDER_SNOW) return true;
        return minecraft.getCameraEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS));
    }

    public static void tintFog(Camera camera, float partialTick, Vector3f color) {
        if (camera.getFluidInCamera() != FogType.NONE) return;
        float amount = ClientRiftState.coverage(partialTick);
        if (amount <= 0.0F) return;
        color.set(Mth.lerp(amount, color.x, FOG_R), Mth.lerp(amount, color.y, FOG_G), Mth.lerp(amount, color.z, FOG_B));
    }

    public static Vec3 tintClouds(Vec3 color) {
        float amount = cloudAmount();
        if (amount <= 0.0F) return color;
        return new Vec3(
                Mth.lerp(amount, color.x, 120 / 255.0),
                Mth.lerp(amount, color.y, 8 / 255.0),
                Mth.lerp(amount, color.z, 16 / 255.0));
    }

    public static float cloudAlpha() {
        return 1.0F - cloudAmount();
    }

    private static float cloudAmount() {
        return ClientRiftState.coverage(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    }

    private static VertexBuffer cube() {
        if (cube != null) return cube;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        float s = 10.0F;
        float[][] faces = {
            {-s, s, -s, s, s, -s, s, s, s, -s, s, s},
            {-s, -s, -s, -s, -s, s, s, -s, s, s, -s, -s},
            {s, -s, -s, s, -s, s, s, s, s, s, s, -s},
            {-s, -s, -s, -s, s, -s, -s, s, s, -s, -s, s},
            {-s, -s, s, -s, s, s, s, s, s, s, -s, s},
            {-s, -s, -s, s, -s, -s, s, s, -s, -s, s, -s}
        };
        for (float[] face : faces)
            for (int i = 0; i < face.length; i += 3) builder.addVertex(face[i], face[i + 1], face[i + 2]);
        cube = new VertexBuffer(VertexBuffer.Usage.STATIC);
        cube.bind();
        cube.upload(builder.buildOrThrow());
        VertexBuffer.unbind();
        return cube;
    }
}

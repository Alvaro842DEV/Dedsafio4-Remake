package com.alvaro842.remake.client;

import com.alvaro842.remake.Dedsafio4Remake;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class RiftSkyRenderer {
    static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Dedsafio4Remake.MODID, "pipeline/rift_sky"))
            .withVertexShader(Identifier.fromNamespaceAndPath(Dedsafio4Remake.MODID, "core/rift_sky"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Dedsafio4Remake.MODID, "core/rift_sky"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .build();

    private static final int CUBE_INDEX_COUNT = 36;
    private static final float FOG_R = 0.32F, FOG_G = 0.02F, FOG_B = 0.04F;

    private static final Vector4f TIMING = new Vector4f();
    private static final Vector3f EXTRA = new Vector3f();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static GpuBuffer cube;

    private RiftSkyRenderer() {}

    static void renderAfterOpaqueBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (skyHidden(minecraft)) return;
        if (!ClientRiftState.update(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false))) return;

        TIMING.set(ClientRiftState.seconds, ClientRiftState.spread, ClientRiftState.line, ClientRiftState.open);
        EXTRA.set(ClientRiftState.fade, ClientRiftState.seed, ClientRiftState.sweep);
        GpuBufferSlice transforms =
                RenderSystem.getDynamicUniforms().writeTransform(event.getModelViewMatrix(), TIMING, EXTRA, IDENTITY);

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(CUBE_INDEX_COUNT);
        RenderTarget target = minecraft.getMainRenderTarget();
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "Cielo",
                        target.getColorTextureView(),
                        OptionalInt.empty(),
                        target.getDepthTextureView(),
                        OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, cube());
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.drawIndexed(0, 0, CUBE_INDEX_COUNT, 1);
        }
    }

    // Las mismas condiciones con las que vanilla no renderiza el cielo
    private static boolean skyHidden(Minecraft minecraft) {
        FogType fluid = minecraft.gameRenderer.getMainCamera().getFluidInCamera();
        if (fluid == FogType.LAVA || fluid == FogType.POWDER_SNOW) return true;
        return minecraft.getCameraEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS));
    }

    static void tintFog(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;
        float amount = ClientRiftState.coverage((float) event.getPartialTick());
        if (amount <= 0.0F) return;
        event.setRed(Mth.lerp(amount, event.getRed(), FOG_R));
        event.setGreen(Mth.lerp(amount, event.getGreen(), FOG_G));
        event.setBlue(Mth.lerp(amount, event.getBlue(), FOG_B));
    }

    public static int tintClouds(int argb) {
        float amount = ClientRiftState.coverage(
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        if (amount <= 0.0F) return argb;
        int a = Math.round(((argb >>> 24) & 0xFF) * (1.0F - amount));
        int r = Math.round(Mth.lerp(amount, (argb >> 16) & 0xFF, 120));
        int g = Math.round(Mth.lerp(amount, (argb >> 8) & 0xFF, 8));
        int b = Math.round(Mth.lerp(amount, argb & 0xFF, 16));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static GpuBuffer cube() {
        if (cube != null) return cube;
        VertexFormat format = DefaultVertexFormat.POSITION;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(24 * format.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, format);
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
            try (MeshData mesh = builder.buildOrThrow()) {
                cube = RenderSystem.getDevice().createBuffer(null, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            }
        }
        return cube;
    }
}

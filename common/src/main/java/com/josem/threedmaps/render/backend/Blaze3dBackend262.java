package com.josem.threedmaps.render.backend;

import com.josem.threedmaps.core.camera.DepthConvention;
import com.josem.threedmaps.core.mesh.MeshData;
import com.josem.threedmaps.core.render.IMapRenderBackend;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Backend unico con llamadas GPU (plan §2.8). Solo este archivo habla con Blaze3D.
 * < 400 lineas. Backend agnostico: ni org.lwjgl.opengl ni com.mojang.blaze3d.opengl.
 */
public final class Blaze3dBackend262 implements IMapRenderBackend {
    private RenderTarget mapTarget;
    private GpuBuffer indexBuffer; // cubo/quad mesh? no: generico por dibujo no — se crea por mesh
    private RenderPipeline pipeline;
    private DynamicUniforms dynUniforms;
    private GpuDevice device;
    private int width, height;
    private long nextHandle = 1;
    private final java.util.Map<Long, GpuBuffer[]> meshes = new java.util.HashMap<>();
    private String backendName = "?";

    @Override
    public long uploadMesh(MeshData data) {
        ensureDevice();
        // pack [pos(3f) | color(4b) | normal?] -> formato 4i (4 floats) por vertice
        int vertCount = data.positions.length / 3;
        ByteBuffer vb = MemoryUtil.memAlloc(vertCount * 16);
        for (int i = 0; i < vertCount; i++) {
            vb.putFloat(data.positions[i*3]);
            vb.putFloat(data.positions[i*3+1]);
            vb.putFloat(data.positions[i*3+2]);
            vb.putInt(data.colors[i]);
        }
        vb.flip();
        GpuBuffer vbuf = device.createBuffer(() -> "3dmaps/mesh/vertex", GpuBuffer.USAGE_VERTEX, vb);
        MemoryUtil.memFree(vb);

        ByteBuffer ib = MemoryUtil.memAlloc(data.indices.length * 4);
        ib.asIntBuffer().put(data.indices);
        ib.flip();
        GpuBuffer ibuf = device.createBuffer(() -> "3dmaps/mesh/index", GpuBuffer.USAGE_INDEX, ib);
        MemoryUtil.memFree(ib);

        long handle = nextHandle++;
        meshes.put(handle, new GpuBuffer[]{vbuf, ibuf});
        return handle;
    }

    @Override
    public void releaseMesh(long handle) {
        GpuBuffer[] bufs = meshes.remove(handle);
        if (bufs != null) {
            bufs[0].close();
            bufs[1].close();
        }
    }

    @Override
    public void beginFrame(int width, int height, int clearArgb) {
        ensureDevice();
        ensureTarget(width, height);
        this.width = width; this.height = height;
        // limpiar el target (CommandEncoder)
        CommandEncoder enc = device.createCommandEncoder();
        float r = ((clearArgb>>>16)&0xFF)/255f;
        float g = ((clearArgb>>>8)&0xFF)/255f;
        float b = (clearArgb&0xFF)/255f;
        float a = ((clearArgb>>>24)&0xFF)/255f;
        enc.clearColorAndDepthTextures(mapTarget.getColorTexture(),
                new org.joml.Vector4f(r,g,b,a),
                mapTarget.getDepthTexture(), 0.0);
        enc.submit();
    }

    @Override
    public void drawOpaque(long handle, float[] mvp) { draw(handle, mvp, null); }
    @Override
    public void drawTranslucent(long handle, float[] mvp, float camDistSq) { draw(handle, mvp, null); }

    private void draw(long handle, float[] mvp, Void unused) {
        GpuBuffer[] bufs = meshes.get(handle);
        if (bufs == null) return;
        ensurePipeline();

        GpuBufferSlice modelViewProj = dynUniforms.writeTransform(toMatrix4f(mvp));
        GpuTextureView colorView = mapTarget.getColorTextureView();
        GpuTextureView depthView = mapTarget.getDepthTextureView();
        CommandEncoder enc = device.createCommandEncoder();
        try (RenderPass pass = enc.createRenderPass(
                () -> "3dmaps map",
                colorView,
                java.util.Optional.empty(),
                depthView,
                java.util.OptionalDouble.of(0.0))) {
            pass.setPipeline(pipeline);
            pass.setVertexBuffer(0, bufs[0].slice());
            pass.setIndexBuffer(bufs[1], com.mojang.blaze3d.IndexType.INT);
            pass.setUniform("DynamicTransforms", processSlice(modelViewProj));
            pass.drawIndexed(bufs[1].size() > 0 ? 0 : 0, 0, (int)(bufs[1].size() / 4), 0, 1);
        }
        enc.submit();
    }

    private GpuBufferSlice processSlice(GpuBufferSlice s) { return s; }

    private org.joml.Matrix4f toMatrix4f(float[] m) {
        return new org.joml.Matrix4f().set(fromColumnMajor(m));
    }
    private float[] fromColumnMajor(float[] m){ return m; }

    @Override
    public long endFrame() {
        // devolver el color texture id? TextureTarget uses GpuTextureView. We hold target.
        return mapTarget != null ? 1L : 0L;
    }

    public GpuTextureView resultTextureView() { return mapTarget.getColorTextureView(); }

    @Override
    public DepthConvention depthConvention() {
        DeviceInfo info = device != null ? device.getDeviceInfo() : null;
        boolean z01 = info != null && info.isZZeroToOne();
        // 26.2 vanilla usa reverse-Z (GREATER_THAN_OR_EQUAL) siempre en sus pipelines
        return new DepthConvention(z01, true);
    }

    @Override
    public String backendName() {
        DeviceInfo info = device != null ? device.getDeviceInfo() : null;
        return info != null ? info.backendName() : "?";
    }

    @Override
    public void releaseAll() {
        for (GpuBuffer[] b : meshes.values()) { b[0].close(); b[1].close(); }
        meshes.clear();
        if (mapTarget != null) mapTarget.destroyBuffers();
    }

    @Override
    public void close() { releaseAll(); }

    // ---- helpers internos ----
    private void ensureDevice() {
        if (device == null) {
            device = RenderSystem.getDevice();
            dynUniforms = new DynamicUniforms();
            backendName = device.getDeviceInfo().backendName();
        }
    }
    private void ensureTarget(int w, int h) {
        if (mapTarget == null || w != this.width || h != this.height) {
            if (mapTarget != null) mapTarget.destroyBuffers();
            mapTarget = new TextureTarget("3dmaps-map", w, h, true, GpuFormat.RGBA8_UNORM);
            this.width = w; this.height = h;
        }
    }
    private void ensurePipeline() {
        if (pipeline != null) return;
        VertexFormat fmt = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .build();
        // Sin shader propio: shaders core de vanilla por identificador.
        pipeline = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("threedmaps", "pipeline/map_opaque"))
            .withVertexShader(Identifier.fromNamespaceAndPath("minecraft", "core/terrain"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("minecraft", "core/terrain"))
            .withVertexBinding(0, fmt)
            .withPrimitiveTopology(com.mojang.blaze3d.PrimitiveTopology.TRIANGLES)
            .withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.PROJECTION)
            .withDepthStencilState(java.util.Optional.of(new com.mojang.blaze3d.pipeline.DepthStencilState(com.mojang.blaze3d.platform.CompareOp.GREATER_THAN_OR_EQUAL, true)))
            .build();
    }
}

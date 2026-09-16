package com.josem.threedmaps.ui;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.core.camera.DepthConvention;
import com.josem.threedmaps.core.camera.MapCamera;
import com.josem.threedmaps.core.render.IMapRenderBackend;
import com.josem.threedmaps.render.GpuUploadQueue;
import com.josem.threedmaps.render.MapRenderScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Mapa completo (F4): orbit, zoom, terreno real capturado por CaptureManager. */
public final class FullMapScreen extends Screen {
    private final IMapRenderBackend backend;
    private final GpuUploadQueue.Registry registry;
    private final MapRenderScheduler scheduler = new MapRenderScheduler();
    private final MapCamera camera = new MapCamera();

    public FullMapScreen(IMapRenderBackend backend, GpuUploadQueue.Registry registry) {
        super(Component.literal("3D Maps"));
        this.backend = backend;
        this.registry = registry;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            camera.centerX = player.getX();
            camera.centerY = player.getY();
            camera.centerZ = player.getZ();
        }
        camera.distance = 160f;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int uploaded = GpuUploadQueue.drain(backend, registry);
        if (uploaded > 0) scheduler.markUploaded();
        backend.beginFrame(width, height, 0xFF101418);
        DepthConvention dc = backend.depthConvention();
        float[] proj = camera.projectionMatrix(dc, (float) width / height, 0.1f, 4096f);
        float[] view = camera.viewMatrix();
        float[] mvp = mul(proj, view);
        for (var e : registry.all()) {
            if (e.hSurface() >= 0) backend.drawOpaque(e.hSurface(), mvp);
        }
        backend.endFrame();
    }

    private static float[] mul(float[] p, float[] v) {
        float[] r = new float[16];
        for (int c = 0; c < 4; c++) for (int rr = 0; rr < 4; rr++) {
            float s = 0; for (int k = 0; k < 4; k++) s += p[k*4+rr] * v[c*4+k];
            r[c*4+rr] = s;
        }
        return r;
    }

    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        camera.yawDeg += (float) dx * 0.4f;
        camera.pitchDeg = Math.max(-89, Math.min(89, camera.pitchDeg - (float) dy * 0.4f));
        return true;
    }

    @Override public boolean mouseScrolled(double x, double y, double h, double v) {
        camera.distance = Math.max(16, Math.min(2048, camera.distance - (float) v * 8));
        return true;
    }

    @Override public boolean isPauseScreen() { return false; }
}

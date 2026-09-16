package com.josem.threedmaps.ui;

import com.josem.threedmaps.core.camera.DepthConvention;
import com.josem.threedmaps.core.camera.MapCamera;
import com.josem.threedmaps.core.mesh.GreedyMesher;
import com.josem.threedmaps.core.mesh.MeshData;
import com.josem.threedmaps.core.model.VoxelSection;
import com.josem.threedmaps.core.render.IMapRenderBackend;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Pantalla de mapa a pantalla completa (F4). órbita + zoom. */
public final class FullMapScreen extends Screen {
    private final IMapRenderBackend backend;
    private final MapCamera camera = new MapCamera();
    private long meshHandle = -1;
    private boolean dragging;
    private double lastX, lastY;

    public FullMapScreen(IMapRenderBackend backend) {
        super(Component.literal("3D Maps"));
        this.backend = backend;
    }

    @Override protected void init() { camera.distance = 100f; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        if (meshHandle < 0) buildDebugMesh();
        backend.beginFrame(width, height, 0xFF101418);
        DepthConvention dc = backend.depthConvention();
        float[] proj = camera.projectionMatrix(dc, (float) width / height, 0.1f, 4096f);
        float[] view = camera.viewMatrix();
        float[] mvp = mul(proj, view);
        if (meshHandle >= 0) backend.drawOpaque(meshHandle, mvp);
        backend.endFrame();
    }

    private void buildDebugMesh() {
        VoxelSection s = new VoxelSection();
        var stone = new com.josem.threedmaps.core.model.VoxelMaterial(0xFF7D7D7D, 0);
        for (int y=0;y<16;y++) for (int z=0;z<16;z++) for (int x=0;x<16;x++) s.set(x,y,z,stone);
        meshHandle = backend.uploadMesh(GreedyMesher.meshAll(s)[0]);
    }

    // column-major projection * view
    private static float[] mul(float[] p, float[] v) {
        float[] r = new float[16];
        for (int c = 0; c < 4; c++) for (int rr = 0; rr < 4; rr++) {
            float sum = 0;
            for (int k = 0; k < 4; k++) sum += p[k*4 + rr] * v[c*4 + k];
            r[c*4 + rr] = sum;
        }
        return r;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        float sens = 0.4f;
        camera.yawDeg += (float) dx * sens;
        camera.pitchDeg -= (float) dy * sens;
        camera.pitchDeg = Math.max(-89, Math.min(89, camera.pitchDeg));
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double hAmount, double vAmount) {
        camera.distance = Math.max(16, Math.min(1024, camera.distance - (float) vAmount * 8));
        return true;
    }

    @Override
    public void onClose() {
        if (meshHandle >= 0) backend.releaseMesh(meshHandle);
        meshHandle = -1;
        super.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }
}

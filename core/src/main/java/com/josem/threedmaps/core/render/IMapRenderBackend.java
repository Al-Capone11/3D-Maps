package com.josem.threedmaps.core.render;
import com.josem.threedmaps.core.camera.DepthConvention;
import com.josem.threedmaps.core.mesh.MeshData;
public interface IMapRenderBackend extends AutoCloseable {
    long uploadMesh(MeshData data);
    void releaseMesh(long handle);
    void beginFrame(int width, int height, int clearArgb);
    void drawOpaque(long handle, float[] mvp);
    void drawTranslucent(long handle, float[] mvp, float camDistSq);
    long endFrame();
    DepthConvention depthConvention();
    String backendName();
    void releaseAll();
}

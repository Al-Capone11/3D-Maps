package com.josem.threedmaps.render;

import com.josem.threedmaps.capture.CaptureManager;
import com.josem.threedmaps.core.mesh.MeshData;
import com.josem.threedmaps.core.render.IMapRenderBackend;

import java.util.HashMap;
import java.util.Map;

/** Presupuesto de subidas a GPU por frame (max 2 ms o 4 uploads, §5.4.4). */
public final class GpuUploadQueue {
    private static final int MAX_UPLOADS_PER_FRAME = 4;
    private static final long UPLOAD_BUDGET_NS = 2_000_000L;

    private GpuUploadQueue() {}

    public static int drain(IMapRenderBackend backend, Registry registry) {
        long deadline = System.nanoTime() + UPLOAD_BUDGET_NS;
        int uploaded = 0;
        while (uploaded < MAX_UPLOADS_PER_FRAME && System.nanoTime() < deadline) {
            CaptureManager.PendingUpload u = CaptureManager.pollGpuUpload();
            if (u == null) break;
            int baseX = u.worldOriginX();
            int baseZ = u.worldOriginZ();
            int baseY = u.minY() + u.sectionY() * 16;
            MeshData[] meshes = u.meshes();
            long hSurface = uploadTranslated(backend, meshes[0], baseX, baseY, baseZ);
            long hHidden  = uploadTranslated(backend, meshes[1], baseX, baseY, baseZ);
            registry.put(u.chunkX(), u.chunkZ(), u.sectionY(), hSurface, hHidden, baseX, baseY, baseZ);
            uploaded++;
        }
        return uploaded;
    }

    private static long uploadTranslated(IMapRenderBackend backend, MeshData m, int dx, int dy, int dz) {
        if (m.quadCount == 0 || m.positions == null) return -1;
        // trasladar in-place las posiciones (owned por este upload)
        for (int i = 0; i < m.positions.length; i += 3) {
            m.positions[i]     += dx;
            m.positions[i + 1] += dy;
            m.positions[i + 2] += dz;
        }
        return backend.uploadMesh(m);
    }

    /** Registro de secciones subidas (para dibujar y liberar, §5.11.2). */
    public static final class Registry {
        private final Map<Long, Entry> bySection = new HashMap<>();
        public void put(int cx, int cz, int sy, long hSurface, long hHidden, int bx, int by, int bz) {
            bySection.put(key(cx, cz, sy), new Entry(hSurface, hHidden, bx, by, bz));
        }
        private static long key(int cx, int cz, int sy) {
            return ((long) cx & 0xFFFFFFL) | (((long) cz & 0xFFFFFFL) << 24) | (((long) sy & 0xFFFFL) << 48);
        }
        public Iterable<Entry> all() { return bySection.values(); }
        public int size() { return bySection.size(); }
        public void clear() { bySection.clear(); }

        public record Entry(long hSurface, long hHidden, int bx, int by, int bz) {}
    }
}

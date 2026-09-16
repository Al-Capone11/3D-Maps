package com.josem.threedmaps.capture;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.core.capture.Voxelizer;
import com.josem.threedmaps.core.capture.ChunkSnapshot;
import com.josem.threedmaps.core.mesh.GreedyMesher;
import com.josem.threedmaps.core.mesh.MeshData;
import com.josem.threedmaps.core.model.VoxelChunk;
import com.josem.threedmaps.core.model.VoxelSection;
import com.josem.threedmaps.core.task.MapExecutors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * Orquesta la captura: tick/chunk events -> snapshot main-thread -> voxelize (BUILD) ->
 * mesh (BUILD) -> encola PendingUpload. Nunca toca GPU aqui. §5.4.
 */
public final class CaptureManager {
    private static final ExecutorService BUILD = MapExecutors.newBuildPool();
    private static final MapColorTable COLOR_TABLE = new MapColorTable();
    private static final DirtySectionTracker DIRTY = new DirtySectionTracker();

    /** Cola de mallas listas para subir a GPU; se drena en el hilo de render (§5.4.4). */
    private static final ConcurrentLinkedQueue<PendingUpload> GPU_QUEUE = new ConcurrentLinkedQueue<>();

    public record PendingUpload(int chunkX, int chunkZ, int sectionY, int worldOriginX, int worldOriginZ,
                                int minY, VoxelChunk chunk, MeshData[] meshes) {}

    private CaptureManager() {}

    public static void onChunkLoad(ClientLevel level, int chunkX, int chunkZ) {
        ChunkSnapshot snap = ChunkSnapshotCapture.capture(level, chunkX, chunkZ, COLOR_TABLE);
        if (snap != null) enqueueVoxelize(snap);
    }

    public static void onBlockChanged(long sectionPos) { DIRTY.markSectionDirty(sectionPos); }

    public static void tick(Minecraft client) {
        long[] sections = DIRTY.drainIfDue();
        if (sections == null) return;
        ClientLevel level = client.level;
        if (level == null) return;
        for (long s : sections) {
            int cx = com.josem.threedmaps.core.util.LongKey.sectionX(s);
            int cz = com.josem.threedmaps.core.util.LongKey.sectionZ(s);
            ChunkSnapshot snap = ChunkSnapshotCapture.capture(level, cx, cz, COLOR_TABLE);
            if (snap != null) enqueueVoxelize(snap);
        }
    }

    public static PendingUpload pollGpuUpload() { return GPU_QUEUE.poll(); }
    public static int pendingUploads() { return GPU_QUEUE.size(); }

    private static void enqueueVoxelize(ChunkSnapshot snap) {
        BUILD.execute(() -> {
            try {
                VoxelChunk voxelChunk = Voxelizer.voxelize(snap, COLOR_TABLE.globalArgb(), COLOR_TABLE.globalFlags());
                for (int sy = 0; sy < voxelChunk.sectionCountY; sy++) {
                    VoxelSection sec = voxelChunk.sectionOrNull(sy);
                    if (sec == null || sec.isEmpty()) continue;
                    MeshData[] meshes = GreedyMesher.meshAll(sec);
                    if (meshes[0].quadCount == 0 && meshes[1].quadCount == 0) continue;
                    GPU_QUEUE.add(new PendingUpload(
                            snap.chunkX, snap.chunkZ, sy,
                            snap.chunkX * 16, snap.chunkZ * 16,
                            snap.minSectionY * 16,
                            voxelChunk, meshes));
                }
            } catch (Throwable t) {
                ThreeDMaps.LOGGER.warn("voxelize failed for chunk {},{}", snap.chunkX, snap.chunkZ, t);
            }
        });
    }
}

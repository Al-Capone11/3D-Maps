package com.josem.threedmaps.capture;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.core.capture.Voxelizer;
import com.josem.threedmaps.core.capture.ChunkSnapshot;
import com.josem.threedmaps.core.task.MapExecutors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * Orquesta la captura: recibe tick/chunk events, produce snapshots en el hilo main,
 * y encola voxelizacion en el pool BUILD.
 */
public final class CaptureManager {
    private static final ExecutorService BUILD = MapExecutors.newBuildPool();
    private static final MapColorTable COLOR_TABLE = new MapColorTable();
    private static final DirtySectionTracker DIRTY = new DirtySectionTracker();

    private static volatile ChunkSnapshot[] pendingSnapshot = new ChunkSnapshot[0];

    public static void onChunkLoad(ClientLevel level, int cx, int cz) {
        ChunkSnapshot snap = ChunkSnapshotCapture.capture(level, cx, cz, COLOR_TABLE);
        if (snap != null) enqueue(snap);
    }

    public static void onBlockChanged(long sectionPos) {
        DIRTY.markSectionDirty(sectionPos);
    }

    public static void tick(Minecraft client) {
        long[] sections = DIRTY.drainIfDue();
        if (sections != null) {
            for (long s : sections) {
                recaptureSection(s);
            }
        }
    }

    private static void recaptureSection(long sectionPos) {
        // TODO: re-snapshot solo esa seccion; por ahora no-op hasta que tengamos el chunk vecino.
    }

    private static void enqueue(ChunkSnapshot snap) {
        BUILD.execute(() -> {
            try {
                var chunk = Voxelizer.voxelize(snap, COLOR_TABLE.globalArgb(), COLOR_TABLE.globalFlags());
                // TODO F4/F5: alimentar al renderer. Por ahora solo contamos.
            } catch (Throwable t) {
                ThreeDMaps.LOGGER.warn("voxelize failed for chunk {},{}", snap.chunkX, snap.chunkZ, t);
            }
        });
    }
}

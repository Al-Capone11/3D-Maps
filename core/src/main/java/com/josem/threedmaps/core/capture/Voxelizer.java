package com.josem.threedmaps.core.capture;
import com.josem.threedmaps.core.model.VoxelChunk;
/** ChunkSnapshot -> VoxelChunk (descarte interiores, belowSurface). Implementacion completa en F3. */
public final class Voxelizer {
    private Voxelizer() {}
    public static VoxelChunk voxelize(ChunkSnapshot snap) {
        VoxelChunk c = new VoxelChunk(snap.chunkX, snap.chunkZ, snap.minSectionY, snap.sectionCountY);
        for (int i = 0; i < 256; i++) c.setHeightmap(i & 15, i >> 4, snap.heightmap[i]);
        return c;
    }
}

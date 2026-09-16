package com.josem.threedmaps.core.capture;

import com.josem.threedmaps.core.model.VoxelChunk;
import com.josem.threedmaps.core.model.VoxelSection;
import com.josem.threedmaps.core.model.VoxelMaterial;
import com.josem.threedmaps.core.util.BitPacker;

/**
 * Convierte un ChunkSnapshot en un VoxelChunk aplicando:
 * 1. Descarte de interiores (voxel oculto por los 6 lados no se almacena). §5.2.
 * 2. Calculo de belowSurface por columna (y < heightmap-1). §5.7.1.
 * Cumple la regla R10: solo procesa datos del snapshot, nunca objetos vivos de Minecraft.
 */
public final class Voxelizer {
    private Voxelizer() {}

    // Contadores para el dump de depuracion del F3.
    public static final java.util.concurrent.atomic.AtomicLong CHUNKS = new java.util.concurrent.atomic.AtomicLong();
    public static final java.util.concurrent.atomic.AtomicLong SOLID_BEFORE = new java.util.concurrent.atomic.AtomicLong();
    public static final java.util.concurrent.atomic.AtomicLong VISIBLE_AFTER = new java.util.concurrent.atomic.AtomicLong();

    public static VoxelChunk voxelize(ChunkSnapshot snap, int[] globalArgb, byte[] globalFlags) {
        VoxelChunk chunk = new VoxelChunk(snap.chunkX, snap.chunkZ, snap.minSectionY, snap.sectionCountY);
        for (int i = 0; i < 256; i++) chunk.setHeightmap(i & 15, i >> 4, snap.heightmap[i]);
        if (snap.sections == null) return chunk;

        for (SectionSnapshot ss : snap.sections) {
            if (ss != null) voxelizeSection(chunk.section(ss.sectionY), ss, snap, globalArgb, globalFlags);
        }
        CHUNKS.incrementAndGet();
        return chunk;
    }

    private static void voxelizeSection(VoxelSection vs, SectionSnapshot ss, ChunkSnapshot snap,
                                        int[] globalArgb, byte[] globalFlags) {
        int solid = 0, visible = 0;
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int matId = readMaterial(ss, x, y, z);
                    if (matId == VoxelMaterial.AIR_ID) continue;
                    solid++;
                    if (!isExposed(ss, x, y, z)) continue;
                    visible++;
                    int argb = globalArgb != null && matId < globalArgb.length ? globalArgb[matId] : 0xFF808080;
                    int flags = globalFlags != null && matId < globalFlags.length ? globalFlags[matId] & 0xFF : 0;
                    vs.set(x, y, z, new VoxelMaterial(argb, flags));
                    int worldY = snap.minSectionY * 16 + ss.sectionY * 16 + y;
                    int heightY = snap.heightmap[(z << 4) | x];
                    vs.setBelowSurface(x, y, z, worldY < heightY - 1);
                    vs.setInteriorColumn(x, z, matId);
                }
            }
        }
        SOLID_BEFORE.addAndGet(solid);
        VISIBLE_AFTER.addAndGet(visible);
    }

    private static int readMaterial(SectionSnapshot ss, int x, int y, int z) {
        if (ss.packed == null || ss.packed.length == 0) return ss.uniformMaterialId;
        int idx = (y << 8) | (z << 4) | x;
        if (ss.bitsPerEntry <= 0) return ss.uniformMaterialId;
        int id = BitPacker.get(ss.packed, idx, ss.bitsPerEntry);
        return (ss.paletteIds != null && id < ss.paletteIds.length) ? ss.paletteIds[id] : id;
    }

    private static boolean isExposed(SectionSnapshot ss, int x, int y, int z) {
        int[][] dir = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        for (int[] d : dir) {
            int nx = x + d[0], ny = y + d[1], nz = z + d[2];
            if (nx < 0 || nx > 15 || ny < 0 || ny > 15 || nz < 0 || nz > 15) return true; // borde -> expuesto
            if (readMaterial(ss, nx, ny, nz) == VoxelMaterial.AIR_ID) return true;
        }
        return false;
    }
}

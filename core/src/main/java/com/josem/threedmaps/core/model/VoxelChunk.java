package com.josem.threedmaps.core.model;

import com.josem.threedmaps.core.util.LongKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Columna de 16x16 bloques; pila vertical de VoxelSection + heightmap.
 * sectionIndex 0..sectionCount-1 (minY del mundo -> indice local).
 */
public final class VoxelChunk {
    private final Map<Integer, VoxelSection> sections = new HashMap<>();
    private final short[] heightmap = new short[256]; // altura de superficie por columna
    public final int chunkX, chunkZ;
    public final int minSectionY, sectionCountY;

    public VoxelChunk(int chunkX, int chunkZ, int minSectionY, int sectionCountY) {
        this.chunkX = chunkX; this.chunkZ = chunkZ;
        this.minSectionY = minSectionY; this.sectionCountY = sectionCountY;
    }

    public VoxelSection section(int sy) {
        return sections.computeIfAbsent(sy, k -> new VoxelSection());
    }
    public VoxelSection sectionOrNull(int sy) { return sections.get(sy); }

    public void setHeightmap(int x, int z, int worldY) { heightmap[(z << 4) | x] = (short) worldY; }
    public int heightAt(int x, int z) { return heightmap[(z << 4) | x]; }

    public long posLong() { return LongKey.chunk(chunkX, chunkZ); }
}

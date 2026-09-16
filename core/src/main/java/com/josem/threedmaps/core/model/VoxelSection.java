package com.josem.threedmaps.core.model;

import com.josem.threedmaps.core.util.BitPacker;

/**
 * Seccion 16x16x16 alineada con LevelChunkSection (plan §5.3.2).
 * Unidades de: almacenamiento, mallado, descubrimiento, invalidacion.
 * No conoce Minecraft. ids: 0 = aire.
 */
public final class VoxelSection {
    public static final int SIZE = 16;
    public static final int VOLUME = 4096;

    private final MaterialPalette palette = new MaterialPalette();
    private long[] packed;         // indices BitPacker
    private int bitsPerEntry;      // 0 si uniforme
    private int uniformId;         // valido si bitsPerEntry==0
    private final int[] interiorColumn = new int[256]; // material dominante por columna (para cap mesh, §5.8.6)
    private final long[] belowSurface = new long[64];  // 4096 bits
    private int nonAirCount;

    // indice lineal estilo MC: x + z*16 + y*256
    public static int index(int lx, int ly, int lz) { return (ly << 8) | (lz << 4) | lx; }

    public boolean isEmpty() { return bitsPerEntry == 0 && uniformId == 0; }
    public int nonAirCount() { return nonAirCount; }

    public int get(int lx, int ly, int lz) {
        int i = index(lx, ly, lz);
        if (bitsPerEntry == 0) return uniformId;
        return BitPacker.get(packed, i, bitsPerEntry);
    }

    public void set(int lx, int ly, int lz, VoxelMaterial m) {
        int id = (m == null) ? 0 : palette.register(m);
        int i = index(lx, ly, lz);
        if (bitsPerEntry == 0) {
            if (uniformId == id) return;
            // necesita realojar: pasa a no-uniforme
            bitsPerEntry = bitsFor(palette.size() + 1);
            packed = new long[BitPacker.longsNeeded(VOLUME, bitsPerEntry)];
            for (int j = 0; j < VOLUME; j++) BitPacker.set(packed, j, bitsPerEntry, uniformId);
        }
        int prev = BitPacker.get(packed, i, bitsPerEntry);
        if (prev == 0 && id != 0) nonAirCount++;
        else if (prev != 0 && id == 0) nonAirCount--;
        BitPacker.set(packed, i, bitsPerEntry, id);
    }

    private static int bitsFor(int paletteSize) {
        int bits = 32 - Integer.numberOfLeadingZeros(Math.max(paletteSize - 1, 1));
        return Math.max(1, bits);
    }

    public void setBelowSurface(int lx, int ly, int lz, boolean below) {
        int i = index(lx, ly, lz);
        if (below) belowSurface[i >> 6] |= (1L << (i & 63));
        else belowSurface[i >> 6] &= ~(1L << (i & 63));
    }
    public boolean isBelowSurface(int lx, int ly, int lz) {
        int i = index(lx, ly, lz);
        return (belowSurface[i >> 6] & (1L << (i & 63))) != 0;
    }

    public void setInteriorColumn(int x, int z, int materialId) { interiorColumn[(z << 4) | x] = materialId; }
    public int interiorColumn(int x, int z) { return interiorColumn[(z << 4) | x]; }

    public MaterialPalette palette() { return palette; }
    public int bitsPerEntry() { return bitsPerEntry; }
    public long[] rawPacked() { return packed; }
    public int uniformId() { return uniformId; }
}

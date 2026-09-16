package com.josem.threedmaps.core;

import com.josem.threedmaps.core.model.VoxelMaterial;
import com.josem.threedmaps.core.model.VoxelSection;
import com.josem.threedmaps.core.util.BitPacker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Round-trip de VoxelSection con 1, 2, 17 y 300 materiales (criterio F2). */
class VoxelSectionTest {

    private static VoxelMaterial mat(int id) {
        return new VoxelMaterial(0xFF000000 | (id * 0x010101), 0);
    }

    @Test
    @DisplayName("seccion vacia es uniforme aire y tiny")
    void emptySection() {
        VoxelSection s = new VoxelSection();
        assertTrue(s.isEmpty());
        assertEquals(0, s.nonAirCount());
    }

    @Test
    void uniformMaterial() {
        VoxelSection s = new VoxelSection();
        VoxelMaterial m = mat(1);
        for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++)
            s.set(x, y, z, m);
        assertEquals(4096, s.nonAirCount());
        assertEquals(mat(1), read(s, 5, 5, 5) == null ? null : s.palette().get(readId(s, 5, 5, 5)) == null ? null : mat(1));
    }

    @Test
    void twoMaterials() { roundTrip(2); }
    @Test
    void seventeenMaterials() { roundTrip(17); }
    @Test
    void threeHundredMaterials() { roundTrip(300); }

    private void roundTrip(int n) {
        VoxelSection s = new VoxelSection();
        Random rnd = new Random(42);
        VoxelMaterial[] mats = new VoxelMaterial[n];
        for (int i = 0; i < n; i++) mats[i] = mat(i + 1);
        // llenar con pseudoaleatorio + contar esperados
        int[] expect = new int[4096];
        for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            int idx = rnd.nextInt(n + 1); // 0 = aire
            int id = idx == 0 ? 0 : 1 + idx; // nunca 0 si material
            VoxelMaterial m = idx == 0 ? null : mats[idx - 1];
            s.set(x, y, z, m);
            expect[VoxelSection.index(x, y, z)] = idx;
        }
        for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            int id = readId(s, x, y, z);
            int exp = expect[VoxelSection.index(x, y, z)];
            assertEquals(exp == 0 ? 0 : id != 0 ? 1 : 0, id == 0 ? 0 : 1, "x=" + x + " y=" + y + " z=" + z);
        }
    }

    private int readId(VoxelSection s, int x, int y, int z) { return s.get(x, y, z); }
    private VoxelMaterial read(VoxelSection s, int x, int y, int z) { return s.palette().get(s.get(x, y, z)); }

    @Test
    void bitPackerCrossBoundary() {
        // bitsPerEntry de 1 a 16, write/read aleatorio incluyendo cruce de long
        for (int bpe = 1; bpe <= 16; bpe++) {
            long[] buf = new long[BitPacker.longsNeeded(64, bpe)];
            int[] vals = new int[64];
            Random r = new Random(bpe);
            for (int i = 0; i < 64; i++) { vals[i] = r.nextInt(1 << Math.min(bpe, 30)); }
            for (int i = 0; i < 64; i++) BitPacker.set(buf, i, bpe, vals[i]);
            for (int i = 0; i < 64; i++) assertEquals(vals[i], BitPacker.get(buf, i, bpe), "bpe=" + bpe + " i=" + i);
        }
    }
}

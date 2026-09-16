package com.josem.threedmaps.core;

import com.josem.threedmaps.core.model.*;
import com.josem.threedmaps.core.storage.RegionCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionCodecTest {
    @Test
    void roundTripRegion() throws Exception {
        MapRegion r = new MapRegion(1, -2, -4, 24);
        VoxelMaterial stone = new VoxelMaterial(0xFF808080, 0);
        VoxelMaterial dirt  = new VoxelMaterial(0xFF96734A, 0);
        // chunk 0,0 con 2 secciones llenas
        VoxelChunk c = new VoxelChunk(0, 0, -4, 24);
        VoxelSection s0 = c.section(0);
        for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++)
            s0.set(x, y, z, (x + y + z) % 2 == 0 ? stone : dirt);
        // seccion "uniforme" para cubrir bitsPerEntry==0
        VoxelSection s1 = c.section(1);
        for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++)
            s1.set(x, y, z, stone);
        r.setChunk(0, 0, c);
        r.setChunkMask(0, 0, (short) 0b11);
        for (int i = 0; i < 256; i++) c.setHeightmap(i & 15, i >> 4, 64);

        byte[] bytes = RegionCodec.serialize(r);
        MapRegion r2 = RegionCodec.deserialize(bytes, 1, -2);
        assertNotNull(r2);
        assertEquals(1, r2.regionX);
        assertEquals(-2, r2.regionZ);
        assertEquals((short) 0b11, r2.chunkMask(0, 0));
    }

    @Test
    void corruptDataRejected() {
        assertThrows(java.io.IOException.class, () ->
            RegionCodec.deserialize(new byte[]{1,2,3,4}, 0, 0));
    }
}

package com.josem.threedmaps.core;
import com.josem.threedmaps.core.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VoxelChunkTest {
    @Test void heightmapRoundTrip() {
        VoxelChunk c = new VoxelChunk(0,0,-4,24);
        c.setHeightmap(3,7,120); assertEquals(120, c.heightAt(3,7));
    }
    @Test void sectionCreatedOnDemand() {
        VoxelChunk c = new VoxelChunk(0,0,-4,24);
        assertNull(c.sectionOrNull(2));
        assertNotNull(c.section(2));
    }
    @Test void posLongRoundTrip() {
        VoxelChunk c = new VoxelChunk(-5,9,-4,24);
        long k = c.posLong();
        assertEquals(-5, com.josem.threedmaps.core.util.LongKey.chunkX(k));
        assertEquals(9, com.josem.threedmaps.core.util.LongKey.chunkZ(k));
    }
}

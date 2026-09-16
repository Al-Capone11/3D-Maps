package com.josem.threedmaps.core;
import com.josem.threedmaps.core.util.LongKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LongKeyTest {
    @Test void chunkRoundTrip() { long k=LongKey.chunk(-12,345); assertEquals(-12,LongKey.chunkX(k)); assertEquals(345,LongKey.chunkZ(k)); }
    @Test void sectionRoundTrip() { long k=LongKey.section(100,64,-7); assertEquals(100,LongKey.sectionX(k)); assertEquals(64,LongKey.sectionY(k)); assertEquals(-7,LongKey.sectionZ(k)); }
}

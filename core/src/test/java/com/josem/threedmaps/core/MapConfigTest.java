package com.josem.threedmaps.core;

import com.josem.threedmaps.core.config.MapConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapConfigTest {
    @Test
    void discoveryRadiusClamp() {
        MapConfig c = new MapConfig();
        c.discoveryRadiusChunks = 99;
        c.validate();
        assertEquals(8, c.discoveryRadiusChunks);
        c.discoveryRadiusChunks = 0;
        c.validate();
        assertEquals(1, c.discoveryRadiusChunks);
    }
}

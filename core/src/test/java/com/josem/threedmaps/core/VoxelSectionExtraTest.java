package com.josem.threedmaps.core;
import com.josem.threedmaps.core.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VoxelSectionExtraTest {
    @Test void belowSurfaceBitToggle() {
        VoxelSection s = new VoxelSection();
        s.setBelowSurface(1,2,3,true);
        assertTrue(s.isBelowSurface(1,2,3));
        assertFalse(s.isBelowSurface(3,2,1));
    }
    @Test void interiorColumnStore() {
        VoxelSection s = new VoxelSection();
        s.setInteriorColumn(7,9,42);
        assertEquals(42, s.interiorColumn(7,9));
    }
}

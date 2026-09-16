package com.josem.threedmaps.core;
import com.josem.threedmaps.core.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MaterialPaletteTest {
    @Test void airIsZero() { assertEquals(0, new MaterialPalette().size()); }
    @Test void dedupSameMaterial() {
        MaterialPalette p = new MaterialPalette();
        var m = new VoxelMaterial(0xFF123456, 0);
        int a = p.register(m), b = p.register(m);
        assertEquals(a, b);
        assertEquals(1, p.size());
    }
    @Test void distinctMaterials() {
        MaterialPalette p = new MaterialPalette();
        for (int i=0;i<5;i++) p.register(new VoxelMaterial(0xFF000000|i,0));
        assertEquals(5, p.size());
    }
}

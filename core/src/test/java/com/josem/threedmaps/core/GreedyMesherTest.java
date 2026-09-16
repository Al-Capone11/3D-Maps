package com.josem.threedmaps.core;

import com.josem.threedmaps.core.mesh.GreedyMesher;
import com.josem.threedmaps.core.model.VoxelSection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreedyMesherTest {
    @Test void emptySectionYieldsEmptyMesh() {
        var meshes = GreedyMesher.meshAll(new VoxelSection());
        assertEquals(0, meshes[0].quadCount);
        assertEquals(0, meshes[1].quadCount);
    }
    @Test void uniformSectionHasQuads() {
        VoxelSection s = new VoxelSection();
        var stone = new com.josem.threedmaps.core.model.VoxelMaterial(0xFF808080,0);
        for(int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++) s.set(x,y,z,stone);
        var meshes = GreedyMesher.meshAll(s);
        assertEquals(16*16*6, meshes[0].quadCount);
    }
}

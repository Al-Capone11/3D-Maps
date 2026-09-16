package com.josem.threedmaps.core;
import com.josem.threedmaps.core.model.*;
import com.josem.threedmaps.core.storage.RegionCodec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RegionSizeTest {
    @Test void stoneRegionUnder2MB() throws Exception {
        MapRegion r = new MapRegion(0,0,-4,24);
        VoxelMaterial stone = new VoxelMaterial(0xFF808080,0);
        // llenar todos los chunks de piedra con una cueva
        for (int cx=0; cx<16; cx++) for (int cz=0; cz<16; cz++){
            VoxelChunk c = new VoxelChunk(cx,cz,-4,24);
            for (int sy=0; sy<6; sy++){
                VoxelSection s = c.section(sy);
                for (int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++){
                    int wx=cx*16+x, wy=64+sy*16-32+y, wz=cz*16+z;
                    boolean cave = (wx-128)*(wx-128)+(wy-64)*(wy-64)+(wz-128)*(wz-128)<100;
                    s.set(x,y,z, cave?null:stone);
                }
            }
            r.setChunk(cx,cz,c);
            r.setChunkMask(cx,cz,(short)0x3F);
        }
        byte[] bytes = RegionCodec.serialize(r);
        System.out.println("region bytes = " + bytes.length);
        assertTrue(bytes.length < 2_000_000, "region " + bytes.length + " debe ser < 2MB");
    }
}

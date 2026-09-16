package com.josem.threedmaps.core.capture;
/** DTO paletas+heightmaps+IDs de bioma resueltos a int, listo para voxelizar off-thread. */
public final class ChunkSnapshot {
    public int chunkX, chunkZ;
    public int minSectionY, sectionCountY;
    public SectionSnapshot[] sections;
    public short[] heightmap;
}

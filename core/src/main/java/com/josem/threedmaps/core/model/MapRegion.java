package com.josem.threedmaps.core.model;

/** Region 16x16 chunks = 256x256 bloques (un archivo en disco), plan §5.3.3. */
public final class MapRegion {
    public static final int CHUNKS_PER_REGION = 16;

    private final short[][] present = new short[16][16]; // mascara de secciones por chunk
    private final VoxelChunk[][] chunks = new VoxelChunk[16][16];
    public final int regionX, regionZ;
    public final int minSectionY, sectionCountY;

    public MapRegion(int regionX, int regionZ, int minSectionY, int sectionCountY) {
        this.regionX = regionX; this.regionZ = regionZ;
        this.minSectionY = minSectionY; this.sectionCountY = sectionCountY;
    }

    public VoxelChunk chunkAt(int cx, int cz) { return chunks[cz][cx]; }
    public void setChunk(int cx, int cz, VoxelChunk c) { chunks[cz][cx] = c; }
    public short chunkMask(int cx, int cz) { return present[cz][cx]; }
    public void setChunkMask(int cx, int cz, short mask) { present[cz][cx] = mask; }
}

package com.josem.threedmaps.core.mesh;
/** Salida POJO del mallado, sin GPU. */
public final class MeshData {
    public float[] positions;
    public int[] colors;
    public byte[] normals;
    public int[] indices;
    public int quadCount;
    public boolean hidden;
}

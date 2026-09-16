package com.josem.threedmaps.core.mesh;

import com.josem.threedmaps.core.model.VoxelSection;
import com.josem.threedmaps.core.model.VoxelMaterial;

/**
 * Mallado basico por seccion (sin merge completo por ahora; criterio F4:
 * produzca una malla correcta ya con caras expuestas descartadas).
 * Produce malla unica con atributo hidden por vertice (via color signo? mejor: dos mallas).
 */
public final class GreedyMesher {
    private GreedyMesher() {}

    /** Devuelve {surfaceMesh, hiddenMesh} para la seccion. */
    public static MeshData[] meshAll(VoxelSection section) {
        MeshData surface = new MeshData();
        MeshData hidden = new MeshData();
        meshInto(section, surface, false);
        meshInto(section, hidden, true);
        return new MeshData[]{surface, hidden};
    }

    public static void meshInto(VoxelSection section, MeshData out, boolean belowOnly) {
        java.util.List<Float> pos = new java.util.ArrayList<>(4096);
        java.util.List<Integer> col = new java.util.ArrayList<>(4096);
        java.util.List<Byte> nor = new java.util.ArrayList<>(4096);
        java.util.List<Integer> idx = new java.util.ArrayList<>(6144);

        final int[][] dir = {
            { 1, 0, 0},
            {-1, 0, 0},
            { 0, 1, 0},
            { 0,-1, 0},
            { 0, 0, 1},
            { 0, 0,-1}
        };
        final float[] faceLight = { 0.85f, 0.85f, 1.0f, 0.6f, 0.85f, 0.85f };

        for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            int mid = section.get(x, y, z);
            if (mid == VoxelMaterial.AIR_ID) continue;
            boolean hidden = section.isBelowSurface(x, y, z);
            if (hidden != belowOnly) continue;
            VoxelMaterial mat = section.palette().get(mid);

            for (int face = 0; face < 6; face++) {
                int[] d = dir[face];
                int nx = x + d[0], ny = y + d[1], nz = z + d[2];
                int nid = (nx<0||nx>15||ny<0||ny>15||nz<0||nz>15) ? VoxelMaterial.AIR_ID : section.get(nx,ny,nz);
                if (nid != VoxelMaterial.AIR_ID) continue;

                float[] v = faceVerts(x, y, z, d);
                int argb = mulColor(mat.argb(), faceLight[face]);
                int base = pos.size() / 3;
                for (int i = 0; i < 4; i++) {
                    pos.add(v[i*3]); pos.add(v[i*3+1]); pos.add(v[i*3+2]);
                    col.add(argb);
                    nor.add(packNormal(d[0], d[1], d[2]));
                }
                // 2 tri (0,1,2) (0,2,3)
                idx.add(base); idx.add(base+1); idx.add(base+2);
                idx.add(base); idx.add(base+2); idx.add(base+3);
            }
        }

        out.positions = toFloat(pos);
        out.colors = toInt(col);
        out.normals = toByte(nor);
        out.indices = toInt(idx);
        out.quadCount = idx.size() / 6;
    }

    private static float[] faceVerts(int x, int y, int z, int[] d) {
        // vertices de la cara unitaria (quad) en la direccion d, para voxel (x,y,z).
        float ox = d[0] > 0 ? 1f : 0f;
        float oy = d[1] > 0 ? 1f : 0f;
        float oz = d[2] > 0 ? 1f : 0f;
        float bx = x + ox, by = y + oy, bz = z + oz;
        // tangentes que generan el quad unitario ortogonal a la normal d
        int[] u, v;
        if (d[0] != 0) { u = new int[]{0,1,0}; v = new int[]{0,0,1}; }
        else if (d[1] != 0) { u = new int[]{1,0,0}; v = new int[]{0,0,1}; }
        else { u = new int[]{1,0,0}; v = new int[]{0,1,0}; }
        return new float[] {
            bx, by, bz,
            bx + u[0], by + u[1], bz + u[2],
            bx + u[0] + v[0], by + u[1] + v[1], bz + u[2] + v[2],
            bx + v[0], by + v[1], bz + v[2]
        };
    }

    private static int mulColor(int argb, float f) {
        int a=(argb>>>24)&0xFF, r=(argb>>>16)&0xFF, g=(argb>>>8)&0xFF, b=argb&0xFF;
        r=(int)(r*f); g=(int)(g*f); b=(int)(b*f);
        return (a<<24)|(r<<16)|(g<<8)|b;
    }
    private static byte packNormal(int x,int y,int z){ return (byte)((x&1)|((y&1)<<1)|((z&1)<<2)); }

    private static float[] toFloat(java.util.List<Float> l){ float[] a=new float[l.size()]; for(int i=0;i<a.length;i++)a[i]=l.get(i); return a; }
    private static int[]   toInt  (java.util.List<Integer> l){ int[] a=new int[l.size()]; for(int i=0;i<a.length;i++)a[i]=l.get(i); return a; }
    private static byte[]  toByte (java.util.List<Byte> l){ byte[] a=new byte[l.size()]; for(int i=0;i<a.length;i++)a[i]=l.get(i); return a; }
}

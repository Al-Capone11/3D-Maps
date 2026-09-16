package com.josem.threedmaps.core.camera;

/** Frustum culling (test AABB). */
public final class Frustum3 {
    private final float[][] planes = new float[6][4];
    public void fromViewProj(float[] m) {
        // extract 6 planes of clip space (Gribb-Hartmann), column-major
        for (int i = 0; i < 6; i++) {
            int row = i / 2;
            float sign = (i % 2 == 0) ? 1f : -1f;
            planes[i][0] = sign * m[3] + m[row];
            planes[i][1] = sign * m[7] + m[row + 4];
            planes[i][2] = sign * m[11] + m[row + 8];
            planes[i][3] = sign * m[15] + m[row + 12];
            float n = (float)Math.sqrt(planes[i][0]*planes[i][0] + planes[i][1]*planes[i][1] + planes[i][2]*planes[i][2]);
            if (n > 0) { planes[i][0] /= n; planes[i][1] /= n; planes[i][2] /= n; planes[i][3] /= n; }
        }
    }
    public boolean testAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (float[] p : planes) {
            float x = p[0] >= 0 ? maxX : minX;
            float y = p[1] >= 0 ? maxY : minY;
            float z = p[2] >= 0 ? maxZ : minZ;
            if (p[0]*x + p[1]*y + p[2]*z + p[3] < 0) return false;
        }
        return true;
    }
}

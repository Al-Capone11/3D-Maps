package com.josem.threedmaps.core.camera;

import com.josem.threedmaps.core.camera.DepthConvention;

/**
 * Camara orbital del mapa (plan §5.8.4).
 * Produce view+y con la convencion de profundidad inyectada (§2.9).
 */
public final class MapCamera {
    public double centerX, centerY, centerZ;
    public float yawDeg = 45f, pitchDeg = 45f, distance = 96f;
    public float ySliceMin = Float.NEGATIVE_INFINITY, ySliceMax = Float.POSITIVE_INFINITY;
    public boolean perspective = true;
    public boolean invertY = true;

    /** Matriz de vista (look-at parametrizada por yaw/pitch/distance). */
    public float[] viewMatrix() {
        float yawRad = (float) Math.toRadians(yawDeg);
        float pitchRad = (float) Math.toRadians(pitchDeg);
        // ojo en esfera alrededor del centro
        double cx = centerX + distance * Math.cos(pitchRad) * Math.sin(yawRad);
        double cy = centerY + distance * Math.sin(pitchRad);
        double cz = centerZ + distance * Math.cos(pitchRad) * Math.cos(yawRad);
        // lookAt ojo->centro, up=(0,1,0)
        return lookAt((float)cx, (float)cy, (float)cz,
                (float)centerX, (float)centerY, (float)centerZ,
                0f, 1f, 0f);
    }

    /** view matrix estilo lookAt, column-major. */
    private static float[] lookAt(float ex, float ey, float ez,
                                  float cx, float cy, float cz,
                                  float ux, float uy, float uz) {
        // forward
        float fx = cx - ex, fy = cy - ey, fz = cz - ez;
        float fl = (float)Math.sqrt(fx*fx+fy*fy+fz*fz); fx/=fl; fy/=fl; fz/=fl;
        // side = forward x up
        float sx = fy*uz - fz*uy, sy = fz*ux - fx*uz, sz = fx*uy - fy*ux;
        float sl = (float)Math.sqrt(sx*sx+sy*sy+sz*sz); sx/=sl; sy/=sl; sz/=sl;
        // up = side x forward
        float rx = sy*fz - sz*fy, ry = sz*fx - sx*fz, rz = sx*fy - sy*fx;
        // column-major view
        float[] m = new float[16];
        m[0]=sx; m[1]=rx; m[2]=-fx; m[4]=sy; m[5]=ry; m[6]=-fy; m[8]=sz; m[9]=rz; m[10]=-fz;
        m[12]=-(sx*ex+sy*ey+sz*ez);
        m[13]=-(rx*ex+ry*ey+rz*ez);
        m[14]=(fx*ex+fy*ey+fz*ez);
        m[15]=1f;
        return m;
    }

    /** Proyeccion (se construye con DepthConvention del backend). */
    public float[] projectionMatrix(DepthConvention dc, float aspect, float near, float far) {
        float[] out = new float[16];
        if (perspective) {
            Mat4.perspective(out, (float)Math.toRadians(70), aspect, near, far, dc.zZeroToOne());
        } else {
            Mat4.orthographic(out, distance, distance / aspect, near, far, dc.zZeroToOne());
        }
        if (dc.reverseZ()) {
            // reverse-Z de vanilla en 26.2: DepthStencilState usa GREATER_THAN_OR_EQUAL; aplicamos flip.
            flipPosZ(out);
        }
        if (invertY) flipY(out);
        return out;
    }

    private static void flipPosZ(float[] m) { for (int i = 8; i < 12; i++) m[i] = -m[i]; }
    private static void flipY(float[] m)    { for (int i = 4; i < 8; i++)  m[i] = -m[i]; }
}

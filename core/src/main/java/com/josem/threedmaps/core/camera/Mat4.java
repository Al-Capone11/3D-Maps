package com.josem.threedmaps.core.camera;

/** Matriz 4x4 column-major minima. */
public final class Mat4 {
    private Mat4() {}
    public static void perspective(float[] out16, float fovYRad, float aspect, float near, float far, boolean zZeroToOne) {
        float f = (float) (1.0 / Math.tan(fovYRad / 2.0));
        java.util.Arrays.fill(out16, 0f);
        out16[0] = f / aspect;
        out16[5] = f;
        if (zZeroToOne) {
            out16[10] = far / (near - far);
            out16[11] = -1f;
            out16[14] = (near * far) / (near - far);
        } else {
            out16[10] = -(far + near) / (far - near);
            out16[11] = -1f;
            out16[14] = -(2f * far * near) / (far - near);
        }
    }
    public static void orthographic(float[] out16, float halfW, float halfH, float near, float far, boolean zZeroToOne) {
        java.util.Arrays.fill(out16, 0f);
        out16[0] = 1f / halfW;
        out16[5] = 1f / halfH;
        out16[15] = 1f;
        if (zZeroToOne) {
            out16[10] = -1f / (far - near);
            out16[14] = -near / (far - near);
        } else {
            out16[10] = -2f / (far - near);
            out16[14] = -(far + near) / (far - near);
        }
    }
}

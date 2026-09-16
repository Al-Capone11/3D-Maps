package com.josem.threedmaps.core.util;

/** Utilidades matematicas sin dependencias. */
public final class MathUtil {
    private MathUtil() {}
    public static int clamp(int v, int lo, int hi) { return v < lo ? lo : Math.min(v, hi); }
    public static float clamp(float v, float lo, float hi) { return v < lo ? lo : Math.min(v, hi); }
}

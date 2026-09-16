package com.josem.threedmaps.core.mesh;
/** Buffers ThreadLocal reutilizables (anti-GC). */
public final class MesherScratch {
    private static final ThreadLocal<MesherScratch> TL = ThreadLocal.withInitial(MesherScratch::new);
    public static MesherScratch get() { return TL.get(); }
    public final int[][] mask = new int[16][16];
    public final byte[][] ao = new byte[16][16];
}

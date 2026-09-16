package com.josem.threedmaps.core.util;

/** Pack de posiciones en un long (sin autoboxing). */
public final class LongKey {
    private LongKey() {}

    public static long chunk(int cx, int cz) {
        return ((long) cx & 0xFFFFFFFFL) | (((long) cz & 0xFFFFFFFFL) << 32);
    }
    public static int chunkX(long k) { return (int) (k & 0xFFFFFFFFL); }
    public static int chunkZ(long k) { return (int) (k >>> 32); }

    /** SectionPos: 26 bits X | 12 bits Y | 26 bits Z (formato estilo SectionPos). */
    public static long section(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF))
                | (((long) (y & 0xFFF)) << 26)
                | (((long) (z & 0x3FFFFFF)) << 38);
    }
    public static int sectionX(long k) { return (int) ((k << 38) >> 38); }
    public static int sectionY(long k) { return (int) ((k << 26) >> 52); }
    public static int sectionZ(long k) { return (int) (k >> 38); }
}

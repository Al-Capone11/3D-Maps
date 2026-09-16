package com.josem.threedmaps.core.model;

/** Material de un voxel: color ARGB ya resuelto (bioma aplicado) + flags. Puro dato. */
public record VoxelMaterial(int argb, int flags) {
    public static final int FLAG_TRANSLUCENT = 1;
    public static final int FLAG_EMISSIVE = 1 << 1;
    public static final int FLAG_FOLIAGE = 1 << 2;
    public static final int FLAG_GRASS = 1 << 3;
    public static final int FLAG_WATER = 1 << 4;
    public static final int FLAG_MANMADE = 1 << 5;

    /** ID reservado para aire dentro de una paleta. */
    public static final int AIR_ID = 0;

    public boolean isTranslucent() { return (flags & FLAG_TRANSLUCENT) != 0; }
}

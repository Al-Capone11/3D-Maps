package com.josem.threedmaps.core.capture;

/** Datos crudos de una seccion listos para voxelizar (copia propia, sin referencias a MC). */
public final class SectionSnapshot {
    public int sectionY;             // indice vertical local (0..sectionCount-1)
    public int bitsPerEntry;
    public int[] paletteIds;         // ids locales -> ids globales de material
    public long[] packed;            // copia defensiva
    public int uniformMaterialId;    // si bitsPerEntry == 0
}

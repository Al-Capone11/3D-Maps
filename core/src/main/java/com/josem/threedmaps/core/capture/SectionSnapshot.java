package com.josem.threedmaps.core.capture;
/** Datos crudos de una seccion (paleta + packed data + biomas por celda 4^3). */
public final class SectionSnapshot {
    public int sectionY;
    public int bitsPerEntry;
    public int[] paletteIds;
    public long[] packed;
}

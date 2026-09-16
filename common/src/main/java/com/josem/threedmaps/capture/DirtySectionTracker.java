package com.josem.threedmaps.capture;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/** Anti-rebote de 4 ticks: acumula secciones sucias y las vacia cada 4 ticks. §5.5.1. */
public final class DirtySectionTracker {
    private final LongOpenHashSet dirty = new LongOpenHashSet();
    private int ticks;

    public synchronized void markSectionDirty(long sectionPosLong) { dirty.add(sectionPosLong); }

    /** Llamar cada tick. Devuelve las secciones a remallar cada 4 ticks. */
    public synchronized long[] drainIfDue() {
        if (++ticks < 4) return null;
        ticks = 0;
        if (dirty.isEmpty()) return null;
        long[] out = dirty.toLongArray();
        dirty.clear();
        return out;
    }
}

package com.josem.threedmaps.core.discovery;

import com.josem.threedmaps.core.util.LongKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Bitsets por chunk de secciones descubiertas (plan §5.7.2).
 * UNICO punto de entrada para conceder visibilidad subterranea: NO llamar desde Chunky/pregen
 * ni render. La pregeneracion prepara geometria; NUNCA concede descubrimiento.
 */
public final class DiscoveryState {
    private final Map<Long, byte[]> byChunk = new HashMap<>();

    public boolean isDiscovered(int cx, int cz, int sectionIndex) {
        byte[] b = byChunk.get(LongKey.chunk(cx, cz));
        return b != null && sectionIndex >= 0 && sectionIndex < b.length * 8
                && (b[sectionIndex >> 3] & (1 << (sectionIndex & 7))) != 0;
    }

    /** @return true si cambio algo. */
    public boolean discover(int cx, int cz, int sectionIndex) {
        long key = LongKey.chunk(cx, cz);
        byte[] b = byChunk.computeIfAbsent(key, k -> new byte[Math.max(1, (sectionIndex >> 3) + 1)]);
        int need = (sectionIndex >> 3) + 1;
        if (need > b.length) {
            byte[] nb = new byte[Math.max(need, b.length * 2)];
            System.arraycopy(b, 0, nb, 0, b.length);
            b = nb;
            byChunk.put(key, b);
        }
        int bit = sectionIndex & 7;
        int prev = b[sectionIndex >> 3];
        b[sectionIndex >> 3] = (byte) (prev | (1 << bit));
        return prev != b[sectionIndex >> 3];
    }
}

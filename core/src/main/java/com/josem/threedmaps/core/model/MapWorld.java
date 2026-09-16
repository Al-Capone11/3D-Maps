package com.josem.threedmaps.core.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Regiones por dimension en RAM, cache LRU (plan §5.3.3). */
public final class MapWorld {
    private final WorldKey key;
    private final int maxRegions;
    private final Map<Long, MapRegion> regions = new LinkedHashMap<>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, MapRegion> e) {
            return size() > maxRegions;
        }
    };

    public MapWorld(WorldKey key, int maxRegions) { this.key = key; this.maxRegions = maxRegions; }

    public MapRegion region(int rx, int rz, int minY, int countY) {
        long k = ((long) rx & 0xFFFFFFFFL) | (((long) rz & 0xFFFFFFFFL) << 32);
        return regions.computeIfAbsent(k, l -> new MapRegion(rx, rz, minY, countY));
    }
    public WorldKey key() { return key; }
}

package com.josem.threedmaps.capture;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Tabla de materiales: cachea argb+flags por BlockState (es inmutable).
 * Expuesta a core como arrays planos (globalArgb, globalFlags) que Voxelizer consume.
 */
public final class MapColorTable {
    private final Map<BlockState, Integer> cache = new IdentityHashMap<>();
    private int[] globalArgb = new int[] {0xFF808080};
    private byte[] globalFlags = new byte[] {0};
    private int nextId = 1;

    public synchronized int register(int argb, int flags) {
        int id = nextId++;
        if (id >= globalArgb.length) {
            int[] a = new int[globalArgb.length * 2];
            System.arraycopy(globalArgb, 0, a, 0, globalArgb.length);
            globalArgb = a;
            byte[] f = new byte[globalFlags.length * 2];
            System.arraycopy(globalFlags, 0, f, 0, globalFlags.length);
            globalFlags = f;
        }
        globalArgb[id] = argb;
        globalFlags[id] = (byte) flags;
        return id;
    }

    public int[] globalArgb() { return globalArgb; }
    public byte[] globalFlags() { return globalFlags; }

    public int colorOf(BlockState bs) {
        Integer id = cache.get(bs);
        if (id != null) return globalArgb[id];
        int argb = bs.getBlock().defaultMapColor().calculateARGBColor(MapColor.Brightness.HIGH);
        int flags = bs.canOcclude() ? 0 : 1;
        return register(argb, flags);
    }
}

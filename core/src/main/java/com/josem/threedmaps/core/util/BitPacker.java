package com.josem.threedmaps.core.util;

/** Packs/unpacks small unsigned integers into a long[] array (plan §5.3.2 / F2 trap: bit entries crossing long boundaries). */
public final class BitPacker {
    private BitPacker() {}

    /** Number of longs needed for {@code count} entries of {@code bitsPerEntry} bits. */
    public static int longsNeeded(int count, int bitsPerEntry) {
        return (int) (((long) count * bitsPerEntry + 63L) / 64L);
    }

    public static int get(long[] data, int index, int bitsPerEntry) {
        if (bitsPerEntry <= 0) return 0;
        long bitIndex = (long) index * bitsPerEntry;
        int startLong = (int) (bitIndex >> 6);
        int startOffset = (int) (bitIndex & 63L);
        int endOffset = startOffset + bitsPerEntry;
        int mask = (bitsPerEntry >= 32) ? -1 : ((1 << bitsPerEntry) - 1);
        if (endOffset <= 64) {
            return (int) ((data[startLong] >>> startOffset) & mask);
        }
        int secondBits = endOffset - 64;
        return (int) (((data[startLong] >>> startOffset) | (data[startLong + 1] << (64 - startOffset))) & mask);
    }

    public static void set(long[] data, int index, int bitsPerEntry, int value) {
        if (bitsPerEntry <= 0) return;
        value &= (bitsPerEntry >= 32) ? -1 : ((1 << bitsPerEntry) - 1);
        long bitIndex = (long) index * bitsPerEntry;
        int startLong = (int) (bitIndex >> 6);
        int startOffset = (int) (bitIndex & 63L);
        data[startLong] = (data[startLong] & ~((long) ((bitsPerEntry >= 32) ? -1 : ((1 << bitsPerEntry) - 1)) << startOffset))
                | ((long) value << startOffset);
        int endOffset = startOffset + bitsPerEntry;
        if (endOffset > 64) {
            int secondBits = endOffset - 64;
            long mask2 = (1L << secondBits) - 1;
            data[startLong + 1] = (data[startLong + 1] & ~mask2) | (((long) value >>> (bitsPerEntry - secondBits)) & mask2);
        }
    }
}

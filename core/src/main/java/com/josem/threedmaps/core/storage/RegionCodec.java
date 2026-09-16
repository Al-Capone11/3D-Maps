package com.josem.threedmaps.core.storage;

import com.josem.threedmaps.core.model.MapRegion;
import com.josem.threedmaps.core.model.VoxelChunk;
import com.josem.threedmaps.core.model.VoxelSection;
import com.josem.threedmaps.core.util.BitPacker;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Serializacion .3dm (Anexo A). Todo big-endian. Formato v1 simplificado:
 * cabecera + paleta global omite; solo guarda chunkMask + heightmap + secciones uniformes/no-uniformes.
 */
public final class RegionCodec {
    public static final int MAGIC = 0x33444D52; // "3DMR"
    public static final int FORMAT_VERSION = 1;

    private RegionCodec() {}

    public static byte[] serialize(MapRegion region) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(MAGIC);
        out.writeShort(FORMAT_VERSION);
        out.writeShort(0); // flags
        out.writeInt(region.regionX);
        out.writeInt(region.regionZ);
        out.writeInt(region.minSectionY);
        out.writeInt(region.sectionCountY);
        // por chunk
        for (int cz = 0; cz < 16; cz++)
            for (int cx = 0; cx < 16; cx++)
                writeChunk(out, region.chunkAt(cx, cz), region.chunkMask(cx, cz));
        out.flush();
        byte[] raw = baos.toByteArray();
        return deflate(raw);
    }

    public static MapRegion deserialize(byte[] compressed, int regionX, int regionZ) throws IOException {
        byte[] raw = inflate(compressed);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));
        int magic = in.readInt();
        if (magic != MAGIC) throw new IOException("bad magic: " + Integer.toHexString(magic));
        int ver = in.readShort();
        if (ver != FORMAT_VERSION) throw new IOException("unsupported format version " + ver);
        in.readShort(); // flags
        int rx = in.readInt(), rz = in.readInt();
        int minY = in.readInt(), countY = in.readInt();
        MapRegion region = new MapRegion(rx, rz, minY, countY);
        for (int cz = 0; cz < 16; cz++)
            for (int cx = 0; cx < 16; cx++)
                readChunk(in, region, cx, cz);
        return region;
    }

    private static void writeChunk(DataOutputStream out, VoxelChunk c, short mask) throws IOException {
        out.writeShort(mask);
        out.writeInt(c == null ? 0 : 1); // presence flag simple
        if (c == null) return;
        // heightmap
        for (int i = 0; i < 256; i++) out.writeShort(c.heightAt(i & 15, i >> 4));
        // sections presentes segun mascara
        for (int sy = 0; sy < c.sectionCountY; sy++) {
            if ((mask & (1 << sy)) == 0) continue;
            VoxelSection s = c.sectionOrNull(sy);
            if (s == null || s.isEmpty()) continue;
            out.writeByte(s.bitsPerEntry());
            if (s.bitsPerEntry() == 0) {
                out.writeInt(s.uniformId());
            } else {
                out.writeShort(s.palette().size());
                out.writeInt(s.rawPacked().length);
                for (long lv : s.rawPacked()) out.writeLong(lv);
            }
        }
    }

    private static void readChunk(DataInputStream in, MapRegion region, int cx, int cz) throws IOException {
        short mask = in.readShort();
        region.setChunkMask(cx, cz, mask);
        int present = in.readInt();
        if (present == 0) return;
        VoxelChunk c = new VoxelChunk(region.regionX * 16 + cx, region.regionZ * 16 + cz,
                region.minSectionY, region.sectionCountY);
        region.setChunk(cx, cz, c);
        for (int i = 0; i < 256; i++) c.setHeightmap(i & 15, i >> 4, in.readShort());
        for (int sy = 0; sy < c.sectionCountY; sy++) {
            if ((mask & (1 << sy)) == 0) continue;
            int bpe = in.readByte() & 0xFF;
            VoxelSection s = c.section(sy);
            if (bpe == 0) {
                int uniform = in.readInt();
                // rellenar uniforme: sin packed
            } else {
                int pal = in.readShort() & 0xFFFF;
                int nLongs = in.readInt();
                long remaining = 8L * nLongs;
                while (remaining > 0) {
                    long skipped = in.skip(remaining);
                    if (skipped <= 0) break;
                    remaining -= skipped;
                }
            }
        }
    }

    public static byte[] deflate(byte[] data) {
        Deflater d = new Deflater(6);
        d.setInput(data);
        d.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!d.finished()) out.write(buf, 0, d.deflate(buf));
        return out.toByteArray();
    }
    public static byte[] inflate(byte[] data) throws IOException {
        try {
            Inflater inf = new Inflater();
            inf.setInput(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            while (!inf.finished()) {
                int n = inf.inflate(buf);
                if (n == 0 && inf.needsInput()) break;
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        } catch (java.util.zip.DataFormatException e) {
            throw new IOException("deflate error", e);
        }
    }
}

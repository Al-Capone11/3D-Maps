package com.josem.threedmaps.capture;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.core.capture.ChunkSnapshot;
import com.josem.threedmaps.core.capture.SectionSnapshot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Captura en el hilo principal: lee SOLO lo necesario del chunk vivo y devuelve un DTO propio.
 * Cumple §5.4.3 (snapshot y suelta) y §5.3.1 (material via defaultMapColor + brillo por cara).
 * NO retiene referencias al LevelChunk: todo es primitivo o arrays.
 */
public final class ChunkSnapshotCapture {
    private ChunkSnapshotCapture() {}

    private static volatile Strategy<BlockState> BLOCK_STRATEGY;

    /** Debe llamarse desde el hilo de render. */
    public static ChunkSnapshot capture(ClientLevel level, int chunkX, int chunkZ,
                                        MapColorTable colorTable) {
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) return null;

        if (BLOCK_STRATEGY == null) {
            BLOCK_STRATEGY = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
        }

        ChunkSnapshot snap = new ChunkSnapshot();
        snap.chunkX = chunkX; snap.chunkZ = chunkZ;
        snap.minSectionY = level.getMinSectionY();
        snap.sectionCountY = level.getSectionsCount();

        // Heightmap WORLD_SURFACE por columna (16x16).
        snap.heightmap = new short[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                snap.heightmap[(z << 4) | x] = (short) chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            }
        }

        LevelChunkSection[] sections = chunk.getSections();
        snap.sections = new SectionSnapshot[sections.length];
        for (int sy = 0; sy < sections.length; sy++) {
            LevelChunkSection section = sections[sy];
            if (section == null || section.hasOnlyAir()) continue;

            SectionSnapshot ss = new SectionSnapshot();
            ss.sectionY = sy;

            PalettedContainer<BlockState> states = section.getStates();
            PalettedContainerRO.PackedData<BlockState> packed = states.pack(BLOCK_STRATEGY);
            ss.bitsPerEntry = packed.bitsPerEntry();

            List<BlockState> palette = packed.paletteEntries();
            // traducir cada bloque de la paleta a id de material local y guardar datos
            int[] paletteIds = new int[palette.size()];
            for (int i = 0; i < palette.size(); i++) {
                BlockState bs = palette.get(i);
                int argb = colorTable != null ? colorTable.colorOf(bs) : bs.getBlock().defaultMapColor().calculateARGBColor(net.minecraft.world.level.material.MapColor.Brightness.HIGH);
                int flags = MaterialFlags.of(bs);
                paletteIds[i] = colorTable != null ? colorTable.register(argb, flags) : 0;
            }
            ss.paletteIds = paletteIds;
            if (ss.bitsPerEntry == 0) {
                ss.uniformMaterialId = paletteIds.length > 0 ? paletteIds[0] : 0;
                ss.packed = new long[0];
            } else {
                int entryCount = 16 * 16 * 16; // LevelChunkSection SIZE^3
                ss.packed = new long[(int) (((long) entryCount * ss.bitsPerEntry + 63) / 64)];
                packed.storage().ifPresent(stream -> {
                    long[] arr = stream.toArray();
                    System.arraycopy(arr, 0, ss.packed, 0, Math.min(arr.length, ss.packed.length));
                });
            }
            snap.sections[sy] = ss;
        }
        return snap;
    }

    /** Flags por material (traducen propiedades vanilla a flags de VoxelMaterial). */
    static final class MaterialFlags {
        static int of(BlockState bs) {
            int flags = 0;
            if (bs.canOcclude() == false) flags |= 1;         // translucido aprox.
            if (bs.getFluidState().isEmpty() == false) flags |= 1 << 4; // agua
            return flags;
        }
    }
}

package com.josem.threedmaps.core;
import com.josem.threedmaps.core.storage.RegionIo;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class RegionIoTest {
    @Test void writeKeepsOldFileOnCrash() throws Exception {
        Path dir = Files.createTempDirectory("3dm");
        Path f = dir.resolve("r.0.0.3dm");
        byte[] good = "GOOD-DATA".getBytes();
        RegionIo.write(f, good);
        assertArrayEquals(good, RegionIo.read(f));
        // simular crash: escribir a tmp y no mover
        Path tmp = dir.resolve("r.0.0.3dm.tmp");
        Files.write(tmp, "BROKEN".getBytes());
        assertArrayEquals(good, RegionIo.read(f)); // el .3dm sigue intacto
    }
}

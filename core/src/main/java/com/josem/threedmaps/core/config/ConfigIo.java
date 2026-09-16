package com.josem.threedmaps.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Load/save config con tolerancia: nunca crashea por JSON malo (plan §5.9). */
public final class ConfigIo {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private ConfigIo() {}

    public static MapConfig load(Path file) {
        if (!Files.exists(file)) return saveDefault(file);
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            MapConfig c = GSON.fromJson(json, MapConfig.class);
            if (c == null) c = new MapConfig();
            c.validate();
            return c;
        } catch (IOException | JsonSyntaxException e) {
            // config corrupto: regenerar (plan: no crashear)
            System.err.println("[3dmaps] config corrupto, usando defaults: " + e.getMessage());
            return saveDefault(file);
        }
    }

    private static MapConfig saveDefault(Path file) {
        MapConfig c = new MapConfig();
        save(c, file);
        return c;
    }

    public static void save(MapConfig c, Path file) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(c), StandardCharsets.UTF_8);
            moveAtomic(tmp, file);
        } catch (IOException e) {
            System.err.println("[3dmaps] no se pudo guardar config: " + e.getMessage());
        }
    }

    static void moveAtomic(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(from, to, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

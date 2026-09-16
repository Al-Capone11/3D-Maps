package com.josem.threedmaps.core.model;

/** Clave de mundo persistente (perfil + dimension), plan §5.3.4. */
public record WorldKey(String profile, String dimension) {
    // profile singleplayer: sp/<carpeta>; multiplayer: mp/<sha1(addr)[:16]>
}

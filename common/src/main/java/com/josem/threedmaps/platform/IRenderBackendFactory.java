package com.josem.threedmaps.platform;

import com.josem.threedmaps.core.render.IMapRenderBackend;

/** Crea el backend de render de esta version. */
public interface IRenderBackendFactory {
    IMapRenderBackend create();
}

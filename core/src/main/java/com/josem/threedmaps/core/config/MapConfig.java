package com.josem.threedmaps.core.config;

import com.josem.threedmaps.core.util.MathUtil;

/** POJO de configuracion (plan §5.9.1). Serializado por Gson en common. */
public final class MapConfig {
    public boolean modEnabled = true;

    // Minimapa
    public boolean minimapEnabled = true;
    public int minimapAnchor = 1; // 0..3 corners
    public int minimapSize = 160;
    public int minimapMarginX = 8, minimapMarginY = 8;
    public float minimapOpacity = 1.0f;
    public int minimapShape = 0; // square
    public int minimapRotation = 0; // rotating
    public float minimapPitch = 45f;
    public float minimapZoomBlocks = 96f;
    public int minimapRefreshHz = 20;
    public boolean showCoordinates = true, showBiome = true, showCompass = true;

    // Descubrimiento subterraneo (tope duro 8)
    public int discoveryRadiusChunks = 4;
    public int discoveryMode = 0; // RADIUS
    public boolean showUndiscoveredAsFog = false;
    public boolean fadeInDiscovery = true;
    public int fadeInMillis = 500;

    // Rendimiento
    public int buildThreads = 0;
    public int maxGpuUploadsPerFrame = 4;
    public int gpuUploadBudgetMicros = 2000;
    public boolean enableAmbientOcclusion = true;

    // Almacenamiento
    public int autosaveSeconds = 30;
    public int maxDiskUsageMb = 2048;

    public void validate() {
        discoveryRadiusChunks = MathUtil.clamp(discoveryRadiusChunks, 1, 8);
        minimapSize = MathUtil.clamp(minimapSize, 80, 400);
        minimapRefreshHz = MathUtil.clamp(minimapRefreshHz, 5, 60);
    }
}

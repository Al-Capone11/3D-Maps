package com.josem.threedmaps.core;

/**
 * Agnostic entry point for the 3D Maps core. Pure Java, zero Minecraft imports.
 * Lifecycle: {@link #init()} once at startup, {@link #shutdown()} on game close.
 */
public final class ThreeDMapsCore {
    public static final String MOD_ID = "threedmaps";
    public static final String MOD_NAME = "3D Maps";

    private static volatile boolean initialized = false;

    private ThreeDMapsCore() {}

    public static synchronized void init() {
        initialized = true;
    }

    public static synchronized void shutdown() {
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}

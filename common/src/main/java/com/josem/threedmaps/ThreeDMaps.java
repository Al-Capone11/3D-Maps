package com.josem.threedmaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared constants and logger. */
public final class ThreeDMaps {
    public static final String MOD_ID = "threedmaps";
    public static final String MOD_NAME = "3D Maps";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /** Dev-only assertions; read from system property so cost is zero in production. */
    public static final boolean DEV = Boolean.getBoolean("threedmaps.dev");

    private ThreeDMaps() {}
}
